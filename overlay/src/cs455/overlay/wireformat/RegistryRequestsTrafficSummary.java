package cs455.overlay.wireformat;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


public class RegistryRequestsTrafficSummary implements Event {

	private int messageType; //message type 1 - 12
	private Protocol t = Protocol.REGISTRY_REQUESTS_TRAFFIC_SUMMARY; //type of event
	
	public RegistryRequestsTrafficSummary(byte[] data) {
		ByteArrayInputStream baInputStream = new ByteArrayInputStream(data); //byte array input stream
		DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));//get a data input stream
		//try to populate fields
		try {
			messageType = din.readInt();
			baInputStream.close();
			din.close();
			
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}

	}
	//generate message object from node
	public RegistryRequestsTrafficSummary() {
		messageType = 11;
	}
	//return protocol of event
	public Protocol getType() {
		return t;
	}
	//return message type
	public int getMessageType() {
		return messageType;
	}
	//return bytes[] of the event
	public byte[] getBytes() {
		byte[] marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream (new BufferedOutputStream(baOutputStream));
		
		try {
			dout.writeInt(messageType);
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
