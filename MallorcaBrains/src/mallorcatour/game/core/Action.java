/*     */ package mallorcatour.game.core;
/*     */ 
/*     */ import java.io.IOException;
/*     */ import java.io.ObjectInputStream;
/*     */ import java.io.Serializable;
/*     */ 
/*     */ public class Action
/*     */   implements Serializable
/*     */ {
/*     */   private static final long serialVersionUID = 1L;
/*     */   private static final double THRESHOLD_FOR_GOING_ALLIN = 0.6D;
/*     */   private static final double PERCENT_OF_POT_FOR_BET = 0.7D;
/*     */   private static final double PERCENT_OF_POT_FOR_RAISE = 0.7D;
/*     */   private double amount;
/*     */   private Type type;
/*     */   private double percentOfPot;
/*  52 */   private static final Action ALL_IN_ACTION = new Action(Type.AGGRESSIVE, -1.0D);
/*     */ 
/*     */   public Action(Type type, double amount)
/*     */   {
/*  56 */     this.type = type;
/*  57 */     this.amount = amount;
/*     */   }
/*     */ 
/*     */   private Action(Type type) {
/*  61 */     this.type = type;
/*     */   }
/*     */ 
/*     */   public double getAmount()
/*     */   {
/*  68 */     return this.amount;
/*     */   }
/*     */ 
/*     */   public void setAmount(double amount) {
/*  72 */     this.amount = amount;
/*     */   }
/*     */ 
/*     */   public double getPercentOfPot()
/*     */   {
/*  79 */     return this.percentOfPot;
/*     */   }
/*     */ 
/*     */   public boolean isFold() {
/*  83 */     return this.type == Type.FOLD;
/*     */   }
/*     */ 
/*     */   public boolean isPassive() {
/*  87 */     return this.type == Type.PASSIVE;
/*     */   }
/*     */ 
/*     */   public boolean isAggressive() {
/*  91 */     return this.type == Type.AGGRESSIVE;
/*     */   }
/*     */ 
/*     */   public boolean isAllin() {
/*  95 */     return this == ALL_IN_ACTION;
/*     */   }
/*     */ 
/*     */   public static Action passive() {
/*  99 */     Action result = new Action(Type.PASSIVE);
/* 100 */     return result;
/*     */   }
/*     */ 
/*     */   public static Action aggressive() {
/* 104 */     Action result = new Action(Type.AGGRESSIVE);
/* 105 */     return result;
/*     */   }
/*     */ 
/*     */   public static Action createBetAction(double pot, double percent, double effectiveStack)
/*     */   {
/* 110 */     double amountOfBet = percent * pot;
/* 111 */     if (effectiveStack - amountOfBet < 0.6D * (pot + 2.0D * amountOfBet))
/*     */     {
/* 113 */       return ALL_IN_ACTION;
/*     */     }
/* 115 */     Action result = new Action(Type.AGGRESSIVE, amountOfBet);
/* 116 */     result.percentOfPot = percent;
/* 117 */     return result;
/*     */   }
/*     */ 
/*     */   public static Action createBetAction(double pot, double effectiveStack) {
/* 121 */     double amountOfBet = 0.7D * pot;
/* 122 */     if (effectiveStack - amountOfBet < 0.6D * (pot + 2.0D * amountOfBet))
/*     */     {
/* 124 */       return ALL_IN_ACTION;
/*     */     }
/* 126 */     Action result = new Action(Type.AGGRESSIVE, amountOfBet);
/* 127 */     result.percentOfPot = 0.7D;
/* 128 */     return result;
/*     */   }
/*     */ 
/*     */   public static Action createRaiseAction(double toCall, double pot, double effectiveStack)
/*     */   {
/* 133 */     double raiseAmount = 0.7D * (toCall + pot);
/* 134 */     if (effectiveStack - raiseAmount < 0.6D * (pot + toCall + 2.0D * raiseAmount))
/*     */     {
/* 136 */       return ALL_IN_ACTION;
/*     */     }
/* 138 */     Action result = new Action(Type.AGGRESSIVE, raiseAmount);
/* 139 */     result.percentOfPot = 0.7D;
/* 140 */     return result;
/*     */   }
/*     */ 
/*     */   public static Action createRaiseAction(double toCall, double pot, double effectiveStack, double percent)
/*     */   {
/* 145 */     double raiseAmount = percent * (toCall + pot);
/* 146 */     if (effectiveStack - raiseAmount < 0.6D * (pot + toCall + 2.0D * raiseAmount))
/*     */     {
/* 148 */       return ALL_IN_ACTION;
/*     */     }
/* 150 */     Action result = new Action(Type.AGGRESSIVE, raiseAmount);
/* 151 */     result.percentOfPot = percent;
/* 152 */     return result;
/*     */   }
/*     */ 
/*     */   public static Action createRaiseAction(double amount, double percent)
/*     */   {
/* 157 */     Action result = new Action(Type.AGGRESSIVE, amount);
/* 158 */     result.percentOfPot = percent;
/* 159 */     return result;
/*     */   }
/*     */ 
/*     */   public static Action allInAction() {
/* 163 */     return ALL_IN_ACTION;
/*     */   }
/*     */ 
/*     */   public static Action foldAction() {
/* 167 */     return new Action(Type.FOLD);
/*     */   }
/*     */ 
/*     */   public static Action checkAction() {
/* 171 */     return new Action(Type.PASSIVE, 0.0D);
/*     */   }
/*     */ 
/*     */   public static Action betAction(double amount) {
/* 175 */     return new Action(Type.AGGRESSIVE, amount);
/*     */   }
/*     */ 
/*     */   public static Action raiseAction(double amount) {
/* 179 */     return new Action(Type.AGGRESSIVE, amount);
/*     */   }
/*     */ 
/*     */   public static Action callAction(double toCall) {
/* 183 */     Action result = new Action(Type.PASSIVE, toCall);
/* 184 */     return result;
/*     */   }
/*     */ 
/*     */   public boolean isCall() {
/* 188 */     return (this.type == Type.PASSIVE) && (this.amount != 0.0D);
/*     */   }
/*     */ 
/*     */   public boolean isCheck() {
/* 192 */     return (this.type == Type.PASSIVE) && (this.amount == 0.0D);
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 197 */     if (this.type == Type.AGGRESSIVE) {
/* 198 */       if (this != ALL_IN_ACTION) {
/* 199 */         return "Raise " + this.amount;
/*     */       }
/* 201 */       return "All-in";
/*     */     }
/* 203 */     if (this.type == Type.PASSIVE) {
/* 204 */       if (this.amount == 0.0D) {
/* 205 */         return "Check";
/*     */       }
/*     */ 
/* 208 */       return "Call " + this.amount;
/*     */     }
/*     */ 
/* 211 */     return this.type.toString();
/*     */   }
/*     */ 
/*     */   public boolean equals(Object obj)
/*     */   {
/* 217 */     if (obj == null) {
/* 218 */       return false;
/*     */     }
/* 220 */     if (getClass() != obj.getClass()) {
/* 221 */       return false;
/*     */     }
/* 223 */     Action other = (Action)obj;
/* 224 */     if ((other.type == Type.FOLD) && (this.type == Type.FOLD)) {
/* 225 */       return true;
/*     */     }
/* 227 */     if (Double.doubleToLongBits(this.amount) != Double.doubleToLongBits(other.amount)) {
/* 228 */       return false;
/*     */     }
/* 230 */     if ((this.type != other.type) && ((this.type == null) || (!this.type.equals(other.type)))) {
/* 231 */       return false;
/*     */     }
/* 233 */     if (Double.doubleToLongBits(this.percentOfPot) != Double.doubleToLongBits(other.percentOfPot)) {
/* 234 */       return false;
/*     */     }
/* 236 */     return true;
/*     */   }
/*     */ 
/*     */   public int hashCode()
/*     */   {
/* 241 */     int hash = 5;
/* 242 */     return hash;
/*     */   }
/*     */ 
/*     */   private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
/* 246 */     in.defaultReadObject();
/* 247 */     if (this.type.equals(Type.FOLD))
/* 248 */       this.type = Type.FOLD;
/* 249 */     else if (this.type.equals(Type.PASSIVE))
/* 250 */       this.type = Type.PASSIVE;
/* 251 */     else if (this.type.equals(Type.AGGRESSIVE))
/* 252 */       this.type = Type.AGGRESSIVE;
/*     */     else
/* 254 */       throw new IllegalArgumentException();
/*     */   }
/*     */ 
/*     */   public static class Type
/*     */     implements Serializable
/*     */   {
/*     */     private static final long serialVersionUID = 1L;
/*     */     private String action;
/*  30 */     public static final Type FOLD = new Type("Fold");
/*  31 */     public static final Type PASSIVE = new Type("Passive");
/*  32 */     public static final Type AGGRESSIVE = new Type("Aggressive");
/*     */ 
/*     */     private Type(String action)
/*     */     {
/*  23 */       this.action = action;
/*     */     }
/*     */ 
/*     */     public String toString()
/*     */     {
/*  28 */       return this.action;
/*     */     }
/*     */ 
/*     */     public boolean equals(Object other)
/*     */     {
/*  36 */       if (!(other instanceof Type)) {
/*  37 */         return false;
/*     */       }
/*  39 */       if (!((Type)other).action.equals(this.action)) {
/*  40 */         return false;
/*     */       }
/*  42 */       return true;
/*     */     }
/*     */   }
/*     */ }

/* Location:           /home/andriipanasiuk/workspace-private/mallorcatour/lib/MallorcaResources.jar
 * Qualified Name:     mallorcatour.game.core.Action
 * JD-Core Version:    0.6.2
 */