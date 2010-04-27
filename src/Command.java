import java.util.ArrayList;
import java.util.StringTokenizer;
/**
 * Class used to parse the commands given by a string. Used to create a network topology.
 * Format for the string to be parsed should be the following:
 * ie. SwitchMacID SwitchMacID SwitchMacID (ending with /r or /n)
 * 
 * @author Christoper Trinh
 * @version 1.0 2010/2/14
 */
public class Command
{  
   private String bridgeMAC;
   private String connectedMAC;
   private int bridgePortNumber;
   private int connectedPortNumber;

   /**
    * Construct a command object containing the Switch MAC id and its connected 
    * Switched MAC ids. 
    */
   public Command()
   {
      bridgeMAC = "";
      connectedMAC = "";
      bridgePortNumber = -1;
      connectedPortNumber = -1;
   }

   /**
    * Get the MAC id of Switch from the string being parsed. 
    * @return the switch MAC id
    */
   public String getOriginMAC()
   {
      return bridgeMAC;
   }

   /**
    * Get an ArrayList of MAC ids that are connected to the switch.
    * @return ArrayList of the MAC ids of other switches that are connected 
    * to the switch.
    */
   public String getTargetMAC()
   {
      return connectedMAC;
   }

   
   public int getOrignPortNumber()
   {
      return bridgePortNumber;
   }
   
   public int getTargetPortNumber()
   {
      return connectedPortNumber;
   }
   
   /**
    * Parse a String a determine the switch MAC id and their associated 
    * connected switches' MAC id. Used to create the network topology from a string.
    * @param input is String to be parsed to obtain network topology.
    */
   public void parse(String input)
   {
      StringTokenizer st = new StringTokenizer(input);
      
      int i = 0;
      while (st.hasMoreTokens()) 
      {
         if(i == 0)
            bridgeMAC = st.nextToken();
         else if(i == 1)
            bridgePortNumber = Integer.parseInt(st.nextToken());
         else if(i == 2)
            connectedMAC = st.nextToken();
         else
            connectedPortNumber = Integer.parseInt(st.nextToken());    
         i++;
      }
   }
}


