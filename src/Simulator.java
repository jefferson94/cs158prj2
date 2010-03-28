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
 * @version 0.1 April 5, 2010
 *
 */
public class Simulator 
{
	public static void main(String[] args)
	{
		if (args.length != 2)
		{
			System.out.println("Usage: simulator switches links");
			System.exit(1);
		}
		switches = Integer.parseInt(args[0]);
		links = Integer.parseInt(args[1]);
		ArrayList<Switch> topology = buildTopology(switches, links);
		while(!topology.get(0).isConverged())
		{
			for (int i = 0; i < topology.size(); i++)
			{
				Switch current = topology.get(i);
				current.incrementClock();
			}
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
	
	private static int switches;
	private static int links;
}
