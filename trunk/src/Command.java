/**
 * Class used to parse the commands given by a string. Used to create a network topology.
 * @author Christoper Trinh
 * @version 1.0 2010/2/14
 */

import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;

public class Command
{  
   private String switchMAC;
   private ArrayList<String> connectedSwitch;

   /**
    * Construct a command object containing the command name and its' associated
    * parameters. 
    */
   public Command()
   {
      switchMAC = "";
      connectedSwitch = new ArrayList<String>();
   }

   public String getMacID()
   {
      return switchMAC;
   }

   public ArrayList<String> getConnectedSwitches()
   {
      return connectedSwitch;
   }

   /**
    * Parse a String a determine the command and their associated parameters.
    * @param in
    */
   public void parse(String input)
   {
      int i = 0;      
      StringTokenizer st = new StringTokenizer(input);
      
      switchMAC = st.nextToken();
      System.out.println("MACID: " + switchMAC);
      
      while (st.hasMoreTokens()) {
         connectedSwitch.add(st.nextToken());
         System.out.println("Connected to #" + (i+1) + ": " + connectedSwitch.get(i));
         i++;
      }
      
      System.out.println("parse: done");
   }
}


