/*    */ package mallorcatour.grandtorino.nn.germany;
/*    */ 
/*    */ import mallorcatour.grandtorino.nn.core.AbstractNeurals;
/*    */ 
/*    */ public class GermanyNeurals extends AbstractNeurals
/*    */ {
/*    */   private static final String PREFLOP_NN_PATH = "preflop.mlp";
/*    */   private static final String FLOP_NN_PATH = "flop.mlp";
/*    */   private static final String TURN_NN_PATH = "turn.mlp";
/*    */   private static final String RIVER_NN_PATH = "river.mlp";
/*    */ 
/*    */   public String getPreflopPath()
/*    */   {
/* 20 */     return "preflop.mlp";
/*    */   }
/*    */ 
/*    */   public String getFlopPath() {
/* 24 */     return "flop.mlp";
/*    */   }
/*    */ 
/*    */   public String getTurnPath() {
/* 28 */     return "turn.mlp";
/*    */   }
/*    */ 
/*    */   public String getRiverPath() {
/* 32 */     return "river.mlp";
/*    */   }
/*    */ 
/*    */   protected String getSituationsPath()
/*    */   {
/* 37 */     throw new RuntimeException("This neurals is for modelling villain");
/*    */   }
/*    */ 
/*    */   public double getAggressionFactor()
/*    */   {
/* 42 */     return 4.5D;
/*    */   }
/*    */ 
/*    */   public double getWtsd()
/*    */   {
/* 47 */     return 0.53D;
/*    */   }
/*    */ 
/*    */   public double getAggressionFrequency() {
/* 51 */     return 0.62D;
/*    */   }
/*    */ 
/*    */   public double getFoldFrequency() {
/* 55 */     return 0.09D;
/*    */   }
/*    */ 
/*    */   public String getName()
/*    */   {
/* 60 */     return "Germany";
/*    */   }
/*    */ }

/* Location:           /home/andriipanasiuk/workspace-private/mallorcatour/lib/MallorcaResources.jar
 * Qualified Name:     mallorcatour.grandtorino.nn.germany.GermanyNeurals
 * JD-Core Version:    0.6.2
 */