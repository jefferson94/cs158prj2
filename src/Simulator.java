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

   private static final int ADDLINK = 1;
   private static final int ADDNODE = 2;
   private static final int DELLINK = 3;
   private static final int DELNODE = 4;
   private static final int EXIT_PROG = 5;
   
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
      System.out.println("Links in the topology (sorted by bridge MAC)");
      
      for (int i = 0; i < edges.size() ; i++) 
         System.out.println((i + 1) + ". " + (edges.get(i)).toString());

      System.out.println("Done with topology construction.\nConverging...");
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
            addToTopology(cmd);
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
      for(Bridge x : nodes)
         x.refresh();
      
      long currentTime = System.currentTimeMillis();
      for(Bridge b : nodes)
         b.run();
      
      while(!allConverge())
      {
         display();
      }

      display();
      long elapseTime = (System.currentTimeMillis() - currentTime);

      for(Bridge b : nodes)
         b.stopTimers();

      System.out.println("Nodes: " + nodes.size());
      System.out.println("Edges: " + edges.size());
      System.out.println("Convergence Time: " + (elapseTime + 20000) + " ms");
   }
   
   public void display()
   {
      for(Bridge b : nodes)
         System.out.println(b);
   }

    private void addToTopology(Command cmd) {
        Bridge source = findSwitch(cmd.getOriginMAC());
        if (source == null) {
            source = new Bridge(cmd.getOriginMAC());
            nodes.add(source);
        }

        Bridge target = findSwitch(cmd.getTargetMAC());
        if (target == null) {
            target = new Bridge(cmd.getTargetMAC());
            nodes.add(target);
        }

        int srcPort = cmd.getOrignPortNumber();
        int dstPort = cmd.getTargetPortNumber();

        // create the edge between the switches
        Edge temp = new Edge(source, target, srcPort, dstPort);

        // make sure the two random switches are not the same switch (loop to self).
        // make sure there's not already an edge between switches
        if ((!source.equals(target)) && (!edges.contains(temp))) {
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

    private void editTopology(int action) {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String input = "";
        try {
            if(action == DELLINK){
                System.out.println("   Format: bridge port1");
                System.out.print("   Bridge to edit: ");
            } else if (action == DELNODE) {
                System.out.print("   Bridge to delete: ");
            } else {
                System.out.println("   Format: bridge1 port1 bridge2 port2");
                System.out.print("   Bridge to edit: ");
            }
            input = br.readLine();
        } catch (IOException ioe) {
            System.out.println("Error reading input");
            System.exit(1);
        }

        // add link or node to topology
        if (action != DELNODE) {
            Command cmd = new Command();
            cmd.parse(input);

            if ((action == ADDLINK) ||(action == ADDNODE)) {
               addToTopology(cmd);
            } else {
               // delete link
               Bridge origin = findSwitch(cmd.getOriginMAC());
               if (origin == null) {
                  System.out.println("Can not find bridge with Id: " + cmd.getOriginMAC());
               } else {
                  int index = nodes.indexOf(origin);
                  nodes.get(index).disablePort(cmd.getOrignPortNumber());

               }
            }

        } else {
            // delete node
           Bridge origin = findSwitch(input);
           if (origin == null) {
              System.out.println("Can not find bridge with Id: " + input);
           } else {
              int index = nodes.indexOf(origin);

              for(int i = 0; i < nodes.get(index).numberOfPorts(); i++)
                 nodes.get(index).disablePort(i);

           }
        }
    }

    public static void main(String[] args)
    {
        Simulator demo = new Simulator();
        demo.processFile(args[0]);
        demo.displayTopologyLink();
        demo.run();

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String input = "";
        while (input.compareTo(String.valueOf(EXIT_PROG)) != 0) {
            try {
                System.out.println("[1]Add Link\n[2]Add Node\n[3]Delete Link\n[4]Delete Node\n[5]Exit");
                System.out.print("Enter command: ");
                input = br.readLine();
            } catch (IOException ioe) {
                System.out.println("Error reading input");
                System.exit(1);
            }
            int opt = Integer.parseInt(input);
            switch (opt) {
                case 1:
                    demo.editTopology(ADDLINK);
                    break;
                case 2:
                    demo.editTopology(ADDNODE);
                    break;
                case 3:
                    demo.editTopology(DELLINK);
                    break;
                case 4:
                    demo.editTopology(DELNODE);
                    break;
                default:
                    //System.out.println("Goodbye");
                    break;
            }
            if (opt != EXIT_PROG) {
                demo.displayTopologyLink();
                demo.run();
            }
        }
    }
}