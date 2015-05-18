/*    */ package mallorcatour.game.core;
/*    */ 
/*    */ import java.io.Serializable;
/*    */ 
/*    */ public final class PlayerAction
/*    */   implements Serializable
/*    */ {
/*    */   private static final long serialVersionUID = 1L;
/*    */   private String name;
/*    */   private Action action;
/*    */ 
/*    */   public PlayerAction(String name, Action action)
/*    */   {
/* 20 */     this.name = name;
/* 21 */     this.action = action;
/*    */   }
/*    */ 
/*    */   public String getName()
/*    */   {
/* 28 */     return this.name;
/*    */   }
/*    */ 
/*    */   public Action getAction()
/*    */   {
/* 35 */     return this.action;
/*    */   }
/*    */ 
/*    */   public String toString()
/*    */   {
/* 40 */     return this.name + " " + this.action.toString();
/*    */   }
/*    */ }

/* Location:           /home/andriipanasiuk/workspace-private/mallorcatour/lib/MallorcaResources.jar
 * Qualified Name:     mallorcatour.game.core.PlayerAction
 * JD-Core Version:    0.6.2
 */