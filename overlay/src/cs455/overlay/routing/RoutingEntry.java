package cs455.overlay.routing;

//Entry for routing table
public class RoutingEntry {
	private byte[] ipAddress;
	private int portNum;
	private int serverPort;
	//constructor
	public RoutingEntry(byte[] ipAddress, int portNum, int serverPort){;
		this.ipAddress = ipAddress;
		this.portNum = portNum;
		this.serverPort = serverPort;
	}
	//getter
	public int getPortNum(){
		return portNum;
	}
	public int getServerPort(){
		return serverPort;
	}
	//getter
	public byte[] getIpAddress(){
		return ipAddress;
	}
	public String getIpAddressString(){
        int i = 4;
        String ipAddress = "";
        for (byte raw : this.ipAddress)
        {
            ipAddress += (raw & 0xFF);
            if (--i > 0)
            {
                ipAddress += ".";
            }
        }
 
        return ipAddress;
	}
}
