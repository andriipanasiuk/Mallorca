/*    */ package mallorcatour.util;
/*    */ 
/*    */ import java.io.PrintStream;
/*    */ 
/*    */ public class Log
/*    */ {
/*    */   public static void d(String log)
/*    */   {
/* 14 */     System.out.println(log);
/*    */   }
/*    */ 
/*    */   public static void f(String path, String log)
/*    */   {
/* 19 */     MyFileWriter fileWriter = MyFileWriter.prepareForWriting(path, true);
/* 20 */     fileWriter.addToFile(log, true);
/* 21 */     fileWriter.endWriting();
/*    */   }
/*    */ }

/* Location:           /home/andriipanasiuk/workspace-private/mallorcatour/lib/Utils.jar
 * Qualified Name:     mallorcatour.util.Log
 * JD-Core Version:    0.6.2
 */