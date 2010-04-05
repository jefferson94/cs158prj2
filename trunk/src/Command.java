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
   private String switchMAC;
   private ArrayList<String> connectedSwitch;

   /**
    * Construct a command object containing the Switch MAC id and its connected 
    * Switched MAC ids. 
    */
   public Command()
   {
      switchMAC = "";
      connectedSwitch = new ArrayList<String>();
   }

   /**
    * Get the MAC id of Switch from the string being parsed. 
    * @return the switch MAC id
    */
   public String getMacID()
   {
      return switchMAC;
   }

   /**
    * Get an ArrayList of MAC ids that are connected to the switch.
    * @return ArrayList of the MAC ids of other switches that are connected 
    * to the switch.
    */
   public ArrayList<String> getConnectedSwitches()
   {
      return connectedSwitch;
   }

   /**
    * Parse a String a determine the switch MAC id and their associated 
    * connected switches' MAC id. Used to create the network topology from a string.
    * @param input is String to be parsed to obtain network topology.
    */
   public void parse(String input)
   {
      int i = 0;      
      StringTokenizer st = new StringTokenizer(input);
      
      switchMAC = st.nextToken();
      //System.out.println("MACID: " + switchMAC);
      
      while (st.hasMoreTokens()) {
         connectedSwitch.add(st.nextToken());
         //System.out.println("Connected to #" + (i+1) + ": " + connectedSwitch.get(i));
         i++;
      }
      
      //System.out.println("parse: done");
   }
}


