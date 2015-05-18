/*    */ package mallorcatour.util;
/*    */ 
/*    */ import java.io.BufferedReader;
/*    */ import java.io.FileNotFoundException;
/*    */ import java.io.FileReader;
/*    */ import java.io.IOException;
/*    */ import java.io.InputStream;
/*    */ import java.io.InputStreamReader;
/*    */ import java.util.ArrayList;
/*    */ import java.util.List;
/*    */ 
/*    */ public class ReaderUtils
/*    */ {
/*    */   public static List<String> readLines(BufferedReader reader, int lineCount)
/*    */   {
/* 24 */     List result = new ArrayList();
/* 25 */     for (int i = 0; i < lineCount; i++) {
/* 26 */       result.add(readLine(reader));
/*    */     }
/* 28 */     return result;
/*    */   }
/*    */ 
/*    */   public static void skipLines(BufferedReader reader, int lineCount) {
/* 32 */     for (int i = 0; i < lineCount; i++)
/* 33 */       readLine(reader);
/*    */   }
/*    */ 
/*    */   public static String readLine(BufferedReader reader)
/*    */   {
/* 38 */     String result = null;
/*    */     try {
/* 40 */       result = reader.readLine();
/*    */     } catch (IOException ex) {
/* 42 */       throw new RuntimeException(ex);
/*    */     }
/* 44 */     return result;
/*    */   }
/*    */ 
/*    */   public static BufferedReader initReader(String filename) {
/* 48 */     BufferedReader reader = null;
/*    */     try {
/* 50 */       reader = new BufferedReader(new FileReader(filename));
/*    */     } catch (FileNotFoundException ex) {
/* 52 */       throw new RuntimeException(ex);
/*    */     }
/* 54 */     return reader;
/*    */   }
/*    */ 
/*    */   public static BufferedReader initReader(InputStream stream) {
/* 58 */     BufferedReader reader = null;
/* 59 */     reader = new BufferedReader(new InputStreamReader(stream));
/* 60 */     return reader;
/*    */   }
/*    */ }

/* Location:           /home/andriipanasiuk/workspace-private/mallorcatour/lib/Utils.jar
 * Qualified Name:     mallorcatour.util.ReaderUtils
 * JD-Core Version:    0.6.2
 */