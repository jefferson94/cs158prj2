import java.io.*;
import java.util.*;

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
   private ArrayList<Bridge> nodes;
   private ArrayList<Edge> edges;
   
   
   public Simulator()
   {
      nodes = new ArrayList<Bridge>();
      edges = new ArrayList<Edge>();
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

      System.out.println("Done with topology construction.\n");
   }

   
   /**
    * Finds a switch based on the MAC id assigned to it.
    * @param targetMacID string representation of the switch MAC id to find within the 
    * current network topology.
    * @return the switch with the targetMacID.
    */
   public Bridge findSwitch(String targetMacID)
   {
      for(Bridge temp : nodes)
      {
         if(temp.getMacID().compareTo(targetMacID) == 0)
            return temp;
      }
      return null;
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
            
            
            Bridge source = findSwitch(cmd.getOriginMAC());
            if(source == null)
            {
               source = new Bridge(cmd.getOriginMAC());
               nodes.add(source);
            }
            
            Bridge target = findSwitch(cmd.getTargetMAC());
            if (target == null)
            {
                target = new Bridge(cmd.getTargetMAC());
                nodes.add(target);
            }
            
            // create the edge between the switches
            Edge temp = new Edge(source, target);

            // make sure the two random switches are not the same switch (loop to self).
            // make sure there's not already an edge between switches
            if((!source.equals(target)) && (!edges.contains(temp)))
            {
               // add to array list
               edges.add(temp);
               
               // Create the linkage between switches first for the origin port to the 
               // destination, then destination to the origin.
               Port sourcePort = new Port(cmd.getOrignPortNumber());
               Port targetPort = new Port(cmd.getTargetPortNumber());
               source.addPort(sourcePort);
               target.addPort(targetPort);
               sourcePort.connectTo(targetPort);
            }
         }
      }
      catch (FileNotFoundException ex)
      {
         System.out.println("Reader/parser error, file probably not found");
      }
   }
   
   public boolean allConverge()
   {
      for(Bridge b : nodes)
      {
         if(!b.isConverged())
            return false;
      }
      return true;
   }
   

   public void run()
   {
      long currentTime = System.currentTimeMillis();
      for(Bridge b : nodes)
         b.run();
      
      while(!allConverge())
      {
         display();
      }
      
      System.out.println("Convergence Time: " + ((System.currentTimeMillis() - currentTime)  / 1000));
      
      for(Bridge b : nodes)
         b.stopTimers();
   }
   
   public void display()
   {
      for(Bridge b : nodes)
         System.out.println(b);
   }
   
	public static void main(String[] args)
	{
	   Simulator demo = new Simulator();
	   BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
	   if (args.length > 0)
		   demo.processFile(args[0]);
	   else
	   {
		   System.out.println("Enter a filename");
		   try
		   {
			   demo.processFile(reader.readLine());
		   }
		   catch (IOException ioe)
		   {
			   System.out.println("Could not read filename");
			   System.exit(1);
		   }
	   }
	   demo.displayTopologyLink();
	   boolean quit = false;
	   do
	   {
		   demo.run();
		   System.out.println("(b)reak a link or (q)uit");
		   try
		   {
			   String choice = reader.readLine();
			   if (choice.equals("q"))
				   quit = true;
			   else if (choice.equals("b"))
			   {
				   System.out.println("Choose a bridge");
			   }
		   }
		   catch (IOException ioe)
		   {
			   System.out.println("Could not read input");
			   System.exit(1);
		   }
	   } while (!quit);
//	   System.exit(0);
	}
}