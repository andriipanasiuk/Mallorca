/*     */ package mallorcatour.util;
/*     */ 
/*     */ import java.awt.AWTException;
/*     */ import java.awt.Point;
/*     */ import java.awt.Rectangle;
/*     */ import java.awt.Robot;
/*     */ import java.awt.event.KeyEvent;
/*     */ import java.lang.reflect.Field;
/*     */ 
/*     */ public class RobotUtils
/*     */ {
/*     */   private static Robot robot;
/*     */ 
/*     */   private static void initRobot()
/*     */   {
/*  28 */     if (robot == null)
/*     */       try {
/*  30 */         robot = new Robot();
/*     */       } catch (AWTException ex) {
/*  32 */         throw new RuntimeException(ex);
/*     */       }
/*     */   }
/*     */ 
/*     */   public static void pressLeftMouse(int delay, int x, int y)
/*     */   {
/*  45 */     robot.delay(delay);
/*  46 */     robot.mouseMove(x, y);
/*  47 */     robot.mousePress(16);
/*  48 */     robot.delay(400);
/*  49 */     robot.mouseRelease(16);
/*  50 */     Log.d("pressMouse() x: " + x + " y: " + y);
/*     */   }
/*     */ 
/*     */   public static void pressRightMouse(int delay, int x, int y) {
/*  54 */     robot.delay(delay);
/*  55 */     robot.mouseMove(x, y);
/*  56 */     robot.mousePress(8);
/*  57 */     robot.mouseRelease(8);
/*  58 */     Log.d("pressMouse() x: " + x + " y: " + y);
/*     */   }
/*     */ 
/*     */   public static void pressMouse(int delay) {
/*  62 */     robot.delay(delay);
/*  63 */     robot.mousePress(16);
/*  64 */     robot.mouseRelease(16);
/*     */   }
/*     */ 
/*     */   public static void pressMouse() {
/*  68 */     pressMouse(0);
/*     */   }
/*     */ 
/*     */   public static void clickButton(String buttonLetter) {
/*  72 */     Log.d("Press button() button: " + buttonLetter);
/*  73 */     if (buttonLetter.length() != 1) {
/*  74 */       throw new IllegalArgumentException("Button must be only letter on the keyboard");
/*     */     }
/*  76 */     Field keyCodeField = null;
/*  77 */     int keyCode = 0;
/*     */     try {
/*  79 */       keyCodeField = KeyEvent.class.getField("VK_" + buttonLetter.toUpperCase());
/*  80 */       keyCode = keyCodeField.getInt(null);
/*     */     }
/*     */     catch (NoSuchFieldException ex) {
/*  83 */       throw new RuntimeException(ex);
/*     */     } catch (SecurityException ex) {
/*  85 */       throw new RuntimeException(ex);
/*     */     } catch (IllegalArgumentException ex) {
/*  87 */       throw new RuntimeException(ex);
/*     */     } catch (IllegalAccessException ex) {
/*  89 */       throw new RuntimeException(ex);
/*     */     }
/*  91 */     clickButton(keyCode);
/*     */   }
/*     */ 
/*     */   public static void clickButton(int keyCode) {
/*  95 */     clickButton(keyCode, 60);
/*     */   }
/*     */ 
/*     */   public static void clickButton(int keyCode, int delay) {
/*  99 */     robot.keyPress(keyCode);
/* 100 */     robot.delay(delay);
/* 101 */     robot.keyRelease(keyCode);
/*     */   }
/*     */ 
/*     */   public static void pressButton(int keyCode) {
/* 105 */     robot.keyPress(keyCode);
/*     */   }
/*     */ 
/*     */   public static void releaseButton(int keyCode) {
/* 109 */     robot.keyRelease(keyCode);
/*     */   }
/*     */ 
/*     */   public static boolean pressMouseOnImage(int delay, String path, Rectangle rect) {
/* 113 */     Point p = ImageUtils.isOnScreen(path, rect);
/* 114 */     if (p != null) {
/* 115 */       pressLeftMouse(delay, rect.x + p.x + 1, rect.y + p.y + 1);
/* 116 */       return true;
/*     */     }
/* 118 */     return false;
/*     */   }
/*     */ 
/*     */   public static boolean pressMouseOnImage(String path, Rectangle rect)
/*     */   {
/* 123 */     return pressMouseOnImage(0, path, rect);
/*     */   }
/*     */ 
/*     */   public static boolean pressMouseOnImage(String path) {
/* 127 */     return pressMouseOnImage(0, path, ImageUtils.screenRectangle);
/*     */   }
/*     */ 
/*     */   public static void delay(int ms) {
/* 131 */     robot.delay(ms);
/*     */   }
/*     */ 
/*     */   public static synchronized void moveMouse(int x, int y, IMouseMover mouseMover) {
/* 135 */     mouseMover.moveTo(robot, new Point(x, y));
/*     */   }
/*     */ 
/*     */   static
/*     */   {
/*  24 */     initRobot();
/*     */   }
/*     */ }

/* Location:           /home/andriipanasiuk/workspace-private/mallorcatour/lib/Utils.jar
 * Qualified Name:     mallorcatour.util.RobotUtils
 * JD-Core Version:    0.6.2
 */