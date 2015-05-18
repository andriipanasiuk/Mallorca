/*    */ package mallorcatour.util;
/*    */ 
/*    */ public class ThreadUtils
/*    */ {
/*    */   public static void sleep(long millis)
/*    */   {
/*    */     try
/*    */     {
/* 15 */       Thread.sleep(millis);
/*    */     } catch (InterruptedException ex) {
/* 17 */       throw new RuntimeException(ex);
/*    */     }
/*    */   }
/*    */ }

/* Location:           /home/andriipanasiuk/workspace-private/mallorcatour/lib/Utils.jar
 * Qualified Name:     mallorcatour.util.ThreadUtils
 * JD-Core Version:    0.6.2
 */