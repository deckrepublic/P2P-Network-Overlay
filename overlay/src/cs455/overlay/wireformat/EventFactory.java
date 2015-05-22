package cs455.overlay.wireformat;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;


//class responsible for creating an Event based off of the data 
public class EventFactory {
	
	public EventFactory (){	
		
	}
	//creates the event
	public Event createEvent(byte[] data){
		switch(getMessageType(data)){
			case 2: return new OverlayNodeSendsRegistration(data);
			case 3: return new RegistryReportsRegistrationStatus(data);
			case 4: return new OverlayNodeSendsDeregistration(data);
			case 5: return new RegistryReportsDeregistrationStatus(data);
			case 6: return new RegistrySendsNodeManifest(data);
			case 7: return new NodeReportsOverlaySetupStatus(data);
			case 8: return new RegistryRequestsTaskInitiate(data);
			case 9: return new OverlayNodeSendsData(data);
			case 10: return new OverlayNodeReportsTaskFinished(data);
			case 11: return new RegistryRequestsTrafficSummary(data);
			case 12: return new OverlayNodeReportsTrafficSummary(data);
			default: return null;
		}
	}
	//for when event factory only gets pure byte[]
	public int getMessageType(byte[] data){
		ByteArrayInputStream baInputStream = new ByteArrayInputStream(data); //byte array input stream
		DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));//get a data input stream
		int messageType = -1;
		//try to populate fields
		try {
			messageType = din.readInt();
			baInputStream.close();
			din.close();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		return messageType;
	}
}
