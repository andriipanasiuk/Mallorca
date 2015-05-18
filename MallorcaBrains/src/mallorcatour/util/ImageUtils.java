/*     */ package mallorcatour.util;
/*     */ 
/*     */ import java.awt.AWTException;
/*     */ import java.awt.Point;
/*     */ import java.awt.Rectangle;
/*     */ import java.awt.Robot;
/*     */ import java.awt.image.BufferedImage;
/*     */ import java.awt.image.ColorModel;
/*     */ import java.awt.image.WritableRaster;
/*     */ import java.io.File;
/*     */ import java.io.IOException;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ import javax.imageio.ImageIO;
/*     */ 
/*     */ public class ImageUtils
/*     */ {
/*     */   private static final String DEBUG_DIRECTORY = "temp";
/*  27 */   public static final Rectangle screenRectangle = new Rectangle(1280, 800);
/*     */ 
/*     */   public static Point isPartOf(BufferedImage allImage, BufferedImage partImage)
/*     */   {
/*  47 */     return isPartOf(allImage, partImage, IImageProcessor.EmptyProcessor);
/*     */   }
/*     */ 
/*     */   public static Point isPartOf(BufferedImage allImage, BufferedImage partImage, IImageProcessor preprocess)
/*     */   {
/*  52 */     preprocess.processImage(allImage);
/*  53 */     preprocess.processImage(partImage);
/*     */ 
/*  55 */     int allWidth = allImage.getWidth();
/*  56 */     int allHeight = allImage.getHeight();
/*  57 */     int partWidth = partImage.getWidth();
/*  58 */     int partHeight = partImage.getHeight();
/*     */ 
/*  60 */     for (int allHeightIndex = 0; (allHeightIndex < allHeight) && 
/*  61 */       (allHeightIndex + partHeight <= allHeight); allHeightIndex++)
/*     */     {
/*  65 */       label164: for (int allWidthIndex = 0; (allWidthIndex < allWidth) && 
/*  66 */         (allWidthIndex + partWidth <= allWidth); allWidthIndex++)
/*     */       {
/*  69 */         for (int partWidthIndex = 0; partWidthIndex < partWidth; partWidthIndex++)
/*     */         {
/*     */           int i;
/*  70 */           for (int partHeightIndex = 0; partHeightIndex < partHeight; partHeightIndex++)
/*     */           {
/*  72 */             int allColor = allImage.getRGB(allWidthIndex + partWidthIndex, allHeightIndex + partHeightIndex);
/*  73 */             int partColor = partImage.getRGB(partWidthIndex, partHeightIndex);
/*  74 */             if (allColor != partColor) {
/*     */               break label164;
/*     */             }
/*  77 */             i = 0;
/*     */           }
/*     */         }
/*     */ 
/*  81 */         return new Point(allWidthIndex, allHeightIndex);
/*     */       }
/*     */     }
/*  84 */     return null;
/*     */   }
/*     */ 
/*     */   public static Point isPartOf(BufferedImage allImage, BufferedImage partImage, Rectangle rect)
/*     */   {
/*  89 */     return isPartOf(allImage.getSubimage(rect.x, rect.y, rect.width, rect.height), partImage);
/*     */   }
/*     */ 
/*     */   public static BufferedImage fromFile(String filename)
/*     */   {
/*     */     try {
/*  95 */       return ImageIO.read(new File(filename));
/*     */     } catch (IOException ex) {
/*  97 */       throw new RuntimeException("Some problem with file: " + filename, ex);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void toFile(BufferedImage image, String filename, boolean debug) {
/* 102 */     if (image == null) {
/* 103 */       throw new IllegalArgumentException("ImageUtils cannot write null image to file");
/*     */     }
/* 105 */     File toFile = null;
/* 106 */     if (debug) {
/* 107 */       File tempDir = new File("temp");
/* 108 */       tempDir.mkdir();
/* 109 */       toFile = new File("temp/" + filename);
/*     */     } else {
/* 111 */       toFile = new File(filename);
/*     */     }
/*     */     try {
/* 114 */       ImageIO.write(image, "png", toFile);
/*     */     } catch (IOException ex) {
/* 116 */       throw new RuntimeException(ex);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static List<Point> getAllEntries(BufferedImage allImage, BufferedImage partImage) {
/* 121 */     List result = new ArrayList();
/* 122 */     Point p = new Point(0, 0);
/* 123 */     BufferedImage imageForSearchIn = allImage;
/*     */     while (true) {
/* 125 */       int width = imageForSearchIn.getWidth();
/* 126 */       int height = imageForSearchIn.getHeight();
/* 127 */       p = isPartOf(imageForSearchIn, partImage);
/* 128 */       if (p == null) {
/*     */         break;
/*     */       }
/* 131 */       result.add(p);
/* 132 */       imageForSearchIn.setRGB(p.x, p.y, partImage.getRGB(0, 0) + 1);
/* 133 */       imageForSearchIn = imageForSearchIn.getSubimage(0, p.y, width, height - p.y);
/*     */     }
/* 135 */     return result;
/*     */   }
/*     */ 
/*     */   public static Point isOnScreen(String path) {
/* 139 */     return isOnScreen(fromFile(path));
/*     */   }
/*     */ 
/*     */   public static Point isOnScreen(String path, Rectangle rect) {
/* 143 */     Point p = isOnScreen(fromFile(path), rect);
/* 144 */     return p;
/*     */   }
/*     */ 
/*     */   public static Point isOnScreen(BufferedImage partImage, Rectangle rect) {
/* 148 */     BufferedImage screenImage = getScreenCapture(rect);
/*     */ 
/* 150 */     return isPartOf(screenImage, partImage);
/*     */   }
/*     */ 
/*     */   public static Point isOnScreen(BufferedImage partImage) {
/* 154 */     return isOnScreen(partImage, screenRectangle);
/*     */   }
/*     */ 
/*     */   public static BufferedImage getScreenCapture(Rectangle rect) {
/*     */     BufferedImage screenImage;
/*     */     try {
/* 160 */       screenImage = new Robot().createScreenCapture(rect);
/*     */     } catch (AWTException ex) {
/* 162 */       throw new RuntimeException(ex);
/*     */     }
/* 164 */     return screenImage;
/*     */   }
/*     */ 
/*     */   public static BufferedImage getScreenCapture() {
/*     */     BufferedImage screenImage;
/*     */     try {
/* 170 */       screenImage = new Robot().createScreenCapture(screenRectangle);
/*     */     } catch (AWTException ex) {
/* 172 */       throw new RuntimeException(ex);
/*     */     }
/* 174 */     return screenImage;
/*     */   }
/*     */ 
/*     */   public static boolean waitForImage(int delay, String path) {
/* 178 */     return waitForImage(delay, path, 2147483647);
/*     */   }
/*     */ 
/*     */   public static boolean waitForImage(int delay, String path, Rectangle rect) {
/* 182 */     return waitForImage(delay, path, 2147483647, rect);
/*     */   }
/*     */ 
/*     */   public static boolean waitForImage(int delay, String path, int timeout, Rectangle rect) {
/* 186 */     Point p = isOnScreen(path, rect);
/* 187 */     int waitingTime = 0;
/* 188 */     while (p == null) {
/* 189 */       if (waitingTime > timeout)
/* 190 */         return false;
/*     */       try
/*     */       {
/* 193 */         Thread.sleep(delay);
/*     */       } catch (InterruptedException ex) {
/* 195 */         throw new RuntimeException(ex);
/*     */       }
/* 197 */       waitingTime += delay;
/* 198 */       p = isOnScreen(path);
/*     */     }
/* 200 */     return true;
/*     */   }
/*     */ 
/*     */   public static boolean waitForImage(int delay, String path, int timeout) {
/* 204 */     return waitForImage(delay, path, timeout, screenRectangle);
/*     */   }
/*     */ 
/*     */   public static void fillByColor(BufferedImage image, int color) {
/* 208 */     for (int i = 0; i < image.getWidth(); i++)
/* 209 */       for (int j = 0; j < image.getHeight(); j++)
/* 210 */         image.setRGB(i, j, color);
/*     */   }
/*     */ 
/*     */   public static BufferedImage getSubimage(BufferedImage allImage, Rectangle rect)
/*     */   {
/* 217 */     BufferedImage result = allImage.getSubimage(rect.x, rect.y, rect.width, rect.height);
/* 218 */     return result;
/*     */   }
/*     */ 
/*     */   public static BufferedImage createImage(BufferedImage bi) {
/* 222 */     ColorModel cm = bi.getColorModel();
/* 223 */     boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
/* 224 */     WritableRaster raster = bi.copyData(null);
/* 225 */     BufferedImage result = new BufferedImage(cm, raster, isAlphaPremultiplied, null);
/* 226 */     return result;
/*     */   }
/*     */ 
/*     */   public static abstract interface SearchedImage
/*     */   {
/*     */     public abstract BufferedImage getSearchedImage();
/*     */ 
/*     */     public abstract Rectangle getRectangle();
/*     */   }
/*     */ }

/* Location:           /home/andriipanasiuk/workspace-private/mallorcatour/lib/Utils.jar
 * Qualified Name:     mallorcatour.util.ImageUtils
 * JD-Core Version:    0.6.2
 */