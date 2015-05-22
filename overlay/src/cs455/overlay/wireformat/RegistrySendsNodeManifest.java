package cs455.overlay.wireformat;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import cs455.overlay.node.*;
import cs455.overlay.routing.*;

public class RegistrySendsNodeManifest implements Event{
	private int messageType; //message type 1 - 12
	private int tableSize; //table size of local table for message node which is log base 2 of global routing table 
	private int [] hopId; //array of node ids that are 2^index hops away from node in overlay
	private int[] ipLength; //array of ip address lengths that are 2^index hops away from node in overlay
	private byte[][] ipAddresses; //array of ip addresses that are 2^index hops away from node in overlay
	private int [] portNumbers; //array of port numbers for each ip address
	private int numberOfNodes; //number of nodes in the system
	private int[] nodeIdList; // list of nodes in the system
	private int startKey; //node that is started on
	
	private Protocol t = Protocol.REGISTRY_SENDS_NODE_MANIFEST; //able to identify type of event
	public RegistrySendsNodeManifest(byte[] data){
		ByteArrayInputStream baInputStream = new ByteArrayInputStream(data); //byte array input stream
		DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));//get a data input stream
		//try to populate fields
		try {
			messageType = din.readInt();
			tableSize = din.readInt();
			//set size of arrays

			hopId = new int[tableSize];
			ipLength = new int[tableSize];
			ipAddresses = new byte[tableSize][];
			portNumbers = new int[tableSize];
			//go through data and try to set all values
			for(int i = 0; i < tableSize; i++){
				hopId[i] = din.readInt();
				ipLength[i] = din.readInt();
				ipAddresses[i] = new byte[ipLength[i]];
				din.readFully(ipAddresses[i]);
				portNumbers[i] = din.readInt();
			}
			numberOfNodes = din.readInt();
			nodeIdList = new int[numberOfNodes];
			for(int i = 0; i < numberOfNodes; i++){
				nodeIdList[i] = din.readInt();
			}

			baInputStream.close();
			din.close();
			
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}

	}
	//generate message object from node if unsuccessful
	public RegistrySendsNodeManifest(Registry node, int startKey){
		messageType = 6;
		tableSize = log2(node.tableSize());
		this.startKey = startKey;
		hopId = new int[tableSize];
		ipLength = new int[tableSize];
		ipAddresses = new byte[tableSize][];
		portNumbers = new int[tableSize];
		//byte[] b = string.getBytes(Charset.forName("UTF-8"));
			//get iterators of values and keys
			Collection<RoutingEntry> values = node.getValues();
			Iterator<Integer> keys = node.getKeys().iterator();
			Iterator<RoutingEntry> ptr = values.iterator();
			Integer ptrkey = null;
			RoutingEntry ptrvalue = null;
			while(ptr.hasNext() && keys.hasNext()){
				//get elements to compare
				ptrkey = keys.next(); 
				ptrvalue = ptr.next();
				//go through collection until we get to the correct starting position
				if(ptrkey.equals(new Integer(startKey))){
				break;
				}
			}
			//have initializer variable
			int initcount = 1;
			int nextSet = 0;
			//now start going through
			for(int i = 0; i <= node.tableSize() && nextSet < tableSize; i++){
				//now check if we hit the end so we start from the beginning to have circular property
				if(!ptr.hasNext() && !keys.hasNext()){
					keys = node.getKeys().iterator();
					ptr = values.iterator();
				}
				ptrkey = keys.next(); 
				ptrvalue = ptr.next();
				int index = log2(initcount);
				if(nextSet == index){
						hopId[nextSet] = ptrkey.intValue();
						ipLength[nextSet] = ptrvalue.getIpAddress().length;
						ipAddresses[nextSet] = ptrvalue.getIpAddress();
						portNumbers[nextSet] = ptrvalue.getServerPort();
						nextSet ++;
				}

				initcount ++;

			}
			numberOfNodes = node.tableSize();
			nodeIdList = new int[numberOfNodes];
			int count = 0;
			for(Integer fuckyou : node.getKeys()) {
				nodeIdList[count] = fuckyou.intValue();
				count ++;
			}

	}
	//handy method found online for finding log base 2 in integer arithmetic
    public static int log2(int x) {
        int y,v;
        // No log of 0 or negative
        if (x <= 0) {
            throw new IllegalArgumentException(""+x+" <= 0");
        }
        // Calculate log2 (it's actually floor log2)
        v = x;
        y = -1;
        while (v>0) {
            v >>=1;
            y++;
        }
        return y;
    }
    //return type
	public Protocol getType() {
		return t;
	}
	//serialize the bytes
	public byte[] getBytes() {
		byte[] marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream (new BufferedOutputStream(baOutputStream));
		//try to write all values from the arrays
		try {
			dout.writeInt(messageType);
			dout.writeInt(tableSize);
			for(int i = 0; i < tableSize; i++){
				dout.writeInt(hopId[i]);
				dout.writeInt(ipLength[i]);
				dout.write(ipAddresses[i]);
				dout.writeInt(portNumbers[i]);
			}
			dout.writeInt(numberOfNodes);
			for (int i = 0; i < numberOfNodes; i++){
				dout.writeInt(nodeIdList[i]);
			}
		//close every stream
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
	//return String representation of ip address at 2^index hops away
	public String getIpAddress(int index){
        int i = 4;
        String ipAddress = "";
        for (byte raw : ipAddresses[index])
        {
            ipAddress += (raw & 0xFF);
            if (--i > 0)
            {
                ipAddress += ".";
            }
        }
 
        return ipAddress;
	}
	public byte[] getIp(int index){
		return ipAddresses[index];
	}
	//return table size 
	public int getTableSize(){
		return tableSize;
	}
	//return port number at an index
	public int getPortNum(int index){
		return portNumbers[index];
	}
	//return hopID at an index
	public int getHopId(int index){
		return hopId[index];
	}
	//return node number 
	public int getNodeNumber(){
		return numberOfNodes;
	}
	//return startkey
	public int getStartKey(){
		return startKey;
	}
	//return node number 
	public int [] getNodes(){
		return nodeIdList;
	}
}
