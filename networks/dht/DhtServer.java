/** Server for simple distributed hash table that stores (key,value) strings.
 *  
 *  usage: DhtServer myIp numRoutes cfgFile [ cache ] [ debug ] [ predFile ]
 *  
 *  myIp is the IP address to use for this server's socket
 *  numRoutes is the max number of nodes allowed in the DHT's routing table;
 *  typically lg(numNodes)
 *  cfgFile is the name of a file in which the server writes the IP
 * address and port number of its socket
 *  cache is an optional argument; if present it is the literal string
 * "cache"; when cache is present, the caching feature of the
 * server is enabled; otherwise it is not
 *  debug is an optional argument; if present it is the literal string
 * "debug"; when debug is present, a copy of every packet received
 * and sent is printed on stdout
 *  predFile is an optional argument specifying the configuration file of
 * this node's predecessor in the DHT; this file is used to obtain
 * the IP address and port number of the precessor's socket,
 * allowing this node to join the DHT by contacting predecessor
 *  
 *  The DHT uses UDP packets containing ASCII text. Here's an example of the
 *  UDP payload for a get request from a client.
 *  
 *  CSE473 DHTPv0.1
 *  type:get
 *  key:dungeons
 *  tag:12345
 *  ttl:100
 *  
 *  The first line is just an identifying string that is required in every
 *  DHT packet. The remaining lines all start with a keyword and :, usually
 *  followed by some additional text. Here, the type field specifies that
 *  this is a get request; the key field specifies the key to be looked up;
 *  the tag is a client-specified tag that is returned in the response; and
 *  can be used by the client to match responses with requests; the ttl is
 *  decremented by every DhtServer and if <0, causes the packet to be discarded.
 *  
 *  Possible responses to the above request include:
 *  
 *  CSE473 DHTPv0.1
 *  type:success
 *  key:dungeons
 *  value:dragons
 *  tag:12345
 *  ttl:95
 *  
 *  or
 *  
 *  CSE473 DHTPv0.1
 *  type:no match
 *  key:dungeons
 *  tag:12345
 *  ttl:95
 *  
 *  Put requests are formatted similarly, but in this case the client typically
 *  specifies a value field (omitting the value field causes the pair with the
 *  specified key to be removed).
 *  
 *  The packet type "failure" is used to indicate an error of some sort; in 
 *  this case, the "failure" field provides an explanation of the failure. 
 *  The "join" type is used by a server to join an existing DHT. In the same
 *  way, the "leave" type is used by the leaving server to circle around the 
 *  DHT asking other servers' to delete it from their routing tables.  The 
 *  "transfer" type is used to transfer (key,value) pairs to a newly added 
 *  server. The "update" type is used to update the predecessor, successor, 
 *  or hash range of another DHT server, usually when a join or leave even 
 *  happens. 
 *
 *  Other fields and their use are described briefly below
 *  clientAdr is used to specify the IP address and port number of the 
 *              client that sent a particular request; it is added to a request
 *              packet by the first server to receive the request, before 
 *              forwarding the packet to another node in the DHT; an example of
 *              the format is clientAdr:123.45.67.89:51349.
 *  relayAdr  is used to specify the IP address and port number of the first
 *              server to receive a request packet from the client; it is added
 *              to the packet by the first server before forwarding the packet.
 *  hashRange is a pair of integers separated by a colon, specifying a range
 *              of hash indices; it is included in the response to a "join" 
 *              packet, to inform the new DHT server of the set of hash values
 *              it is responsible for; it is also included in the update packet
 *              to update the hash range a server is responsible for.
 *  succInfo  is the IP address and port number of a server, followed by its
 *              first hash index; this information is included in the response
 *              to a join packet to inform the new DHT server about its 
 *              immediate successor; successor also included in the update packet 
 *              to change the immediate successor of a DHT server; an example 
 *              of the format is succInfo:123.45.6.7:5678:987654321.
 *  predInfo is also the IP address and port number of a server, followed
 *              by its first hash index; this information is included in a join
 *              packet to inform the successor DHT server of its new 
 *              predecessor; it is also included in update packets to update 
 *              the new predecessor of a server.
 *  senderInfo is the IP address and port number of a DHT server, followed by
 *              its first hash index; this information is sent by a DHT to 
 *              provide routing information that can be used by other servers.
 *              It also used in leave packet to let other servers know the IP
 *              address and port number information of the leaving server.
 */


import java.io.*;
import java.net.*;
import java.util.*;




