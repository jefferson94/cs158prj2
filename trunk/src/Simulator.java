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
   /**
    * Create a Simulator with a network topology. The topology is a weighted simple 
    * undirected graph data structure. 
    */
   public Simulator()
   {
     topology = new SimpleWeightedGraph<Switch, DefaultEdge>(DefaultEdge.class);
   }
   	
   /**
    * Process a topology file and creates a network topology based on it. 
    * @param filename of the topology input file. 
    */
   public void processFile(String filename)
   {
      try
      {
         FileReader r = new FileReader(filename);
         Scanner in = new Scanner(r);
         
 
         while (in.hasNextLine())
         {
            System.out.println("Call cmd.parse(in) now!");
            Command cmd = new Command();
            cmd.parse(in.nextLine());
            
            Switch source = (findSwitch(cmd.getMacID()) != null) ? findSwitch(cmd.getMacID()) :
               addSwitch(cmd.getMacID());
            
            for(String macID : cmd.getConnectedSwitches())
               addLink(source, macID);
         }
      }
      catch (FileNotFoundException ex)
      {
         System.out.println("Reader/parser error, file probably not found");
      }
   }
	
   /**
    * Get the current topology of the network as string. First index being the list of 
    * all the vertices/nodes. The rest are the the edges in tuple pair.
    * @return the current topology of the network as a string.
    */
   public String getTopology()
   {
      return topology.toString();
   }
   
   /**
    * Adds a switch/node to the current network topology.
    * @param macID the id to assign to the switch.
    * @return the newly added switch.
    */
   private Switch addSwitch(String macID)
   {
      Switch node = new Switch();
      node.setMacID(macID);
      // Will not recreate any switches already existing in the topology.
      topology.addVertex(node);
      return node;
   }
   
   /**
    * Finds a switch based on the MAC id assigned to it.
    * @param targetMacID string representation of the switch MAC id to find within the 
    * current network topology.
    * @return the switch with the targetMacID.
    */
   private Switch findSwitch(String targetMacID)
   {
      for(Switch temp : topology.vertexSet())
      {
         if(temp.getMac().compareTo(targetMacID) == 0)
            return temp;
      }
      return null;
   }
   
   /**
    * Add a link(or an edge) to a pair of switches in the current network topology.
    * If the destinationMacID doesn't have a switch already created, one will be created for that 
    * MAC id and added to the network topology.
    * @param origin the switch pair to connect to (ie. <v1, v2>, v1 in this representation).
    * @param destinationMacID string MAC id of the switch to connect to. (v2 from the previous
    * example).
    */
   private void addLink(Switch origin, String destinationMacID)
   {  
      Switch target = (findSwitch(destinationMacID) != null) ? findSwitch(destinationMacID) :
         addSwitch(destinationMacID);
      
      // Create the linkage in the topology.
      // Will not recreate any new links if link already exist between the two switches.
      if(topology.addEdge(origin, target) != null)
      {
         // Create the linkage between switches first for the origin port to the 
         // destination, then destination to the origin.
         origin.addPort(new Port(Port.LISTENING, target));
         target.addPort(new Port(Port.LISTENING, origin));
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
	   
	   System.out.println("\nTopology output:");
	   System.out.println(demo.getTopology());
	}
	
	private UndirectedGraph<Switch, DefaultEdge> topology;

}
