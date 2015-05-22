package cs455.overlay.node;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

import cs455.overlay.transport.*;
import cs455.overlay.util.InteractiveCommandParser;
import cs455.overlay.routing.*;
import cs455.overlay.wireformat.*;

//one of minimum 10 nodes responsible for sending (possible relay) receiving of packets in overlay network
public class MessagingNode extends Thread implements Node {
	private int idNumber; //random identifier for node
	private String registryHost; //host for registry in charge
	private int registryPort; //port to connect to
	private TCPConnection registry; //registry connection to send data to
	private TCPServerThread server; //server thread for listening
	private EventFactory eventFactory; //factory for dealing with messages
	private RoutingTable localTable; //local table for messaging node
	private TCPConnectionsCache localCache; //local connection cache
    static Random rand;
    private int localPort;
	private int [] nodeList; //for when the node wants to randomly send messages out
	private int totalNodeNumber; //total number of nodes
	private long numPacketsToSend; //num of packets to send set by registry
	private long packetsSent; // number of packets that were only started by the node sent
	private long packetsRelayed; //number of packets that were only relayed by the node
	private long sumOfPacketSent; //sum of packet pay loads started by this node
	private long packetsRecieved; //packets with this node as final destination
	private long sumOfPacketReceived; //only with packets that have this node as final destination
	private Queue<OverlayNodeSendsData> relayQueue = new LinkedList<OverlayNodeSendsData>();
	
