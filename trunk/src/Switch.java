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
      clock = 0;
      cost = 0;
      switchInterface = new ArrayList<Port>();
      macID = null;
      rootID = null;
      priority = "8000";
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
      switchInterface = portList;
      priority = "8000.";
      this.macID = priority + macID;
      rootID = macID;
   }
   
   public void setMacID(String input)
   {
      macID = input;
   }
   
   /* public String getMacID()
   {
      return macID;
   } */
   
   /**
    * Adds a new port to the switch interface. For now, it also prints the 
    * addresses of the two Switches. This feature is useful for debugging and 
    * may or may not be needed later.
    * NOTE: Be aware that ports shouldn't be blocked. LEARNING should be used.
    * 
    * @param p the port to add to the list of interfaces for the switch.
    */
   public void addPort(Port p)
   {
      switchInterface.add(p);
      System.out.println(macID + " connected to " + p.getNeighbor().getMac());
   }
   
   /**
    * Returns this Switch's MAC address. Useful for debugging. We may or may 
    * not need it later.
    * 
    * @return local MAC address
    */
   public String getMac() {return macID;}
   
   /**
    * Increment the clock value. (maybe use the Timer object instead?)
    */
   public void incrementClock()
   {
      clock++;
      if (start)
      {
    	  for (Port p : switchInterface)
    	  {
    		  p.setState(p.LISTENING);
    	  }
    	  start = false;
      }
      if (clock - helloTime >= HELLO_TIMER)
    	  sendBPDU();
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
   public void sendBPDU()
   {
      for(int i = 0; i < switchInterface.size(); i++)
      {
         int timestampSec = clock; // Calendar.getInstance().get(Calendar.SECOND);
         // BPDU for STP.
         BPDU dataFrame = new BPDU(0, 0, topologyChange, 
        		 topologyChangeAck, rootID, cost, macID, i, timestampSec,
        		 AGE_TIMER, helloTime, FORWARDING_TIMER);
         Port p = switchInterface.get(i);
         if(p.getState() != Port.BLOCKING)
            p.getNeighbor().receiveBPDU(p, dataFrame);
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
    * Receive a BPDU and configure itself(the switch) based on it. 
    * @param sender
    */
   public void receiveBPDU(Port p, BPDU frame)
   {
	   if (p.getState() == Port.LEARNING)
	   {
		   int index = switchInterface.indexOf(p);
		   if (index > macAddressTable.size())
		   {
			   for (int i = switchInterface.size(); i < index; i++)
				   macAddressTable.add(null);
		   }
		   macAddressTable.add(index, frame.getSenderID().substring(4));
	   }
	   if (!converged)
	   {
		   if (!rootID.equals(frame.getRootID()))
			   electRootBridge(frame);
		   else if (macID.equals(frame.getRootID()))
		   {
			   p.setRole(p.DESIGNATED);
		   } else if (!haveRootPort())
			   electRootPort(frame);
		   else
			   electDesignatedPort();
		   if (clock - forwardTime >= FORWARDING_TIMER)
		   {
			   if (p.getState() == Port.LEARNING)
				   {
					   if (p.getRole() == Port.ROOT || p.getRole() == Port.DESIGNATED)
						   p.setState(Port.FORWARDING);
					   else
						   p.setState(Port.BLOCKING);
					   converged = true;
				   } else
					   p.setState(Port.LEARNING);
		   }
	   }
   }
   
   /**
    * Assign a root Bridge.
    */
   public void electRootBridge(BPDU frame)
   {
      if(frame.getRootID().compareTo(this.rootID) < 0)
      {
         this.rootID = frame.getRootID();
         this.cost += frame.getCost();
      }
   }
   
   /**
    * Assign a root port.
    */
   public void electRootPort(BPDU frame)
   {
      cost += frame.getCost();
   }
   
   /**
    * Assign a designated port.
    */
   public void electDesignatedPort()
   {
      
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
}