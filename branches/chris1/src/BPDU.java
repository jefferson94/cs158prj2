/**
 * A representation of a BPDU. See the 802.1D and 802.1w specs for the format 
 * of a BPDU. An instance of this class doesn't need to know anything. It's 
 * just a way to store information. Switches pass instances of this class back 
 * and forth to communicate with each other. Each instance of this class is 
 * (hopefully) cleaned up with the Java GarbageCollector.
 * 
 * @author John Le Mieux
 * @author Christopher Trinh
 * @version 0.1 April 5, 2010
 */
public class BPDU 
{
	/**
	 * Constructor for a STP Configuration BPDU. These constructors have 
	 * parameters for all of the 802.1 fields except the Protocol Identifier 
	 * which is always 0.
	 * 
	 * @param version 0 for STP
	 * @param type 0 for Configuration
	 * @param topologyChange 
	 * @param topologyChangeAck
	 * @param rootID hexidecimal representation of Root Bridge ID
	 * @param cost 19 for FastEthernet
	 * @param senderID hexidecimal representaion of Sender's Bridge ID
	 * @param portID interface number that this BPDU is being sent out
	 * @param messageAge timestamp that this BPDU was generated
	 * @param maxAge 20 for the default max age
	 * @param helloTime 2 for the default hello time
	 * @param forwardDelay 15 for the default forward delay
	 */
	public BPDU(int version, int type, boolean topologyChange, 
			boolean topologyChangeAck, String rootID, int cost, 
			String senderID, int portID, int messageAge, int maxAge, 
			int helloTime, int forwardDelay)
	{
		this.version = version;
		this.type = type;
		this.topologyChange = topologyChange;
		this.topologyChangeAck = topologyChangeAck;
		this.rootID = rootID;
		this.cost = cost;
		this.senderID = senderID;
		this.portID = portID;
		this.messageAge = messageAge;
		this.maxAge = maxAge;
		this.helloTime = helloTime;
		this.forwardDelay = forwardDelay;
	}
	
	/**
	 * Constructor for STP Topology Change Notification BPDU
	 * 
	 * @param version 0 for STP
	 * @param type 128 for TCN
	 */
	public BPDU(int version, int type)
	{
		this.version = version;
		this.type = type;
	}
	/**
	 * Constructor for RSTP BPDU
	 * 
	 * @param version 2 for RSTP
	 * @param type 2 for RSTP BPDU
	 * @param topologyChange
	 * @param proposal
	 * @param portRole
	 * @param learning
	 * @param forwarding
	 * @param agreement
	 * @param topologyChangeAck
	 * @param rootID hexidecimal representation of the Root Bridge ID
	 * @param cost 19 for FastEthernet
	 * @param senderID hexidecimal representation of the sender's Bridge ID
	 * @param portID interface number that this BPDU is being sent out
	 * @param messageAge timestamp that this BPDU was generated
	 * @param maxAge 20 for the default Max Age
	 * @param helloTime 2 for the default Hello Timer
	 * @param forwardDelay 15 for the default Forward Delay
	 */
	public BPDU(int version, int type, boolean topologyChange, 
			boolean proposal, int portRole, boolean learning, 
			boolean forwarding, boolean agreement, boolean topologyChangeAck, 
			String rootID, int cost, String senderID, int portID, 
			int messageAge, int maxAge, int helloTime, int forwardDelay)
	{
		this.version = version;
		this.type = type;
		this.topologyChange = topologyChange;
		this.proposal = proposal;
		this.portRole = portRole;
		this.learning = learning;
		this.forwarding = forwarding;
		this.agreement = agreement;
		this.topologyChangeAck = topologyChangeAck;
		this.rootID = rootID;
		this.cost = cost;
		this.senderID = senderID;
		this.portID = portID;
		this.messageAge = messageAge;
		this.maxAge = maxAge;
		this.helloTime = helloTime;
		this.forwardDelay = forwardDelay;
	}
	
	public int getVersion() {return version;}
	
	public int getType() {return type;}
	
	public boolean getTopologyChange() {return topologyChange;}
	
	public boolean getProposal() {return proposal;}
	
	public int getPortRole() {return portRole;}
	
	public boolean getLearning() {return learning;}
	
	public boolean getForwarding() {return forwarding;}
	
	public boolean getAgreement() {return agreement;}
	
	public boolean getTopologyChangeAck() {return topologyChangeAck;}
	
	public String getRootID() {return rootID;}
	
	public int getCost() {return cost;}
	
	public String getSenderID() {return senderID;}
	
	public int getPortID() {return portID;}
	
	public int getMessageAge() {return messageAge;}
	
	public int getMaxAge() {return maxAge;}
	
	public int getHelloTime() {return helloTime;}
	
	public int getForwardDelay() {return forwardDelay;}
	
	public String toString()
	{
	   return "Root id: " + getRootID() + " Sender id: " + getSenderID() + " Port id: " + getPortID();
	}
	
	private int version;
	private int type;
	private boolean topologyChange;
	private boolean proposal;
	private int portRole;
	private boolean learning;
	private boolean forwarding;
	private boolean agreement;
	private boolean topologyChangeAck;
	private String rootID;
	private int cost;
	private String senderID;
	private int portID;
	private int messageAge;
	private int maxAge;
	private int helloTime;
	private int forwardDelay;
}
