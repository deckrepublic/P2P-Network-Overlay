package cs455.overlay.wireformat;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import cs455.overlay.node.MessagingNode;

public class OverlayNodeReportsTrafficSummary implements Event {

	private int messageType; //message type 1 - 12
	private int nodeId; // assigned node id 
	private long packetsSent; // number of packets that were only started by the node sent
	private long packetsRelayed; //number of packets that were only relayed by the node
	private long sumOfPacketSent; //sum of packet payloads started by this node
	private long packetsRecieved; //packets with this node as final destination
	private long sumOfPacketReceived; //only with packets that have this node as final destination
	
	private Protocol t = Protocol.OVERLAY_NODE_REPORTS_TRAFFIC_SUMMARY; //type of event
	
	public OverlayNodeReportsTrafficSummary(byte[] data) {
		ByteArrayInputStream baInputStream = new ByteArrayInputStream(data); //byte array input stream
		DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));//get a data input stream
		//try to populate fields
		try {
			messageType = din.readInt();
			nodeId = din.readInt();
			packetsSent = din.readLong();
			packetsRelayed = din.readLong();
			sumOfPacketSent = din.readLong();
			packetsRecieved = din.readLong();
			sumOfPacketReceived = din.readLong();

			baInputStream.close();
			din.close();
			
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}

	}
	//generate message object from node
	public OverlayNodeReportsTrafficSummary(MessagingNode node) {
		messageType = 12;
		nodeId = node.getNodeId();
		packetsSent = node.getPacketsSent();
		packetsRelayed = node.getPacketsRelayed();
		sumOfPacketSent = node.getSumOfPacketSent();
		packetsRecieved = node.getPacketsRecieved();
		sumOfPacketReceived = node.getSumOfPacketReceived();
	}
	//return protocol type
	public Protocol getType() {
		return t;
	}
	//getter
	public int getNodeId(){
		return nodeId;
	}
	//getter
	public long getPacketsSent(){
		return packetsSent;
	}
	//getter
	public long getPacketsRelayed(){
		return packetsRelayed;
	}
	//getter
	public long getSumOfPacketSent(){
		return sumOfPacketSent;
	}
	//getter
	public long getPacketsRecieved(){
		return packetsRecieved;
	}
	//getter
	public long getSumOfPacketReceived(){
		return sumOfPacketReceived;
	}
	//return byte[] of event
	public byte[] getBytes() {
		byte[] marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream (new BufferedOutputStream(baOutputStream));
		
		try {
			dout.writeInt(messageType);
			dout.writeInt(nodeId);
			dout.writeLong(packetsSent);
			dout.writeLong(packetsRelayed);
			dout.writeLong(sumOfPacketSent);
			dout.writeLong(packetsRecieved);
			dout.writeLong(sumOfPacketReceived);
			dout.flush();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		
		marshalledBytes = baOutputStream.toByteArray();
		try {
			baOutputStream.close();
			dout.close();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		return marshalledBytes;
	}

}