import sun.misc.Signal;
import sun.misc.SignalHandler;

public class DhtServer {
	private static int numRoutes;	// number of routes in routing table
	private static boolean cacheOn;	// enables caching when true
	private static boolean debug;	// enables debug messages when true

	private static HashMap<String,String> map;	// key/value pairs
	private static HashMap<String,String> cache;	// cached pairs
	private static List<Pair<InetSocketAddress,Integer>> rteTbl;

	private static DatagramSocket sock;
	private static InetSocketAddress myAdr;
	private static InetSocketAddress predecessor; // DHT predecessor
	private static Pair<InetSocketAddress,Integer> myInfo; 
	private static Pair<InetSocketAddress,Integer> predInfo; 
	private static Pair<InetSocketAddress,Integer> succInfo; // successor
	private static Pair<Integer,Integer> hashRange; // my DHT hash range
	private static int sendTag;		// tag for new outgoing packets
	// flag for waiting leave message circle back
	private static boolean stopFlag;

	/** Main method for DHT server.
	 *  Processes command line arguments, initializes data, joins DHT,
	 *  then starts processing requests from clients.
	 */
	public static void main(String[] args) {
		// process command-line arguments
		if (args.length < 3) {
			System.err.println("usage: DhtServer myIp numRoutes " +
					"cfgFile [debug] [ predFile ] ");
			System.exit(1);
		}
		numRoutes = Integer.parseInt(args[1]);
		String cfgFile = args[2];
		cacheOn = debug = false;
		stopFlag = false;
		String predFile = null;
		for (int i = 3; i < args.length; i++) {
			if (args[i].equals("cache")) cacheOn = true;
			else if (args[i].equals("debug")) debug = true;
			else predFile = args[i];
		}

		// open socket for receiving packets
		// write ip and port to config file
		// read predecessor's ip/port from predFile (if there is one)
		InetAddress myIp = null; sock = null; predecessor = null;
		try {	
			myIp = InetAddress.getByName(args[0]);
			sock = new DatagramSocket(0,myIp);
			BufferedWriter cfg =
					new BufferedWriter(
							new OutputStreamWriter(
									new FileOutputStream(cfgFile),
									"US-ASCII"));
			cfg.write("" +	myIp.getHostAddress() + " " +
					sock.getLocalPort());
			cfg.newLine();
			cfg.close();
			if (predFile != null) {
				BufferedReader pred =
						new BufferedReader(
								new InputStreamReader(
										new FileInputStream(predFile),
										"US-ASCII"));
				String s = pred.readLine();
				String[] chunks = s.split(" ");
				predecessor = new InetSocketAddress(
						chunks[0],Integer.parseInt(chunks[1]));
			}
		} catch(Exception e) {
			System.err.println("usage: DhtServer myIp numRoutes " +
					"cfgFile [ cache ] [ debug ] " +
					"[ predFile ] ");
			System.exit(1);
		}
		myAdr = new InetSocketAddress(myIp,sock.getLocalPort());

		// initialize data structures	
		map = new HashMap<String,String>();
		cache = new HashMap<String,String>();
		rteTbl = new LinkedList<Pair<InetSocketAddress,Integer>>();

		// join the DHT (if not the first node)
		hashRange = new Pair<Integer,Integer>(0,Integer.MAX_VALUE);
		myInfo = null;
		succInfo = null;
		predInfo = null;
		if (predecessor != null) {
			join(predecessor);
		} else {
			myInfo = new Pair<InetSocketAddress,Integer>(myAdr,0);
			succInfo = new Pair<InetSocketAddress,Integer>(myAdr,0);
			predInfo = new Pair<InetSocketAddress,Integer>(myAdr,0);
		}


		// start processing requests from clients
		Packet p = new Packet();
		Packet reply = new Packet();
		InetSocketAddress sender = null;
		sendTag = 1;

		/* this function will be called if there's a "TERM" or "INT"
		 * captured by the signal handler. It simply execute the leave
		 * function and leave the program.
		 */ 



		SignalHandler handler = new SignalHandler() {  
			public void handle(Signal signal) { 
				leave();
				System.exit(0);
			}  
		};
		//Signal.handle(new Signal("KILL"), handler); // capture kill -9 signal
		Signal.handle(new Signal("TERM"), handler); // capture kill -15 signal
		Signal.handle(new Signal("INT"), handler); // capture ctrl+c




		while (true) {
			try { sender = p.receive(sock,debug);
			} catch(Exception e) {
				System.err.println("received packet failure");
				continue;
			}
			if (sender == null) {
				System.err.println("received packet failure");
				continue;
			}
			if (!p.check()) {
				reply.clear();
				reply.type = "failure";
				reply.reason = p.reason;
				reply.tag = p.tag;
				reply.ttl = p.ttl;
				reply.send(sock,sender,debug);
				continue;
			}
			handlePacket(p,sender);
		}
	}

