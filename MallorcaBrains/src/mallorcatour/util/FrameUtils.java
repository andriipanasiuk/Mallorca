/*    */ package mallorcatour.util;
/*    */ 
/*    */ import java.awt.Component;
/*    */ import java.io.File;
/*    */ import javax.swing.JFileChooser;
/*    */ 
/*    */ public class FrameUtils
/*    */ {
/*    */   public static String openFileChooser(Component parent, String curDirectory)
/*    */   {
/* 18 */     JFileChooser chooser = new JFileChooser(curDirectory);
/* 19 */     int returnVal = chooser.showOpenDialog(parent);
/* 20 */     if (returnVal == 0) {
/* 21 */       return chooser.getSelectedFile().getAbsolutePath();
/*    */     }
/* 23 */     return null;
/*    */   }
/*    */ 
/*    */   public static String openDirectoryChooser(Component parent, String curDirectory) {
/* 27 */     JFileChooser chooser = new JFileChooser(curDirectory);
/* 28 */     chooser.setFileSelectionMode(1);
/* 29 */     int returnVal = chooser.showOpenDialog(parent);
/* 30 */     if (returnVal == 0) {
/* 31 */       return chooser.getCurrentDirectory().getAbsolutePath();
/*    */     }
/* 33 */     return null;
/*    */   }
/*    */ 
/*    */   public static String openFileChooser(Component parent) {
/* 37 */     JFileChooser chooser = new JFileChooser();
/* 38 */     int returnVal = chooser.showOpenDialog(parent);
/* 39 */     if (returnVal == 0) {
/* 40 */       return chooser.getSelectedFile().getAbsolutePath();
/*    */     }
/* 42 */     return null;
/*    */   }
/*    */ 
/*    */   public static String[] openFileChooserForMultipleFiles(Component parent, String curDirectory)
/*    */   {
/* 47 */     JFileChooser chooser = new JFileChooser(curDirectory);
/* 48 */     chooser.setMultiSelectionEnabled(true);
/* 49 */     int returnVal = chooser.showOpenDialog(parent);
/* 50 */     if (returnVal == 0) {
/* 51 */       File[] files = chooser.getSelectedFiles();
/* 52 */       String[] result = new String[files.length];
/* 53 */       for (int i = 0; i < files.length; i++) {
/* 54 */         result[i] = files[i].getAbsolutePath();
/*    */       }
/* 56 */       return result;
/*    */     }
/* 58 */     return null;
/*    */   }
/*    */ }

/* Location:           /home/andriipanasiuk/workspace-private/mallorcatour/lib/Utils.jar
 * Qualified Name:     mallorcatour.util.FrameUtils
 * JD-Core Version:    0.6.2
 */