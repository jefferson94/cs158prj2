/**
 * A way to keep track of what interfaces were disabled in STP so they can be 
 * recreated in RSTP.
 * 
 * @author John Le Mieux
 * @version 1.0 April 26, 2010
 *
 */
public class Disabled 
{
	/**
	 * Initializes a new disabled interface for storage. Call this constructor 
	 * as the interface goes down and keep it in an ArrayList to preserve 
	 * ordering.
	 * 
	 * @param s the Switch containing the interface
	 * @param p the interface that is disabled
	 */
	public Disabled(Switch s, int p)
	{
		this.s = s;
		this.p = p;
	}
	
	public Switch getSwitch()
	{
		return s;
	}
	
	public int getPort()
	{
		return p;
	}
	
	private Switch s;
	private int p;
}