	/** Hash a string, returning a 32 bit integer.
	 *  @param s is a string, typically the key from some get/put operation.
	 *  @return and integer hash value in the interval [0,2^31).
	 */
	public static int hashit(String s) {
		while (s.length() < 16) s += s;
		byte[] sbytes = null;
		try { sbytes = s.getBytes("US-ASCII"); 
		} catch(Exception e) {
			System.out.println("illegal key string");
			System.exit(1);
		}
		int i = 0;
		int h = 0x37ace45d;
		while (i+1 < sbytes.length) {
			int x = (sbytes[i] << 8) | sbytes[i+1];
			h *= x;
			int top = h & 0xffff0000;
			int bot = h & 0xffff;
			h = top | (bot ^ ((top >> 16)&0xffff));
			i += 2;
		}
		if (h < 0) h = -(h+1);
		return h;
	}

	/** Leave an existing DHT.
	 *  
	 *	Send a leave packet to it's successor and wait until stopFlag is 
	 * 	set to "true", which means leave packet is circle back.
	 *
	 *	Send an update packet with the new hashRange and succInfo fields to 
	 *  its predecessor, and sends an update packet with the predInfo 
	 *  field to its successor. 
	 *	
	 *	Transfers all keys and values to predecessor.  
	 *	Clear all the existing cache, map and rteTbl information
	 */
	public static void leave() {

		Packet leavePacket = new Packet();
		leavePacket.type = "leave";
		leavePacket.senderInfo = myInfo;
		leavePacket.tag = sendTag + 1;
		sendTag++;
		leavePacket.send(sock, succInfo.left,debug);

		Packet response = new Packet();

		while (!stopFlag){
			try{
				Thread.sleep(100);
			}
			catch(InterruptedException e){
				continue;
			}
		}
		Packet updatePred = new Packet();
		updatePred.type = "update";
		updatePred.succInfo = succInfo;
		updatePred.tag = sendTag +1;
		sendTag++;
		updatePred.hashRange = new Pair<Integer, Integer>(predInfo.right,hashRange.right);
		updatePred.send(sock,predecessor,debug);
		Packet updateSucc = new Packet();
		updateSucc.type = "update";
		updateSucc.predInfo = predInfo;
		updateSucc.tag = sendTag + 1;
		sendTag++;
		updateSucc.send(sock,succInfo.left,debug);
		Set<String> keys = map.keySet();
		Iterator<String> i = keys.iterator();
		while (i.hasNext()){
			String next = i.next().toString();
			Packet transferPacket = new Packet();
			transferPacket.type = "transfer";
			transferPacket.key = next;
			transferPacket.val = map.get(next);
			transferPacket.tag = sendTag +1;
			sendTag++;
			transferPacket.send(sock,predecessor , debug);
		}
		cache.clear();
		map.clear();
		rteTbl.clear();
	}

	/** Handle a update packet from a prospective DHT node.
	 *  @param p is the received join packet
	 *  @param adr is the socket address of the host that
	 *  
	 *	The update message might contains infomation need update,
	 *	including predInfo, succInfo, and hashRange. 
	 *  And add the new Predecessor/Successor into the routing table.
	 *	If succInfo is updated, succInfo should be removed from 
	 *	the routing table and the new succInfo should be added
	 *	into the new routing table.
	 */
	public static void handleUpdate(Packet p, InetSocketAddress adr) {
		if (p.predInfo != null){
			predInfo = p.predInfo;
			predecessor = predInfo.left;
		}
		if (p.succInfo != null){
			succInfo = p.succInfo;
			addRoute(succInfo);
		}
		if (p.hashRange != null){
			hashRange = p.hashRange;
		}
	}

	/** Handle a leave packet from a leaving DHT node.
	 *  @param p is the received join packet
	 *  @param adr is the socket address of the host that sent the leave packet
	 *
	 *  If the leave packet is sent by this server, set the stopFlag.
	 *  Otherwise firstly send the received leave packet to its successor,
	 *  and then remove the routing entry with the senderInfo of the packet.
	 */
	public static void handleLeave(Packet p, InetSocketAddress adr) {
		if (p.senderInfo.equals(myInfo)){
			stopFlag = true;
			return;
		}
		// send the leave message to successor 
		p.send(sock, succInfo.left, debug);

		//remove the senderInfo from route table
		removeRoute(p.senderInfo);
	}

