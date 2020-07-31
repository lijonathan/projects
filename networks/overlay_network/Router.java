import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

/** Router module for an overlay router.
 *
 *  Your documentation here
 *  Should describe the protocol used by the router and a high level
 *  description of when various packets are sent and how received
 *  packets are processed
 */
public class Router implements Runnable {
	private Thread myThread;	// thread that executes run() method

	private int myIp;		// ip address in the overlay
	private String myIpString;	// String representation
	private ArrayList<Prefix> pfxList; // list of prefixes to advertise
	private ArrayList<NborInfo> nborList; // list of info about neighbors

	private class LinkInfo { // class used to record link information
		public int peerIp;	// IP address of peer in overlay net
		public double cost;	// in seconds
		public boolean gotReply; // flag to detect hello replies
		public int helloState;	// set to 3 when hello reply received
		// decremented whenever hello reply
		// is not received; when 0, link is down

		// link cost statistics
		public int count;
		public double totalCost;
		public double minCost;
		public double maxCost;

		LinkInfo() {
			cost = 0; gotReply = true; helloState = 3;
			count = 0; totalCost = 0; minCost = 10; maxCost = 0;
		}
	}
	private ArrayList<LinkInfo> lnkVec;  // indexed by link number

	private class Route { // routing table entry
		public Prefix pfx;	// destination prefix for route
		public double timestamp; // time this route was generated
		public double cost;	// cost of route in ns
		public LinkedList<Integer> path; // list of router IPs;
		// destination at end of list
		public int outLink;	// outgoing link for this route
		public boolean valid;	//indicate the valid of the route
	}
	private ArrayList<Route> rteTbl;  // routing table

	private Forwarder fwdr;		// reference to Forwarder object

	private double now;		// current time in ns
	private static final double sec = 1000000000;  // ns per sec

	private int debug;		// controls debugging output
	private boolean quit;		// stop thread when true
	private boolean enFA;		// link failure advertisement enable


	/** Initialize a new Router object.
	 *  
	 *  @param myIp is an integer representing the overlay IP address of
	 *  this node in the overlay network
	 *  @param fwdr is a reference to the Forwarder object through which
	 *  the Router sends and receives packets
	 *  @param pfxList is a list of prefixes advertised by this router
	 *  @param nborList is a list of neighbors of this node
	 *
	 *  @param debug is an integer that controls the amount of debugging
	 *  information that is to be printed
	 */

	Router(int myIp, Forwarder fwdr, ArrayList<Prefix> pfxList,
			ArrayList<NborInfo> nborList, int debug, boolean enFA) {
		this.myIp = myIp; this.myIpString = Util.ip2string(myIp);
		this.fwdr = fwdr; this.pfxList = pfxList;
		this.nborList = nborList; this.debug = debug;
		this.enFA = enFA;

		lnkVec = new ArrayList<LinkInfo>();
		for (NborInfo nbor : nborList) {
			LinkInfo lnk = new LinkInfo();
			lnk.peerIp = nbor.ip;
			lnk.cost = nbor.delay;
			lnkVec.add(lnk);
		}
		rteTbl = new ArrayList<Route>();
		quit = false;
	}

	/** Instantiate and start a thread to execute run(). */
	public void start() {
		myThread = new Thread(this); myThread.start();
	}

	/** Terminate the thread. */
	public void stop() throws Exception { quit = true; myThread.join(); }

