/*    */ package mallorcatour.util;
/*    */ 
/*    */ public abstract interface OnExceptionListener
/*    */ {
/* 13 */   public static final OnExceptionListener EMPTY = new OnExceptionListener() {
/* 13 */     public void onException(Exception e) {  }  } ;
/*    */ 
/*    */   public abstract void onException(Exception paramException);
/*    */ }

/* Location:           /home/andriipanasiuk/workspace-private/mallorcatour/lib/Utils.jar
 * Qualified Name:     mallorcatour.util.OnExceptionListener
 * JD-Core Version:    0.6.2
 */