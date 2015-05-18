/*     */ package mallorcatour.util;
/*     */ 
/*     */ public class ArrayUtils
/*     */ {
/*     */   public static int[] removeAll(int[] array, int elementToRemove)
/*     */   {
/*  14 */     int newCount = array.length;
/*  15 */     for (int value : array) {
/*  16 */       if (elementToRemove == value) {
/*  17 */         newCount--;
/*     */       }
/*     */     }
/*  20 */     int[] result = new int[newCount];
/*  21 */     int i = 0;
/*  22 */     for (int value : array) {
/*  23 */       if (elementToRemove != value) {
/*  24 */         result[(i++)] = value;
/*     */       }
/*     */     }
/*  27 */     return result;
/*     */   }
/*     */ 
/*     */   public static boolean containsElement(int[] array, int element) {
/*  31 */     for (int value : array) {
/*  32 */       if (value == element) {
/*  33 */         return true;
/*     */       }
/*     */     }
/*  36 */     return false;
/*     */   }
/*     */ 
/*     */   public static <T> boolean containsElement(T[] array, T element) {
/*  40 */     for (Object value : array) {
/*  41 */       if (value.equals(element)) {
/*  42 */         return true;
/*     */       }
/*     */     }
/*  45 */     return false;
/*     */   }
/*     */ 
/*     */   public static int[] reverse(int[] array) {
/*  49 */     int length = array.length;
/*  50 */     int[] result = new int[length];
/*  51 */     int i = length - 1;
/*  52 */     for (int value : array) {
/*  53 */       result[(i--)] = value;
/*     */     }
/*  55 */     return result;
/*     */   }
/*     */ 
/*     */   public static void sortAscending(int[] array) {
/*  59 */     sortAscending(array, 0, array.length);
/*     */   }
/*     */ 
/*     */   public static void sortDescendingForShort(int[] array)
/*     */   {
/*  64 */     int len = array.length;
/*  65 */     int off = 0;
/*  66 */     if (len >= 8) {
/*  67 */       throw new IllegalArgumentException();
/*     */     }
/*  69 */     for (int i = off; i < len + off; i++)
/*  70 */       for (int j = i; (j > off) && (array[(j - 1)] < array[j]); j--)
/*  71 */         swap(array, j, j - 1);
/*     */   }
/*     */ 
/*     */   private static void sortAscending(int[] x, int off, int len)
/*     */   {
/*  81 */     if (len < 7) {
/*  82 */       for (int i = off; i < len + off; i++) {
/*  83 */         for (int j = i; (j > off) && (x[(j - 1)] > x[j]); j--) {
/*  84 */           swap(x, j, j - 1);
/*     */         }
/*     */       }
/*  87 */       return;
/*     */     }
/*     */ 
/*  91 */     int m = off + (len >> 1);
/*  92 */     if (len > 7) {
/*  93 */       int l = off;
/*  94 */       int n = off + len - 1;
/*  95 */       if (len > 40) {
/*  96 */         int s = len / 8;
/*  97 */         l = med3(x, l, l + s, l + 2 * s);
/*  98 */         m = med3(x, m - s, m, m + s);
/*  99 */         n = med3(x, n - 2 * s, n - s, n);
/*     */       }
/* 101 */       m = med3(x, l, m, n);
/*     */     }
/* 103 */     int v = x[m];
/*     */ 
/* 106 */     int a = off; int b = a; int c = off + len - 1; int d = c;
/*     */     while (true) {
/* 108 */       if ((b <= c) && (x[b] <= v)) {
/* 109 */         if (x[b] == v) {
/* 110 */           swap(x, a++, b);
/*     */         }
/* 112 */         b++;
/*     */       } else {
/* 114 */         while ((c >= b) && (x[c] >= v)) {
/* 115 */           if (x[c] == v) {
/* 116 */             swap(x, c, d--);
/*     */           }
/* 118 */           c--;
/*     */         }
/* 120 */         if (b > c) {
/*     */           break;
/*     */         }
/* 123 */         swap(x, b++, c--);
/*     */       }
/*     */     }
/*     */ 
/* 127 */     int n = off + len;
/* 128 */     int s = Math.min(a - off, b - a);
/* 129 */     vecswap(x, off, b - s, s);
/* 130 */     s = Math.min(d - c, n - d - 1);
/* 131 */     vecswap(x, b, n - s, s);
/*     */ 
/* 134 */     if ((s = b - a) > 1) {
/* 135 */       sortAscending(x, off, s);
/*     */     }
/* 137 */     if ((s = d - c) > 1)
/* 138 */       sortAscending(x, n - s, s);
/*     */   }
/*     */ 
/*     */   private static void vecswap(int[] x, int a, int b, int n)
/*     */   {
/* 146 */     for (int i = 0; i < n; b++) {
/* 147 */       swap(x, a, b);
/*     */ 
/* 146 */       i++; a++;
/*     */     }
/*     */   }
/*     */ 
/*     */   private static int med3(int[] x, int a, int b, int c)
/*     */   {
/* 155 */     return x[a] > x[c] ? c : x[b] > x[c] ? b : x[a] < x[b] ? a : x[a] < x[c] ? c : x[b] < x[c] ? b : a;
/*     */   }
/*     */ 
/*     */   private static void swap(int[] x, int a, int b)
/*     */   {
/* 164 */     int t = x[a];
/* 165 */     x[a] = x[b];
/* 166 */     x[b] = t;
/*     */   }
/*     */ }

/* Location:           /home/andriipanasiuk/workspace-private/mallorcatour/lib/Utils.jar
 * Qualified Name:     mallorcatour.util.ArrayUtils
 * JD-Core Version:    0.6.2
 */