	/** This is the main thread for the Router object.
	 *
	 * The run method handles sending hello packets
	 * every second, sending advertisements every
	 * 10 seconds, and sending incoming packets to
	 * the handleIncoming function.
	 * After the while loop finishes, the Router
	 * link cost statistics are also printed out.
	 */
	public void run() {
		double t0 = System.nanoTime()/sec;
		now = 0;
		double helloTime, pvSendTime;
		helloTime = pvSendTime = now;
		while (!quit) {
			now = System.nanoTime()/sec - t0;
			// if it's time to send hello packets, do it
			if(helloTime + 1 < now){//send once every second
				helloTime = now;
				sendHellos();
			}
			// else if it's time to send advertisements, do it
			else if(pvSendTime + 10 < now){//once every 10 seconds
				pvSendTime = now;
				sendAdverts();
			}

			// else if the forwarder has an incoming packet
			// to be processed, retrieve it and process it
			else if(fwdr.incomingPkt()){
				handleIncoming();
			}

			// else nothing to do, so take a nap
			else{
				try{
					Thread.sleep(1);
				}
				catch(InterruptedException e){
					e.printStackTrace();
				}
			}

		}
		String s = String.format("Router link cost statistics\n" + 
				"%8s %8s %8s %8s %8s\n","peerIp","count","avgCost",
				"minCost","maxCost");
		for (LinkInfo lnk : lnkVec) {
			if (lnk.count == 0) continue;
			s += String.format("%8s %8d %8.3f %8.3f %8.3f\n",
					Util.ip2string(lnk.peerIp), lnk.count,
					lnk.totalCost/lnk.count,
					lnk.minCost, lnk.maxCost);
		}
		System.out.println(s);
	}

	/** Lookup route in routing table.
	 *
	 * @param pfx is IP address prefix to be looked up.
	 * @return a reference to the Route that matches the prefix or null
	 */
	private Route lookupRoute(Prefix pfx) {
		Route match = null;
		if (pfxList.contains(pfx)){
			int index = pfxList.indexOf(pfx);
			match = rteTbl.get(index);
		}
		return match;
	}

	/** Add a route to the routing table.
	 * 
	 *  @param rte is a route to be added to the table; no check is
	 *  done to make sure this route does not conflict with an existing
	 *  route
	 */
	private void addRoute(Route rte) {
		rteTbl.add(rte);
	}

	/** Update a route in the routing table.
	 *
	 *  @param rte is a reference to a route in the routing table.
	 *  @param nuRte is a reference to a new route that has the same
	 *  prefix as rte
	 *  @return true if rte is modified, else false
	 *
	 *  This method replaces certain fields in rte with fields
	 *  in nuRte. Specifically,
	 *
	 *  if nuRte has a link field that refers to a disabled
	 *  link, ignore it and return false
	 *
	 *  else, if the route is invalid, then update the route
	 *  and return true,
	 *
	 *  else, if both routes have the same path and link,
	 *  then the timestamp and cost fields of rte are updated
	 *
	 *  else, if nuRte has a cost that is less than .9 times the
	 *  cost of rte, then all fields in rte except the prefix fields
	 *  are replaced with the corresponding fields in nuRte
	 *
	 *  else, if nuRte is at least 20 seconds newer than rte
	 *  (as indicated by their timestamps), then all fields of
	 *  rte except the prefix fields are replaced
	 *
	 *  else, if the link field for rte refers to a link that is
	 *  currently disabled, replace all fields in rte but the
	 *  prefix fields
	 */
	private boolean updateRoute(Route rte, Route nuRte) {
		boolean result = false;
		LinkInfo nuLink = lnkVec.get(nuRte.outLink);
		LinkInfo oldLink = lnkVec.get(rte.outLink);
		if (nuLink.helloState == 0){
			result = false;
		}
		// if the route is invalid, update and return true
		else if (rte.valid == false){
			replaceExceptPrefix(rte,nuRte);
			result = true;
		}
		//if both routes have same path and link, then timestamp 
		//and cost of rte are updated
		else if (rte.outLink == nuRte.outLink && 
				rte.path == nuRte.path){
			rte.timestamp = nuRte.timestamp;
			rte.cost = nuRte.cost;
			result = true;
		}
		//if nuRte has a cost that is less than .9 times the
		//cost of rte, then all fields in rte except the prefix fields
		//are replaced with the corresponding fields in nuRte
		else if (nuRte.cost < (.9 * rte.cost)){
			replaceExceptPrefix (rte, nuRte);
			result = true;
		}
		//else, if nuRte is at least 20 seconds newer than rte
		//(as indicated by their timestamps), then all fields of
		//rte except the prefix fields are replaced
		else if (nuRte.timestamp - rte.timestamp >= 20){
			replaceExceptPrefix (rte, nuRte);
			result = true;
		}
		//else, if the link field for rte refers to a link that is
		//currently disabled, replace all fields in rte but the
		//prefix fields
		else if (oldLink.helloState == 0){
			replaceExceptPrefix(rte, nuRte);
			result = true;
		}

		return result;	
	}
	/**
	 * @param rte original reference to the route in the 
	 * 	routing table
	 * @param nuRte reference to the new route whose 
	 * 	fields will replace rte's fields
	 * 
	 * This function replaces all of rte's fields, except 
	 * for prefix, with nuRte's field values
	 */
	private void replaceExceptPrefix (Route rte, Route nuRte){
		rte.cost = nuRte.cost;
		rte.outLink = nuRte.outLink;
		rte.path = nuRte.path;
		rte.timestamp = nuRte.timestamp;
		rte.valid = nuRte.valid;
	}

