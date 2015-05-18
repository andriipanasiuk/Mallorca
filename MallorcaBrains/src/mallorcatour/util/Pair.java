/*    */ package mallorcatour.util;
/*    */ 
/*    */ import java.io.Serializable;
/*    */ 
/*    */ public class Pair<F extends Serializable, S extends Serializable>
/*    */   implements Serializable
/*    */ {
/*    */   private static final long serialVersionUID = 1L;
/*    */   public F first;
/*    */   public S second;
/*    */ 
/*    */   public Pair(F first, S second)
/*    */   {
/* 20 */     this.first = first;
/* 21 */     this.second = second;
/*    */   }
/*    */ 
/*    */   public String toString()
/*    */   {
/* 26 */     return this.first.toString() + " " + this.second.toString();
/*    */   }
/*    */ }

/* Location:           /home/andriipanasiuk/workspace-private/mallorcatour/lib/Utils.jar
 * Qualified Name:     mallorcatour.util.Pair
 * JD-Core Version:    0.6.2
 */