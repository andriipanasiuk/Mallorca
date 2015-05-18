/*    */ package mallorcatour.util;
/*    */ 
/*    */ import java.awt.Point;
/*    */ import java.awt.Rectangle;
/*    */ 
/*    */ public class RecognizerUtils
/*    */ {
/*    */   public static Rectangle getGlobalRectangle(Rectangle localRectangle, Point topLeftPosition)
/*    */   {
/* 18 */     if (topLeftPosition == null) {
/* 19 */       throw new NullPointerException();
/*    */     }
/* 21 */     Rectangle result = new Rectangle(localRectangle);
/* 22 */     result.translate(topLeftPosition.x, topLeftPosition.y);
/* 23 */     return result;
/*    */   }
/*    */ }

/* Location:           /home/andriipanasiuk/workspace-private/mallorcatour/lib/Utils.jar
 * Qualified Name:     mallorcatour.util.RecognizerUtils
 * JD-Core Version:    0.6.2
 */