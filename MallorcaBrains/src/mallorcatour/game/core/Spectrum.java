/*     */ package mallorcatour.game.core;
/*     */ 
/*     */ import java.io.Serializable;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Collections;
/*     */ import java.util.Comparator;
/*     */ import java.util.HashMap;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Map.Entry;
/*     */ import java.util.Set;
/*     */ 
/*     */ public class Spectrum
/*     */   implements Iterable<HoleCards>, Serializable
/*     */ {
/*     */   private Map<HoleCards, Double> weights;
/*     */ 
/*     */   public Spectrum()
/*     */   {
/*  28 */     this.weights = new HashMap();
/*     */   }
/*     */ 
/*     */   public boolean isEmpty() {
/*  32 */     return this.weights.isEmpty();
/*     */   }
/*     */ 
/*     */   public static Spectrum random() {
/*  36 */     Spectrum result = new Spectrum();
/*  37 */     List cards = Deck.getCards();
/*  38 */     for (int i = 0; i < cards.size(); i++) {
/*  39 */       for (int j = i + 1; j < cards.size(); j++) {
/*  40 */         result.weights.put(new HoleCards((Card)cards.get(i), (Card)cards.get(j)), Double.valueOf(1.0D));
/*     */       }
/*     */     }
/*  43 */     return result;
/*     */   }
/*     */ 
/*     */   public void remove(Card card) {
/*  47 */     List cardsForRemove = new ArrayList();
/*  48 */     for (HoleCards cards : this) {
/*  49 */       if (cards.hasCard(card)) {
/*  50 */         cardsForRemove.add(cards);
/*     */       }
/*     */     }
/*  53 */     for (HoleCards cards : cardsForRemove)
/*  54 */       remove(cards);
/*     */   }
/*     */ 
/*     */   public void remove(Card[] cards)
/*     */   {
/*  59 */     List cardsForRemove = new ArrayList();
/*  60 */     for (HoleCards holeCards : this) {
/*  61 */       for (Card card : cards) {
/*  62 */         if (holeCards.hasCard(card)) {
/*  63 */           cardsForRemove.add(holeCards);
/*  64 */           break;
/*     */         }
/*     */       }
/*     */     }
/*  68 */     for (HoleCards holeCards : cardsForRemove)
/*  69 */       remove(holeCards);
/*     */   }
/*     */ 
/*     */   public void add(HoleCards cards, double weight)
/*     */   {
/*  74 */     if (weight != 0.0D)
/*  75 */       this.weights.put(cards, Double.valueOf(weight));
/*     */   }
/*     */ 
/*     */   public void add(HoleCards cards)
/*     */   {
/*  80 */     this.weights.put(cards, Double.valueOf(1.0D));
/*     */   }
/*     */ 
/*     */   public void remove(HoleCards holeCards) {
/*  84 */     this.weights.remove(holeCards);
/*     */   }
/*     */ 
/*     */   public void add(String cards)
/*     */   {
/*  89 */     Card.Value firstRange = Card.Value.valueOf(cards.charAt(0));
/*  90 */     Card.Value secondRange = Card.Value.valueOf(cards.charAt(1));
/*  91 */     boolean all = cards.length() == 2;
/*     */     Iterator i$;
/*     */     Card.Suit suit1;
/*     */     Iterator i$;
/*  92 */     if (!all) {
/*  93 */       boolean suited = cards.charAt(2) == 's';
/*  94 */       if (suited) {
/*  95 */         for (Card.Suit suit : Card.Suit.getSuits()) {
/*  96 */           this.weights.put(new HoleCards(new Card(firstRange, suit), new Card(secondRange, suit)), Double.valueOf(1.0D));
/*     */         }
/*     */       }
/*     */       else
/* 100 */         for (i$ = Card.Suit.getSuits().iterator(); i$.hasNext(); ) { suit1 = (Card.Suit)i$.next();
/* 101 */           for (Card.Suit suit2 : Card.Suit.getSuits())
/* 102 */             if (suit1 != suit2)
/*     */             {
/* 105 */               this.weights.put(new HoleCards(new Card(firstRange, suit1), new Card(secondRange, suit2)), Double.valueOf(1.0D));
/*     */             }
/*     */         }
/*     */     }
/*     */     else
/*     */     {
/* 111 */       for (i$ = Card.Suit.getSuits().iterator(); i$.hasNext(); ) { suit1 = (Card.Suit)i$.next();
/* 112 */         for (Card.Suit suit2 : Card.Suit.getSuits())
/* 113 */           if ((firstRange != secondRange) || (suit1 != suit2))
/*     */           {
/* 116 */             this.weights.put(new HoleCards(new Card(firstRange, suit1), new Card(secondRange, suit2)), Double.valueOf(1.0D));
/*     */           } }
/*     */     }
/*     */     Card.Suit suit1;
/*     */   }
/*     */ 
/*     */   public double getWeight(HoleCards cards)
/*     */   {
/* 124 */     Double result = (Double)this.weights.get(cards);
/* 125 */     if (result == null) {
/* 126 */       return 0.0D;
/*     */     }
/* 128 */     return result.doubleValue();
/*     */   }
/*     */ 
/*     */   public double summaryWeight()
/*     */   {
/* 133 */     double sum = 0.0D;
/* 134 */     for (Map.Entry entry : this.weights.entrySet()) {
/* 135 */       sum += ((Double)entry.getValue()).doubleValue();
/*     */     }
/* 137 */     return sum;
/*     */   }
/*     */ 
/*     */   public double maxWeight() {
/* 141 */     double maxValue = -1.797693134862316E+308D;
/* 142 */     for (Map.Entry entry : this.weights.entrySet()) {
/* 143 */       if (maxValue < ((Double)entry.getValue()).doubleValue()) {
/* 144 */         maxValue = ((Double)entry.getValue()).doubleValue();
/*     */       }
/*     */     }
/* 147 */     return maxValue;
/*     */   }
/*     */ 
/*     */   public Iterator<HoleCards> iterator() {
/* 151 */     return this.weights.keySet().iterator();
/*     */   }
/*     */ 
/*     */   public int size() {
/* 155 */     return this.weights.size();
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 160 */     Set set = this.weights.entrySet();
/* 161 */     List list = new ArrayList(set);
/* 162 */     Collections.sort(list, new Comparator()
/*     */     {
/*     */       public int compare(Map.Entry<HoleCards, Double> o1, Map.Entry<HoleCards, Double> o2) {
/* 165 */         return Double.compare(((Double)o1.getValue()).doubleValue(), ((Double)o2.getValue()).doubleValue());
/*     */       }
/*     */     });
/* 168 */     Collections.reverse(list);
/* 169 */     StringBuilder result = new StringBuilder();
/* 170 */     for (Map.Entry item : list) {
/* 171 */       result.append(item.getKey());
/* 172 */       result.append(": ");
/* 173 */       result.append(item.getValue());
/* 174 */       result.append("\n");
/*     */     }
/* 176 */     return result.toString();
/*     */   }
/*     */ }

/* Location:           /home/andriipanasiuk/workspace-private/mallorcatour/lib/MallorcaResources.jar
 * Qualified Name:     mallorcatour.game.core.Spectrum
 * JD-Core Version:    0.6.2
 */