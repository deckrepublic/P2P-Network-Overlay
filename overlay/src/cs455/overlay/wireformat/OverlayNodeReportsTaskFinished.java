package cs455.overlay.wireformat;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import cs455.overlay.node.*;

public class OverlayNodeReportsTaskFinished implements Event{

	private int messageType; //message type 1 - 12
	private int ipLength; //length of ip address
	private byte[] ipAddress; //get the ip address
	private int portNum; //port number of node
	private int nodeId; //id of node
	private Protocol t = Protocol.OVERLAY_NODE_REPORTS_TASK_FINISHED; //type of event
	
	public OverlayNodeReportsTaskFinished(byte[] data) {
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
	public OverlayNodeReportsTaskFinished(MessagingNode node) {
		messageType = 10;
		ipLength = node.getIpAddress().length;
		ipAddress = node.getIpAddress();
		portNum = node.getPort();
		nodeId = node.getNodeId();
	}
	//return message type
	public int getMessageType() {
		return messageType;
	}
	//return port number
	public int getPortNumber() {
		return portNum;
	}
	//return node id
	public int getNodeId() {
		return nodeId;
	}
	//return protocol of type
	public Protocol getType() {
		return t;
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
	//return bytes[] of the event
	public byte[] getBytes() {
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

}
