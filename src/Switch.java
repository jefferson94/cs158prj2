import java.util.ArrayList;
import java.util.Calendar;

/**
 * A representation of a Layer 2 switch. When multiple instances of this class 
 * are connected to each other using Ports, they will perform Spanning Tree 
 * Protocol algorithm until all Ports are either in BLOCKED or FORWARDING 
 * state. At this time, each Switch will know it's converged, but continue 
 * sending BPDUs. If a Switch stops hearing Configuration BPDUs from another 
 * Switch or begins hearing Configuration BPDUs from a new Switch, it will 
 * start sending Topology Change Notification BPDUs and prepare to recalculate 
 * STP.
 * 
 *  @author Christopher Trinh
 *  @author John Le Mieux
 *  @author Peter Le
 *  @version 0.1 April 5, 2010
 */
public class Switch 
{
   private final static int HELLO_TIMER = 2;
   private final static int FORWARDING_TIMER = 15;
   private final static int AGE_TIMER = 20;
   
   private ArrayList<Port> switchInterface;
   private int clock;
   private int cost;
   private String macID;
   private String rootID;
   private String priority;
   private Port rootPort;
   
   private ArrayList<String> macAddressTable = new ArrayList<String>();
   
   private boolean topologyChange = false;
   private boolean topologyChangeAck = false;
   
   private int helloTime = clock;
   private int forwardTime = clock;
   private int ageTime = clock;
   
   private boolean start = true;
   private boolean converged = false;
   
   
   /**
    * Initializes clock, root cost, Bridge ID, Root Bridge ID, and ArrayList of 
    * Ports to 0 or null.
    */
   public Switch()
   {      
      this(0, 0, new ArrayList<Port>(), null);
   }
   
   /**
    * Allows initialization of a new Switch.
    * 
    * @param clockValue the current (simulated) time
    * @param costValue the cost to the Root Bridge
    * @param portList an ArrayList of Ports on this Switch
    * @param macID this Switch's Bridge ID or MAC address
    */
   public Switch(int clockValue, int costValue, ArrayList<Port> portList, String macID)
   {
      cost = costValue;
      clock = clockValue;
      rootPort = null;
      switchInterface = portList;
      priority = "8000.";
      this.macID = macID;
      rootID = macID;
   }
   
   /**
    * Adds a new port to the switch interface. Ports should be initially set 
    * to a BLOCKING state.
    * 
    * @param p the port to add to the list of interfaces for the switch.
    */
   public void addPort(Port p)
   {
      switchInterface.add(p);
   }
   
   /**
    * Set the Switch's MAC address(or ID). 
    * NOTE: input MUST be unique across the network topology.
    * @param input a unique String use as a the switch's MAC address(or ID).
    */
   public void setMacID(String input)
   {
      macID = input;
   }
   
   /**
    * Returns this Switch's MAC address. Useful for debugging. We may or may 
    * not need it later.
    * 
    * @return local MAC address
    */
   public String getMac() {return macID;}
   
   /**
    * Increment the clock value.
    */
   public void incrementClock()
   {
      clock++;

      /**
       * NOTE
       * Ports are set to Port.BLOCKING after initialization and only changed after 
       * STP tells them to (which is when BDPUs are received on that port).
       */
//      if (start)
//      {
//    	  for (Port p : switchInterface)
//    	  {
//    		  p.setState(p.LISTENING);
//    	  }
//    	  start = false;
//      }
      if (clock - helloTime >= HELLO_TIMER)
      {
    	  sendBPDU();
    	  /**
    	   * NOTE
    	   * Since this is a event simulator and helloTimer doesn't really expire. I think we should set processing
    	   * of received BPDUs after the initial sending of the BDPUs. And subsequent events will have the processing 
    	   * of BDPUs simultaneously sending out other BDPUs.
    	   * Does that make sense in terms of an event simulator?
    	   */
    	  processReceivedBPDU(); 
      }
   }
   
   /**
    * Displays the switches attributes, in particular the MAC ID. For debugging purposes.
    * NOTE: Should add more attributes in the string. 
    */
   public String toString()
   {
      return "MAC ID: " + getMac();
      //+ " Port count: " + switchInterface.size() + "\n";
   }
   
   /**
    * Checks to see if another switch is equal to this one. Checking is done by comparing
    * their MAC ID, since this is by definition unique to each switch (universally). 
    */
   public boolean equals(Object other)
   {
       if (!(other instanceof Switch)) {
           return false;
       }
       return macID.compareTo(((Switch) other).macID) == 0;
   }

   /**
    * Send a BPDU to all active ports in its switch interface. 
    * @param receiver 
    */
   private void sendBPDU()
   {
      int timestampSec = clock;
      String root = priority + rootID;
      String mac = priority + macID;
      for(int i = 0; i < switchInterface.size(); i++)
      {
         // BPDU for STP.
         BPDU dataFrame = new BPDU(0, 0, topologyChange, 
        		 topologyChangeAck, root, cost, mac, i, timestampSec,
        		 AGE_TIMER, helloTime, FORWARDING_TIMER);
         Port p = switchInterface.get(i);
         /**
          * NOTE
          * My understanding is that BDPUs are always sent regardless of the ports state. 
          * Reasoning: BLOCKING ports are still able to receive BPDUs and all ports are set to
          * BLOCKING after initialization.
          * Sidenote: If we implement disable then we can just replace that with != Port.DISABLED
          */
//         if(p.getState() != Port.BLOCKING)
//           p.getConnected().receiveBPDU(p.getConnected(), dataFrame);
         p.sendBPDU(dataFrame);
      }
   }
   
