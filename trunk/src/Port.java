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
 * TODO 
 * Ports no longer know their 'neighboring' switch, instead they only know the other switch's port 
 * which is connected/linked to. Received packets are now attributed to ports. 
 *
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
   private BPDU recievedPacket;
   
   /**
    * Initial state of the port is set to BLOCKING (the value of 0).
    */
   public Port()
   {
      this(BLOCKING, null, 1);
   }
   
   /**
    * Set the initial state of the port.
    * @param initialState is the state to set the port to, use the constant values
    * defined already in this class (see the 'final' constant listed values above). 
    * @param otherSwitchPort is the switch port 'this' port is connected to. 
    * @param cost is the path cost for the link this port uses; for fast-ethernet this value would be 19.
    * 
    * TODO
    * Previously we had 'Switch connectedNeighbor' that made no sense. Ports are located on 
    * switches and they connect to other ports on other switches (not to the switch themselves).
    */
   public Port(int initialState, Port otherSwitchPort, int cost)
   {
      portState = initialState;
      connectTo(otherSwitchPort);
      recievedPacket = null;
      pathCost = cost;
   }
   
   /**
    * Connect two Ports. They are bi-directional connections, since there is no such representation 
    * of a one way connection.
    * 
    * @param ingress the Port to be bi-directionally connected.
    */
   public void connectTo(Port ingress)
   {
	   this.connected = ingress;
	   ingress.connected = this.connected;
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
    * @return packet data that was setn to this port.
    */
   public BPDU receivedBPDU()
   {
      return recievedPacket;
   }
   
   /**
    * Send a packet to the port(target) that this port is connect to.
    * @param sent packet to send to the connect port(target)
    */
   public void sendBPDU(BPDU sent)
   {
      connected.recievedPacket = sent;
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
   
//   /**
//    * Get all the neighbors or the links(other switch's port) to this port.
//    * NOTE: we are making the assumption there is only 1 link per port. 
//    * @return the connected neighboring switch.
//    */
//   public Switch getNeighbor()
//   {
//      return neighbor;
//   }
//   
//   /**
//    * Set the neighboring switch.
//    * @param connectedNeighbor switch that this port is connected to. 
//    */
//   public void setNeighbor(Switch connectedNeighbor)
//   {
//      neighbor = connectedNeighbor;
//   }
   
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
}
