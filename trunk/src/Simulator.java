/**
 * A Spanning Tree Protocol simulator. It builds a random topology with the 
 * number of switches and links specified in the command line. The switches 
 * then perform the STP algorithm. When they have converged, the simulator 
 * should print the states of every switchport. Later implementations will 
 * include random dropped links and RSTP. Further revisions may include MST, 
 * PVST+, Portfast with BPDU Guard, Uplinkfast, and/or Backbonefast.
 * 
 * @author John Le Mieux
 * @author Christopher Trinh
 * @author Peter Le
 * @version 0.1 April 5, 2010
 *
 */

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import org.jgrapht.*;
import org.jgrapht.graph.*;

public class Simulator 
{
   
   public Simulator()
   {
     topology = new SimpleWeightedGraph<Switch, DefaultEdge>(DefaultEdge.class);
   }
   	
   /**
    * Process a command file and execute the commands.
    * @param inFile a File object, that contains the commands to execute.
    */
   public void processFile(String filename)
   {
      try
      {
         FileReader r = new FileReader(filename);
         Scanner in = new Scanner(r);
         
 
         while (in.hasNextLine())
         {
            System.out.println("Call cmd.parse(in)  now!");
            Command cmd = new Command();
            cmd.parse(in.nextLine());
            
            Switch origin = new Switch();
            origin.setMacID(cmd.getMacID());
            // Will not recreate any switches already existing in the topology.
            topology.addVertex(origin);
            
            for(String macID : cmd.getConnectedSwitches())
            {
               Switch destination = new Switch();
               destination.setMacID(macID);
               // Will not recreate any switches already existing in the topology.
               topology.addVertex(destination);
           
               // Create the linkage in the topology.
               // Will not recreate any new links if link already exist between the two switches.
               if(topology.addEdge(origin, destination) != null)
               {
                  // Create the linkage between switches first for the origin port to the 
                  // destination, then destination to the origin.
                  origin.addPort(new Port(Port.LISTENING, destination));
                  destination.addPort(new Port(Port.LISTENING, origin));
               }
            }
         }
      }
      catch (FileNotFoundException ex)
      {
         System.out.println("Reader/parser error, file probably not found");
      }
   }
	
	
	/**
	 * Builds a random topology graph based on command line user preferences.
	 * 
	 * @param switches the number of switches in the topology, specified in the 
	 * first command line argument
	 * @param links the number of links (segments, extended LANs, cables) in 
	 * the topology, specified in the second command line argument
	 * @return an ArrayList of Switches, each with a MAC address and some 
	 * number of links connecting it to other switches
	 */
	private static ArrayList<Switch> buildTopology(int switches, int links)
	{
		ArrayList<Switch> topology = new ArrayList<Switch>();
		for (int i = 0; i < switches; i++)
		{
			String mac = Long.toHexString((new Random()).nextLong());
			int length = mac.length();
			if (length < 12)
			{
				for (int j = 0; j < 12 - length; j++)
				{
					mac = "0" + mac;
				}
			} else if (length > 12)
				mac = mac.substring(0, 12);
			mac = mac.substring(0, 4) + "." + mac.substring(4, 8) + "." + mac.substring(8);
			topology.add(new Switch(0, 0, new ArrayList<Port>(), mac));
		}
		for (int i = 0; i < links; i++)
		{
			topology.get((new Random().nextInt(switches))).addPort(new Port(Port.BLOCKING, topology.get((new Random()).nextInt(switches))));
		}
		return topology;
	}
	
	
	public static void main(String[] args)
	{
	   Simulator demo = new Simulator();
	   demo.processFile(args[0]);
	}
	
	private UndirectedGraph<Switch, DefaultEdge> topology;

}
