/**
 * 
 */
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;

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
   
   public Switch()
   {
      clock = 0;
      cost = 0;
      switchInterface = new ArrayList<Port>();
      macID = null;
      rootID = null;
   }
   
   public Switch(int clockValue, int costValue, ArrayList<Port> portList, String macID)
   {
      cost = costValue;
      clock = clockValue;
      switchInterface = portList;
      this.macID = macID;
   }
   
   /**
    * Adds a new port to the switch interface.
    * NOTE: Be aware that ports shouldn't be blocked. LEARNING should be used.
    * @param p the port to add to the list of interfaces for the switch.
    */
   public void addPort(Port p)
   {
      switchInterface.add(p);
   }
   
   /**
    * Increment the clock value. (maybe use the Timer object instead?)
    */
   public void incrementClock()
   {
      clock++;
   }
   
   /**
    * Send a BPDU to all active ports in its switch interface. 
    * @param receiver 
    */
   public void sendBPDU()
   {
      for(int i = 0; i < switchInterface.size(); i++)
      {
         int timestampSec = Calendar.getInstance().get(Calendar.SECOND);
         // BPDU for STP.
         BPDU dataFrame = new BPDU(0, 0, false, 
               false, rootID, cost, 
               macID, i, timestampSec, AGE_TIMER, 
               HELLO_TIMER, FORWARDING_TIMER);
         Port p = switchInterface.get(i);
         if(p.getState() != Port.BLOCKING)
            p.getNeighbor().receiveBPDU(dataFrame);
      }
   }

   /**
    * Receive a BPDU and configure itself(the switch) based on it. 
    * @param sender
    */
   public void receiveBPDU(BPDU frame)
   {
      if(frame.getTopologyChange())
      {
         electRootBridge(frame);
      }
      else
      {
         electRootPort(frame);
         
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
         this.cost = frame.getCost() + 1;
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
}