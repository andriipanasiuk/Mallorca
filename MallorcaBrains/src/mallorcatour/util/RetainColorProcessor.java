/*    */ package mallorcatour.util;
/*    */ 
/*    */ import java.awt.Color;
/*    */ import java.awt.image.BufferedImage;
/*    */ 
/*    */ public class RetainColorProcessor
/*    */   implements IImageProcessor
/*    */ {
/* 16 */   private static final Color DEFAULT_COLOR = Color.BLACK;
/*    */   private final Color color;
/*    */   private final Color bgColor;
/*    */ 
/*    */   public RetainColorProcessor(Color colorToRetain, Color bgColor)
/*    */   {
/* 21 */     this.color = colorToRetain;
/* 22 */     this.bgColor = bgColor;
/*    */   }
/*    */ 
/*    */   public void processImage(BufferedImage image) {
/* 26 */     for (int x = 0; x < image.getWidth(); x++)
/* 27 */       for (int y = 0; y < image.getHeight(); y++)
/* 28 */         if (this.color.getRGB() != image.getRGB(x, y))
/* 29 */           image.setRGB(x, y, this.bgColor.getRGB());
/*    */   }
/*    */ }

/* Location:           /home/andriipanasiuk/workspace-private/mallorcatour/lib/Utils.jar
 * Qualified Name:     mallorcatour.util.RetainColorProcessor
 * JD-Core Version:    0.6.2
 */