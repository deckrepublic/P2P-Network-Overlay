package cs455.overlay.wireformat;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import cs455.overlay.node.*;

public class RegistryReportsRegistrationStatus implements Event{
	private int messageType; //message type 1 - 12
	private int nodeIdSuccessMessage; //port number of node
	private int iLength; //length of ip address
	private byte[] infoString; //get the ip address
	private Protocol t = Protocol.REGISTRY_REPORTS_REGISTRATION_STATUS; //able to identify type of event
	
	public RegistryReportsRegistrationStatus(byte[] data){
		ByteArrayInputStream baInputStream = new ByteArrayInputStream(data); //byte array input stream
		DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));//get a data input stream
		//try to populate fields
		try {
			messageType = din.readInt();
			nodeIdSuccessMessage = din.readInt();
			iLength = din.readInt();

			infoString = new byte[iLength];
			din.readFully(infoString);

			baInputStream.close();
			din.close();
			
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}

	}
	//generate message object from node if unsuccessful
	public RegistryReportsRegistrationStatus(Registry node){
		messageType = 3;
		nodeIdSuccessMessage = -1;
		//byte[] b = string.getBytes(Charset.forName("UTF-8"));
		infoString = new String("Registration request unsuccessful. " +
				"The number of messaging nodes currently constituting the overlay is (" + node.tableSize() +
				")").getBytes(Charset.forName("UTF-8"));
		iLength = infoString.length;
	}
	//generate message object from node if successful
	public RegistryReportsRegistrationStatus(Registry node, int id){
		messageType = 3;
		nodeIdSuccessMessage = id;
		//byte[] b = string.getBytes(Charset.forName("UTF-8"));
		infoString = new String("Registration request successful. " +
				"The number of messaging nodes currently constituting the overlay is (" + node.tableSize() +
				")").getBytes(Charset.forName("UTF-8"));
		iLength = infoString.length;
	}
	//generate a byte[] to send to registry
	public byte[] getBytes(){
		byte[] marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream (new BufferedOutputStream(baOutputStream));
		
		try {
			dout.writeInt(messageType);
			dout.writeInt(nodeIdSuccessMessage);
			dout.writeInt(iLength);
			dout.write(infoString);
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
	//get info string in actual string format
	public String getInfoString() {
		try {
			return new String(infoString, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			System.out.println(e.getMessage());
		}
		return null;
	}
	//get the int message type
	public int getMessageType() {
		return messageType;
	}
	//get node id
	public int getNodeIdSuccessMessage(){
		return nodeIdSuccessMessage;
	}
	public Protocol getType() {
		return t;
	}
}
