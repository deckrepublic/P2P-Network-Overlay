package cs455.overlay.wireformat;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import cs455.overlay.node.MessagingNode;

public class OverlayNodeSendsDeregistration implements Event {
	private int messageType; //message type 1 - 12
	private int ipLength; //length of ip address
	private byte[] ipAddress; //get the ip address
	private int portNum; //port number of node
	private int nodeId; //nodeId of node
	private Protocol t = Protocol.OVERLAY_NODE_SENDS_DEREGISTRATION; //type of event
	
	public OverlayNodeSendsDeregistration(byte[] data){
		ByteArrayInputStream baInputStream = new ByteArrayInputStream(data); //byte array input stream
		DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));//get a data input stream
		//try to populate fields
		try {
			messageType = din.readInt();
			ipLength = din.readInt();
			
			ipAddress = new byte[ipLength];
			din.readFully(ipAddress);
			portNum = din.readInt();
			nodeId = din.readInt();
			baInputStream.close();
			din.close();
			
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}

	}
	//generate message object from node
	public OverlayNodeSendsDeregistration(MessagingNode node){
		messageType = 4;
		ipLength = node.getIpAddress().length;
		ipAddress = node.getIpAddress();
		portNum = node.getRegPort();
		nodeId = node.getNodeId();
	}
	//generate a byte[] to send to registry
	public byte[] getBytes(){
		byte[] marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream (new BufferedOutputStream(baOutputStream));
		
		try {
			dout.writeInt(messageType);
			dout.writeInt(ipLength);
			dout.write(ipAddress);
			dout.writeInt(portNum);
			dout.writeInt(nodeId);
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

	//convert from raw bytes to String
	public String getIpAddress(){
        int i = 4;
        String ipAddress = "";
        for (byte raw : this.ipAddress)
        {
            ipAddress += (raw & 0xFF);
            if (--i > 0)
            {
                ipAddress += ".";
            }
        }
 
        return ipAddress;
	}
	public int getMessageType() {
		return messageType;
	}
	public int getPort(){
		return portNum;
	}
	public int getNodeId(){
		return nodeId;
	}
	public Protocol getType() {
		return t;
	}
}
