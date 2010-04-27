import java.util.Timer;
import java.util.TimerTask;

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
 * 
 * @author Christopher Trinh
 * @author John Le Mieux
 * @author Peter Le
 * @version 0.2 April 12, 2010
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
  
   private int number;
   private Port connected;
   private int portState;
   private int rootPathCost;
   private String senderID;
   private int role;
   private BPDU storedBPDU;
   
   private int ageTime;
   private Timer max;
   
   /**
    * Set the initial state of the port.
    * @param initialState is the state to set the port to, use the constant values
    * defined already in this class (see the 'final' constant listed values above). 
    * @param otherSwitchPort is the switch port 'this' port is connected to. 
    * @param cost is the path cost for the link this port uses; for 
    * fast-ethernet this value would be 19.
    * 
    */
   public Port(int interfaceNumber)
   {
      portState = Port.BLOCKING;
      connected =  null;
      storedBPDU = null;
      rootPathCost = 0;
      senderID = null;
      role = NONDESIGNATED;
      number = interfaceNumber;
      max = new Timer();
     
   }
     
   /**
    * The max age timer controls the maximum length of time that passes before a bridge port saves its configuration BPDU information. 
    * This time is 20 seconds by default.
    * 
    * When a new configuration BPDU is received that is equal to or better than the recorded information on the port, all the BPDU information is stored.
    * The age timer begins to run. The age timer starts at the message age that is received in that configuration BPDU. 
    * If this age timer reaches max age before another BPDU is received that refreshes the timer, the information is aged out for that port.
    */
   private void maxAgeTimer()
   {
      max.scheduleAtFixedRate(new TimerTask(){
         public void run()
         {
            if(storedBPDU != null)
            {   
               int currentAge = storedBPDU.getMessageAge();
               if(currentAge == storedBPDU.getMaxAge())
               {
                  storedBPDU = null; // Age out this information; possible break in the link.
                  cancel();
               }        
               storedBPDU.setMessageAge(currentAge + 1);
            }
         }
      }, 0, 1000); // No delay when called, runs every 1 second.
   }
   
   /** 
    * Set the port to blocking state.
    */
   public void toBlocking()
   {
      portState = Port.BLOCKING;
   }
   
   /**
    * Set port state to listening. Goes back to blocking state if timer exceeds forwarding delay and 
    * port state is still set to listening, meaning it wasn't elected as a designated port or root port. 
    */
   public void toListening()
   {
      portState = Port.LISTENING;
      final Timer listen = new Timer();
      listen.scheduleAtFixedRate(new TimerTask(){
         private int seconds = 0;
         public void run()
         {
            if((storedBPDU.getForwardDelay() <= seconds) && (portState == Port.LISTENING))
            {   
               portState = Port.BLOCKING; // Was not elected as a Designated Port.
               listen.cancel();
            }
            seconds++;
         }
      }, 0, 1000); 
   }
   
   /**
    * Imitates the learning phase of the port where it listens for MAC from frames which 
    * it receives. In this simulator it just waits for 15secs before it moves on to the fowarding state. 
    */
   public void toLearning()
   {
      portState = Port.LEARNING;
      final Timer learn = new Timer();
      learn.scheduleAtFixedRate(new TimerTask(){
         private int seconds = 0;
         public void run()
         {
            seconds++;
         }
      }, 0, 1000);
   }
   
   /**
    * Sets the state of the port to Forwarding.
    */
   public void toFowarding()
   {
      portState = Port.FORWARDING;
   }
   
   /**
    * Send a BDPU to the port(target) that this port is connect to. 
    * Start age timer for that BDPU on the port that receives the BDPU.
    * @param sent packet to send to the connect port(target)
    */
   public void sendBPDU(BPDU sent)
   {
      if (connected != null)
      {
         connected.storedBPDU = sent;
         connected.maxAgeTimer();
      }
   }
   
   public BPDU getStoredBPDU()
   {
      return storedBPDU;
   }
   
   public void setInterfaceNumber(int value)
   {
      number = value; 
   }
   
   public int getInterfaceNumber()
   {
      return number;
   }
   
   /**
    * Connect two Ports. They are bi-directional connections, since there is no 
    * such representation of a one way connection.
    * 
    * @param ingress the Port to be bi-directionally connected.
    */
   public void connectTo(Port ingress)
   {
	   this.connected = ingress;
	   if (ingress != null)
		   ingress.connected = this;
   }
   
   /**
    * 
    * @return the connected switchport
    */
   public Port getConnected()
   {
	   return connected;
   }

   
   public String getSenderID()
   {
      return this.senderID;
   }
   
   public int getRootPathCost()
   {
      return rootPathCost;
   }
   
   public int getPathCost()
   {
      return storedBPDU.getCost();
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
   
}
