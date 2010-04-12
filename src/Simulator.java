import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.Collections;

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
 * @version 0.2 April 12, 2010
 *
 */
public class Simulator 
{
   private ArrayList<Switch> nodes;
   private ArrayList<Edge> edges;
   
   /**
    * Create a Simulator with a network topology. The topology is a weighted simple 
    * undirected graph data structure. 
    */
   public Simulator()
   {
      nodes = new ArrayList<Switch>();
      edges = new ArrayList<Edge>();
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
            Command cmd = new Command();
            cmd.parse(in.nextLine());
            
            
            Switch source = findSwitch(cmd.getMacID());
            if(source == null)
            {
               source = new Switch();
               source.setMacID(cmd.getMacID());
               nodes.add(source);
            }
                        
            for(String macID : cmd.getConnectedSwitches())
            {
               Switch target = findSwitch(macID);
               if (target == null)
               {
            	   target = new Switch();
            	   target.setMacID(macID);
            	   nodes.add(target);
               }

               createTopologyLink(source, target);
            }
         }
         
         displayTopologyLink();
      }
      catch (FileNotFoundException ex)
      {
         System.out.println("Reader/parser error, file probably not found");
      }
   }
	
   
   /**
    * Finds a switch based on the MAC id assigned to it.
    * @param targetMacID string representation of the switch MAC id to find within the 
    * current network topology.
    * @return the switch with the targetMacID.
    */
   private Switch findSwitch(String targetMacID)
   {
      for(Switch temp : nodes)
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
   private void addLink(Switch origin, Switch target)
   {  
         // Create the linkage between switches first for the origin port to the 
         // destination, then destination to the origin.
         Port egress = new Port();
         Port ingress = new Port();
         origin.addPort(egress);
         target.addPort(ingress);
         egress.connectTo(ingress);
         System.out.println(origin.getMac() + " " + target.getMac());
   }
   
   /**
    * Create a link between two switches. In terms of graphs it creates an edge
    * between two vertices.
    * @param s1 first switch/origin node for connection
    * @param s2 second switch/target node for connection
    * @return false if s1 and s2 are the same switch and if there already exist 
    *    an between s1 and s2, otherwise create edge and link and return true.
    */
   private boolean createTopologyLink(Switch s1, Switch s2)
   {
      // create the edge between the switches
      Edge e1 = new Edge(s1,s2);

      // make sure the two random switches are not the same switch (loop to self)
      if(s1.getMac().compareTo(s2.getMac()) == 0)
         return false;
      // make sure there's not already an edge between switches
      else if (edges.contains(e1))
         return false;
      else
      {
         // add to array list
         edges.add(e1);
         addLink(s1, s2);
         return true;
      }
   }
	
   /**
    * Display the all links(edges in terms of graphs) in the current topology.
    */
   private void displayTopologyLink()
   {
      // sort the edges by MAC for easier reading
      Collections.sort(edges);
      System.out.println("Links in the topology (sorted by switch MAC)");
      
      for (int i = 0; i < edges.size() ; i++) 
         System.out.println((i + 1) + ". " + (edges.get(i)).toString());

      System.out.println("Done with topology construction");
   }
   
	/**
	 * Builds a random topology graph based on command line user preferences.
	 * 
	 * @param switchAmount the number of switches in the topology, specified in the 
	 * first command line argument
	 * @param linkAmount the number of links (segments, extended LANs, cables) in 
	 * the topology, specified in the second command line argument
	 * @return an ArrayList of Switches, each with a MAC address and some 
	 * number of links connecting it to other switches
	 */
	public void buildTopology(int switchAmount, int linkAmount)
	{
		for (int i = 0; i < switchAmount; i++)
		{
			String mac = Long.toHexString((new Random()).nextLong());
			int length = mac.length();
			if (length < 12)
			{
				for (int j = 0; j < 12 - length; j++)
				{
					mac = "0" + mac;
				}
			} 
			else if (length > 12)
				mac = mac.substring(0, 12);
			mac = mac.substring(0, 4) + "." + mac.substring(4, 8) + "." + mac.substring(8);
			if (!nodes.contains(findSwitch(mac)))
				nodes.add(new Switch(0, 0, new ArrayList<Port>(), mac));
			else
				i--;
		}
		for (int i = 0; i < linkAmount; i++)
		{
		   // get 2 random switches to build a link between
		   Switch s1 = nodes.get(new Random().nextInt(nodes.size()));
         Switch s2 = nodes.get(new Random().nextInt(nodes.size()));
         
         while(!createTopologyLink(s1, s2))
         {
            s1 = nodes.get(new Random().nextInt(nodes.size()));
            s2 = nodes.get(new Random().nextInt(nodes.size()));
         }     
		}

		//displayTopologyLink();
	}
	
	/**
	 * Check if all the switches in the topology is converged.
	 * This means that all ports are either BLOCKING or FORWARDING.
	 *
	 * @return true if all switches' ports are blocking or forwarding, 
	 *   false otherwise.
	 */
	public boolean isConverged()
	{
		for (Switch s : nodes)
		{
                    // sometimes never gets out of this loop
			if (!s.isConverged())
				return false;
		}
		return true;
	}
	
	/**
	 * Get a list of all the switches in the current topology.
	 * @return an array list of all the switches in the current topology.
	 */
   public ArrayList<Switch> getSwitches()
   {
      return nodes;
   }
	
	public static void main(String[] args)
	{
	   Simulator demo = new Simulator();
	   if (args.length == 1)
		   demo.processFile(args[0]);
	   else if (args.length == 2)
		   demo.buildTopology(Integer.valueOf(args[0]), Integer.valueOf(args[1]));
	   else
	   {
		   System.out.println("Usage: simulator filename");
		   System.out.println("       simulator switches links");
		   System.exit(0);
	   }
	   
	   for (int i = 0; i < 15; i++)
	   {
		   while (!demo.isConverged())
		   {
			   for (Switch s : demo.getSwitches())
			   {
				   //s.printState();
				   s.incrementClock();
			   }
		   }
		   //System.out.println("CONVERGED!@!@!@!");
		   for (Switch s : demo.getSwitches())
		   {
			   s.printState();
		   }
		   // randomly break link
		   int broken;
		   Switch b;
		   do
		   {
			   b = demo.nodes.get(new Random().nextInt(demo.nodes.size()));
			   broken = b.breakLink();
		   } while (broken == -1);
		   System.out.println("Interface " + broken + " on Switch " + b.getMac() + " is disabled.");
		   for (int j = 0; j < Switch.AGE_TIMER; j++)
			   for (Switch s : demo.getSwitches())
				   s.incrementClock();
	   }
	}
}