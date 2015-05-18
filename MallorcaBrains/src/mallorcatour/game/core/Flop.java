/*    */ package mallorcatour.game.core;
/*    */ 
/*    */ public class Flop
/*    */ {
/*    */   public Card first;
/*    */   public Card second;
/*    */   public Card third;
/*    */ 
/*    */   public Flop(Card flop1, Card flop2, Card flop3)
/*    */   {
/* 14 */     this.first = flop1;
/* 15 */     this.second = flop2;
/* 16 */     this.third = flop3;
/*    */   }
/*    */ 
/*    */   public Card[] toArray()
/*    */   {
/* 22 */     return new Card[] { this.first, this.second, this.third };
/*    */   }
/*    */ }

/* Location:           /home/andriipanasiuk/workspace-private/mallorcatour/lib/MallorcaResources.jar
 * Qualified Name:     mallorcatour.game.core.Flop
 * JD-Core Version:    0.6.2
 */