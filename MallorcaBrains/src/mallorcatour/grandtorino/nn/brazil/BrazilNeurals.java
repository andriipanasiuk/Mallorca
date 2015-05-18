/*    */ package mallorcatour.grandtorino.nn.brazil;
/*    */ 
/*    */ import mallorcatour.grandtorino.nn.core.AbstractNeurals;
/*    */ 
/*    */ public class BrazilNeurals extends AbstractNeurals
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
/* 43 */     return 3.7D;
/*    */   }
/*    */ 
/*    */   public double getWtsd()
/*    */   {
/* 48 */     return 0.4D;
/*    */   }
/*    */ 
/*    */   public double getAggressionFrequency() {
/* 52 */     return 0.52D;
/*    */   }
/*    */ 
/*    */   public double getFoldFrequency() {
/* 56 */     return 0.29D;
/*    */   }
/*    */ 
/*    */   public String getName()
/*    */   {
/* 61 */     return "Brazil";
/*    */   }
/*    */ }

/* Location:           /home/andriipanasiuk/workspace-private/mallorcatour/lib/MallorcaResources.jar
 * Qualified Name:     mallorcatour.grandtorino.nn.brazil.BrazilNeurals
 * JD-Core Version:    0.6.2
 */