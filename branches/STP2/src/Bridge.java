import java.util.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class Bridge
{
   private final int FORWARD_DELAY = 15;
   private final int MAX_AGE = 20;
   private final int HELLO_DELAY = 2;
   
   // Might need to sync the map, when modifying it. 
   // Collections.synchronizedMap
   private ArrayList<Port> portList;
   private String macID;
   private String rootID;
   private int rootCost;
   private int sentMessageAge;
   private Port rootPort;
   
   private Timer hello;
   private Timer monitor;
   
   /**
    * Allows initialization of a new Bridge.
    * 
    * @param portList an ArrayList of Ports on this Switch
    * @param macID this Switch's Bridge ID or MAC address
    */
   public Bridge(String macID)
   {
      this.portList = new ArrayList<Port>();
      rootPort = null;
      rootID = macID;
      rootCost = 0;
      sentMessageAge = 0; // Root bridge always sends this value as 0. Sort of like TTL.
      this.macID = macID;
      hello = new Timer();
      monitor = new Timer();
   }
   
   /**
    * Checks to see if another switch is equal to this one. Checking is done by comparing
    * their MAC ID, since this is by definition unique to each switch (universally). 
    */
   public boolean equals(Object other)
   {
       if (!(other instanceof Bridge))
          return false;
       return macID.compareTo(((Bridge) other).macID) == 0;
   }
   
   /**
    * Adds a new port to the switch interface. Ports should be initially set 
    * to a BLOCKING state.
    * 
    * @param p the port to add to the list of interfaces for the switch.
    */
   public void addPort(Port p)
   {
         portList.add(p);
   }
   
   /**
    * Should be called when the all switches converged.
    */
   public void stopAllTimers()
   {
      hello.cancel();
      monitor.cancel();
   }
   
   public void setMacID(String input)
   {
      macID = input;
   }
   
   public String getMacID()
   {
      return macID;
   }
   
   public void run()
   {
      //Starting root war.
      for(Port p : portList)
         p.toListening();
      
      helloTimer(); 
      monitorTimer();
   }
   
   public boolean isConverged()
   {
      for(Port p : portList)
      {
         if((p.getState() != Port.FORWARDING) || (p.getState() != Port.BLOCKING) || 
               (p.getState() != Port.BLOCKING))
         {
            return false;
         }
      }
      return true;
   }
   
   public String toString()
   {
      String temp = "Bridge MAC ID:\t" + macID + "\n";
      temp += "Root MAC ID:\t" + rootID + "\n";
      temp += "Port count:\t " + portList.size() + "\n";
      temp += "Port ID \t" + "Role \t\t" + "State\n";
      
      for(int i = 0; i < portList.size(); i++)
      {
         Port p = portList.get(i);
         String role = "";
         String state = "";
         
         switch (p.getRole())
         {
            case Port.ROOT: 
               role = "ROOT";
               break;
            case Port.DESIGNATED: 
               role = "DESIGNATED";
               break;
            default:
               role = "Nondesignated";
         }

         switch (p.getState())
         {
            case Port.BLOCKING:
               state = "Blocking";
               break;
            case Port.LEARNING:
               state = "Learning";
               break;
            case Port.LISTENING:
               state = "Listening";
               break;
            case Port.FORWARDING:
               state = "Forwarding";
               break;
            default:
               state = "ERROR/DIS";
         }
         
         temp += i + "\t" + role + "\t\t" + state +"\n"; 
      }
      
      return temp + "\n";
   }
   
   private boolean isRootBridge()
   {
      return rootID.compareTo(macID) == 0;
   }
   
   private void electRootBridge(Port p)
   {
      if(rootID.compareTo(p.getStoredBPDU().getRootID()) > 0)
      {
         rootID = p.getStoredBPDU().getRootID();
         rootCost = p.getStoredBPDU().getCost() + 1;    
     }
   }
   
   /**
    * Assign a root port.
    */
   private void electRootPort()
   {
      /*int bestRootCost = Integer.MAX_VALUE;
      int rootPortIndex = -1;
      for(int i = 0; i < portList.size(); i++)
      {
         Port p = portList.get(i);
         
         System.out.println(macID);
         System.out.println("Port # " + p.getInterfaceNumber() + " cost to root: " + p.getRootPathCost());
         System.out.println("Best cost: " + bestRootCost);
         
         if(p.getState() != Port.DISABLED)
         {
            if(p.getRootPathCost() < bestRootCost)
            {
               bestRootCost = p.getRootPathCost();
               rootPortIndex = i;
            }
            else if(p.getRootPathCost() == bestRootCost)
            {
            	if (rootPortIndex != -1)
            	{
            		String id = portList.get(rootPortIndex).getSenderID();
            		if (id != null)
            		{
            			if(id.compareTo(p.getSenderID()) > 0)
            				rootPortIndex = i;
            		}
            	}
            }
         }
      }
/*************************************************************************/
	   int rootPortIndex = portList.size();
	   int bestRootCost = Integer.MAX_VALUE;
	   for (int i = portList.size() - 1; i >= 0; i--)
	   {
		   Port p = portList.get(i);
		   if (p.getRootPathCost() != 0 && p.getRootPathCost() < bestRootCost)
		   {
			   bestRootCost = p.getRootPathCost();
			   rootPortIndex = i;
		   }
	         System.out.println(macID);
	         System.out.println("Port # " + p.getInterfaceNumber() + " cost to root: " + p.getRootPathCost());
	         System.out.println("Best cost: " + bestRootCost);
	   }
	   if (rootPortIndex == portList.size())
		   System.out.println("This is the Root Bridge");
	   else
	   {
		   System.out.println("Root Port is #" + rootPortIndex);
		   rootPort = portList.get(rootPortIndex);
		   portList.get(rootPortIndex).setRole(Port.ROOT);
	   }
      portList.get(rootPortIndex).toLearning();
   }

   
   private void electDesignatedPort(Port p)
   {
      boolean isDesignated = false;
      
      if (isRootBridge())
         isDesignated = true;
      else
      {
         if (p.getConnected().getRole() == Port.ROOT)
            isDesignated = true;
         if (rootCost < p.getRootPathCost())
            isDesignated = true;
         else if (rootCost == p.getRootPathCost())
         {
        	 String id = p.getSenderID();
        	 if (id != null)
        	 {
        		 if (macID.compareTo(id) < 0)
        			 isDesignated = true;
        	 }
         }
      }
     
      if(isDesignated)
      {
         p.setRole(Port.DESIGNATED);
         p.toLearning();
      }
      else 
         p.toBlocking();
   }

   
   private void monitorTimer()
   {
      monitor.scheduleAtFixedRate(new TimerTask(){
         public void run()
         {
            if(isConverged())
               monitor.cancel();
            else
            {
               for(Port p : portList)
               {
                  BPDU dataUnit = p.getStoredBPDU();
                  
                  if((dataUnit != null) && (p.getState() == Port.LISTENING))
                  {
                     if(rootID.compareTo(dataUnit.getRootID()) != 0)
                     {
                    	 p.setRootPathCost(dataUnit.getCost() + 1);
                        electRootBridge(p);
                     }
                     else if((rootPort == null) && (!isRootBridge()))
                        electRootPort();
                     else
                        electDesignatedPort(p);
                  }
               }
            }
         }
      }, 2000, 1000); // delay start till after initial hello BPDUs are sent.
   }
   
   /** 
    * Every 2 seconds, each port should produce a BPDU to its connected bridge.
    * Eventually only the root bridge will be sending these; the rest will forward. 
    */
   private void helloTimer()
   {      
      hello.scheduleAtFixedRate(new TimerTask(){
         public void run()
         {     
            for(Port p : portList)
            {
               if((p.getState() != Port.DISABLED) && (p.getState() != Port.BLOCKING) && (p.getRole() != Port.ROOT))
               {
                  BPDU configBPDU = new BPDU(0, 0, false, 
                     false, rootID, rootCost, macID, p.getInterfaceNumber(), sentMessageAge,
                     MAX_AGE, HELLO_DELAY, FORWARD_DELAY);
                  p.sendBPDU(configBPDU);
               }
            }
         }
      }, 0, HELLO_DELAY * 1000);
   }
   
}
