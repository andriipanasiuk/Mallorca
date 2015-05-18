/*    */ package mallorcatour.grandtorino.nn.danielxn;
/*    */ 
/*    */ import mallorcatour.grandtorino.nn.core.AbstractNeurals;
/*    */ 
/*    */ public class DanielxnNeurals extends AbstractNeurals
/*    */ {
/*    */   private static final String PREFLOP_NN_PATH = "preflop.mlp";
/*    */   private static final String FLOP_NN_PATH = "flop.mlp";
/*    */   private static final String TURN_NN_PATH = "turn.mlp";
/*    */   private static final String RIVER_NN_PATH = "river.mlp";
/*    */   private static final String SITUATIONS_PATH = "situations.pkr";
/*    */ 
/*    */   public String getPreflopPath()
/*    */   {
/* 21 */     return "preflop.mlp";
/*    */   }
/*    */ 
/*    */   public String getFlopPath() {
/* 25 */     return "flop.mlp";
/*    */   }
/*    */ 
/*    */   public String getTurnPath() {
/* 29 */     return "turn.mlp";
/*    */   }
/*    */ 
/*    */   public String getRiverPath() {
/* 33 */     return "river.mlp";
/*    */   }
/*    */ 
/*    */   protected String getSituationsPath()
/*    */   {
/* 38 */     return "situations.pkr";
/*    */   }
/*    */ 
/*    */   public double getAggressionFactor()
/*    */   {
/* 43 */     return 4.5D;
/*    */   }
/*    */ 
/*    */   public double getWtsd()
/*    */   {
/* 48 */     return 0.13D;
/*    */   }
/*    */ 
/*    */   public double getAggressionFrequency() {
/* 52 */     throw new UnsupportedOperationException("Not supported yet.");
/*    */   }
/*    */ 
/*    */   public double getFoldFrequency() {
/* 56 */     throw new UnsupportedOperationException("Not supported yet.");
/*    */   }
/*    */ 
/*    */   public String getName()
/*    */   {
/* 61 */     return "DanielXn";
/*    */   }
/*    */ }

/* Location:           /home/andriipanasiuk/workspace-private/mallorcatour/lib/MallorcaResources.jar
 * Qualified Name:     mallorcatour.grandtorino.nn.danielxn.DanielxnNeurals
 * JD-Core Version:    0.6.2
 */