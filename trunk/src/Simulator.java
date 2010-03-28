import java.util.*;

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
		ListIterator<Switch> it1 = topology.listIterator();
		ListIterator<Switch> it2 = topology.listIterator();
		for (int i = 0; i < links; i++)
		{
			topology.get((new Random().nextInt(switches))).addPort(new Port(Port.BLOCKING, topology.get((new Random()).nextInt(switches))));
		}
		return topology;
	}
	
	private static void reset(ListIterator<Switch> it)
	{
		while (it.hasPrevious())
		{
			it.previous();
		}
	}
	
	private static int switches;
	private static int links;
}
