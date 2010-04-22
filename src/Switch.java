import java.util.ArrayList;
import java.util.Random;

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
 *  @version 0.2 April 12, 2010
 */
public class Switch 
{
   private final static int HELLO_TIMER = 2;
   private final static int FORWARDING_TIMER = 15;
   public final static int AGE_TIMER = 20;
   
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
   
   // RSTP flags
   private boolean proposal;
   private boolean learning = false;
   private boolean forwarding = false;
   private boolean agreement = false;

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
      priority = "8000";
      this.macID = macID;
      rootID = macID;
   }
   
   /**
    * Resets the clock to 0 and converged to false. Call this when resetting 
    * the topology.
    */
   public void reset()
   {
	   clock = 0;
	   converged = false;
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
    * Also tells this switch that it is root.
    * NOTE: input MUST be unique across the network topology.
    * @param input a unique String use as a the switch's MAC address(or ID).
    */
   public void setMacID(String input)
   {
      macID = input;
      rootID = input;
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
    * Increment the clock value for STP.
    */
   public void incrementClock()
   {
      clock++;

      /**
       * NOTE
       * Ports are set to Port.BLOCKING after initialization and only changed after 
       * STP tells them to (which is when BDPUs are received on that port).
       */
      if (clock - helloTime >= HELLO_TIMER)
      {
    	  sendBPDU();
    	  processReceivedBPDU();
      }
   }
   
   /**
    * Increment the clock value for RSTP.
    */
   public void rstpIncrementClock()
   {
      clock++;

      /**
       * NOTE
       * Ports are set to Port.BLOCKING after initialization and only changed after 
       * STP tells them to (which is when BDPUs are received on that port).
       */
      if (clock - helloTime >= HELLO_TIMER)
      {
    	  sendRSTP();
    	  processReceivedBPDU();
      }
   }
   
   /**
    * Displays the switches attributes, in particular the MAC ID. For debugging purposes.
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
    * Send a STP Configuration BPDU to all non-blocking ports.
    */
   private void sendBPDU()
   {
      int timestampSec = clock;
      String root = priority + "." + rootID;
      String mac = priority + "." + macID;
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
    * Send a RSTP BPDU to all non-blocking ports.
    */
   private void sendRSTP()
   {
      int timestampSec = clock;
      String root = priority + "." + rootID;
      String mac = priority + "." + macID;
      for(int i = 0; i < switchInterface.size(); i++)
      {
         // BPDU for STP.
         BPDU dataFrame = new BPDU(2, 2, topologyChange, proposal, 
        		 switchInterface.get(i).getRole(), learning, forwarding, 
        		 agreement, topologyChangeAck, root, cost, mac, i, timestampSec,
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
	   {
		   if (p.getRole() == Port.ROOT)
			   return true;
	   }
	   return false;
   }

   /**
    * Process a Received BPDU and configure itself(the switch) based on it.
    * Utilizes the STP.  
    */
   private void processReceivedBPDU()
   {
	   String root = priority + "." + rootID;
	   for (Port p : switchInterface)
	   {
		   if (p.getState() != Port.DISABLED && p.getConnected() != null)
		   {
			   BPDU frame = p.getFrame();
			   if (frame != null)
			   {
				   p.setAge(frame.getMessageAge());
				   if (frame.getType() == 0) // STP
				   {
					   if (p.getState() == Port.BLOCKING && (macID == rootID || !root.equals(frame.getRootID())))
					   {
						   p.setState(Port.LISTENING);
						   //System.out.println("At time " + clock + " Switch " + macID + " Port " + p + " is LISTENING");
					   }
					   if (p.getState() == Port.LISTENING)
					   {
						   if (!root.equals(frame.getRootID()))
							   electRootBridge(p, frame);
						   if (!haveRootPort())
							   electRootPort();
						   if (rootID == macID || (p.getRole() != Port.DESIGNATED && p.getConnected().getRole() != Port.DESIGNATED))
							   electDesignatedPort(p, frame);
						   if ((clock - forwardTime) >= FORWARDING_TIMER)
						   {
							   p.setState(Port.LEARNING);
							   //	forwardTime = clock;
						   }
					   }
					   else if (p.getState() == Port.LEARNING)
					   {
						   int index = switchInterface.indexOf(p);
						   if (index >= macAddressTable.size())
						   {
							   for (int i = 0; i <= index; i++)
								   macAddressTable.add("");
						   }
						   if (frame.getSenderID() != null)
							   macAddressTable.set(index, frame.getSenderID().substring(5));
						   if ((clock - forwardTime) >= FORWARDING_TIMER)
						   {
							   int role = p.getRole();
							   if (role == Port.ROOT || role == Port.DESIGNATED)
								   p.setState(Port.FORWARDING);
							   else
								   p.setState(Port.BLOCKING);
							   //checkConverged(); // CHANGE: Checking should be done at the very end.
						   }
					   }
				   } else if (frame.getType() == 128) // TCN
				   {
					   p.sendBPDU(new BPDU(0, 0, topologyChange, 
				        		 true, root, cost, priority + "." + macID, 
				        		 switchInterface.indexOf(p), clock,
				        		 AGE_TIMER, helloTime, FORWARDING_TIMER));
					   if (converged)
					   {
						   p.setState(Port.LISTENING);
						   p.setRole(Port.NONDESIGNATED);
						   //System.out.println("Network reconverging...");
						   // flood TCNs
						   for (Port other : switchInterface)
						   {
							   if (other != p && other.getState() != Port.DISABLED) // split horizon
							   {
								   other.sendBPDU(new BPDU(0, 128));
								   other.setState(Port.LISTENING);
								   other.setRole(Port.NONDESIGNATED);
							   }
						   }
						   //converged = false;
						   rootPort = null;
					   } else
						   sendBPDU();
				   }
			   } else if ((clock - p.getAge()) >= AGE_TIMER)
			   {
				   // If this port hasn't heard from its neighbor in AGE_TIMER
				   // send a Topology Change Notification
				   for (Port other : switchInterface)
				   {
					   if (other != p)
					   {
						   other.sendBPDU((new BPDU(0, 128)));
					   }
				   }
				   //System.out.println("Issuing topology change notification");
				   p.setState(Port.DISABLED);
				   //converged = false;
			   }
		   }
	   }
	   checkConverged();
   }
   
   /**
    * Assign a root Bridge.
    */
   public void electRootBridge(Port p, BPDU frame)
   {
	   String root = priority + "." + rootID;
	   if (frame.getRootID() != null)
	   {
		   if(root.compareTo(frame.getRootID()) > 0)
		   {
			   this.rootID = frame.getRootID().substring(5);
			   p.setPathCost(frame.getCost() + 19); // Assuming all interfaces are FastEthernet
			   for (Port switchport : switchInterface)
			   {
				   if (switchport.getRole() == Port.ROOT || switchport.getRole() == Port.DESIGNATED)
					   switchport.setRole(Port.NONDESIGNATED);
			   }
		   }
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
      if(this.rootPort == null && macID != rootID)
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
    	  }
      }
   }
   
   public void electDesignatedPort(Port p, BPDU frame)
   {
	   if (macID == rootID)
	   {
		   p.setRole(Port.DESIGNATED);
	   } else if (p.getRole() != Port.ROOT && p.getConnected().getRole() != Port.DESIGNATED)
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
	   //System.out.println("I think I'm converged = " + converged);
   }
   
   /**
    * Determines if this Switch thinks the topology is in STP converged state. 
    * Used to terminate the main simulator loop.
    * 
    * @return true if all Ports are in FORWARDING or BLOCKED state, also 
    *    in a case where a switch has no connection to other switches (stranded/leaf node).
    */
   public boolean isConverged()
   {
      if(hasPorts())
         return converged;
      else
         return true;
   }
   
   /**
    * Determine whether this switch has connections to other switches
    * @return true if switch has ports to connect to other switches, otherwise
    *    false.
    */
   public boolean hasPorts()
   {
      return switchInterface.size() > 0;
   }
   
   /**
    * Prints the state of each interface. Call this method when the LAN is 
    * converged.
    */
   public void printState()
   {
	   System.out.println("Bridge ID: " + macID);
	   
	   if(!hasPorts())
	      System.out.println("Leaf/stranded switch not connected to the topology.");
	   else
	   {
   	   if(macID == rootID)
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
      			   System.out.println("\t\tCost: " + p.getPathCost());
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
   		      case Port.BLOCKING:
   		         System.out.println("Blocking");
   		         break;
   		      case Port.LEARNING:
   	            System.out.println("Learning");
   	            break;
   	         case Port.LISTENING:
   	            System.out.println("Listening");
   	            break;
      		   case Port.FORWARDING:
      			   System.out.println("Forwarding");
      			   break;
      		   default:
      			   System.out.println("Disabled - error.");
   		   }
   	   }
   	   System.out.println("MAC Address Table");
   	   for (int i = 0; i < macAddressTable.size(); i++)
   	   {
   		   if (macAddressTable.get(i) != "")
   			   System.out.println("\t\t" + i + " " + macAddressTable.get(i));
   	   }
	   }
   }
   
   /**
    * Disables a random interface on this Switch
    * 
    * @return the number of the disabled interface or -1 if there is an error
    */
   public int breakLink()
   {
	   if (switchInterface.size() > 0)
	   {
		   int port = new Random().nextInt(switchInterface.size());
		   Port p = switchInterface.get(port);
		   if (p.getState() == Port.FORWARDING)
		   {
			   p.connectTo(null);
			   p.setState(Port.DISABLED);
			   converged = false;
			   return port;
		   }
	   }
	   return -1;
   }
   
   /**
    * Disables a particular interface on this Switch
    * 
    * @param link the number of the interface to be disabled
    */
   public void breakLink(int link)
   {
	   Port p = switchInterface.get(link);
	   p.connectTo(null);
	   p.setState(Port.DISABLED);
	   converged = false;
   }
}