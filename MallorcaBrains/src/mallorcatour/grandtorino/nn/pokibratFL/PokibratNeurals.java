/*    */ package mallorcatour.grandtorino.nn.pokibratFL;
/*    */ 
/*    */ import mallorcatour.grandtorino.nn.core.AbstractNeurals;
/*    */ 
/*    */ public class PokibratNeurals extends AbstractNeurals
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
/* 47 */     return 5.7D;
/*    */   }
/*    */ 
/*    */   public double getWtsd()
/*    */   {
/* 52 */     return 0.38D;
/*    */   }
/*    */ 
/*    */   public double getAggressionFrequency() {
/* 56 */     return 0.58D;
/*    */   }
/*    */ 
/*    */   public double getFoldFrequency() {
/* 60 */     return 0.31D;
/*    */   }
/*    */ 
/*    */   public String getName()
/*    */   {
/* 65 */     return "Pokibrat";
/*    */   }
/*    */ }

/* Location:           /home/andriipanasiuk/workspace-private/mallorcatour/lib/MallorcaResources.jar
 * Qualified Name:     mallorcatour.grandtorino.nn.pokibratFL.PokibratNeurals
 * JD-Core Version:    0.6.2
 */