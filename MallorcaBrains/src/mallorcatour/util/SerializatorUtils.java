/*    */ package mallorcatour.util;
/*    */ 
/*    */ import java.io.BufferedInputStream;
/*    */ import java.io.BufferedOutputStream;
/*    */ import java.io.File;
/*    */ import java.io.FileInputStream;
/*    */ import java.io.FileNotFoundException;
/*    */ import java.io.FileOutputStream;
/*    */ import java.io.IOException;
/*    */ import java.io.InputStream;
/*    */ import java.io.ObjectInputStream;
/*    */ import java.io.ObjectOutputStream;
/*    */ import java.io.Serializable;
/*    */ 
/*    */ public class SerializatorUtils
/*    */ {
/*    */   public static void save(String filePath, Serializable object)
/*    */   {
/* 32 */     ObjectOutputStream out = null;
/*    */     try {
/* 34 */       File file = new File(filePath);
/* 35 */       if (file.exists()) {
/* 36 */         file.delete();
/*    */       }
/* 38 */       out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
/* 39 */       out.writeObject(object);
/* 40 */       out.flush();
/*    */     } catch (IOException ioe) {
/* 42 */       throw new RuntimeException(ioe);
/*    */     } finally {
/* 44 */       if (out != null)
/*    */         try {
/* 46 */           out.close();
/*    */         } catch (IOException e) {
/* 48 */           throw new RuntimeException(e);
/*    */         }
/*    */     }
/*    */   }
/*    */ 
/*    */   public static <T extends Serializable> T load(String filePath, Class<T> clazz)
/*    */   {
/* 61 */     ObjectInputStream oistream = null;
/*    */     try {
/* 63 */       File file = new File(filePath);
/* 64 */       if (!file.exists()) {
/* 65 */         throw new FileNotFoundException("Cannot find file: " + filePath);
/*    */       }
/*    */ 
/* 68 */       oistream = new ObjectInputStream(new BufferedInputStream(new FileInputStream(filePath)));
/* 69 */       Serializable result = (Serializable)oistream.readObject();
/* 70 */       return result;
/*    */     } catch (IOException ioe) {
/* 72 */       throw new RuntimeException(ioe);
/*    */     } catch (ClassNotFoundException cnfe) {
/* 74 */       throw new RuntimeException(cnfe);
/*    */     } finally {
/* 76 */       if (oistream != null)
/*    */         try {
/* 78 */           oistream.close();
/*    */         } catch (IOException ioe) {
/* 80 */           throw new RuntimeException(ioe);
/*    */         }
/*    */     }
/*    */   }
/*    */ 
/*    */   public static <T extends Serializable> T load(InputStream stream, Class<T> clazz)
/*    */   {
/* 93 */     ObjectInputStream oistream = null;
/*    */     try {
/* 95 */       oistream = new ObjectInputStream(stream);
/* 96 */       Serializable result = (Serializable)oistream.readObject();
/* 97 */       return result;
/*    */     } catch (IOException ioe) {
/* 99 */       throw new RuntimeException(ioe);
/*    */     } catch (ClassNotFoundException cnfe) {
/* 101 */       throw new RuntimeException(cnfe);
/*    */     } finally {
/* 103 */       if (oistream != null)
/*    */         try {
/* 105 */           oistream.close();
/*    */         } catch (IOException ioe) {
/* 107 */           throw new RuntimeException(ioe);
/*    */         }
/*    */     }
/*    */   }
/*    */ }

/* Location:           /home/andriipanasiuk/workspace-private/mallorcatour/lib/Utils.jar
 * Qualified Name:     mallorcatour.util.SerializatorUtils
 * JD-Core Version:    0.6.2
 */