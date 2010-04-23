/**
 * Class to hold ports. 
 * 
 * Listing of all the port states: 
 * Blocking - A port that would cause a switching loop, no user data is sent or received but 
 *    it may go into forwarding mode if the other links in use were to fail and the spanning 
 *    tree algorithm determines the port may transition to the forwarding state. BPDU data is 
 *    still received in blocking state.
 * Listening - The switch processes BPDUs and awaits possible new information that would 
 *    cause it to return to the blocking state.
 * Learning - While the port does not yet forward frames (packets) it does learn source 
 *    addresses from frames received and adds them to the filtering database 
 *    (switching database)
 * Forwarding - A port receiving and sending data, normal operation. STP still monitors 
 *    incoming BPDUs that would indicate it should return to the blocking state to prevent
 *    a loop.
 * Disabled - Not strictly part of STP, a network administrator can manually disable a port
 * 
 * @author Christopher Trinh
 * @author John Le Mieux
 * @author Peter Le
 * @version 0.2 April 12, 2010
 */
public class Port 
{
   public final static int BLOCKING = 0;
   public final static int LISTENING = 1;
   public final static int LEARNING = 2;
   public final static int FORWARDING = 3;
   public final static int DISABLED = 4;
   
   public final static int ROOT = 0;
   public final static int DESIGNATED = 1;
   public final static int ALTERNATE = 2;
   public final static int BACKUP = 3;
   public final static int NONDESIGNATED = 4;
  
//   private Switch neighbor;
   private Port connected;
   private int portState;
   private int pathCost;
   private int role;
   private BPDU receivedFrame;
   
   private int ageTime;
   
   // RSTP variables
   private int infoIs;
   private boolean reselect;
   private boolean forwarding;
   private boolean learning;
   private int[] portTimes; // (messageAge, maxAge, forwardDelay, helloTime)
   private boolean sendRstp;
   private int tcWhile; // topology change timer
   private boolean newInfo; // should a BPDU be sent?
   private int[] rootTimes; // (messageAge, maxAge, forwardDelay, helloTime)
   private boolean agreed;
   private boolean proposing;
   private boolean rstpVersion;
   private boolean operPointToPointMAC = true; // these are all point-to-point links, yes?
   private boolean sync;
   private boolean reRoot;
   private boolean select;
   private boolean rcvdTc;
   private boolean rcvdTcAck;
   private boolean rcvdTcn;
   private boolean tcprop;
   private boolean rcvdSTP;
   private boolean rcvdRSTP;
   private int rcvdInfoWhile;
   
   // Port's Spanning Tree Information States
   public final static int MINE = 0;
   public final static int AGED = 1;
   public final static int RECEIVED = 2;
   
   // Port Information States
   public final static int SUPERIOR_DESIGNATED_INFO = 0;
   public final static int REPEATED_DESIGNATED_INFO = 1;
   public final static int INFERIOR_DESIGNATED_INFO = 2;
   public final static int INFERIOR_ROOT_ALTERNATE_INFO = 3;
   public final static int OTHER_INFO = 4;
   
   /**
    * Initial state of the port is set to BLOCKING (the value of 0).
    */
   public Port()
   {
      this(BLOCKING, null, 0);
   }
   
   /**
    * Set the initial state of the port.
    * @param initialState is the state to set the port to, use the constant values
    * defined already in this class (see the 'final' constant listed values above). 
    * @param otherSwitchPort is the switch port 'this' port is connected to. 
    * @param cost is the path cost for the link this port uses; for 
    * fast-ethernet this value would be 19.
    * 
    */
   public Port(int initialState, Port otherSwitchPort, int cost)
   {
      portState = initialState;
      if (otherSwitchPort != null)
    	  connectTo(otherSwitchPort);
      receivedFrame = null;
      pathCost = cost;
      role = NONDESIGNATED;
   }
   
   /**
    * Connect two Ports. They are bi-directional connections, since there is no 
    * such representation of a one way connection.
    * 
    * @param ingress the Port to be bi-directionally connected.
    */
   public void connectTo(Port ingress)
   {
	   this.connected = ingress;
	   if (ingress != null)
		   ingress.connected = this;
   }
   
   /**
    * 
    * @return the connected switchport
    */
   public Port getConnected()
   {
	   return connected;
   }
   
   /**
    * Get the received packet that was sent to this port.
    * 
    * @return packet data that was sent to this port.
    */
   public BPDU receivedBPDU()
   {
      return receivedFrame;
   }
   
   /**
    * Send a packet to the port(target) that this port is connect to.
    * @param sent packet to send to the connect port(target)
    */
   public void sendBPDU(BPDU sent)
   {
	   if (connected != null)
		   connected.receivedFrame = sent;
   }
   
   public int getPathCost()
   {
      return pathCost;
   }
   
   
   public void setPathCost(int cost)
   {
      pathCost = cost;
   }
   
   /**
    * Get the current state of the port.
    * @return the current state of the port.
    */
   public int getState()
   {
      return portState;
   }
   
   /**
    * Set the current state of the port.
    * @param stateValue the value to set the port to. 
    */
   public void setState(int stateValue)
   {
      portState = stateValue;
   }
   
   /**
    * Sets the role for this interface.
    * 
    * @param portRole ROOT, DESIGNATED, ALTERNATE, BACKUP, or NONDESIGNATED
    */
   public void setRole(int portRole)
   {
	   role = portRole;
   }
   
