package cs455.overlay.node;


import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;
import java.util.Set;

import cs455.overlay.routing.RoutingEntry;
import cs455.overlay.routing.RoutingTable;
import cs455.overlay.transport.*;
import cs455.overlay.util.InteractiveCommandParser;
import cs455.overlay.util.StatisticsCollectorAndDisplay;
import cs455.overlay.wireformat.*;

public class Registry implements Node {
	private int portNum; //port of registry
	private TCPServerThread server; //server thread for registry
	private EventFactory eventFactory; //generates events from responses or messages from messaging node
	private RoutingTable globalTable; //will contain every single node that registers with the registry
	private int idNumber;
	private LinkedList<RegistrySendsNodeManifest> manifests;
    static Random rand;
	private TCPConnectionsCache localCache; //connection list
	StatisticsCollectorAndDisplay report;
	//constructor
	public Registry(int port) {
		portNum = port; //set
		eventFactory = new EventFactory(); //Initialize
		server = new TCPServerThread(this,portNum); //Initialize
		globalTable = new RoutingTable(); //Initialize
		idNumber = 0;
		localCache = new TCPConnectionsCache();
		rand = new Random(System.currentTimeMillis());
		manifests = new LinkedList<RegistrySendsNodeManifest>();
	}
	public void onEvent(String ipAddress, byte[] data) {
		Event onevent = eventFactory.createEvent(data);
		switch(onevent.getType()){
			case OVERLAY_NODE_SENDS_REGISTRATION: { OverlayNodeSendsRegistration event = (OverlayNodeSendsRegistration)onevent;
					if(ipAddress.compareTo(event.getIpAddress()) == 0 && !globalTable.contains(event.getIpAddress(), event.getPort())){
						this.registerNode(event,true);
					}
					else{
						this.registerNode(event,false);
					}
					break;
			}
			case OVERLAY_NODE_SENDS_DEREGISTRATION: { OverlayNodeSendsDeregistration event = (OverlayNodeSendsDeregistration)onevent;
					if(ipAddress.compareTo(event.getIpAddress()) == 0 && globalTable.contains(event.getIpAddress(), event.getPort())){
						this.deregisterNode(event,true);
						this.sendNodeManifest();
					}
					else{
						this.deregisterNode(event,false);
					}
					break;
			}
			default: onEvent(data);
					 break;
		}
	}
	//sends data to event factory to intemperate bytes for response
	public void onEvent(byte[] data) {
		Event onevent = eventFactory.createEvent(data);
		switch(onevent.getType()){
			case NODE_REPORTS_OVERLAY_SETUP_STATUS: { NodeReportsOverlaySetupStatus event = (NodeReportsOverlaySetupStatus)onevent;
					 System.out.println(event.getInfoString());
					 break;
			}
			case OVERLAY_NODE_REPORTS_TASK_FINISHED: { OverlayNodeReportsTaskFinished event = (OverlayNodeReportsTaskFinished) onevent;
					 System.out.println("Node " + event.getNodeId() + " reports task finished");
					 System.out.println("Waiting 10 seconds until send of traffic report");
					    try {
							Thread.sleep(10000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							System.out.println("could not wait");
						}                 //1000 milliseconds is one second., give time to wait
					 this.sendTrafficSummaryRequest(event);
					 break;
			}
			case OVERLAY_NODE_REPORTS_TRAFFIC_SUMMARY: { OverlayNodeReportsTrafficSummary event = (OverlayNodeReportsTrafficSummary) onevent;	
					 this.relayTrafficReport(event);
					 break;
			}
			default: System.out.println("Error: invalid event from message node");
					 break;
		}
	}
	//register the node from sent massage
	public void registerNode(OverlayNodeSendsRegistration event, boolean flag){
		TCPConnection connection = this.getConnection(event.getIpAddress(), event.getPort());
		int nodeId = randInt(1,127);
		while(globalTable.contains(nodeId)){
			nodeId = randInt(1,127);
		}
		connection.setId(nodeId);
		if(flag){
			globalTable.addEntry(new RoutingEntry(event.getIp(),event.getPort(), event.getServerPort()),nodeId);
			try {
				connection.sendData(new RegistryReportsRegistrationStatus(this, nodeId).getBytes());
			} catch (IOException e) {
				System.out.println("could not send registration report");
			}
		}else{
			try {
				connection.sendData(new RegistryReportsRegistrationStatus(this).getBytes());
			} catch (IOException e) {
				System.out.println("could not send registration report");
			}
		}
		//System.out.println(globalTable.contains(event.getIpAddress(), event.getPort()));
	}
	//deregister the node from sent massage
	public void deregisterNode(OverlayNodeSendsDeregistration event, boolean flag){
		TCPConnection connection = this.getConnection(event.getIpAddress(), event.getPort());
		if(flag){
			
			try {
				connection.sendData(new RegistryReportsDeregistrationStatus(this, event.getPort()).getBytes());
				globalTable.remove(event.getNodeId());
			} catch (IOException e) {
				System.out.println("could not send deregistration report");
			}
		}else{
			try {
				connection.sendData(new RegistryReportsDeregistrationStatus(this).getBytes());
			} catch (IOException e) {
				System.out.println("could not send registration report");
			}
		}
		//System.out.println(globalTable.contains(event.getIpAddress(), event.getPort()));
	}
	//send the node manifest for each node to that node
	public void sendNodeManifest(){
		Iterator<Integer> ptr = getKeys().iterator();
		report = new StatisticsCollectorAndDisplay(tableSize());
		//for each connection send node manifest
		for(int i = 0; i < tableSize() && ptr.hasNext(); i++){
			int id = ptr.next().intValue();
			try {
				RegistrySendsNodeManifest event = new RegistrySendsNodeManifest(this, id);
				getConnection(id).sendData(event.getBytes());
				manifests.add(event);
				
			} catch (IOException e) {
				System.out.println("could not send manifest");
			}
		}
	}
	//initiate
	public void sendTaskInitiate(long packetNum) {
		Iterator<TCPConnection> ptr = this.getConnectionValues();
		report.changeSize(tableSize());
		while(ptr.hasNext()){
			try {
				ptr.next().sendData(new RegistryRequestsTaskInitiate(packetNum).getBytes());
			} catch (IOException e) {
				System.out.println("could not send task initiate");
			}
		}
	}
	//ask for node to report traffic summary
	public void sendTrafficSummaryRequest(OverlayNodeReportsTaskFinished event) {
		TCPConnection connection = this.getConnection(event.getNodeId());
		try {
			connection.sendData(new RegistryRequestsTrafficSummary().getBytes());
		} catch (IOException e) {
			System.out.println("could not send traffic request");
		}
	}
	//relay traffic report
	public void relayTrafficReport(OverlayNodeReportsTrafficSummary event) {
		synchronized(report){
			report.add(event);
		}
	}
	public void listMessagingNodes() {
		Iterator<Integer> keys = this.getKeys().iterator();
		for(RoutingEntry r: this.getValues()){
			if(keys.hasNext()) System.out.println(
					"Node id: " + keys.next().intValue() + " Ip address: " + r.getIpAddressString() + " Port number: " + r.getPortNum() + "\n\n");
		}
	}
	public void listManifests(){
		for(RegistrySendsNodeManifest event : manifests){
			System.out.println("Manifest for node " + event.getStartKey() + "\n"
					+ "Routing Table: ");
			for(int i = 0; i < event.getTableSize(); i++){
				System.out.println("Node " + event.getHopId(i) + " IP: " + event.getIpAddress(i) + " Port: " + event.getPortNum(i) + "\n");
			}
		}
	}
	//return port number of socket
	public int getPort() {
		return this.server.getSocket().getLocalPort();
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
	//generate a random number for nodes to register with
	public static int randInt(int min, int max) {
	    // nextInt is normally exclusive of the top value,
	    // so add 1 to make it inclusive
	    int randomNum = rand.nextInt((max - min) + 1) + min;

	    return randomNum;
	}
	//return registry Id (should be zero)
	public int getNodeId() {
		return idNumber;
	}
	//add to connection cache
	public synchronized void addConnection(TCPConnection connection) {
		this.localCache.addConnection(connection);
		
	}
	//remove from connection cache
	public synchronized void removeConnection(TCPConnection connection) {
		this.localCache.removeConnection(connection);
	}
	//get a connection based off index
	public synchronized TCPConnection getConnection(int id) {
		return this.localCache.getConnection(id);
	}
	public synchronized TCPConnection getConnection(String address, int port){
		return this.localCache.getConnection(address, port);
	}
	//return size of routing table
	public synchronized int tableSize(){
		return globalTable.size();
	}
	//returns values of routing table global 
	public synchronized Collection<RoutingEntry> getValues() {
		return globalTable.getValues();
	}
	//returns values of connection table 
	public synchronized Iterator<TCPConnection> getConnectionValues() {
		return localCache.getConnections();
	}
	//returns Enumeration of Keys
	public synchronized Set<Integer> getKeys() {
		return globalTable.getKeys();
	}
	//main
	public static void main(String args[]) {
		Registry hub;
		//check if args are present
		if(args.length > 0 ){
			int portname = Integer.parseInt(args[0]);
			hub = new Registry(portname);
		}
		else{
			hub = new Registry(0);
		}
		System.out.println("listening on port: " + hub.server.getSocket().getLocalPort());	
		InteractiveCommandParser commander = new InteractiveCommandParser(hub);
		commander.run();
	}

}
