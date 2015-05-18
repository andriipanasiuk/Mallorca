/*    */ package mallorcatour.util;
/*    */ 
/*    */ import java.awt.image.BufferedImage;
/*    */ 
/*    */ public abstract interface IImageProcessor
/*    */ {
/* 15 */   public static final IImageProcessor EmptyProcessor = new IImageProcessor() {
/* 15 */     public void processImage(BufferedImage image) {  }  } ;
/*    */ 
/*    */   public abstract void processImage(BufferedImage paramBufferedImage);
/*    */ }

/* Location:           /home/andriipanasiuk/workspace-private/mallorcatour/lib/Utils.jar
 * Qualified Name:     mallorcatour.util.IImageProcessor
 * JD-Core Version:    0.6.2
 */