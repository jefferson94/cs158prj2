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
}