	/** Join an existing DHT.
	 *  @param predAdr is the socket address of a server in the DHT,
	 *  
	 *	Send a packet with type join to predAdr, the server that will
	 * be the current server's predecessor. Wait for the response packet 
	 * and if the response packet is a "success" packet from the predecessor,
	 *  set the current server's predecessor, successor, and hashrange with 
	 *  the corresponding fields from the packet. Add the successor to the
	 *  current server's routing table.
	 */
	public static void join(InetSocketAddress predAdr) {
		Packet join_packet = new Packet();
		join_packet.type = "join";
		int tag = sendTag + 1;
		sendTag++;
		join_packet.tag = tag;
		join_packet.send(sock, predAdr,debug);

		Packet response = new Packet();
		while(true){
			InetSocketAddress remote_address = response.receive(sock, debug);
			if(remote_address.equals(predAdr)){
				predInfo = response.predInfo;
				predecessor = predAdr;
				succInfo = response.succInfo;
				hashRange = response.hashRange;
				myInfo = new Pair<InetSocketAddress, Integer>(myAdr,hashRange.left);
				addRoute(succInfo);
				break;
			}
			if(!response.check()){
				System.out.println("DhtServer server failed to join: " + response.reason);
				return;
			}
			if(response.tag != join_packet.tag || !response.type.equals("success")){
				System.out.println("DhtServer join failed after recieving packet: " + response.toString());
				return;
			}
		}
	}

	/** Handle a join packet from a prospective DHT node.
	 *  @param p is the received join packet
	 *  @param succAdr is the socket address of the host that
	 *  sent the join packet (the new successor)
	 *
	 *  Split the current server's hashrange in half, and pass the 
	 *  upper half as the hashrange for the new server. Send a 
	 *  "success" packet with the new server's hashrange, predecessor 
	 *  information, and Successor information to the server that 
	 *  sent the "join" packet. Update the current server's 
	 *  hashrange and successor information. Update the current
	 *  server's previous successor with an update packet that 
	 *  sets its new predecessor as the server that sent the 
	 *  "join" packet.
	 */
	public static void handleJoin(Packet p, InetSocketAddress succAdr) {
		//find the new hashRanges 
		InetSocketAddress oldSuccAdr = succInfo.left;
		int oldBeginning = hashRange.left;
		int oldEnd = hashRange.left+ (hashRange.right-hashRange.left)/2;
		int newEnd = hashRange.right;
		int newBegin = oldEnd + 1;

		hashRange.left = oldBeginning;
		hashRange.right = oldEnd;		
		//send the success packet to the server
		// that requested to join
		Packet successPacket = new Packet();
		successPacket.type = "success";
		successPacket.hashRange = new Pair<Integer, Integer>(newBegin, newEnd);
		successPacket.tag = sendTag +1;
		sendTag++;
		successPacket.predInfo = myInfo;
		successPacket.succInfo = succInfo;

		succInfo = new Pair<InetSocketAddress, Integer> (succAdr, newBegin);
		successPacket.send(sock,succAdr,debug);
		//update the successor with its new predInfo
		Packet updateSucc = new Packet();
		updateSucc.type = "update";
		updateSucc.predInfo = succInfo;
		updateSucc.tag = sendTag + 1;
		sendTag++;
		updateSucc.send(sock, oldSuccAdr,debug);

		addRoute(succInfo);

		//transfer the key-value pairs to the 
		// new server and update the current 
		// server's map
		Set<String> keys = map.keySet();
		Iterator<String> i = keys.iterator();
		while (i.hasNext() && newBegin <= newEnd){
			String next = i.next().toString();
			Packet transferPacket = new Packet();
			transferPacket.tag = sendTag +1;
			sendTag++;
			transferPacket.type = "transfer";
			transferPacket.key = next;
			transferPacket.val = map.get(next);
			transferPacket.send(sock, succAdr, debug);
			map.remove(next);
			newBegin++;
		}
	}

