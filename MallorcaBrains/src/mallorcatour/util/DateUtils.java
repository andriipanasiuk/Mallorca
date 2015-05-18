/*    */ package mallorcatour.util;
/*    */ 
/*    */ import java.text.DateFormat;
/*    */ import java.text.ParseException;
/*    */ import java.text.SimpleDateFormat;
/*    */ import java.util.Date;
/*    */ import java.util.HashMap;
/*    */ import java.util.Map;
/*    */ 
/*    */ public class DateUtils
/*    */ {
/* 20 */   private static final Map<String, Integer> monthes = new HashMap();
/*    */   private static final String DATE_FORMAT = "yyyy_MM_dd_HH_mm_ss";
/*    */ 
/*    */   public static String getDate(boolean withNano)
/*    */   {
/* 39 */     DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
/* 40 */     Date date = new Date();
/* 41 */     String result = dateFormat.format(date);
/* 42 */     if (withNano) {
/* 43 */       result = result + "_" + System.currentTimeMillis();
/*    */     }
/* 45 */     return result;
/*    */   }
/*    */ 
/*    */   public static Date parsePADate(String date) {
/* 49 */     date = preprocessDate(date);
/* 50 */     SimpleDateFormat format = new SimpleDateFormat("M dd, yyyy - HH:mm:ss");
/* 51 */     Date result = null;
/*    */     try {
/* 53 */       result = format.parse(date);
/*    */     } catch (ParseException ex) {
/* 55 */       throw new RuntimeException(ex);
/*    */     }
/* 57 */     return result;
/*    */   }
/*    */ 
/*    */   private static String preprocessDate(String date) {
/* 61 */     String result = null;
/* 62 */     for (String month : monthes.keySet()) {
/* 63 */       if (date.contains(month)) {
/* 64 */         result = date.replace(month, String.valueOf(monthes.get(month)));
/* 65 */         return result;
/*    */       }
/*    */     }
/* 68 */     throw new RuntimeException("Some problem with month in date");
/*    */   }
/*    */ 
/*    */   public static Date parseDate(String date) {
/* 72 */     SimpleDateFormat format = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
/* 73 */     Date result = null;
/*    */     try {
/* 75 */       result = format.parse(date);
/*    */     } catch (ParseException ex) {
/* 77 */       throw new RuntimeException(ex);
/*    */     }
/* 79 */     return result;
/*    */   }
/*    */ 
/*    */   public static Date parseDate(String date, String dateFormat) {
/* 83 */     SimpleDateFormat format = new SimpleDateFormat(dateFormat);
/* 84 */     Date result = null;
/*    */     try {
/* 86 */       result = format.parse(date);
/*    */     } catch (ParseException ex) {
/* 88 */       throw new RuntimeException(ex);
/*    */     }
/* 90 */     return result;
/*    */   }
/*    */ 
/*    */   public static long difference(Date firstDate, Date secondDate)
/*    */   {
/* 95 */     return Math.abs(firstDate.getTime() - secondDate.getTime());
/*    */   }
/*    */ 
/*    */   static
/*    */   {
/* 24 */     monthes.put("January", Integer.valueOf(1));
/* 25 */     monthes.put("February", Integer.valueOf(2));
/* 26 */     monthes.put("March", Integer.valueOf(3));
/* 27 */     monthes.put("April", Integer.valueOf(4));
/* 28 */     monthes.put("May", Integer.valueOf(5));
/* 29 */     monthes.put("June", Integer.valueOf(6));
/* 30 */     monthes.put("July", Integer.valueOf(7));
/* 31 */     monthes.put("August", Integer.valueOf(8));
/* 32 */     monthes.put("September", Integer.valueOf(9));
/* 33 */     monthes.put("October", Integer.valueOf(10));
/* 34 */     monthes.put("November", Integer.valueOf(11));
/* 35 */     monthes.put("December", Integer.valueOf(12));
/*    */   }
/*    */ }

/* Location:           /home/andriipanasiuk/workspace-private/mallorcatour/lib/Utils.jar
 * Qualified Name:     mallorcatour.util.DateUtils
 * JD-Core Version:    0.6.2
 */