	/** Send hello packet to all neighbors.
	 *
	 *  First check for replies. If no reply received on some link,
	 *  update the link status by subtracting 1. If that makes it 0,
	 *  the link is considered down, so we mark all routes using 
	 *  that link as invalid. Also, if certain routes are marked as 
	 *  invalid, we will need to print the table if debug larger 
	 *  than 1, and we need to send failure advertisement by 
	 *  calling sendFailureAdvert if failure advertisement is enable.
	 */
	public void sendHellos() {
		int lnk = 0;
		boolean isChanged = false;
		for (LinkInfo lnkInfo : lnkVec) {

			// if no reply to the last hello, subtract 1 from
			// link status if it's not already 0
			if (!lnkInfo.gotReply){
				if (lnkInfo.helloState != 0){
					lnkInfo.helloState--;
				}	

				// go through the routes to check routes 
				// that contain the failed link
				else {
					for (Route rte : rteTbl){
						if (rte.path.contains(lnk)){
							if (rte.valid != false){
								isChanged = true;
							}
							rte.valid = false;
						}
					}	
				}

			}
			// print routing table if debug is enabled 
			// and valid field of route is changed
			if (debug > 0 && isChanged){
				printTable();
			}

			// send link failure advertisement if enFA is enabled
			// and valid field of route is changed
			if (enFA && isChanged){
				sendFailureAdvert(lnk);
				isChanged = false;
			}

			// send new hello, after setting gotReply to false
			lnkInfo.gotReply = false;
			Packet p = new Packet();
			p.protocol = 2;
			p.ttl = 100;
			p.srcAdr = myIp;
			p.destAdr = lnkInfo.peerIp;
			p.payload = String.format("RPv0\ntype: hello\n"
					+ "timestamp: %.3f \n",
					now);
			fwdr.sendPkt(p, lnk);
		}
	}

	/** Send initial path vector to each of our neighbors.  */
	public void sendAdverts() {
		for (int pfx = 0; pfx < pfxList.size(); pfx++){
			for(int lnk = 0; lnk < nborList.size(); ++lnk){
				if(lnkVec.get(lnk).helloState == 0){
					continue;
				}
				Packet p = new Packet();
				p.protocol = 2; p.ttl = 100;
				p.srcAdr = myIp;
				p.destAdr = lnkVec.get(lnk).peerIp;
				p.payload = String.format("RPv0\ntype: advert\n" 
						+ "pathvec: %s %.3f %d %s\n",
						pfxList.get(pfx).toString(), now, 0, myIpString); //potential problems
				fwdr.sendPkt(p,lnk);
			}
		}
	}


	/** Send link failure advertisement to all available neighbors
	 *
	 *  @param failedLnk is the number of link on which is failed.
	 *
	 */
	public void sendFailureAdvert(int failedLnk){
		int failIp = lnkVec.get(failedLnk).peerIp;
		String failIpString = Util.ip2string(failIp);

		for (int lnk = 0; lnk < nborList.size(); lnk++) {
			if (lnkVec.get(lnk).helloState == 0) continue;
			Packet p = new Packet();
			p.protocol = 2; p.ttl = 100;
			p.srcAdr = myIp;
			p.destAdr = lnkVec.get(lnk).peerIp;
			p.payload = String.format("RPv0\ntype: fadvert\n"
					+ "linkfail: %s %s %.3f %s\n",
					myIpString,  failIpString, now, myIpString);
			fwdr.sendPkt(p,lnk);
		}
	}