	//Messaging Node  
	public MessagingNode(int port, String registry) {
		registryPort = port; // set
		registryHost = registry; //set
		server = new TCPServerThread(this,0); //Initialize
		//initialize the registry
		eventFactory = new EventFactory();//set

		localCache = new TCPConnectionsCache();
		localTable = new RoutingTable();
		rand = new Random(System.currentTimeMillis());
		packetsSent = 0; 
		packetsRelayed = 0; 
		sumOfPacketSent = 0; 
		packetsRecieved = 0; 
		sumOfPacketReceived = 0;
	}
	public void run(){
		while(true){
			OverlayNodeSendsData relayMsg;
			synchronized(relayQueue){
				relayMsg = relayQueue.poll();
			}
			if(relayMsg != null){
				this.relayData(relayMsg);
			}
		}
	}
	//sends data to event factory to intemperate bytes for response
	public void onEvent(byte[] data) {
		Event onevent = eventFactory.createEvent(data);
		if(onevent == null){
			return;
		}
		switch(onevent.getType()){
			case REGISTRY_REPORTS_REGISTRATION_STATUS: {
													   RegistryReportsRegistrationStatus event = (RegistryReportsRegistrationStatus) onevent;
													   if(event.getNodeIdSuccessMessage() > -1){
														   this.idNumber = event.getNodeIdSuccessMessage();
														   System.out.println(event.getInfoString());
													   }
													   else{
														   System.out.println(event.getInfoString());
													   }
													   break;
			}
			case REGISTRY_REPORTS_DEREGISTRATION_STATUS: {
													   RegistryReportsDeregistrationStatus event = (RegistryReportsDeregistrationStatus) onevent;
													   if(event.getNodeIdSuccessMessage() > -1){
														   this.idNumber = event.getNodeIdSuccessMessage();
														   System.out.println(event.getInfoString());
														   System.exit(0);
													   }
													   else{
														   System.out.println(event.getInfoString());
													   }
													   break;
			}
			case REGISTRY_SENDS_NODE_MANIFEST: {
													   RegistrySendsNodeManifest event = (RegistrySendsNodeManifest) onevent;
													   this.setUpOverlay(event);
													   break;
			}
			case REGISTRY_REQUESTS_TASK_INITIATE: {
													   RegistryRequestsTaskInitiate event = (RegistryRequestsTaskInitiate) onevent;
													   this.setNumPacketsToSend(event.getPacketNum());
													   this.initialize();
													   break;
			}
			case OVERLAY_NODE_SENDS_DATA: {
													   OverlayNodeSendsData event = (OverlayNodeSendsData) onevent;
													   int destId = event.getDest();
													   if(destId == this.getNodeId()){
															this.incPacketsRecieved();
															this.incSumOfPacketReceived(event.getPayload());
													   }else{
														   synchronized(relayQueue){
															   relayQueue.add(event);
														   }
													   }
													   
													   break;
			}
			case REGISTRY_REQUESTS_TRAFFIC_SUMMARY: {
				   									   //RegistryRequestsTrafficSummary event = (RegistryRequestsTrafficSummary) onevent; dont need this
				   									   this.sendTrafficSummary();
				   									   break;
			}
			default: System.out.println("Error: invalid message from registry");
					 break;
		}
	}
	//required not used in this class (as of yet)
	public void onEvent(String ipAddress, byte[] data) {
		onEvent(data);
	}
	//first order of business for the node is to register with the registry
	public void sendRegistration(){
		try {
			//create connection on local end to be able to send to registry
			Socket imp = new Socket(InetAddress.getByName(registryHost), registryPort);
			localPort = imp.getLocalPort();
			//imp.bind(new InetSocketAddress(this.getIpAddressString(), this.getPort()));
			this.registry = new TCPConnection(imp, this);

		} catch (IOException e) {
			System.out.println("could not send registration");
		}
		OverlayNodeSendsRegistration send = new OverlayNodeSendsRegistration(this); //create bytes to send
		byte [] data = send.getBytes();
		try {
			registry.sendData(data); //try to send bytes to registry
		} catch (IOException e) {
			System.out.println("could not send registration");
		}
		return;
	}
	//final order of business for the node is to de-register with the registry
	public void sendDeregistration(){
		
		OverlayNodeSendsDeregistration send = new OverlayNodeSendsDeregistration(this); //create bytes to send
		byte [] data = send.getBytes();
		try {
			registry.sendData(data); //try to send bytes to registry
		} catch (IOException e) {
			System.out.println("could not send deregistration");
		}
		return;
	}
	//set up the overlay based off event data
	public void setUpOverlay(RegistrySendsNodeManifest event){
		try {
			Iterator<TCPConnection> toclose = this.getConnections();
			while(toclose.hasNext()){
				toclose.next().close();
			}
			localTable.clear();
			for (int i = 0; i < event.getTableSize(); i++){
			//first set up the routing table
				RoutingEntry toadd = new RoutingEntry(event.getIp(i), event.getPortNum(i), event.getPortNum(i));
				this.addRouting(toadd, event.getHopId(i));
				//now try to set up connection
				Socket imp = new Socket(InetAddress.getByName(toadd.getIpAddressString()), toadd.getPortNum());
				this.addConnection(new TCPConnection(imp, this, event.getHopId(i)));
				nodeList = event.getNodes();
				totalNodeNumber = event.getNodeNumber();
				
			} 
			registry.sendData(new NodeReportsOverlaySetupStatus(this, this.getNodeId()).getBytes());
		}catch (Exception e) {
				try {
					//if failed send the registry failed setup notice
					registry.sendData(new NodeReportsOverlaySetupStatus(this).getBytes());
				} catch (IOException e1) {
					System.out.println("could not send overlay setup status");
				}
			}
	}
	//send traffic summary to registry, make sure to wait to give all relaying time to finish
	public void sendTrafficSummary() {
		try {
			synchronized(registry){
				registry.sendData(new OverlayNodeReportsTrafficSummary(this).getBytes());
			}
			packetsSent = 0; 
			packetsRelayed = 0; 
			sumOfPacketSent = 0; 
			packetsRecieved = 0; 
			sumOfPacketReceived = 0;
			this.setNumPacketsToSend(0);
		} catch(IOException ex) {
		    System.out.println("could not send traffic summary");
		}
	}
	//Initialize sending of nodes
	public void initialize() {
		packetsSent = 0; 
		packetsRelayed = 0; 
		sumOfPacketSent = 0; 
		packetsRecieved = 0; 
		sumOfPacketReceived = 0;
		TCPConnection[] list = new TCPConnection[localCache.size()];
		Iterator<TCPConnection> ptr = this.getConnections();
		int count = 0;
		while(ptr.hasNext()){
			list[count] = ptr.next();
			count ++;
		}
		try {
			for(int i = 0; i < numPacketsToSend; i++){
				int packetPayLoad = rand.nextInt();
				//get a random connection to send to
				synchronized(list[i % (count - 1)]){
				list[i % (count - 1)].sendData(
						new OverlayNodeSendsData(this, packetPayLoad, nodeList[(this.randInt(0, 100) % (totalNodeNumber))], 0, new int [0]
								).getBytes()
						);
				}
				//increment packets sent by one
				this.incPacketsSent();
				//also increase sum of packet payloads
				this.incSumOfPacketSent(packetPayLoad);
			}
		synchronized(registry){
			registry.sendData(new OverlayNodeReportsTaskFinished(this).getBytes());
		}
		} catch(IOException ex) {
		    System.out.println("could not send report of task finished");
		}
	}
	//relay data based of off routing table and certain number of hops
	public  void relayData(OverlayNodeSendsData event){
			int destId = event.getDest();
			// if this node is the final destination

			//play the circle game
			int closestKey = 0;
			Iterator<Integer> keys = this.getKeys().iterator();
			boolean setflag = false;
			for(int i = 0; i < this.getRoutingSize(); i++){
				int checkValue = keys.next().intValue();
				//first case if we have the dest node in routing table
				if(checkValue == destId){
					closestKey = checkValue;
					setflag = true;
					break;
				}
				//second case go through if key at routing entry is less than destination key then it in within that range
				else if(checkValue < destId){
					closestKey = checkValue;
					setflag = true;
				}
				//this is the weird case that sets the circular property
				else if(checkValue > destId){
					//have to check if routing has an entry past this one
					if(i + 1 < this.getRoutingSize()){
						//if the next entry is also greater but less than the current we know the circle starts over
						if(this.getKey(i + 1) > destId && this.getKey(i + 1) < checkValue){
							closestKey = checkValue;
							setflag = true;
						}
					}
				}
				//last case if trying to get back to an entry that is behind start node set default hop to most
				if (i == (this.getRoutingSize() - 1) && !setflag){
					closestKey = this.getKey(i);
				}
			}
			synchronized(getConnection(closestKey)){
				OverlayNodeSendsData nextevent = new OverlayNodeSendsData(this, event.getPayload(), event.getDest(), event.getHop(), event.getNodeTrace());
			
			this.incPacketsRelayed();
			try {
				this.getConnection(closestKey).sendData(nextevent.getBytes());
			} catch (IOException e) {
				System.out.println("could not relay data");
			}}
			//int closestId = this.getRouting(1);
		
	}
	//generate a random number for nodes to register with
	public int randInt(int min, int max) {
	    // nextInt is normally exclusive of the top value,
	    // so add 1 to make it inclusive
	    int randomNum = rand.nextInt((max - min) + 1) + min;

	    return randomNum;
	}
	//return port number of node
	public int getPort() {
		return this.server.getSocket().getLocalPort();
	}
	public int getRegPort() {
		return localPort;
	}
	//return raw byte ip address
	public byte[] getIpAddress() {
		InetAddress address;
		byte[] rawAddress = null;
		try {
			address = InetAddress.getLocalHost();
			rawAddress = address.getAddress();
		} catch (UnknownHostException e) {
			System.out.println("unknown host");
		}
		return rawAddress;
	}
	//return ip address in text format
	public String getIpAddressString() {
		InetAddress address;
		String rawAddress = null;
		try {
			address = InetAddress.getLocalHost();
			rawAddress = address.getHostAddress();
		} catch (UnknownHostException e) {
			System.out.println("unknown host");
		}
		return rawAddress;
	}
	//get id of current node
	public int getNodeId() {
		return idNumber;
	}
	//add to the connection cache
	public synchronized void addConnection(TCPConnection connection) {
		this.localCache.addConnection(connection);
	}
	//remove from the connection cache
	public synchronized void removeConnection(TCPConnection connection) {
		this.localCache.removeConnection(connection);
	}
	public synchronized TCPConnection getConnection(int id) {
		return this.localCache.getConnection(id);
	}
	public synchronized Iterator<TCPConnection> getConnections(){
		return this.localCache.getConnections();
	}
	//add to the routing table
	public synchronized void addRouting(RoutingEntry connection, int id) {
		this.localTable.addEntry(connection, id);
	}
	//remove from the routing table
	public synchronized void removeRouting(int id) {
		this.localTable.remove(id);
	}
	public synchronized void getRouting(int id) {
		this.localTable.getEntry(id);
	}
	//return routing size 
	public synchronized int getRoutingSize() {
		return this.localTable.size();
	}
	//returns an enumeration of table keys
	public synchronized Set<Integer> getKeys(){
		return localTable.getKeys();
	}
	//returns an enumeration of table keys
	public synchronized Integer getKey(int index){
		return localTable.getKey(index);
	}
	//return the numPacketsToSend
	public long getNumPacketsToSend() {
		return numPacketsToSend;
	}
	//numPacketsToSend the numPacketsToSend to set
	public void setNumPacketsToSend(long l) {
		this.numPacketsToSend = l;
	}
	//return the packetsRelayed
	public long getPacketsRelayed() {
		return packetsRelayed;
	}
	//increment packets relayed
	public void incPacketsRelayed() {
		this.packetsRelayed ++;
	}	
	//return the packetsSent
	public long getPacketsSent() {
		return packetsSent;
	}
	//increment packets relayed
	public void incPacketsSent() {
		this.packetsSent ++;
	}
	//return the packetsRecieved
	public long getPacketsRecieved() {
		return packetsRecieved;
	}
	//packetsRecieved the packetsRecieved to set
	public void incPacketsRecieved() {
		this.packetsRecieved++;
	}
	//return the sumOfPacketSent
	public long getSumOfPacketSent() {
		return sumOfPacketSent;
	}
	// sumOfPacketSent the sumOfPacketSent to set
	public void incSumOfPacketSent(long sumOfPacketSent) {
		this.sumOfPacketSent = this.sumOfPacketSent + sumOfPacketSent;
	}
	//return the sumOfPacketReceived
	public long getSumOfPacketReceived() {
		return sumOfPacketReceived;
	}
	//sumOfPacketReceived the sumOfPacketReceived to set
	public void incSumOfPacketReceived(long sumOfPacketReceived) {
		this.sumOfPacketReceived = this.sumOfPacketReceived + sumOfPacketReceived;
	}	
	//print counters and diagnostics
	public void printCountersAndDiagnostics() {
		System.out.println("Counters: \nPackets Relayed: " + this.getPacketsRelayed() + "\n" + "Packets Sent: " 
							+ this.getPacketsSent() + "\n" + "Packets Recieved: " + this.getPacketsRecieved() + "\n" +"Sum Of Packet's Payloads Sent: "
							+ this.getSumOfPacketSent() + "\n" + "Sum Of Packet's Payloads Received: " + this.getSumOfPacketReceived());	
		
	}
	public static void main(String args[]) {
		MessagingNode node = null;
		if(args.length > 1 ){
			int portname = Integer.parseInt(args[1]);
			node = new MessagingNode(portname, args[0]);
		}
		else{
			System.out.println("expecting arguments <registry host> & <registry port num>");
			System.exit(0);
		}
		//System.out.println("listening on: " + node.getPort());
		node.sendRegistration();
		System.out.println("listening on: " + node.getPort());
		Thread t = new Thread(node);
		t.start();
		InteractiveCommandParser commander = new InteractiveCommandParser(node);
		commander.run();
	}

}
