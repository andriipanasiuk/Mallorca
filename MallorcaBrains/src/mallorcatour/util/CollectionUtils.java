/*    */ package mallorcatour.util;
/*    */ 
/*    */ import java.util.ArrayList;
/*    */ import java.util.Iterator;
/*    */ import java.util.List;
/*    */ import java.util.Map;
/*    */ import java.util.Map.Entry;
/*    */ 
/*    */ public class CollectionUtils
/*    */ {
/*    */   public static <T> List<T> subtract(List<T> list1, List<T> list2)
/*    */   {
/* 19 */     List result = new ArrayList();
/* 20 */     for (Iterator i$ = list1.iterator(); i$.hasNext(); ) { Object element = i$.next();
/* 21 */       if (!list2.contains(element)) {
/* 22 */         result.add(element);
/*    */       }
/*    */     }
/* 25 */     return result;
/*    */   }
/*    */ 
/*    */   public static <T> List<T> subtract(List<T> list1, T[] list2) {
/* 29 */     List result = new ArrayList();
/* 30 */     for (Iterator i$ = list1.iterator(); i$.hasNext(); ) { Object element = i$.next();
/* 31 */       if (!ArrayUtils.containsElement(list2, element)) {
/* 32 */         result.add(element);
/*    */       }
/*    */     }
/* 35 */     return result;
/*    */   }
/*    */ 
/*    */   public static <T> List<T> subtract(List<T> list1, T removeElement) {
/* 39 */     List result = new ArrayList();
/* 40 */     for (Iterator i$ = list1.iterator(); i$.hasNext(); ) { Object element = i$.next();
/* 41 */       if (!element.equals(removeElement)) {
/* 42 */         result.add(element);
/*    */       }
/*    */     }
/* 45 */     return result;
/*    */   }
/*    */ 
/*    */   public static <T> void removeAll(List<T> list, T element) {
/* 49 */     while (list.contains(element))
/* 50 */       list.remove(element);
/*    */   }
/*    */ 
/*    */   public static <T> double maxValue(Map<T, Double> map)
/*    */   {
/* 55 */     double result = -1.797693134862316E+308D;
/* 56 */     for (Map.Entry entry : map.entrySet()) {
/* 57 */       if (((Double)entry.getValue()).doubleValue() > result) {
/* 58 */         result = ((Double)entry.getValue()).doubleValue();
/*    */       }
/*    */     }
/* 61 */     return result;
/*    */   }
/*    */ }

/* Location:           /home/andriipanasiuk/workspace-private/mallorcatour/lib/Utils.jar
 * Qualified Name:     mallorcatour.util.CollectionUtils
 * JD-Core Version:    0.6.2
 */