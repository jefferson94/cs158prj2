
public class Edge
{
   private Switch target;
   private Switch origin;
   
   public Edge()
   {
      this(null, null);
   }
   
   public Edge(Switch o, Switch t)
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
      return target.toString() + " " + origin.toString();
   }
   
}
