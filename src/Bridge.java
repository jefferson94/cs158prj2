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
      portList = new ArrayList<Port>();
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
   public synchronized void stopTimers()
   {
      hello.cancel();
      monitor.cancel();
      for(Port p : portList)
         p.stopAgeTimer();
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
         p.toListening(FORWARD_DELAY);
      
      helloTimer(); 
      monitorTimer();
   }
   
   public synchronized boolean isConverged()
   {
      for(Port p : portList)
      {
         if(!((p.getState() == Port.FORWARDING) || (p.getState() == Port.BLOCKING) || 
               (p.getState() == Port.DISABLED)))
            return false;
      }
      return true;
   }
   
   public String toString()
   {
      String temp = "Bridge MAC ID:\t" + macID + "\n";
      temp += "Root MAC ID:\t" + rootID + "\n";
      temp += "Cost to Root Bridge:\t " + rootCost + "\n";
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
   
   private synchronized boolean isRootBridge()
   {
      return rootID.compareTo(macID) == 0;
   }
   
   private synchronized void electRootBridge(Port p)
   {  
      if(rootID.compareTo(p.getStoredBPDU().getRootID()) > 0)
      {
         rootID = p.getStoredBPDU().getRootID();
         rootCost = p.getStoredBPDU().getCost() + 1;
         for(Port temp : portList)
         {
            if(!temp.equals(p))
            {
               BPDU configBPDU = new BPDU(0, 0, false, 
                  false, rootID, rootCost, macID, p.getInterfaceNumber(), sentMessageAge,
                  MAX_AGE, HELLO_DELAY, FORWARD_DELAY);
               p.sendBPDU(configBPDU);
            }
         }     
      }
      
//      System.out.println("Mac: " + macID);
//      System.out.println(p.getInterfaceNumber() + " root cost: " + p.getRootPathCost());
//      System.out.println(this);
   }
   
   /**
    * Assign a root port.
    */
   private synchronized void electRootPort()
   {
      int bestRootCost = Integer.MAX_VALUE;
      int rootPortIndex = -1;
      for(int i = 0; i < portList.size(); i++)
      {
//         Port p = portList.get(i);
         
         if(portList.get(i).getState() != Port.DISABLED)
         {
            if(portList.get(i).getRootPathCost() < bestRootCost)
            {
               bestRootCost = portList.get(i).getRootPathCost();
               rootPortIndex = i;
            }
            else if(portList.get(i).getRootPathCost() == bestRootCost)
            {
               if(portList.get(rootPortIndex).getSenderID().compareTo(portList.get(i).getSenderID()) > 0)
                  rootPortIndex = i;
            }
         }
      }
      
      rootPort = portList.get(rootPortIndex);
      sentMessageAge = rootPort.getStoredBPDU().getMessageAge() + 1;
//      portList.get(rootPortIndex).setRole(Port.ROOT);
      portList.get(rootPortIndex).toLearning(rootPort.getStoredBPDU().getForwardDelay(), true);
//      System.out.println("Mac: " + macID);
//      System.out.println("port root cost: " + rootPort.getRootPathCost());
//      System.out.println("best cost: " +bestRootCost  + " best index: " + rootPortIndex);
//      System.out.println("ROOT PORT!");
//      System.out.println(this);
   }

   
   private synchronized void electDesignatedPort(Port p)
   {
      boolean isDesignated = false;
      
      if (isRootBridge())
         isDesignated = true;
      else
      {
         if (rootCost < p.getRootPathCost())
            isDesignated = true;
         else if (rootCost == p.getRootPathCost())
         {
            if (macID.compareTo(p.getStoredBPDU().getSenderID()) < 0)
               isDesignated = true;
         }
      }
//     
//      System.out.println("Mac id: " + macID);
//      System.out.println("Port # " + p.getInterfaceNumber());
//      System.out.println(" isDesignated" + isDesignated);
      if(isDesignated)
      {
         p.setRole(Port.DESIGNATED);
         p.toLearning(p.getStoredBPDU().getForwardDelay(), false);
      }
//      else 
//         p.toBlocking();
//      System.out.println(this);
   }

   
   private synchronized void monitorTimer()
   {
      monitor = new Timer();
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
                        electRootBridge(p);

                     }
                     else if((rootPort == null) && (!isRootBridge()))
                     {
                        electRootPort();
                     }
                     else
                     {
                        electDesignatedPort(p);
                        
                     }
//                   System.out.println(macID);
//                   System.out.println("Port # " + p.getInterfaceNumber() + " cost to root: " + p.getRootPathCost());
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
   private synchronized void helloTimer()
   {      
      hello = new Timer();
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
