/*    */ package mallorcatour.grandtorino.nn.waterhouse;
/*    */ 
/*    */ import mallorcatour.grandtorino.nn.core.AbstractNeurals;
/*    */ 
/*    */ public class WaterhouseNeurals extends AbstractNeurals
/*    */ {
/*    */   private static final String PREFLOP_NN_PATH = "preflop.mlp";
/*    */   private static final String FLOP_NN_PATH = "flop.mlp";
/*    */   private static final String TURN_NN_PATH = "turn.mlp";
/*    */   private static final String RIVER_NN_PATH = "river.mlp";
/*    */ 
/*    */   protected String getPreflopPath()
/*    */   {
/* 21 */     return "preflop.mlp";
/*    */   }
/*    */ 
/*    */   protected String getFlopPath()
/*    */   {
/* 26 */     return "flop.mlp";
/*    */   }
/*    */ 
/*    */   protected String getTurnPath()
/*    */   {
/* 31 */     return "turn.mlp";
/*    */   }
/*    */ 
/*    */   protected String getRiverPath()
/*    */   {
/* 36 */     return "river.mlp";
/*    */   }
/*    */ 
/*    */   protected String getSituationsPath()
/*    */   {
/* 41 */     throw new RuntimeException("This neurals is for modelling villain");
/*    */   }
/*    */ 
/*    */   public double getAggressionFactor()
/*    */   {
/* 46 */     return 0.76D;
/*    */   }
/*    */ 
/*    */   public double getWtsd()
/*    */   {
/* 51 */     return 0.39D;
/*    */   }
/*    */ 
/*    */   public double getAggressionFrequency() {
/* 55 */     return 0.21D;
/*    */   }
/*    */ 
/*    */   public double getFoldFrequency() {
/* 59 */     return 0.3D;
/*    */   }
/*    */ 
/*    */   public String getName()
/*    */   {
/* 64 */     return "Waterhouse";
/*    */   }
/*    */ }

/* Location:           /home/andriipanasiuk/workspace-private/mallorcatour/lib/MallorcaResources.jar
 * Qualified Name:     mallorcatour.grandtorino.nn.waterhouse.WaterhouseNeurals
 * JD-Core Version:    0.6.2
 */