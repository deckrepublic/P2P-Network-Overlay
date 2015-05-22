package cs455.overlay.wireformat;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import cs455.overlay.node.*;

public class OverlayNodeSendsData implements Event{

	private int messageType; //message type 1 - 12
	private int destinationId; //destination Id
	private int sourceId; //source Id
	
	private int payload; //payload of the packet
	private int numOfHops; //number of hops the packet has currently done
	private int[] nodeIdTrace; //id's of nodes that have been visited by the node
	private Protocol t = Protocol.OVERLAY_NODE_SENDS_DATA; //type of event
	
	//construct event from byte[]
	public OverlayNodeSendsData(byte[] data) {
		ByteArrayInputStream baInputStream = new ByteArrayInputStream(data); //byte array input stream
		DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));//get a data input stream
		//try to populate fields
		try {
			messageType = din.readInt();
			destinationId = din.readInt();
			sourceId = din.readInt();
			payload = din.readInt();
			numOfHops = din.readInt();
			
			nodeIdTrace = new int[numOfHops];
			for(int i = 0; i < numOfHops; i++){
				nodeIdTrace[i] = din.readInt();
			}
			baInputStream.close();
			din.close();
			
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}

	}
	//generate message object from node
	public OverlayNodeSendsData(MessagingNode node, int payload, int destId, int numOfHops, int[] nodeIdTrace){
		messageType = 9;
		destinationId = destId;
		sourceId = node.getNodeId();
		this.payload = payload;
		this.numOfHops = numOfHops + 1; //increment the number of hops by one
		this.nodeIdTrace = new int[numOfHops + 1]; //set the trace so it can accept the sourceId as the latest hop
		for(int i = 0; i < numOfHops; i++){
			this.nodeIdTrace[i] = nodeIdTrace[i];
		}
		this.nodeIdTrace[numOfHops] = sourceId; //add the latest hop
	}
	//return dest id
	public int getDest(){
		return destinationId;
	}
	//returns message type
	public int getMessageType() {
		return messageType;
	}
	//return source id
	public int getSource(){
		return sourceId;
	}
	//return payload
	public int getPayload(){
		return payload;
	}
	//return num of hops
	public int getHop(){
		return numOfHops;
	}
	public int[] getNodeTrace(){
		return nodeIdTrace;
	}
	//return protocol of event
	public Protocol getType() {
		return t;
	}
	//return byte[] of event
	public byte[] getBytes() {
		byte[] marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream (new BufferedOutputStream(baOutputStream));
		
		try {
			dout.writeInt(messageType);
			dout.writeInt(destinationId);
			dout.writeInt(sourceId);
			dout.writeInt(payload);
			dout.writeInt(numOfHops);
			for(int i = 0; i < numOfHops; i++){
				dout.writeInt(nodeIdTrace[i]);
			}
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