	/** Retrieve and process packet received from Forwarder.
	 *
	 *  For hello packets, we simply echo them back.
	 *  For replies to our own hello packets, we update costs.
	 *  For advertisements, we update routing state and propagate
	 *  as appropriate.
	 */
	public void handleIncoming() {
		// parse the packet payload
		Pair<Packet,Integer> pp = fwdr.receivePkt();
		Packet p = pp.left; int lnk = pp.right;

		String[] lines = p.payload.split("\n");
		if (!lines[0].equals("RPv0")) return;

		String[] chunks = lines[1].split(":");
		if (!chunks[0].equals("type")) return;
		String type = chunks[1].trim();
		
		// if it's an route advert, call handleAdvert
		if (type.equals("advert")){
			handleAdvert(lines, lnk);
		}
		// if it's an link failure advert, call handleFailureAdvert
		if (type.equals("fadvert")){
			handleFailureAdvert(lines, lnk);
		}
		// if it's a hello, echo it back
		if (type.equals("hello")){
			chunks = lines[2].split(":");
			if (!chunks[0].equals("timestamp")) return;
			String timestamp = chunks[1].trim();
			p.payload = String.format("RPv0\ntype: echo\n"
					+ "timestamp: %s \n", timestamp);
			fwdr.sendPkt(p, lnk);
		}
		// else it's a reply to a hello packet
		// use timestamp to determine round-trip delay
		// use this to update the link cost using exponential
		// weighted moving average method
		// also, update link cost statistics
		// also, set gotReply to true
		else {
			chunks = lines[2].split(":");
			if (!chunks[0].equals("timestamp")) return;
			String timeString = chunks[1].trim();
			double timestamp = Double.parseDouble(timeString);
			double rtDelay = now - timestamp;

			LinkInfo link = lnkVec.get(lnk);
			link.helloState = 3;
			link.cost = rtDelay/2;
			Route rt = new Route();
			rt.outLink = lnk;
			boolean match = false;
			for (Route rte : rteTbl){
				if (rte.outLink == lnk){
					rt = rte;
					match = true;
					break;
				}
			}		
			Route newRt = rt;
			link.count = 0;
			link.totalCost = 0;
			for (LinkInfo l : lnkVec){
				if(l.helloState != 0){
					link.count++;
					link.totalCost += l.cost;
				}
			}
			newRt.cost = link.cost;
			if (updateRoute(rt,newRt)){
				link.cost = rt.cost;
			}
			if (link.cost > link.maxCost){
				link.maxCost  = link.cost;
			}
			if (link.cost < link.minCost){
				link.minCost = link.cost;
			}
			link.gotReply = true;
			lnkVec.set(lnk, link);
		}

	}

