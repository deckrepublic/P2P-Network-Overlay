package cs455.overlay.node;
import cs455.overlay.transport.TCPConnection;

public interface Node {
	public void onEvent(byte[] data);
	public void onEvent(String ipAddress, byte[] data);
	public int getPort();
	public byte[] getIpAddress();
	public String getIpAddressString();
	public int getNodeId();
	public void addConnection(TCPConnection connection);
	public void removeConnection(TCPConnection connection);
}
