package cs455.overlay.util;

import java.util.Scanner;

import cs455.overlay.node.*;

public class InteractiveCommandParser {
	
	private Registry registry;
	private MessagingNode messagingNode;
	private enum TYPE {REGISTRY, MESSAGINGNODE}
	private TYPE t;
	
	public InteractiveCommandParser(Registry registry){
		this.registry = registry;
		t = TYPE.REGISTRY;
	}
	
	public InteractiveCommandParser(MessagingNode messagingNode){
		this.messagingNode = messagingNode;
		t = TYPE.MESSAGINGNODE;
	}
	
	public void run() {
		switch(t){
		case REGISTRY: {
			Scanner in = new Scanner(System.in);
			while(true){
			      String s;
				  System.out.println("Enter a command: <list-messaging-nodes>, <list-routing-tables>, <setup-overlay>, <start>, and <exit> are the only valid commands");
				  s = in.next();
				  if(s.compareToIgnoreCase("exit") == 0){
					  in.close();
					  break;
				  }
				  else if(s.compareTo("list-messaging-nodes") == 0){
					  registry.listMessagingNodes();
				  } 
				  else if(s.compareTo("list-routing-tables") == 0){
					  registry.listManifests();
				  }
				  else if(s.compareTo("setup-overlay") == 0){
					  registry.sendNodeManifest();
				  }
				  else if(s.compareTo("start") == 0){
					  System.out.println("Enter number of packets for each node to send");
					  long packets = in.nextLong();
					  registry.sendTaskInitiate(packets);
				  }
			}
			
		}
		case MESSAGINGNODE: {
			Scanner in = new Scanner(System.in);
			while(true){
				  String s;
				  System.out.println("Enter a command: <print-counters-and-diagnostics>, and <exit-overlay> are the only valid commands");
				  s = in.next();
				  if(s.compareToIgnoreCase("exit") == 0){
					  in.close();
					  break;
				  }
				  else if(s.compareTo("print-counters-and-diagnostics") == 0){
					  messagingNode.printCountersAndDiagnostics();
				  } 
				  else if(s.compareTo("exit-overlay") == 0){
					  messagingNode.sendDeregistration();
				  }
			}
		}
	  }
	}

}
