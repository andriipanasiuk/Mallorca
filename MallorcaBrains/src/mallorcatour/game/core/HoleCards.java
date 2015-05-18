/*    */ package mallorcatour.game.core;
/*    */ 
/*    */ import java.io.Serializable;
/*    */ 
/*    */ public class HoleCards
/*    */   implements Serializable
/*    */ {
/*    */   private static final long serialVersionUID = -5889114050917400932L;
/*    */   public Card first;
/*    */   public Card second;
/*    */ 
/*    */   public HoleCards(Card card1, Card card2)
/*    */   {
/* 19 */     if (card1.compareTo(card2) == 0) {
/* 20 */       throw new IllegalArgumentException("Cards must not be equals!");
/*    */     }
/* 22 */     this.first = card1;
/* 23 */     this.second = card2;
/*    */   }
/*    */ 
/*    */   public static HoleCards valueOf(String holeCards) {
/* 27 */     Card c1 = Card.valueOf(holeCards.substring(0, 2));
/* 28 */     Card c2 = Card.valueOf(holeCards.substring(2, 4));
/* 29 */     return new HoleCards(c1, c2);
/*    */   }
/*    */ 
/*    */   public boolean hasCard(Card card) {
/* 33 */     return (this.first.equals(card)) || (this.second.equals(card));
/*    */   }
/*    */ 
/*    */   public boolean isSuited() {
/* 37 */     return this.first.isSuitedWith(this.second);
/*    */   }
/*    */ 
/*    */   public boolean isRanked() {
/* 41 */     return this.first.getValue() == this.second.getValue();
/*    */   }
/*    */ 
/*    */   public String toString()
/*    */   {
/* 46 */     return this.second.toString() + " " + this.first.toString();
/*    */   }
/*    */ 
/*    */   public boolean equals(Object obj)
/*    */   {
/* 51 */     if (!(obj instanceof HoleCards)) {
/* 52 */       return false;
/*    */     }
/* 54 */     HoleCards other = (HoleCards)obj;
/* 55 */     if (hashCode() != other.hashCode()) {
/* 56 */       return false;
/*    */     }
/* 58 */     return true;
/*    */   }
/*    */ 
/*    */   public int hashCodeForValues() {
/* 62 */     if (this.first.compareTo(this.second) > 0) {
/* 63 */       return this.first.getValue().intValue() * 13 + this.second.getValue().intValue();
/*    */     }
/* 65 */     return this.second.getValue().intValue() * 13 + this.first.getValue().intValue();
/*    */   }
/*    */ 
/*    */   public static int hashCodeForValues(Card card1, Card card2)
/*    */   {
/* 70 */     if (card1.compareTo(card2) > 0) {
/* 71 */       return card1.getValue().intValue() * 13 + card2.getValue().intValue();
/*    */     }
/* 73 */     return card2.getValue().intValue() * 13 + card1.getValue().intValue();
/*    */   }
/*    */ 
/*    */   public int hashCode()
/*    */   {
/* 79 */     if (this.first.compareTo(this.second) > 0) {
/* 80 */       return this.first.intValue() * 70 + this.second.intValue();
/*    */     }
/* 82 */     return this.second.intValue() * 70 + this.first.intValue();
/*    */   }
/*    */ }

/* Location:           /home/andriipanasiuk/workspace-private/mallorcatour/lib/MallorcaResources.jar
 * Qualified Name:     mallorcatour.game.core.HoleCards
 * JD-Core Version:    0.6.2
 */