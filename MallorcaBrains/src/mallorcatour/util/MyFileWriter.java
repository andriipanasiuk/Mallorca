/*    */ package mallorcatour.util;
/*    */ 
/*    */ import java.io.FileWriter;
/*    */ import java.io.IOException;
/*    */ 
/*    */ public class MyFileWriter
/*    */ {
/*    */   private FileWriter fileWriter;
/*    */ 
/*    */   private MyFileWriter(String path, boolean append)
/*    */   {
/*    */     try
/*    */     {
/* 20 */       this.fileWriter = new FileWriter(path, append);
/*    */     } catch (IOException ex) {
/* 22 */       throw new RuntimeException(ex);
/*    */     }
/*    */   }
/*    */ 
/*    */   public static MyFileWriter prepareForWriting(String path, boolean append) {
/* 27 */     return new MyFileWriter(path, append);
/*    */   }
/*    */ 
/*    */   public void addToFile(String addString, boolean toNewLine) {
/* 31 */     if (this.fileWriter == null)
/* 32 */       throw new IllegalArgumentException("You need prepare file to writing first!");
/*    */     try
/*    */     {
/* 35 */       if (toNewLine)
/* 36 */         this.fileWriter.append("\n" + addString);
/*    */       else
/* 38 */         this.fileWriter.append(addString);
/*    */     }
/*    */     catch (IOException ex) {
/* 41 */       throw new RuntimeException(ex);
/*    */     }
/*    */   }
/*    */ 
/*    */   public void endWriting() {
/*    */     try {
/* 47 */       this.fileWriter.close();
/*    */     } catch (IOException ex) {
/* 49 */       throw new RuntimeException(ex);
/*    */     }
/*    */   }
/*    */ }

/* Location:           /home/andriipanasiuk/workspace-private/mallorcatour/lib/Utils.jar
 * Qualified Name:     mallorcatour.util.MyFileWriter
 * JD-Core Version:    0.6.2
 */