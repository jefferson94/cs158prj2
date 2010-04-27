public class Edge implements Comparable<Edge>
{
   private Bridge target;
   private Bridge origin;
   
   public Edge()
   {
      this(null, null);
   }
   
   public Edge(Bridge o, Bridge t)
   {
      origin = o;
      target = t;
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
      return origin.getMacID() + " connected to " + target.getMacID();
   }

   public int compareTo(Edge e) {
       return (this.origin.getMacID()).compareTo(e.origin.getMacID());
   }
}
