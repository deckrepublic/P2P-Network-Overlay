package cs455.overlay.transport;


import java.util.Iterator;
import java.util.LinkedList;

public class TCPConnectionsCache {
	private LinkedList<TCPConnection> cache; //cache that holds all current connections 
	public TCPConnectionsCache(){
		cache = new LinkedList<TCPConnection>();
	}
	//return connection based of id number
	public synchronized TCPConnection getConnection(int id){
		for(TCPConnection connection : cache){
			if (connection.getId() == id) return connection;
		}
		return null;
	}
	public synchronized Iterator<TCPConnection> getConnections(){
		return cache.iterator();
	}
	//getter

	public synchronized TCPConnection getConnection(String address, int portNum){
		for(TCPConnection connection : cache){
			String localAddress = connection.getSocket().getInetAddress().getHostAddress();
			int localPort = connection.getSocket().getPort();
			if (localAddress.compareTo(address) == 0 && localPort == portNum ) return connection;
		}
		return null;
	}
	//adder
	public synchronized void addConnection(TCPConnection connection){
		cache.add(connection);
	}
	public synchronized int size(){
		return cache.size();
	}
	//remover
	public synchronized void removeConnection(TCPConnection connection){
		cache.remove(connection);
	}
}
