package cs455.overlay.util;

import java.util.LinkedList;	

import cs455.overlay.wireformat.OverlayNodeReportsTrafficSummary;


public class StatisticsCollectorAndDisplay {
	
	private LinkedList<OverlayNodeReportsTrafficSummary> eventList = new LinkedList<OverlayNodeReportsTrafficSummary>();
	private int size;
	//constructor with total size of node list
	public StatisticsCollectorAndDisplay(int i) {
		size = i;
	}
	public void changeSize(int i){
		size = i;
	}
	//add to the event list
	public void add(OverlayNodeReportsTrafficSummary event){
		eventList.add(event);

		if(eventList.size() == size){
			this.show();
			eventList.clear();
		}
	}
	//print event table
	public void show() {
		int sumOfPacketsSent = 0;
		int sumOfPacketsReceived = 0;
		int sumOfPacketsRelayed = 0;
		long sumOfSumOfValuesSent = 0;
		long sumOfSumOfValuesReceived = 0;
		System.out.format("%5s%20s%20s%20s%22s%22s"
		, "node #", "Packets Sent", "Packets Received", "Packets Relayed", "Sum Values Sent","Sum Values Received");
		System.out.println("\n");
		for(OverlayNodeReportsTrafficSummary event : eventList) {
			StringBuilder sb = new StringBuilder();
			sb.append("Node ");
			sb.append(event.getNodeId());
			System.out.format("%5s%20d%20d%20d%20d%20d"
					, sb.toString(), event.getPacketsSent(), event.getPacketsRecieved(), event.getPacketsRelayed(), event.getSumOfPacketSent(), event.getSumOfPacketReceived());
			sumOfPacketsSent += event.getPacketsSent();
			sumOfPacketsReceived += event.getPacketsRecieved();
			sumOfPacketsRelayed += event.getPacketsRelayed();
			sumOfSumOfValuesSent += event.getSumOfPacketSent();
			sumOfSumOfValuesReceived += event.getSumOfPacketReceived();
			System.out.println("\n");
		}
		System.out.format("%5s%20d%20d%20d%20d%20d"
				, "Sum", sumOfPacketsSent, sumOfPacketsReceived, sumOfPacketsRelayed, sumOfSumOfValuesSent, sumOfSumOfValuesReceived);
		
	}

}
