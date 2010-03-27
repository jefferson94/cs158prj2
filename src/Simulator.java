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
			topology.add(new Switch());
		}
		ListIterator<Switch> it1 = topology.listIterator();
		ListIterator<Switch> it2 = topology.listIterator();
		for (int i = 0; i < links; i++)
		{
			if (it1.hasNext())
			{
				it1.next();
				if (it1.hasNext())
				{
					Switch sw1 = it1.next();
					if (it2.hasNext())
					{
						it2.next().connectLink(sw1);
					} else reset(it2);
				} else reset(it1);
			} else
			{
				reset(it1);
			}
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
