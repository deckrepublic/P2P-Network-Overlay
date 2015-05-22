package cs455.overlay.transport;


import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;
import cs455.overlay.node.*;


public class TCPServerThread implements Runnable{
	private ServerSocket serverSocket;
	private TCPConnection theConnection;
	private Node node; //node to trigger event in receiver
	//create a thread to essentially be a server for a node or the registry
	public TCPServerThread(Node node, int port){
		try {
			//node server is responsible for
			this.node = node;
			//server socket responsible for listening 
			serverSocket = new ServerSocket(port);
			port = serverSocket.getLocalPort();
			Thread t = new Thread(this);
			t.start();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}
	//returns socket of server
	public ServerSocket getSocket(){
		return serverSocket;
	}
	//sends data through server connection
	public void sendData(byte[] data){
		try {
			theConnection.sendData(data);
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
//thread function
	//remember to implement an event listener when the server thread
	//receives a message trigger the event and respond
	public void run() {
		while(true){
			try {
				Socket socket = serverSocket.accept();
				theConnection = new TCPConnection(socket,node);
				//add the connection to the list of connections
				node.addConnection(theConnection);
				//implement: The server should receive a message first and then respond to
				//the message, all of this is done with the specific byte[] dictated by the 
				//wire format
				/*
				 * byte[] something = receive from the sender;
				 * byte[] responseToSomething = formulateResponse(something);
				 * send(responseToSomething);
				 */
				//byte[] data = {'h', 'e', 'l', 'l', 'o'};
				//theConnection.sendData(data);
//				theConnection.close();
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
			
	}
}