	/** Handle a get packet.
	 *  @param p is a get packet
	 *  @param senderAdr is the the socket address of the sender
	 *
	 *  checks the hash of the key in packet p to determine if
	 *  the key hash is within the server's hashrange. If the 
	 *  hash is within the hashrange and the map contains the key,
	 *  return the packet with type "success" and the value. If 
	 *  the hash is within the hashrange but the key is not in the
	 *  map, retun the packet with a type "no match". If the hash is
	 *  not in the hashrange, forward the packet. 
	 *  
	 *  If caching is on and the key is contained in the caching 
	 *  map, return the packet with type "success" and the cached value.
	 */
	public static void handleGet(Packet p, InetSocketAddress senderAdr) {

		InetSocketAddress replyAdr;
		int hash = hashit(p.key);
		int left = hashRange.left.intValue();
		int right = hashRange.right.intValue();


		if (left <= hash && hash <= right) {
			// respond to request using map
			if (p.relayAdr != null) {
				replyAdr = p.relayAdr;
				p.senderInfo = myInfo;
			} else {
				replyAdr = senderAdr;
			}
			if (map.containsKey(p.key)) {
				p.type = "success"; 
				p.val = map.get(p.key);
			} else {
				p.type = "no match";
			}
			p.send(sock,replyAdr,debug);
		} 
		// if caching is true
		else if (cache.containsKey(p.key) && cacheOn){
			p.val = cache.get(p.key);
			p.type = "success";
			if(p.relayAdr == null){
				replyAdr = senderAdr;
			}
			else{
				p.senderInfo =  new Pair<InetSocketAddress, Integer> (myAdr, right); //myInfo; //same as
				replyAdr = p.relayAdr;
			}

		}
		else {
			// forward around DHT
			if (p.relayAdr == null) {
				p.relayAdr = myAdr; p.clientAdr = senderAdr;
			}
			forward(p,hash);
		}
	}

	/** Handle a put packet.
	 *  @param p is a put packet
	 *  @param senderAdr is the the socket address of the sender
	 *  
	 *	Check whether the packet's key hash is in the server's hashrange.
	 * If the hash is within the hashrange, add the key-value pair to the
	 * server's map and send the packet with type "success". If the hash 
	 * is not within the hashrange, forward the packet. 
	 * 
	 * If caching is true and the key is in the cache map, remove the
	 * corresponding key-value pair from the cache map.
	 */
	public static void handlePut(Packet p, InetSocketAddress senderAdr) {

		InetSocketAddress replyAdr;
		int hash = hashit(p.key);
		int left = hashRange.left.intValue();
		int right = hashRange.right.intValue();

		// hash is within this server's hashRange
		if (hash >= left && hash <= right){
			map.put(p.key, p.val);
			p.type = "success";
			if (p.relayAdr == null){
				p.send(sock, senderAdr, debug);
			}
			else {
				p.senderInfo = myInfo;
				p.send(sock, p.relayAdr, debug);
			}
		}
		//hash is not within this server's hashRange
		// and the packet was directly from the client
		else if (p.relayAdr == null){
			p.clientAdr = senderAdr;
			p.relayAdr = myAdr;
			forward(p, hash);
		}
		//hash is not within this server's hashRange
		//and p was passed from another server
		else{
			forward(p, hash);
		}
		//if the key from the put request is
		//in the cache, remove the cached pair
		// that corresponds to that key
		if (cache.containsKey(p.key)){
			cache.remove(p.key);
		}
	}

	/** Handle a transfer packet.
	 *  @param p is a transfer packet
	 *  @param senderAdr is the the address (ip:port) of the sender
	 *  
	 *	check that the key hash is within the server's hashrange. If 
	 * the hash is within the hashrange, add the key-value pair to the
	 * map.
	 */
	public static void handleXfer(Packet p, InetSocketAddress senderAdr) {

		int hash = hashit(p.key);
		if(hash >= hashRange.left && hash <= hashRange.right) {
			map.put(p.key, p.val);
		}
	}

