/*    */ package mallorcatour.game.core;
/*    */ 
/*    */ import java.util.ArrayList;
/*    */ import java.util.Collections;
/*    */ import java.util.Iterator;
/*    */ import java.util.List;
/*    */ 
/*    */ public class Deck
/*    */ {
/* 25 */   private static List<Card> allDeck = Collections.unmodifiableList(initCards());
/*    */   private static int[] intDeck;
/*    */   private static int[] intBrecherDeck;
/*    */   private List<Card> currentDeck;
/*    */ 
/*    */   private static List<Card> initCards()
/*    */   {
/* 30 */     List result = new ArrayList();
/* 31 */     for (Iterator i$ = Card.Value.getValues().iterator(); i$.hasNext(); ) { value = (Card.Value)i$.next();
/* 32 */       for (Card.Suit suit : Card.Suit.getSuits())
/* 33 */         result.add(new Card(value, suit));
/*    */     }
/*    */     Card.Value value;
/* 36 */     return result;
/*    */   }
/*    */ 
/*    */   public static int[] getIntCards() {
/* 40 */     return intDeck;
/*    */   }
/*    */ 
/*    */   public static int[] getIntCardsForBrecher() {
/* 44 */     return intBrecherDeck;
/*    */   }
/*    */ 
/*    */   private static void initIntDeck() {
/* 48 */     intDeck = new int[allDeck.size()];
/* 49 */     intBrecherDeck = new int[allDeck.size()];
/* 50 */     for (int i = 0; i < allDeck.size(); i++) {
/* 51 */       intDeck[i] = ((Card)allDeck.get(i)).intValue();
/* 52 */       intBrecherDeck[i] = ((Card)allDeck.get(i)).intValue();
/*    */     }
/*    */   }
/*    */ 
/*    */   public static List<Card> getCards() {
/* 57 */     return allDeck;
/*    */   }
/*    */ 
/*    */   public void refresh() {
/* 61 */     this.currentDeck = new ArrayList(allDeck);
/*    */   }
/*    */ 
/*    */   public void shuffle() {
/* 65 */     Collections.shuffle(this.currentDeck);
/*    */   }
/*    */ 
/*    */   static
/*    */   {
/* 26 */     initIntDeck();
/*    */   }
/*    */ }

/* Location:           /home/andriipanasiuk/workspace-private/mallorcatour/lib/MallorcaResources.jar
 * Qualified Name:     mallorcatour.game.core.Deck
 * JD-Core Version:    0.6.2
 */