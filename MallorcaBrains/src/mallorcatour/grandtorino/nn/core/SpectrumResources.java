/*    */ package mallorcatour.grandtorino.nn.core;
/*    */ 
/*    */ import java.io.File;
/*    */ import java.util.HashMap;
/*    */ import java.util.Map;
/*    */ import java.util.Map.Entry;
/*    */ import mallorcatour.game.core.Spectrum;
/*    */ import mallorcatour.util.Log;
/*    */ import mallorcatour.util.SerializatorUtils;
/*    */ import mallorcatour.util.StringUtils;
/*    */ 
/*    */ public class SpectrumResources
/*    */ {
/* 22 */   private static final Map<Integer, Spectrum> spectrums = new HashMap();
/*    */   private static final String SPECTRUM_PATH = "spectrums/";
/*    */ 
/*    */   private static void initializeSpectrums()
/*    */   {
/* 30 */     File spectrumDir = new File("spectrums/");
/*    */ 
/* 32 */     for (File file : spectrumDir.listFiles()) {
/* 33 */       if (file.getName().contains(".spectrum")) {
/* 34 */         Spectrum spectrum = (Spectrum)SerializatorUtils.load(file.getAbsolutePath(), Spectrum.class);
/* 35 */         int spectrumInt = Integer.parseInt(StringUtils.between(file.getName(), 0, "-percent"));
/* 36 */         spectrums.put(Integer.valueOf(spectrumInt), spectrum);
/*    */       }
/*    */     }
/* 39 */     if (spectrums.size() < 10) {
/* 40 */       throw new RuntimeException("There must be more than 10 spectrums");
/*    */     }
/* 42 */     Log.d(spectrums.size() + " spectrums loaded successfuly.");
/*    */   }
/*    */ 
/*    */   public static Spectrum getSpectrum(int percent)
/*    */   {
/* 47 */     Spectrum result = null;
/* 48 */     double minDistance = 1.7976931348623157E+308D;
/* 49 */     for (Map.Entry entry : spectrums.entrySet()) {
/* 50 */       double currentDistance = Math.abs(percent - ((Integer)entry.getKey()).intValue());
/* 51 */       if (currentDistance < minDistance) {
/* 52 */         result = (Spectrum)entry.getValue();
/* 53 */         minDistance = currentDistance;
/*    */       }
/*    */     }
/* 56 */     return result;
/*    */   }
/*    */ 
/*    */   static
/*    */   {
/* 26 */     initializeSpectrums();
/*    */   }
/*    */ }

/* Location:           /home/andriipanasiuk/workspace-private/mallorcatour/lib/MallorcaResources.jar
 * Qualified Name:     mallorcatour.grandtorino.nn.core.SpectrumResources
 * JD-Core Version:    0.6.2
 */