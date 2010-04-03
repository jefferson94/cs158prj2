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
   
   public int getCost()
   {
	   return cost;
   }
   
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
      /*
       * This is just a cheater way to simulate the recently powered up Switch 
       * transitioning all STP interfaces to LISTENING at the end of POST.
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
    	  /*
    	   * NOTE
    	   * This is important in a real world situation. Not really important 
    	   * one way or the other here.
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
    * Send a BPDU to all non-blocking ports.
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
         /* NOTE
          * An interface in BLOCKING state receives BPDUs but does not forward 
          * them. When it receives a TCN (next implementation), it sends a 
          * TCN ACK out that interface, transitions to LISTENING state, and 
          * floods TCNs until the FORWARDING_TIMER expires.
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
   private void processReceivedBPDU()
   {
	   for (Port p : switchInterface)
	   {
		   BPDU frame = p.getFrame();
		   if (p.getState() == Port.LISTENING)
		   {
			   String root = priority + "." + rootID;
			   if (!root.equals(frame.getRootID()))
				   electRootBridge(p, frame);
			   else if (!haveRootPort())
				   electRootPort();
			   if (rootID == macID || (p.getRole() != Port.DESIGNATED && p.getConnected().getRole() != Port.DESIGNATED))
				   electDesignatedPort(p, frame);
			   if (clock - forwardTime >= FORWARDING_TIMER)
			   {
				   p.setState(Port.LEARNING);
				   forwardTime = clock;
				   System.out.println("At time " + clock + " Switch " + macID + " Port " + switchInterface.indexOf(p) + " is LEARNING");
			   }
			} else if (p.getState() == Port.LEARNING)
			{
				/* TODO
				 * LEARNING state is for loading the MAC Address Table with the 
				 * addresses of other switches and the interfaces to use to get 
				 * there.
				 */
				int index = switchInterface.indexOf(p);
				if (index >= macAddressTable.size())
				{
					for (int i = 0; i <= index; i++)
						macAddressTable.add("");
				}
				macAddressTable.add(index, frame.getSenderID().substring(5));
				if (clock - forwardTime >= FORWARDING_TIMER)
				{
					int role = p.getRole();
					if (role == Port.ROOT || role == Port.DESIGNATED)
						p.setState(Port.FORWARDING);
					else
						p.setState(Port.BLOCKING);
					checkConverged();
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
         this.rootID = frame.getRootID().substring(5);
         p.setPathCost(frame.getCost() + 19); // Assuming all interfaces are FastEthernet
	   for (Port switchport : switchInterface)
	   {
		   if (switchport.getRole() == Port.ROOT || switchport.getRole() == Port.DESIGNATED)
			   switchport.setRole(Port.NONDESIGNATED);
	   }
         System.out.println("At time " + clock + " Switch " + macID + " thinks " + rootID + " is the root.\nMy cost is " + p.getPathCost());
      }
   }
   
   /**
    * Assign a root port.
    */
   public void electRootPort()
   {
	   int portCost = 0;
	   int size = switchInterface.size();
	   int port = size;
      //Remove previous root port, meaning it should set back to a blocking state.
      if(this.rootPort != null)
      {
    	  for (int i = size - 1; i >= 0; i--)
    	  {
    		  int rootCost = switchInterface.get(i).getPathCost();
    		  if (portCost == 0 || (rootCost > 0 && rootCost < portCost))
    		  {
    			  portCost = rootCost;
    			  port = i;
    		  }
    	  }
    	  if (portCost > 0)
    	  {
    		  rootPort = switchInterface.get(port);
    		  rootPort.setRole(Port.ROOT);
    		  cost = portCost;
    		  System.out.println("At time " + clock + " Switch " + macID + " has Port " + rootPort + " as root.\nMy cost is " + cost);
    	  }
      }
      
      //Port is set to Listening after it knows it is a root port.
      this.rootPort.setState(Port.LISTENING);
   }
   
   /* TODO
    * Assign a designated port.
    */
   public void electDesignatedPort(Port p, BPDU frame)
   {
      if (p.getRole() != Port.ROOT && p.getConnected().getRole() != Port.DESIGNATED)
      {
    	  if (p.getConnected().getRole() == Port.ROOT)
    		  p.setRole(Port.DESIGNATED);
    	  else if (cost < frame.getCost())
    		  p.setRole(Port.DESIGNATED);
    	  else if (cost == frame.getCost())
    	  {
    		  if ((priority + "." + macID).compareTo(frame.getSenderID()) < 0)
    			  p.setRole(Port.DESIGNATED);
    		  else if ((priority + "." + macID).compareTo(frame.getSenderID()) == 0)
    		  {
    			  if (switchInterface.indexOf(p) < frame.getPortID())
    				  p.setRole(Port.DESIGNATED);
    		  } else
    			  p.getConnected().setRole(Port.DESIGNATED);
    	  } else
    		  p.getConnected().setRole(Port.DESIGNATED);
      }
      /*if (p.getRole() == Port.DESIGNATED)
    	  System.out.println("At time " + clock + ", between " + macID + " and " + p.getNeighbor().getMac() + ", " + macID + " has the Designated Port");
      else if (p.getConnected().getRole() == Port.DESIGNATED)
    	  System.out.println("At time " + clock + ", between " + macID + " and " + p.getNeighbor().getMac() + ", " + p.getNeighbor().getMac() + " has the the Designated Port");*/
   }
   
   private void checkConverged()
   {
	   boolean maybe = true;
	   for (Port p : switchInterface)
	   {
		   if (p.getState() == Port.LISTENING || p.getState() == Port.LEARNING)
			   maybe = false;
	   }
	   converged = maybe;
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
		   /*
		    * TODO
		    * For the output, as well as debugging, we need a mechanism for 
		    * telling the Switch what is on the other end of a link.
		    */
		   //System.out.println("\t\tConnected to " + p.getNeighbor().getMac());
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
	   System.out.println("MAC Address Table");
	   for (int i = 0; i < macAddressTable.size(); i++)
	   {
		   if (macAddressTable.get(i) != "")
			   System.out.println("\t\t" + i + " " + macAddressTable.get(i));
	   }
   }
}