	/** Handle an advertisement received from another router.
	 *
	 *  @param lines is a list of lines that defines the packet;
	 *  the first two lines have already been processed at this point
	 *
	 *  @param lnk is the number of link on which the packet was received
	 */
	private void handleAdvert(String[] lines, int lnk) {
		// example path vector line
		// pathvec: 1.2.0.0/16 345.678 .052 1.2.0.1 1.2.3.4

		// Parse the path vector line.
		String[] chunks = lines[2].split(":");
		chunks[1] = chunks[1].trim();
		String[] pathVec = chunks[1].split(" ");
		// If there is loop in path vector, ignore this packet.
		boolean looping = false;
		int[] paths = new int[pathVec.length-3];
		for (int i = 3; i < pathVec.length; i++){
			String ipString = pathVec[i].toString().replace(".","");
			paths[i-3] = Integer.parseInt(ipString);
			for (int j = i + 1; j < pathVec.length; j++){
				if (pathVec[i].equals(pathVec[j])){
					looping = true;
					return;
				}
			}
		}
		// Form a new route, with cost equal to path vector cost
		// plus the cost of the link on which it arrived.
		Route newRt = new Route();
		newRt.cost = Double.parseDouble(pathVec[2]) + lnkVec.get(lnk).cost;
		LinkedList<Integer> pathList = new LinkedList<Integer>();
		for (int i : paths){
			pathList.add(i);
		}
		newRt.path = pathList;
		newRt.cost = 0;
		newRt.outLink = lnk;
		newRt.pfx = new Prefix (pathVec[0]);
		newRt.timestamp = now;
		newRt.valid = true;
		
		// Look for a matching route in the routing table
		// and update as appropriate; whenever an update
		// changes the path, print the table if debug>0;
		boolean match = false;
		for (Route rte : rteTbl){
			if (rte.path.equals(newRt.path)){
				match = true;
				int prevOL = newRt.outLink;
				updateRoute(rte, newRt);
				if (debug > 0){ printTable();}
				
				// whenever an update changes the output link,
				// update the forwarding table as well.
				Prefix pre = new Prefix(pathVec[0]);
				pfxList.add(pre);
				fwdr.addRoute(pre, lnk);

				// If the new route changed the routing table,
				// extend the path vector and send it to other neighbors.
				Packet p = new Packet();
				p.protocol = 2; p.ttl = 100;
				p.srcAdr = myIp;
				p.destAdr = lnkVec.get(lnk).peerIp;
				String pathString = "";
				for(Integer path : rte.path){
					String temp = path.toString();
					pathString = pathString+ temp.charAt(0) + "." + temp.charAt(1) + "." + temp.charAt(2) + "." + temp.charAt(3) + " ";
				}
				pathString = pathString + myIpString;
				p.payload = String.format("RPv0\ntype: advert\n" 
						+ "pathvec: %s %s %.3f %s\n",
						rte.pfx ,  rte.timestamp, rte.cost, pathString);
				fwdr.sendPkt(p,lnk);

			}
		}
		if (!match){
			addRoute(newRt);
		}

	}

	/** Handle the failure advertisement received from another router.
	 *
	 *  @param lines is a list of lines that defines the packet;
	 *  the first two lines have already been processed at this point
	 *
	 *  @param lnk is the number of link on which the packet was received
	 */
	private void handleFailureAdvert(String[] lines, int lnk) {
		// example path vector line
		// fadvert: 1.2.0.1 1.3.0.1 345.678 1.4.0.1 1.2.0.1
		// meaning link 1.2.0.1 to 1.3.0.1 is failed


		// Parse the path vector line.
		// If there is loop in path vector, ignore this packet.
		boolean rteChanged = false;
		String[] chunks = lines[2].split(":");
		String[] pathVec = chunks[1].split(" ");
		for (int i = 3; i < pathVec.length; i++){
			for (int j = i; j < pathVec.length; j++){
				if (pathVec[i].equals(pathVec[j])){
					return;
				}
			}
		}
		// go through routes to check if it contains the link
		// set the route as invalid (false) if it does
		for (Route rte : rteTbl){
			rteChanged = false;
			if (rte.path.contains(lnk)){
				rte.valid = false;
				rteChanged = true;
			}
			// update the time stamp if route is changed
			// print route table if route is changed and debug is enabled
			if (rteChanged){
				Route update = rte;
				update.timestamp = now;
				updateRoute(rte, update);
				if (debug > 0){printTable();}
				// If one route is changed, extend the message 
				// and send it to other neighbors.
				sendFailureAdvert(rte.outLink);
			}
		}
	}

	/** Print the contents of the routing table. */
	public void printTable() {
		String s = String.format("Routing table (%.3f)\n"
				+ "%10s %10s %8s %5s %10s \t path\n", now, "prefix", 
				"timestamp", "cost","link", "VLD/INVLD");
		for (Route rte : rteTbl) {
			s += String.format("%10s %10.3f %8.3f",
					rte.pfx.toString(), rte.timestamp, rte.cost);

			s += String.format(" %5d", rte.outLink);

			if (rte.valid == true)
				s+= String.format(" %10s", "valid");
			else
				s+= String.format(" %10s \t", "invalid");

			for (int r :rte.path)
				s += String.format (" %s",Util.ip2string(r));

			if (lnkVec.get(rte.outLink).helloState == 0)
				s += String.format("\t ** disabled link");
			s += "\n";
		}
		System.out.println(s);
	}
}
