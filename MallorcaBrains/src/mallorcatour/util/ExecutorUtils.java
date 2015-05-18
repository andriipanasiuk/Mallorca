/*    */ package mallorcatour.util;
/*    */ 
/*    */ import java.util.concurrent.BlockingQueue;
/*    */ import java.util.concurrent.ExecutorService;
/*    */ import java.util.concurrent.Future;
/*    */ import java.util.concurrent.LinkedBlockingQueue;
/*    */ import java.util.concurrent.ThreadPoolExecutor;
/*    */ import java.util.concurrent.TimeUnit;
/*    */ 
/*    */ public class ExecutorUtils
/*    */ {
/*    */   public static ExecutorService newSingleThreadExecutor(OnExceptionListener listener)
/*    */   {
/* 20 */     return new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue())
/*    */     {
/*    */       public Future<?> submit(final Runnable task)
/*    */       {
/* 26 */         Runnable toRun = new Runnable()
/*    */         {
/*    */           public void run() {
/*    */             try {
/* 30 */               task.run();
/*    */             } catch (Exception ex) {
/* 32 */               ex.printStackTrace(System.out);
/* 33 */               ExecutorUtils.1.this.val$listener.onException(ex);
/*    */             }
/*    */           }
/*    */         };
/* 37 */         return super.submit(toRun);
/*    */       }
/*    */     };
/*    */   }
/*    */ }

/* Location:           /home/andriipanasiuk/workspace-private/mallorcatour/lib/Utils.jar
 * Qualified Name:     mallorcatour.util.ExecutorUtils
 * JD-Core Version:    0.6.2
 */