	/** Handle a reply packet.
	 *  @param p is a reply packet, more specifically, a packet of type
	 *  "success", "failure" or "no match"
	 *  @param senderAdr is the the address (ip:port) of the sender
	 *  
	 *  Set the packet's senderInfo, relayAdr, and clientAdr to null.
	 *  If caching is true and the packet is of type "success", cache 
	 *  the key-value pair in the cache map. 
	 */
	public static void handleReply(Packet p, InetSocketAddress senderAdr) {

		InetSocketAddress client_address = p.clientAdr;
		p.senderInfo = null;
		p.relayAdr = null;
		p.clientAdr = null;
		if (p.type.equals("success") && cacheOn && p.val != null && p.key != null){
			cache.put(p.key, p.val);
		}
		p.send(sock, client_address, debug);

	}
	/** Handle packets received from clients or other servers
	 *  @param p is a packet
	 *  @param senderAdr is the address (ip:port) of the sender
	 */
	public static void handlePacket(Packet p, InetSocketAddress senderAdr) {
		if (p.senderInfo != null & !p.type.equals("leave"))
			addRoute(p.senderInfo);
		if (p.type.equals("get")) {
			handleGet(p,senderAdr);
		} else if (p.type.equals("put")) {
			handlePut(p, senderAdr);
		} else if (p.type.equals("transfer")) {
			handleXfer(p, senderAdr);
		} else if (p.type.equals("success") ||
				p.type.equals("no match") ||
				p.type.equals("failure")) {
			handleReply(p, senderAdr);
		} else if (p.type.equals("join")) {
			handleJoin(p, senderAdr);
		} else if (p.type.equals("update")){
			handleUpdate(p, senderAdr);
		} else if (p.type.equals("leave")){
			handleLeave(p, senderAdr);
		}
	}

	/** Add an entry to the route tabe.
	 *  @param newRoute is a pair (addr,hash) where addr is the socket
	 *  address for some server and hash is the first hash in that
	 *  server's range
	 *
	 *  If the number of entries in the table exceeds the max
	 *  number allowed, the first entry that does not refer to
	 *  the successor of this server, is removed.
	 *  If debug is true and the set of stored routes does change,
	 *  print the string "rteTbl=" + rteTbl. (IMPORTANT)
	 */
	public static void addRoute(Pair<InetSocketAddress,Integer> newRoute){
		for (Pair<InetSocketAddress, Integer> route : rteTbl){
			if(route.equals(newRoute)){
				return;
			}
		}
		rteTbl.add(newRoute);
		if (rteTbl.size() > numRoutes){

			for(Pair<InetSocketAddress, Integer> route : rteTbl) {
				if(!route.equals(succInfo)) {
					rteTbl.remove(route);
					break;
				}
			}
		}
		if (debug){
			System.out.println("rteTbl=" + rteTbl);
		}
	}

	/** Remove an entry from the route tabe.
	 *  @param rmRoute is the route information for some server 
	 *  need to be removed from route table
	 *
	 *  If the route information exists in current entries, remove it.
	 *	Otherwise, do nothing.
	 *  If debug is true and the set of stored routes does change,
	 *  print the string "rteTbl=" + rteTbl. (IMPORTANT)
	 */
	public static void removeRoute(Pair<InetSocketAddress,Integer> rmRoute){
		for (Pair<InetSocketAddress, Integer> route : rteTbl){
			if(route.equals(rmRoute)){
				rteTbl.remove(rmRoute);
				if (debug){
					System.out.println("rteTbl=" + rteTbl);
				}
				return;
			}
		}
	}


	/** Forward a packet using the local routing table.
	 *  @param p is a packet to be forwarded
	 *  @param hash is the hash of the packet's key field
	 *
	 *  This method selects a server from its route table that is
	 *  "closest" to the target of this packet (based on hash).
	 *  If firstHash is the first hash in a server's range, then
	 *  we seek to minimize the difference hash-firstHash, where
	 *  the difference is interpreted modulo the range of hash values.
	 *  IMPORTANT POINT - handle "wrap-around" correctly. 
	 *  Once a server is selected, p is sent to that server.
	 */
	public static void forward(Packet p, int hash) {

		Pair<InetSocketAddress, Integer> best = null;
		int maxHash = hashRange.right;

		// no wrap around
		if (hashRange.right < hash){
			// find the largest first hash that is not greater than hash
			for (Pair<InetSocketAddress, Integer> route : rteTbl){
				if (route.right > maxHash && route.right <= hash){
					maxHash = route.right;
					best = route;
				}
			}
		}
		// does wrap around
		else{
			maxHash = 0;
			// check for firstHashes less than (or equal to hash) 
			// i.e. servers on the other side of the wrap around
			for (Pair<InetSocketAddress, Integer> route : rteTbl){
				if (route.right <= hash  && route.right >= maxHash){
					best = route;
					maxHash = route.right;
				}
			}
			// if no good servers found after the wrap around 
			// before hash, look for the largest firstHash 
			//i.e. the server closest to the wrap around
			if (maxHash == 0 || best == null){
				for (Pair<InetSocketAddress, Integer> route : rteTbl){
					if (route.right >= maxHash){
						best = route;
						maxHash = route.right;
					}
				}
			}
		}
		p.send(sock, best.left, debug);


	}
}