/*    */ package mallorcatour.util;
/*    */ 
/*    */ public class DoubleUtils
/*    */ {
/*    */   private static final double TEN = 10.0D;
/*    */ 
/*    */   public static double digitsAfterComma(double value, int digitsAfterComma)
/*    */   {
/* 16 */     double powerOfTen = Math.pow(10.0D, digitsAfterComma);
/* 17 */     double result = (int)(value * powerOfTen) / powerOfTen;
/* 18 */     return result;
/*    */   }
/*    */ }

/* Location:           /home/andriipanasiuk/workspace-private/mallorcatour/lib/Utils.jar
 * Qualified Name:     mallorcatour.util.DoubleUtils
 * JD-Core Version:    0.6.2
 */