/*    */ package mallorcatour.util;
/*    */ 
/*    */ import java.io.FileWriter;
/*    */ import java.io.IOException;
/*    */ 
/*    */ public class FileUtils
/*    */ {
/*    */   private static FileWriter fileWriter;
/*    */ 
/*    */   public static void prepareForWriting(String path, boolean append)
/*    */   {
/*    */     try
/*    */     {
/* 23 */       fileWriter = new FileWriter(path, append);
/*    */     } catch (IOException ex) {
/* 25 */       throw new RuntimeException(ex);
/*    */     }
/*    */   }
/*    */ 
/*    */   public static void addToFile(String addString, boolean toNewLine) {
/* 30 */     if (fileWriter == null)
/* 31 */       throw new IllegalArgumentException("You need prepare file to writing first!");
/*    */     try
/*    */     {
/* 34 */       if (toNewLine)
/* 35 */         fileWriter.append("\n" + addString);
/*    */       else
/* 37 */         fileWriter.append(addString);
/*    */     }
/*    */     catch (IOException ex) {
/* 40 */       throw new RuntimeException(ex);
/*    */     }
/*    */   }
/*    */ 
/*    */   public static void endWriting() {
/*    */     try {
/* 46 */       fileWriter.close();
/*    */     } catch (IOException ex) {
/* 48 */       throw new RuntimeException(ex);
/*    */     }
/*    */   }
/*    */ }

/* Location:           /home/andriipanasiuk/workspace-private/mallorcatour/lib/Utils.jar
 * Qualified Name:     mallorcatour.util.FileUtils
 * JD-Core Version:    0.6.2
 */