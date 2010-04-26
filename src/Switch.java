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
   private final static int FIX_PATH_COST = 1;
   
   private ArrayList<Port> switchInterface;
   private int clock;
   private int cost;
   private String macID;
   private String rootID;
   private String priority;
   private Port rootPort;
//   private boolean broken;
   
   private ArrayList<String> macAddressTable = new ArrayList<String>();
   
   private boolean topologyChange = false;
   private boolean topologyChangeAck = false;
   
   private int helloTime = clock;
   private int forwardTime = clock;

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
    * Displays the switches attributes, in particular the MAC ID. For debugging purposes.
    */
   public String toString()
   {
      return "MAC ID: " + getMac();      
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
    * Initial configuration of STP. All ports are set to listening state,
    * Being root war.
    */
   public void rootWar()
   {
      for(Port p : switchInterface)
      {
         p.setRole(Port.NONDESIGNATED);
         if(p.getState() != Port.DISABLED)
            p.setState(Port.LISTENING);
      }
      
      topologyChange = false;
      topologyChangeAck = false;
   }
   
   /**
    * Increment the clock value.
    */
   public void incrementClock()
   {
      if(clock == 0)
         rootWar();
      else
         processReceivedBPDU();
      
      sendBPDU();
      clock++;
   }

   /**
    * Send a BPDU to all non-blocking ports.
    */
   private void sendBPDU()
   {
      int timestampSec = clock;
//      String root = priority + "." + rootID;
//      String mac = priority + "." + macID;
      String root = rootID;
      String mac =  macID;
      int version = 0;
      
//      if(topologyChange)
//         version = 128;
//      
//      if(topologyChange && !topologyChangeAck)
//      {
//         BPDU changeBPDU = new BPDU(0, version);
//         if(!isRootBridge())
//            this.rootPort.sendBPDU(changeBPDU);
//      }
//      else 
//      {
//
//      }
      for(int i = 0; i < switchInterface.size(); i++)
      {
         BPDU configBPDU = new BPDU(version, 0, topologyChange, 
               topologyChangeAck, root, cost, mac, i, timestampSec,
               AGE_TIMER, helloTime, FORWARDING_TIMER);
         
         Port p = switchInterface.get(i);
         
         if((p.getState() != Port.BLOCKING) && (p.getRole() != Port.ROOT) && (p.getState() != Port.DISABLED))
            p.sendBPDU(configBPDU);
      }
   }
   
   /**
    * Process a Received BPDU and configure itself(the switch) based on it.
    * Utilizes the STP.  
    */
   private void processReceivedBPDU()
   {
      for( int x = 0; x < switchInterface.size(); x++)
      {
         Port p = switchInterface.get(x);
         BPDU dataUnit = p.getFrame();
         
         if(dataUnit != null)
         {
            if (dataUnit.getType() == 0) // STP
            {
               if((p.getState() == Port.LISTENING) && (p.getRole() == Port.NONDESIGNATED))
               {
                  if (this.rootID.compareTo(dataUnit.getRootID()) != 0)
                     electRootBridge(p, dataUnit);
                  else if (!haveRootPort() && !isRootBridge())
                     electRootPort();
                  else
                     electDesignatedPort(p, dataUnit);
               }
               else if (p.getState() == Port.LEARNING)
               {
                  int index = switchInterface.indexOf(p);
                  if (index >= macAddressTable.size())
                  {
                     for (int i = 0; i <= index; i++)
                          macAddressTable.add("");
                  }
                  if (dataUnit.getSenderID() != null)
                      macAddressTable.set(index, dataUnit.getSenderID());
   
                  int role = p.getRole();
                  if ((role == Port.ROOT) || (role == Port.DESIGNATED))
                     p.setState(Port.FORWARDING);
               }
            }
//            else if(dataUnit.getType() == 128) // TCN
//            {
//               if(!isRootBridge())
//                  topologyChange = true;
//               else if(topologyChangeAck) // ACK flush out tables.
//               {
//                  cost = 0;
//                  macAddressTable = new ArrayList<String>();
//                  rootWar(); // Initial to start up rootWar
//               }              
//               else // Flood TCA and TCs in order age out tables.
//               {
//                  topologyChangeAck = true;
//               }
//            }
         }
         else if(p.getRole() != Port.DESIGNATED) // Possible link breakage, all non-DESIGNATED ports should still be receiving CBPDUs.
         {
            if(p.getConnected().getState() == Port.DISABLED)
            {
               p.setRole(Port.DESIGNATED);
               p.setState(Port.FORWARDING);
            }
            System.out.println("FUBAR occured!");
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
		   this.rootID = frame.getRootID();
		   this.cost = frame.getCost() + FIX_PATH_COST;    
	   }
   }
   
   /**
    * Assign a root port.
    */
   public void electRootPort()
   {
      int bestRootCost = Integer.MAX_VALUE;
      int rootPortIndex = -1;
      for(int i = 0; i < switchInterface.size(); i++)
      {
         if((switchInterface.get(i).getState() != Port.DISABLED) && (switchInterface.get(i).getSenderID() != null))
         {
            if(switchInterface.get(i).getRootPathCost() < bestRootCost)
            {
               bestRootCost = switchInterface.get(i).getRootPathCost();
               rootPortIndex = i;
            }
            else if(switchInterface.get(i).getRootPathCost() == bestRootCost)
            {
               if(switchInterface.get(rootPortIndex).getSenderID().compareTo(switchInterface.get(i).getSenderID()) > 0)
                  rootPortIndex = i;
            }
         }
      }
      
      if(switchInterface.get(rootPortIndex).getConnected().getState() == Port.FORWARDING)
      {
         switchInterface.get(rootPortIndex).setRole(Port.ROOT);
         switchInterface.get(rootPortIndex).setState(Port.LEARNING);
         this.rootPort = switchInterface.get(rootPortIndex);
      }
   }
   
   public void electDesignatedPort(Port p, BPDU frame)
   {
      boolean isDesignated = false;
      
	   if (isRootBridge())
	      isDesignated = true;
	   else
      {
	      if (p.getConnected().getRole() == Port.ROOT)
	         isDesignated = true;
	      if (cost < frame.getCost())
	         isDesignated = true;
	      else if (cost == frame.getCost())
	      {
	         if ((this.macID).compareTo(frame.getSenderID()) < 0)
	            isDesignated = true;
	      }
      }
	   
	   if(isDesignated)
	   {
	      p.setRole(Port.DESIGNATED);
	      p.setState(Port.LEARNING);
	   }
	   else if(p.getConnected().getState() == Port.FORWARDING)
	      p.setState(Port.BLOCKING);
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

   private boolean isRootBridge()
   {
      return this.rootID == this.macID;
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
    * @return true if all Ports are in FORWARDING or BLOCKED state, also 
    *    in a case where a switch has no connection to other switches (stranded/leaf node).
    */
   public boolean isConverged()
   {
      if(hasPorts())
      {
         checkConverged();
         return converged && !topologyChange;
      }
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
      			   System.out.println("\t\tCost: " + this.getCost());
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
   
   public int breakLink()
   {
	   int port = new Random().nextInt(switchInterface.size());
	   Port p = switchInterface.get(port);
//	   if (p.getState() == Port.FORWARDING)
//	   {
//		   p.connectTo(null);
//		   p.setState(Port.DISABLED);
//		   converged = false;
//		   topologyChange = true;
//		   return port;
//	   } else
//		   return -1;
	   
      p.connectTo(null);
      p.setState(Port.DISABLED);
      converged = false;
      topologyChange = true;
      return port;
   }
   public void resetClock() {
       clock = 0;
       //System.out.println(macID + " clock reset to " + clock);
   }
}