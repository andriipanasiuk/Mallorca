/*    */ package mallorcatour.util;
/*    */ 
/*    */ import java.util.Comparator;
/*    */ 
/*    */ public class CompareUtils
/*    */ {
/*    */   public static Comparator<Object> reverse(Comparator<Object> comparator)
/*    */   {
/* 16 */     return new Comparator()
/*    */     {
/*    */       public int compare(Object o1, Object o2) {
/* 19 */         return this.val$comparator.compare(o2, o1);
/*    */       }
/*    */     };
/*    */   }
/*    */ }

/* Location:           /home/andriipanasiuk/workspace-private/mallorcatour/lib/Utils.jar
 * Qualified Name:     mallorcatour.util.CompareUtils
 * JD-Core Version:    0.6.2
 */