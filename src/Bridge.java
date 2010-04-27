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
   private Timer max;
   
   /**
    * Allows initialization of a new Bridge.
    * 
    * @param portList an ArrayList of Ports on this Switch
    * @param macID this Switch's Bridge ID or MAC address
    */
   public Bridge( String macID)
   {
      this.portList = new ArrayList<Port>();
      rootPort = null;
      rootID = null;
      rootCost = 0;
      sentMessageAge = 0; // Root bridge always sends this value as 0. Sort of like TTL.
      this.macID = macID;
      hello = new Timer();
      max = new Timer();
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
   public void addPort(Port p, int interfaceNumber)
   {
      portList.add(interfaceNumber, p);
   }
   
   /**
    * Should be called when the all switches converged.
    */
   public void stopAllTimers()
   {
      hello.cancel();
      max.cancel();
   }
   
   public void setMacID(String input)
   {
      macID = input;
   }
   
   public String getMacID()
   {
      return macID;
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
               if((p.getState() != Port.DISABLED) || (p.getState() != Port.BLOCKING) || (p.getRole() == Port.DESIGNATED))
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
