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
 * @author chtrinh
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
  
   private Switch neighbor;
   private Port connected = null;
   private int portState;
   private int role = NONDESIGNATED;
   private int rootCost = 0;
   
   /**
    * Initial state of the port is set to BLOCKING (the value of 0).
    */
   public Port()
   {
      portState = BLOCKING;
      neighbor = new Switch();
   }
   
   /**
    * Set the initial state of the port.
    * @param initialState is the state to set the port to, use the constant values
    * defined already in this class (see the 'final' constant listed values above). 
    */
   public Port(int initialState, Switch connectedNeighbor)
   {
      portState = initialState;
      neighbor = connectedNeighbor;
   }
   
   /**
    * Connect two Ports.
    * 
    * @param ingress the Port to be connected
    */
   public void connectTo(Port ingress)
   {
	   connected = ingress;
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
    * Get all the neighbors or the links(other switch's port) to this port.
    * NOTE: we are making the assumption there is only 1 link per port. 
    * @return the connected neighboring switch.
    */
   public Switch getNeighbor()
   {
      return neighbor;
   }
   
   /**
    * Set the neighboring switch.
    * @param connectedNeighbor switch that this port is connected to. 
    */
   public void setNeighbor(Switch connectedNeighbor)
   {
      neighbor = connectedNeighbor;
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
    * 
    * @return the cost from this interface to the Root Bridge
    */
   public int getCost()
   {
	   return rootCost;
   }
   
   /**
    * 
    * @param cost the new cost from this interface to the Root Bridge
    */
   public void setCost(int cost)
   {
	   rootCost = cost;
   }
}
