/** Reliable Data Transport class.
 *
 *  This class implements a reliable data transport service.
 *  It uses a go-back-N sliding window protocol on a packet basis.
 *
 *  An application layer thread provides new packet payloads to be
 *  sent using the provided send() method, and retrieves newly arrived
 *  payloads with the receive() method. Each application layer payload
 *  is sent as a separate UDP packet, along with a sequence number and
 *  a type flag that identifies a packet as a data packet or an
 *  acknowledgment. The sequence numbers are 15 bits.
 */

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Rdt implements Runnable {
	private int wSize;	// protocol window size
	private long timeout;	// retransmission timeout in ns
	private Substrate sub;	// Substrate object for packet IO

	private ArrayBlockingQueue<String> fromSrc;
	private ArrayBlockingQueue<String> toSnk;

	// Sending structures and necessary information
	private Packet[] sendBuf; // not yet acked packets
	private short sendBase = 0;	// seq# of first packet in send window
	private short sendSeqNum = 0;	// next available seq# in send window
	private short dupAcks = 0; // should only happen for sendBase-1 packet

	// Receiving structures and necessary information
	private Packet[] recvBuf; // undelivered packets
	private short recvBase = 0;  // seq# of oldest undelivered packet (to application)
	private short expSeqNum = 0;// seq# of packet we expect to receive (from substrate)
	private short lastRcvd = -1; // last packet received properly

	// Time keeping variabels
	private long now = 0;		// current time (relative to t0)
	private long sendAgain = 0;	// time when we send all unacked packets

	private Thread myThread;
	private boolean quit;

	/** Initialize a new Rdt object.
	 *  @param wSize is the window size used by protocol; the sequence #
	 *  space is twice the window size
	 *  @param timeout is the time to wait before retransmitting
	 *  @param sub is a reference to the Substrate object that this object
	 *  uses to handle the socket IO
	 */
	Rdt(int wSize, double timeout, Substrate sub) 
	{
		this.wSize = Math.min(wSize,(1 << 14) - 1);
		this.timeout = ((long) (timeout * 1000000000)); // sec to ns
		this.sub = sub;

		// create queues for application layer interface
		fromSrc = new ArrayBlockingQueue<String>(1000,true);
		toSnk = new ArrayBlockingQueue<String>(1000,true);
		quit = false;

		sendBuf = new Packet[2*wSize];
		recvBuf = new Packet[2*wSize];
	}

	/** Start the Rdt running. */
	public void start() throws Exception {
		myThread = new Thread(this); myThread.start();
	}

	/** Stop the Rdt.  */
	public void stop() throws Exception { quit = true; myThread.join(); }

	/** Increment sequence number, handling wrap-around.
	 *  @param x is a sequence number
	 *  @return next sequence number after x
	 */
	private short incr(short x) {
		x++; return (x < 2*wSize ? x : 0);
	}

	/** Compute the difference between two sequence numbers,
	 *  accounting for "wrap-around"
	 *  @param x is a sequence number
	 *  @param y is another sequence number
	 *  @return difference, assuming x is "clockwise" from y
	 */
	private int diff(short x, short y) {
		return (x >= y ? x-y : (x + 2*wSize) - y);
	}

	/** Main thread for the Rdt object.
	 *
	 *  Inserts payloads received from the application layer into
	 *  packets, and sends them to the substrate. The packets include
	 *  the number of packets and chars sent so far (including the
	 *  current packet). It also takes packets received from
	 *  the substrate and sends the extracted payloads
	 *  up to the application layer. To ensure that packets are
	 *  delivered reliably and in-order, using a sliding
	 *  window protocol with the go-back-N feature.
	 */
	public void run() {
		boolean pause = false;
		long t0 = System.nanoTime();
		long now = 0;		// current time (relative to t0)

		Packet packet;
		/* we still have un-acked packets */
		while (!this.quit || 0 < diff(this.sendSeqNum, this.sendBase)){
			now = System.nanoTime() - t0;
			if(pause) {
				this.sendAgain = now + this.timeout;
			}
			// if receive buffer has a packet that can be
			//    delivered, deliver it to sink
			if (recvBuf[recvBase] != null && toSnk.remainingCapacity() > 0){
				try {
					toSnk.put(recvBuf[recvBase].payload);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				recvBuf[recvBase] = null;
				recvBase = incr(recvBase);
			}
			// else if the substrate has an incoming packet
			//      get the packet from the substrate and process it
			else if(this.sub.incoming()) {
				packet = this.sub.receive();
				//	if it's an ack, update the send buffer and
				//	related data as appropriate
				//	reset the timer if necessary
				short tempSendBase = sendBase;
				if(packet.type == 1) {//ack received
					short temp = sendBase;//safe decrement similar to incr function
					temp--;
					//handles wrap-around
					short compareSeq = (short) (temp >=0 ? temp : 2 * wSize - 1);
					if (compareSeq == packet.seqNum){//if it has already been acked
						dupAcks++;
						if (dupAcks == 3){ // if already acked, 3 duplicate
							dupAcks = 0;
							this.sendAgain = now + this.timeout;
							pause = false;
							short i = (short) (sendBase - 1);
							while (diff(this.sendSeqNum, i) > 0){
								while(!this.sub.ready()){
									try{
										Thread.sleep(1);
									}
									catch(InterruptedException e){
										e.printStackTrace();
									}
								}
								Packet p = sendBuf[i];
								this.sub.send(p);
								i = incr(i);
							}
						}
						sendBase = tempSendBase;
					} 
					else { //if this is the first ack recieved for a packet
						if (packet.seqNum == sendBase){
							sendBase = incr(sendBase);
						}
						this.sendAgain = now + this.timeout;
						pause = false;
					}
				}
				//if it's a data packet, ack it and add it
				//to receive buffer as appropriate
				else if (packet.type == 0){
					Packet ackPacket = new Packet();
					ackPacket.type = 1;
					ackPacket.seqNum = this.expSeqNum;
					recvBuf[packet.seqNum] = packet;
					sub.send(ackPacket);
					if (packet.seqNum == expSeqNum){
						expSeqNum = incr(expSeqNum);
						lastRcvd = this.expSeqNum;
					}
				}
			}
			// else if there is a message from the source waiting
			//      to be sent and the send window is not full
			//	and the substrate can accept a packet
			//      create a packet containing the message,
			//	and send it, after updating the send buffer
			//	and related data
			else if(this.sub.ready() && (0 < this.fromSrc.size()) && 
					(this.wSize > diff(this.sendSeqNum, this.sendBase))){
				packet = new Packet();
				packet.seqNum = this.sendSeqNum;
				packet.type = 0;

				try {
					packet.payload = this.fromSrc.take();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				this.sendBuf[this.sendSeqNum] = packet;
				this.sub.send(packet);
				if(this.sendSeqNum == this.sendBase) {
					pause = true;
					this.sendAgain = now + this.timeout;
				}
				this.sendSeqNum = incr(sendSeqNum);
			}


			else if(this.sendAgain < now && this.sendBase != this.sendSeqNum){
				// else if the resend timer has expired, re-send all
				//      packets in the window and reset the timer
					short i = (short) (sendBase);
					pause = false;
					this.sendAgain = now + this.timeout;
							while (diff(this.sendSeqNum, i) > 0){
								while(!this.sub.ready()){
									try{
										Thread.sleep(1);
									}
									catch(InterruptedException e){
										e.printStackTrace();
									}
								}
								Packet p = sendBuf[i];
								this.sub.send(p);
								i = incr(i);
							}
			}

			// else nothing to do, so sleep for 1 ms
			else{
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/** Send a message to peer.
	 *  @param message is a string to be sent to the peer
	 */
	public void send(String message) {
		try {
			fromSrc.put(message);
		} catch(Exception e) {
			System.err.println("Rdt:send: put exception" + e);
			System.exit(1);
		}
	}

	/** Test if Rdt is ready to send a message.
	 *  @return true if Rdt is ready
	 */
	public boolean ready() { return fromSrc.remainingCapacity() > 0; }

	/** Get an incoming message.
	 *  @return next message
	 */
	public String receive() {
		String s = null;
		try {
			s = toSnk.take();
		} catch(Exception e) {
			System.err.println("Rdt:send: take exception" + e);
			System.exit(1);
		}
		return s;
	}

	/** Test for the presence of an incoming message.
	 *  @return true if there is an incoming message
	 */
	public boolean incoming() { return toSnk.size() > 0; }
}
