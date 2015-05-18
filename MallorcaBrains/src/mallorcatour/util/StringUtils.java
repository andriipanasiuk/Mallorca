/*    */ package mallorcatour.util;
/*    */ 
/*    */ public class StringUtils
/*    */ {
/*    */   public static String between(String find, String string1, String string2)
/*    */   {
/* 14 */     int from = find.indexOf(string1) + string1.length();
/* 15 */     return between(find, from, string2);
/*    */   }
/*    */ 
/*    */   public static String between(String find, int indexFrom, String string2) {
/* 19 */     int from = indexFrom;
/* 20 */     int to = find.indexOf(string2, from);
/* 21 */     if (to == -1) {
/* 22 */       return find.substring(from);
/*    */     }
/* 24 */     return find.substring(from, to);
/*    */   }
/*    */ 
/*    */   public static int[] parseIntArray(String string, String separator)
/*    */   {
/* 29 */     String[] array = string.split(separator);
/* 30 */     int[] result = new int[array.length];
/* 31 */     int i = 0;
/* 32 */     for (String intValue : array) {
/* 33 */       result[(i++)] = Integer.parseInt(intValue);
/*    */     }
/* 35 */     return result;
/*    */   }
/*    */ }

/* Location:           /home/andriipanasiuk/workspace-private/mallorcatour/lib/Utils.jar
 * Qualified Name:     mallorcatour.util.StringUtils
 * JD-Core Version:    0.6.2
 */