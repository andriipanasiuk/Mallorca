/*    */ package mallorcatour.game.core;
/*    */ 
/*    */ import java.io.Serializable;
/*    */ 
/*    */ public class PokerStreet
/*    */   implements Serializable
/*    */ {
/*    */   private static final long serialVersionUID = 1L;
/*    */   private String name;
/*    */   private int intValue;
/* 23 */   public static final PokerStreet PREFLOP = new PokerStreet("preflop", 0);
/* 24 */   public static final PokerStreet FLOP = new PokerStreet("flop", 1);
/* 25 */   public static final PokerStreet TURN = new PokerStreet("turn", 2);
/* 26 */   public static final PokerStreet RIVER = new PokerStreet("river", 3);
/*    */ 
/*    */   private PokerStreet(String name, int intValue)
/*    */   {
/* 20 */     this.name = name;
/* 21 */     this.intValue = intValue;
/*    */   }
/*    */ 
/*    */   public static PokerStreet valueOf(int intValue)
/*    */   {
/* 29 */     switch (intValue) {
/*    */     case 0:
/* 31 */       return PREFLOP;
/*    */     case 1:
/* 33 */       return FLOP;
/*    */     case 2:
/* 35 */       return TURN;
/*    */     case 3:
/* 37 */       return RIVER;
/*    */     }
/* 39 */     throw new IllegalArgumentException("Illegal int value of street; only 0, 1, 2, 3 are legal");
/*    */   }
/*    */ 
/*    */   public String toString()
/*    */   {
/* 47 */     return this.name;
/*    */   }
/*    */ 
/*    */   public int intValue() {
/* 51 */     return this.intValue;
/*    */   }
/*    */ 
/*    */   public boolean equals(Object obj)
/*    */   {
/* 56 */     if (obj != this) {
/* 57 */       return false;
/*    */     }
/* 59 */     return true;
/*    */   }
/*    */ 
/*    */   public int hashCode()
/*    */   {
/* 64 */     int hash = 7;
/* 65 */     hash = 43 * hash + this.name.hashCode();
/* 66 */     return hash;
/*    */   }
/*    */ }

/* Location:           /home/andriipanasiuk/workspace-private/mallorcatour/lib/MallorcaResources.jar
 * Qualified Name:     mallorcatour.game.core.PokerStreet
 * JD-Core Version:    0.6.2
 */