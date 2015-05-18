/*    */ package mallorcatour.grandtorino.nn.core;
/*    */ 
/*    */ import java.io.InputStream;

import mallorcatour.core.game.IPokerStats;

/*    */ import org.neuroph.core.NeuralNetwork;
/*    */ 
/*    */ public abstract class AbstractNeurals
/*    */   implements IPokerStats
/*    */ {
/*    */   private NeuralNetwork preflopNN;
/*    */   private NeuralNetwork flopNN;
/*    */   private NeuralNetwork turnNN;
/*    */   private NeuralNetwork riverNN;
/*    */ 
/*    */   protected abstract String getSituationsPath();
/*    */ 
/*    */   protected abstract String getPreflopPath();
/*    */ 
/*    */   protected abstract String getFlopPath();
/*    */ 
/*    */   protected abstract String getTurnPath();
/*    */ 
/*    */   protected abstract String getRiverPath();
/*    */ 
/*    */   public abstract String getName();
/*    */ 
/*    */   protected AbstractNeurals()
/*    */   {
/* 32 */     init();
/*    */   }
/*    */ 
/*    */   private void init() {
/* 36 */     this.preflopNN = NeuralNetwork.load(getPreflopStream());
/* 37 */     this.flopNN = NeuralNetwork.load(getFlopStream());
/* 38 */     this.turnNN = NeuralNetwork.load(getTurnStream());
/* 39 */     this.riverNN = NeuralNetwork.load(getRiverStream());
/*    */   }
/*    */ 
/*    */   public NeuralNetwork getPreflopNN() {
/* 43 */     return this.preflopNN;
/*    */   }
/*    */ 
/*    */   public NeuralNetwork getFlopNN() {
/* 47 */     return this.flopNN;
/*    */   }
/*    */ 
/*    */   public NeuralNetwork getTurnNN() {
/* 51 */     return this.turnNN;
/*    */   }
/*    */ 
/*    */   public NeuralNetwork getRiverNN() {
/* 55 */     return this.riverNN;
/*    */   }
/*    */ 
/*    */   public InputStream getSituationsStream() {
/* 59 */     return getClass().getResourceAsStream(getSituationsPath());
/*    */   }
/*    */ 
/*    */   private InputStream getPreflopStream() {
/* 63 */     return getClass().getResourceAsStream(getPreflopPath());
/*    */   }
/*    */ 
/*    */   private InputStream getFlopStream() {
/* 67 */     return getClass().getResourceAsStream(getFlopPath());
/*    */   }
/*    */ 
/*    */   private InputStream getTurnStream() {
/* 71 */     return getClass().getResourceAsStream(getTurnPath());
/*    */   }
/*    */ 
/*    */   private InputStream getRiverStream() {
/* 75 */     return getClass().getResourceAsStream(getRiverPath());
/*    */   }
/*    */ }

/* Location:           /home/andriipanasiuk/workspace-private/mallorcatour/lib/MallorcaResources.jar
 * Qualified Name:     mallorcatour.grandtorino.nn.core.AbstractNeurals
 * JD-Core Version:    0.6.2
 */