package cs455.overlay.wireformat;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


public class RegistryRequestsTaskInitiate implements Event{

	private int messageType; //message type 1 - 12
	private long numOfPacketsToSend; //number of packets that the node receiving this packet has to send
	private Protocol t = Protocol.REGISTRY_REQUESTS_TASK_INITIATE; //able to identify type of event
	
	public RegistryRequestsTaskInitiate(byte[] data){
		ByteArrayInputStream baInputStream = new ByteArrayInputStream(data); //byte array input stream
		DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));//get a data input stream
		//try to populate fields
		try {
			messageType = din.readInt();
			numOfPacketsToSend = din.readLong();

			baInputStream.close();
			din.close();
			
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}

	}
	
	//generate message object from node
	public RegistryRequestsTaskInitiate(long num){
		messageType = 8;
		numOfPacketsToSend = num;
	}
	
	//returns protocol of Event
	public Protocol getType() {
		return t;
	}

	//returns message type
	public int getMessageType() {
		return messageType;
	}
	
	//returns byte[] of the event
	public byte[] getBytes() {
		byte[] marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream (new BufferedOutputStream(baOutputStream));
		
		try {
			dout.writeInt(messageType);
			dout.writeLong(numOfPacketsToSend);
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
	
	//returns protocol of Event
	public long getPacketNum() {
		return numOfPacketsToSend;
	}
	
}
