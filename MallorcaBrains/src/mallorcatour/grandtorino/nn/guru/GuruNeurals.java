/*    */ package mallorcatour.grandtorino.nn.guru;
/*    */ 
/*    */ import mallorcatour.grandtorino.nn.core.AbstractNeurals;
/*    */ 
/*    */ public class GuruNeurals extends AbstractNeurals
/*    */ {
/*    */   private static final String PREFLOP_NN_PATH = "preflop.mlp";
/*    */   private static final String FLOP_NN_PATH = "flop.mlp";
/*    */   private static final String TURN_NN_PATH = "turn.mlp";
/*    */   private static final String RIVER_NN_PATH = "river.mlp";
/*    */   private static final String SITUATIONS_PATH = "situations.pkr";
/*    */ 
/*    */   protected String getPreflopPath()
/*    */   {
/* 22 */     return "preflop.mlp";
/*    */   }
/*    */ 
/*    */   protected String getFlopPath()
/*    */   {
/* 27 */     return "flop.mlp";
/*    */   }
/*    */ 
/*    */   protected String getTurnPath()
/*    */   {
/* 32 */     return "turn.mlp";
/*    */   }
/*    */ 
/*    */   protected String getRiverPath()
/*    */   {
/* 37 */     return "river.mlp";
/*    */   }
/*    */ 
/*    */   protected String getSituationsPath()
/*    */   {
/* 42 */     return "situations.pkr";
/*    */   }
/*    */ 
/*    */   public double getAggressionFactor()
/*    */   {
/* 47 */     return 3.34D;
/*    */   }
/*    */ 
/*    */   public double getWtsd()
/*    */   {
/* 52 */     return 0.15D;
/*    */   }
/*    */ 
/*    */   public double getAggressionFrequency() {
/* 56 */     throw new UnsupportedOperationException("Not supported yet.");
/*    */   }
/*    */ 
/*    */   public double getFoldFrequency() {
/* 60 */     throw new UnsupportedOperationException("Not supported yet.");
/*    */   }
/*    */ 
/*    */   public String getName()
/*    */   {
/* 65 */     return "Guru";
/*    */   }
/*    */ }

/* Location:           /home/andriipanasiuk/workspace-private/mallorcatour/lib/MallorcaResources.jar
 * Qualified Name:     mallorcatour.grandtorino.nn.guru.GuruNeurals
 * JD-Core Version:    0.6.2
 */