   /**
    * 
    * @return ROOT, DESIGNATED, ALTERNATE, BACKUP, or NONDESIGNATED
    */
   public int getRole()
   {
	   return role;
   }
   
   /**
    * Takes the current BPDU out of the queue and gives it to the calling Switch.
    * 
    * @return the current BPDU on this interface
    */
   public BPDU getFrame()
   {
	   BPDU frame = receivedFrame;
	   receivedFrame = null;
	   return frame;
   }
   
   public void setAge(int age)
   {
	   ageTime = age;
   }
   
   public int getAge()
   {
	   return ageTime;
   }
   
   // RSTP methods
   
   /**
    * Accessor for infoIs, the state of the Port's STP information.
    * 
    * @return infoIs
    */
   public int getInfoIs()
   {
	   return infoIs;
   }
   
   /**
    * Sets the reselect parameter.
    * 
    * @param reselect the new value of reselect
    */
   public void setReselect(boolean reselect)
   {
	   this.reselect = reselect;
   }
   
   /**
    * Sets forwarding to false. Use this when forwarding has stopped and port 
    * is DISCARDING.
    */
   public void disableForwarding()
   {
	   forwarding = false;
   }
   
   /**
    * Sets forwarding to true. Use this when transitioning to FORWARDING.
    */
   public void enableForwarding()
   {
	   forwarding = true;
   }
   
   /**
    * Sets learning to false. Use this when learning has stopped and port
    * is DISCARDING.
    */
   public void disableLearning()
   {
	   learning = false;
   }
   
   /**
    * Sets learning to true. Use this when transitioning to LEARNING.
    */
   public void enableLearning()
   {
	   learning = true;
   }
   
   public void newTcWhile()
   {
	   if (tcWhile == 0 && sendRstp == true)
	   {
		   tcWhile = portTimes[3] + 1;
		   newInfo = true;
	   }
	   if (tcWhile == 0 && sendRstp == false)
		   tcWhile = rootTimes[1] + rootTimes[2];
   }
   
   public int[] getPortTimes()
   {
	   return portTimes;
   }
   
   /**
    * Clears either the proposed flag or agreed flag accordingly.
    */
   public void recordArgeement()
   {
	   if (rstpVersion && operPointToPointMAC && receivedFrame.getAgreement())
	   {
		   agreed = true;
		   proposing = false;
	   } else
		   agreed = false;
   }
   
   /**
    * Sets agreed flag if the received BPDU has its Learning flag set.
    */
   public void recordDispute()
   {
	   if (receivedFrame.getLearning())
	   {
		   agreed = true;
		   proposing = false;
	   }
   }
   
   /**
    * Sets proposing flag if it receives a proposal.
    */
   public void recordProposal()
   {
	   if (receivedFrame.getPortRole() == DESIGNATED && receivedFrame.getProposal())
	   {
		   proposing = true;
	   }
   }
   
   /**
    * Sets times according to received BPDU.
    */
   public void recordTimes()
   {
	   rootTimes[0] = receivedFrame.getMessageAge();
	   rootTimes[1] = receivedFrame.getMaxAge();
	   rootTimes[2] = receivedFrame.getForwardDelay();
	   rootTimes[3] = receivedFrame.getHelloTime();
	   if (rootTimes[3] < 1)
		   rootTimes[3] = 1;
   }
   
   public void setSync(boolean sync)
   {
	   this.sync = sync;
   }
   
   public void setReRoot(boolean reRoot)
   {
	   this.reRoot = reRoot;
   }
   
   public boolean getReselect()
   {
	   return reselect;
   }
   
   public void setSelect(boolean select)
   {
	   this.select = select;
   }
   
   public void setTcFlags()
   {
	   if (receivedFrame.getTopologyChange())
		   rcvdTc = true;
	   if (receivedFrame.getTopologyChangeAck())
		   rcvdTcAck = true;
	   if (receivedFrame.getType() == 128)
		   rcvdTcn = true;
   }
   
   public void setTcProp(boolean tcprop)
   {
	   this.tcprop = tcprop;
   }
   
   public boolean getReceivedTcn()
   {
	   return rcvdTcn;
   }
   
   public int getTcWhile()
   {
	   return tcWhile;
   }
   
   public boolean getAgreed()
   {
	   return agreed;
   }
   
   public boolean getProposing()
   {
	   return proposing;
   }
   
   public boolean getForwarding()
   {
	   return forwarding;
   }
   
   public boolean getLearning()
   {
	   return learning;
   }
   
   public boolean getRcvdTc()
   {
	   return rcvdTc;
   }
   
   /**
    * Determines if the received BPDU is STP or RSTP.
    */
   public void updtBPDUVersion()
   {
	   int version = receivedFrame.getVersion();
	   if (version == 0 || version == 1)
		   rcvdSTP = true;
	   else
		   rcvdRSTP = true;
   }
   
   /**
    * Updates the rcvdInfoWhile timer.
    */
   public void updtRcvdInfoWhile()
   {
	   if (receivedFrame.getMessageAge() + 1 <= rootTimes[1])
		   rcvdInfoWhile = rootTimes[3] * 3;
	   else
		   rcvdInfoWhile = 0;
   }
}
