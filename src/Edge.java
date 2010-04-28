public class Edge implements Comparable<Edge>
{
   private Bridge target;
   private Bridge origin;
   private int targetPort;
   private int originPort;

   public Edge()
   {
      this(null, null, -1, -1);
   }
   
   public Edge(Bridge o, Bridge t, int op, int tp)
   {
      origin = o;
      target = t;
      originPort = op;
      targetPort = tp;
   }

   public boolean equals(Object other)
   {
      if(!(other instanceof Edge))
         return false;
      else
      {
         Edge e = (Edge)other;
         if(((this.origin == e.origin) || (this.origin == e.target)) && 
            ((this.target == e.target) || (this.target == e.origin)))
            return true;
         else
            return false;
      }
   }
   
   public String toString()
   {
      return origin.getMacID() + " (" + originPort + ") connected to " + target.getMacID() + " (" + targetPort + ")";
   }

   public int compareTo(Edge e) {
       return (this.origin.getMacID()).compareTo(e.origin.getMacID());
   }
}