   /**
    * 
    * @return true if this switch has a Root Port
    */
   public boolean haveRootPort()
   {
	   for (Port p : switchInterface)
		   if (p.getRole() == Port.ROOT)
			   return true;
	   return false;
   }

   /**
    * Process a Received BPDU and configure itself(the switch) based on it.
    * Utilizes the STP.  
    */
   private void processReceivedBPDU( )
   {
      for(Port p : switchInterface)
      {
         BPDU frame = p.receivedBPDU();
         /**
          * NOTE
          * I'm not understanding what is going on here. I know it something to do with 
          * learning of the different mac address this switch is connected to. Can you clarify?
          */
//   	   if (p.getState() == Port.LEARNING)
//   	   {
//   		   int index = switchInterface.indexOf(p);
//   		   if (index >= macAddressTable.size())
//   		   {
//   			   for (int i = 0; i <= index; i++)
//   				   macAddressTable.add("");
//   		   }
//   		   macAddressTable.add(index, frame.getSenderID().substring(4));
//   	   }
         
         if((frame != null) && (!converged))
         {
            //This part is the root war. All switches will continue this process
            //till there is an agreement for the Root Switch.
            if (!rootID.equals(frame.getRootID()))
            { 
               electRootBridge(p, frame);
               //Port that received the BPDUs most likely the root port (conditions where it 
               //is not taken care of in the else statement).
               electRootPort(p);
            }
            else //RootIDs are equal compare distance/cost from root.
            {
               //Try and elect root port
               if(cost > (frame.getCost() + p.getPathCost()))
               {
                  //Better path cost is found, receiving port must be the root port.
                  cost = frame.getCost() + p.getPathCost();
                  electRootPort(p);
               }
               else if(cost == frame.getCost())
               {
                  //Compare BDPU's sender ID to Root Port ID's sender id.
                  if(rootPort.receivedBPDU().getSenderID().compareTo(frame.getSenderID()) < 0)
                     electRootPort(p);
               }   
               
               //Check to see if there is a possibility the port is a designated port.
               if(p.getState() == Port.BLOCKING)
               {
                  if(this.cost < frame.getCost())
                     electDesignatedPort(p);
                  else if(this.cost == frame.getCost())
                  {
                     if(this.macID.compareTo(frame.getSenderID()) < 0)
                        electDesignatedPort(p);
                  }  
               }
               
               //Check to see if timer has aged to set port to forwarding.
               if (clock - forwardTime >= FORWARDING_TIMER)
               {
                  if (p.getState() == Port.LEARNING)
                  {
                     p.setState(Port.FORWARDING);
                     /**
                      * NOTE
                      * How do you find if the switch is convergent? 
                      * The statement below will set convergence after one port is found to be forwarding, 
                      * which isn't always the case when there are multiple ports.
                      */
                     //converged = true;
                  }
               }
            }
         }
	   }
   }
   
   /**
    * Assign a root Bridge.
    */
   public void electRootBridge(Port p, BPDU frame)
   {
      if(this.rootID.compareTo(frame.getRootID()) > 0)
      {
         this.rootID = frame.getRootID().substring(4);
         this.cost = frame.getCost() + p.getPathCost();
      }
   }
   
   /**
    * Assign a root port.
    */
   public void electRootPort(Port p)
   {
      //Remove previous root port, meaning it should set back to a blocking state.
      if(this.rootPort != null)
      {
         this.rootPort.setState(Port.BLOCKING);
         this.rootPort.setRole(Port.NONDESIGNATED);
      }
      
      //Set new root port for this switch.
      this.rootPort = p;
      this.rootPort.setRole(Port.ROOT);
      
      //Port is set to Listening after it knows it is a root port.
      this.rootPort.setState(Port.LISTENING);
   }
   
   /**
    * Assign a designated port.
    */
   public void electDesignatedPort(Port p)
   {
      p.setRole(Port.DESIGNATED);
      p.setState(Port.LISTENING);
   }
   
   /**
    * Determines if this Switch thinks the topology is in STP converged state. 
    * Used to terminate the main simulator loop.
    * 
    * @return true if all Ports are in FORWARDING or BLOCKED state
    */
   public boolean isConverged()
   {
	   return converged;
   }
   
   /**
    * Prints the state of each interface. Call this method when the LAN is 
    * converged.
    */
   public void printState()
   {
	   System.out.println("Bridge ID: " + macID);
	   if (macID == rootID)
		   System.out.println("I am the Root Bridge");
	   System.out.println("\tTime: " + clock);
	   for (int i = 0; i < switchInterface.size(); i++)
	   {
		   System.out.println("\tInterface ID: " + i);
		   Port p = switchInterface.get(i);
		   System.out.print("\t\tPort Role: ");
		   switch (p.getRole())
		   {
		   case Port.ROOT: 
			   System.out.println("Root");
			   break;
		   case Port.DESIGNATED: 
			   System.out.println("Designated");
			   break;
		   default:
			   System.out.println("Nondesignated");
		   }
		   System.out.print("\t\tPort State: ");
		   switch (p.getState())
		   {
		   case Port.FORWARDING:
			   System.out.println("Forwarding");
			   break;
		   default:
			   System.out.println("Blocking");
		   }
		   System.out.println("\t\tPort State: " + p.getState());
	   }
   }
}