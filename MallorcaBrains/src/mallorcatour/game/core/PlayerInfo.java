/*    */ package mallorcatour.game.core;
/*    */ 
/*    */ import java.io.Serializable;
/*    */ 
/*    */ public class PlayerInfo
/*    */   implements Serializable
/*    */ {
/*    */   private static final long serialVersionUID = -4125178498692602233L;
/*    */   private String name;
/*    */   private double stack;
/*    */   private Card holeCard1;
/*    */   private Card holeCard2;
/*    */   private boolean isSittingOut;
/*    */ 
/*    */   public PlayerInfo(String name, double stack, Card holeCard1, Card holeCard2)
/*    */   {
/* 22 */     this.name = name;
/* 23 */     this.stack = stack;
/* 24 */     this.holeCard1 = holeCard1;
/* 25 */     this.holeCard2 = holeCard2;
/*    */   }
/*    */ 
/*    */   public PlayerInfo(String name, double stack, boolean isSittingOut) {
/* 29 */     this.name = name;
/* 30 */     this.stack = stack;
/* 31 */     this.isSittingOut = isSittingOut;
/*    */   }
/*    */ 
/*    */   public PlayerInfo(String name, double stack) {
/* 35 */     this(name, stack, false);
/*    */   }
/*    */ 
/*    */   public void setHoleCards(Card first, Card second) {
/* 39 */     this.holeCard1 = first;
/* 40 */     this.holeCard2 = second;
/*    */   }
/*    */ 
/*    */   public boolean isSittingOut() {
/* 44 */     return this.isSittingOut;
/*    */   }
/*    */ 
/*    */   public double getStack() {
/* 48 */     return this.stack;
/*    */   }
/*    */ 
/*    */   public String getName() {
/* 52 */     return this.name;
/*    */   }
/*    */ 
/*    */   public HoleCards getHoleCards() {
/* 56 */     if ((this.holeCard1 != null) && (this.holeCard2 != null)) {
/* 57 */       return new HoleCards(this.holeCard1, this.holeCard2);
/*    */     }
/* 59 */     return null;
/*    */   }
/*    */ }

/* Location:           /home/andriipanasiuk/workspace-private/mallorcatour/lib/MallorcaResources.jar
 * Qualified Name:     mallorcatour.game.core.PlayerInfo
 * JD-Core Version:    0.6.2
 */