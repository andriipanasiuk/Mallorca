/*     */ package mallorcatour.game.core;
/*     */ 
/*     */ import java.io.Serializable;
/*     */ import java.util.Arrays;
/*     */ import java.util.Collections;
/*     */ import java.util.Comparator;
/*     */ import java.util.List;
/*     */ 
/*     */ public final class Card
/*     */   implements Comparable<Card>, Serializable
/*     */ {
/*     */   private static final long serialVersionUID = 1L;
/*     */   private Value value;
/*     */   private Suit suit;
/*     */   private int intValue;
/*     */   private int intValueForBrecher;
/*     */ 
/*     */   public Card(Value value, Suit suit)
/*     */   {
/* 222 */     this.value = value;
/* 223 */     this.suit = suit;
/* 224 */     this.intValue = (value.intValue * 4 + suit.intValue);
/* 225 */     this.intValueForBrecher = (value.intValue + suit.intValue * 13);
/*     */   }
/*     */ 
/*     */   public Card(int intValue, int intSuit) {
/* 229 */     this.value = Value.valueOf(intValue);
/* 230 */     this.suit = Suit.valueOf(intSuit);
/* 231 */     this.intValue = (this.value.intValue * 4 + this.suit.intValue);
/* 232 */     this.intValueForBrecher = (this.value.intValue + this.suit.intValue * 13);
/*     */   }
/*     */ 
/*     */   public static Card valueOf(String card) {
/* 236 */     if (card.length() != 2) {
/* 237 */       throw new IllegalArgumentException("Card need 2 symbols. " + card + ": illegal!");
/*     */     }
/*     */ 
/* 240 */     Value value = Value.valueOf(card.charAt(0));
/* 241 */     Suit suit = Suit.valueOf(card.charAt(1));
/* 242 */     return new Card(value, suit);
/*     */   }
/*     */ 
/*     */   public static Card valueOf(int value) {
/* 246 */     return new Card(Value.valueOf(value / 4), Suit.valueOf(value % 4));
/*     */   }
/*     */ 
/*     */   public static Card valueOfBrecher(int value) {
/* 250 */     return new Card(Value.valueOf(value % 13), Suit.valueOf(value / 13));
/*     */   }
/*     */ 
/*     */   public boolean isSuitedWith(Card other) {
/* 254 */     return this.suit == other.suit;
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 259 */     return this.value.toString() + this.suit.toString();
/*     */   }
/*     */ 
/*     */   public int compareTo(Card other) {
/* 263 */     int compareValue = this.value.compareTo(other.value);
/* 264 */     if (compareValue != 0) {
/* 265 */       return compareValue;
/*     */     }
/* 267 */     return this.suit.compareTo(other.suit);
/*     */   }
/*     */ 
/*     */   public boolean sameValue(Card other)
/*     */   {
/* 272 */     return this.value.intValue == other.value.intValue;
/*     */   }
/*     */ 
/*     */   public int intValue() {
/* 276 */     return this.intValue;
/*     */   }
/*     */ 
/*     */   public int intValueForBrecher() {
/* 280 */     return this.intValueForBrecher;
/*     */   }
/*     */ 
/*     */   public Value getValue() {
/* 284 */     return this.value;
/*     */   }
/*     */ 
/*     */   public Suit getSuit() {
/* 288 */     return this.suit;
/*     */   }
/*     */ 
/*     */   public int hashCode()
/*     */   {
/* 293 */     int hash = 5;
/* 294 */     hash = 43 * hash + (this.value != null ? this.value.intValue : 0);
/* 295 */     hash = 43 * hash + (this.suit != null ? this.suit.intValue : 0);
/* 296 */     return hash;
/*     */   }
/*     */ 
/*     */   public boolean equals(Object other)
/*     */   {
/* 301 */     if (!(other instanceof Card)) {
/* 302 */       return false;
/*     */     }
/* 304 */     Card otherCard = (Card)other;
/* 305 */     return (this.value == otherCard.value) && (this.suit == otherCard.suit);
/*     */   }
/*     */ 
/*     */   public static int[] convertToIntArray(Card[] cards) {
/* 309 */     int[] result = new int[cards.length];
/* 310 */     for (int i = 0; i < cards.length; i++) {
/* 311 */       result[i] = cards[i].intValue();
/*     */     }
/* 313 */     return result;
/*     */   }
/*     */ 
/*     */   public static int[] convertToIntArray(List<Card> cards) {
/* 317 */     int[] result = new int[cards.size()];
/* 318 */     for (int i = 0; i < cards.size(); i++) {
/* 319 */       result[i] = ((Card)cards.get(i)).intValue();
/*     */     }
/* 321 */     return result;
/*     */   }
/*     */ 
/*     */   public static int[] convertToIntBrecherArray(List<Card> cards) {
/* 325 */     int[] result = new int[cards.size()];
/* 326 */     for (int i = 0; i < cards.size(); i++) {
/* 327 */       result[i] = ((Card)cards.get(i)).intValueForBrecher();
/*     */     }
/* 329 */     return result;
/*     */   }
/*     */ 
/*     */   public static class Value
/*     */     implements Comparable<Value>, Serializable
/*     */   {
/*     */     private static final long serialVersionUID = 1L;
/*     */     private final String value;
/*     */     private final int intValue;
/* 104 */     public static final Value TWO = new Value("2", 0);
/* 105 */     public static final Value THREE = new Value("3", 1);
/* 106 */     public static final Value FOUR = new Value("4", 2);
/* 107 */     public static final Value FIVE = new Value("5", 3);
/* 108 */     public static final Value SIX = new Value("6", 4);
/* 109 */     public static final Value SEVEN = new Value("7", 5);
/* 110 */     public static final Value EIGHT = new Value("8", 6);
/* 111 */     public static final Value NINE = new Value("9", 7);
/* 112 */     public static final Value TEN = new Value("T", 8);
/* 113 */     public static final Value JACK = new Value("J", 9);
/* 114 */     public static final Value QUEEN = new Value("Q", 10);
/* 115 */     public static final Value KING = new Value("K", 11);
/* 116 */     public static final Value ACE = new Value("A", 12);
/* 117 */     private static List<Value> listForCompare = Collections.unmodifiableList(Arrays.asList(new Value[] { TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING, ACE }));
/*     */ 
/*     */     private Value(String value, int intValue)
/*     */     {
/* 101 */       this.value = value;
/* 102 */       this.intValue = intValue;
/*     */     }
/*     */ 
/*     */     public String toString()
/*     */     {
/* 124 */       return String.valueOf(this.value);
/*     */     }
/*     */ 
/*     */     public static Value valueOf(int intValue) {
/* 128 */       return (Value)listForCompare.get(intValue);
/*     */     }
/*     */ 
/*     */     public static Value valueOf(String value) {
/* 132 */       return valueOf(value.charAt(0));
/*     */     }
/*     */ 
/*     */     public static Value valueOf(char valueChar) {
/* 136 */       switch (valueChar) {
/*     */       case '2':
/* 138 */         return TWO;
/*     */       case '3':
/* 140 */         return THREE;
/*     */       case '4':
/* 142 */         return FOUR;
/*     */       case '5':
/* 144 */         return FIVE;
/*     */       case '6':
/* 146 */         return SIX;
/*     */       case '7':
/* 148 */         return SEVEN;
/*     */       case '8':
/* 150 */         return EIGHT;
/*     */       case '9':
/* 152 */         return NINE;
/*     */       case 'T':
/* 154 */         return TEN;
/*     */       case 'J':
/* 156 */         return JACK;
/*     */       case 'Q':
/* 158 */         return QUEEN;
/*     */       case 'K':
/* 160 */         return KING;
/*     */       case 'A':
/* 162 */         return ACE;
/*     */       case ':':
/*     */       case ';':
/*     */       case '<':
/*     */       case '=':
/*     */       case '>':
/*     */       case '?':
/*     */       case '@':
/*     */       case 'B':
/*     */       case 'C':
/*     */       case 'D':
/*     */       case 'E':
/*     */       case 'F':
/*     */       case 'G':
/*     */       case 'H':
/*     */       case 'I':
/*     */       case 'L':
/*     */       case 'M':
/*     */       case 'N':
/*     */       case 'O':
/*     */       case 'P':
/*     */       case 'R':
/* 164 */       case 'S': } throw new IllegalArgumentException("Error in string representation of value");
/*     */     }
/*     */ 
/*     */     public int compareTo(Value other)
/*     */     {
/* 169 */       return this.intValue - other.intValue;
/*     */     }
/*     */ 
/*     */     public static Comparator<Card> getComparator() {
/* 173 */       return new Comparator()
/*     */       {
/*     */         public int compare(Card c1, Card c2) {
/* 176 */           return c1.value.compareTo(c2.value);
/*     */         }
/*     */       };
/*     */     }
/*     */ 
/*     */     public static Comparator<Value> getReverseComparator() {
/* 182 */       return new Comparator()
/*     */       {
/*     */         public int compare(Card.Value v1, Card.Value v2) {
/* 185 */           return v2.intValue - v1.intValue;
/*     */         }
/*     */       };
/*     */     }
/*     */ 
/*     */     public static List<Value> getValues() {
/* 191 */       return listForCompare;
/*     */     }
/*     */ 
/*     */     public int intValue() {
/* 195 */       return this.intValue;
/*     */     }
/*     */ 
/*     */     public Value getOneUpValue() {
/* 199 */       int index = listForCompare.indexOf(this);
/* 200 */       if (index != listForCompare.size() - 1) {
/* 201 */         return (Value)listForCompare.get(listForCompare.indexOf(this) + 1);
/*     */       }
/* 203 */       return null;
/*     */     }
/*     */ 
/*     */     public Value getOneDownValue()
/*     */     {
/* 208 */       int index = listForCompare.indexOf(this);
/* 209 */       if (index != 0) {
/* 210 */         return (Value)listForCompare.get(listForCompare.indexOf(this) - 1);
/*     */       }
/* 212 */       return null;
/*     */     }
/*     */   }
/*     */ 
/*     */   public static class Suit
/*     */     implements Comparable<Suit>, Serializable
/*     */   {
/*     */     private static final long serialVersionUID = 1L;
/*     */     private String suit;
/*     */     private int intValue;
/*  31 */     public static final Suit HEARTS = new Suit("Hearts", 3);
/*  32 */     public static final Suit DIAMONDS = new Suit("Diamonds", 2);
/*  33 */     public static final Suit CLUBS = new Suit("Clubs", 1);
/*  34 */     public static final Suit SPADES = new Suit("Spades", 0);
/*  35 */     private static final List<Suit> listForCompare = Collections.unmodifiableList(Arrays.asList(new Suit[] { SPADES, CLUBS, DIAMONDS, HEARTS }));
/*     */ 
/*     */     private Suit(String suit, int intValue)
/*     */     {
/*  28 */       this.suit = suit;
/*  29 */       this.intValue = intValue;
/*     */     }
/*     */ 
/*     */     public static Suit valueOf(String suit)
/*     */     {
/*  40 */       return valueOf(suit.charAt(0));
/*     */     }
/*     */ 
/*     */     public int intValue() {
/*  44 */       return this.intValue;
/*     */     }
/*     */ 
/*     */     public static Suit valueOf(char suitChar) {
/*  48 */       switch (suitChar) {
/*     */       case 'c':
/*  50 */         return CLUBS;
/*     */       case 's':
/*  52 */         return SPADES;
/*     */       case 'd':
/*  54 */         return DIAMONDS;
/*     */       case 'h':
/*  56 */         return HEARTS;
/*     */       }
/*  58 */       throw new IllegalArgumentException("Error in string representation of suit");
/*     */     }
/*     */ 
/*     */     public static Suit valueOf(int value)
/*     */     {
/*  63 */       return (Suit)listForCompare.get(value);
/*     */     }
/*     */ 
/*     */     public static List<Suit> getSuits() {
/*  67 */       return listForCompare;
/*     */     }
/*     */ 
/*     */     public String toString()
/*     */     {
/*  72 */       return String.valueOf(this.suit.charAt(0)).toLowerCase();
/*     */     }
/*     */ 
/*     */     public int compareTo(Suit other) {
/*  76 */       return this.intValue - other.intValue;
/*     */     }
/*     */ 
/*     */     public static Comparator<Card> getComparator() {
/*  80 */       return new Comparator()
/*     */       {
/*     */         public int compare(Card card1, Card card2) {
/*  83 */           int compareSuit = card1.suit.compareTo(card2.suit);
/*  84 */           if (compareSuit != 0) {
/*  85 */             return compareSuit;
/*     */           }
/*  87 */           return card1.value.compareTo(card2.value);
/*     */         }
/*     */       };
/*     */     }
/*     */   }
/*     */ }

/* Location:           /home/andriipanasiuk/workspace-private/mallorcatour/lib/MallorcaResources.jar
 * Qualified Name:     mallorcatour.game.core.Card
 * JD-Core Version:    0.6.2
 */