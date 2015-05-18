/*     */ package mallorcatour.game.core;
/*     */ 
/*     */ import java.awt.Color;
/*     */ import java.awt.Dimension;
/*     */ import java.awt.Font;
/*     */ import java.awt.Graphics;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.MouseAdapter;
/*     */ import java.awt.event.MouseEvent;
/*     */ import java.io.File;
/*     */ import java.util.HashMap;
/*     */ import java.util.Map;
/*     */ import java.util.Map.Entry;
/*     */ import javax.swing.BorderFactory;
/*     */ import javax.swing.JFrame;
/*     */ import javax.swing.JMenu;
/*     */ import javax.swing.JMenuBar;
/*     */ import javax.swing.JMenuItem;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.JToolTip;
/*     */ import javax.swing.Popup;
/*     */ import javax.swing.PopupFactory;
/*     */ import mallorcatour.util.FrameUtils;
/*     */ import mallorcatour.util.SerializatorUtils;
/*     */ 
/*     */ public class SpectrumFrame extends JFrame
/*     */ {
/*  29 */   private int width = 420; private int height = 420;
/*  30 */   private int quadroSize = this.width / 14;
/*     */   int currentX;
/*     */   int currentY;
/*     */   int oldX;
/*     */   int oldY;
/*     */   private Map<Integer, Double> map;
/*     */   private double maxValue;
/*     */   private JPanel spectrumPanel;
/*     */ 
/*     */   public SpectrumFrame()
/*     */   {
/*  36 */     initComponents();
/*  37 */     setDefaultCloseOperation(2);
/*  38 */     setResizable(false);
/*  39 */     setPreferredSize(new Dimension(this.width, this.height));
/*     */   }
/*     */ 
/*     */   public SpectrumFrame(Spectrum spectrum, String title) {
/*  43 */     initComponents();
/*  44 */     setTitle(title);
/*  45 */     setDefaultCloseOperation(2);
/*  46 */     setResizable(false);
/*  47 */     setPreferredSize(new Dimension(this.width, this.height));
/*  48 */     prepareMap(spectrum);
/*     */   }
/*     */ 
/*     */   private void prepareMap(Spectrum spectrum) {
/*  52 */     this.map = new HashMap();
/*  53 */     for (HoleCards cards : Spectrum.random()) {
/*  54 */       int hashCode = cards.hashCodeForValues();
/*  55 */       if (this.map.get(Integer.valueOf(hashCode)) != null)
/*  56 */         this.map.put(Integer.valueOf(hashCode), Double.valueOf(((Double)this.map.get(Integer.valueOf(hashCode))).doubleValue() + spectrum.getWeight(cards)));
/*     */       else {
/*  58 */         this.map.put(Integer.valueOf(hashCode), Double.valueOf(spectrum.getWeight(cards)));
/*     */       }
/*     */     }
/*  61 */     this.maxValue = maxValue(this.map);
/*     */   }
/*     */ 
/*     */   private void initComponents()
/*     */   {
/*  66 */     JMenuBar menuBar = new JMenuBar();
/*  67 */     JMenu fileMenu = new JMenu("File");
/*  68 */     JMenuItem openItem = new JMenuItem("Open...");
/*  69 */     fileMenu.add(openItem);
/*  70 */     openItem.addActionListener(new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent e) {
/*  73 */         String path = FrameUtils.openFileChooser(SpectrumFrame.this, "./");
/*  74 */         SpectrumFrame.this.setTitle(new File(path).getName());
/*  75 */         Spectrum spectrum = (Spectrum)SerializatorUtils.load(path, Spectrum.class);
/*  76 */         SpectrumFrame.this.prepareMap(spectrum);
/*  77 */         SpectrumFrame.this.spectrumPanel.repaint();
/*     */       }
/*     */     });
/*  80 */     menuBar.add(fileMenu);
/*  81 */     setJMenuBar(menuBar);
/*  82 */     this.spectrumPanel = new SpectrumPanel();
/*  83 */     this.spectrumPanel.addMouseListener(new MouseAdapter()
/*     */     {
/*     */       public void mousePressed(MouseEvent evt)
/*     */       {
/*  87 */         SpectrumFrame.this.spectrumPanelMousePressed(evt);
/*     */       }
/*     */     });
/*  90 */     this.spectrumPanel.setBackground(new Color(255, 255, 255));
/*  91 */     this.spectrumPanel.setBorder(BorderFactory.createBevelBorder(0));
/*     */ 
/*  93 */     setContentPane(this.spectrumPanel);
/*  94 */     pack();
/*     */   }
/*     */ 
/*     */   private void spectrumPanelMousePressed(MouseEvent evt)
/*     */   {
/*  99 */     this.oldX = evt.getX();
/* 100 */     this.oldY = evt.getY();
/* 101 */     int first = this.oldY / this.quadroSize;
/* 102 */     int second = this.oldX / this.quadroSize;
/* 103 */     HoleCards cards = new HoleCards(new Card(Card.Value.valueOf(13 - first), Card.Suit.CLUBS), new Card(Card.Value.valueOf(13 - second), Card.Suit.DIAMONDS));
/*     */ 
/* 105 */     JToolTip tip = new JToolTip();
/* 106 */     tip.setTipText(cards.toString() + ": " + this.map.get(Integer.valueOf(cards.hashCodeForValues())) + " max value: " + this.maxValue);
/*     */ 
/* 108 */     final Popup popup = new PopupFactory().getPopup(this, tip, evt.getXOnScreen(), evt.getYOnScreen());
/* 109 */     popup.show();
/* 110 */     new Thread()
/*     */     {
/*     */       public void run()
/*     */       {
/*     */         try {
/* 115 */           Thread.sleep(1500L);
/* 116 */           popup.hide();
/*     */         } catch (InterruptedException ex) {
/* 118 */           throw new RuntimeException(ex);
/*     */         }
/*     */       }
/*     */     }
/* 110 */     .start();
/*     */   }
/*     */ 
/*     */   private <T> double maxValue(Map<T, Double> map)
/*     */   {
/* 160 */     double max = -1.797693134862316E+308D;
/* 161 */     for (Map.Entry entry : map.entrySet()) {
/* 162 */       if (max < ((Double)entry.getValue()).doubleValue()) {
/* 163 */         max = ((Double)entry.getValue()).doubleValue();
/*     */       }
/*     */     }
/* 166 */     return max;
/*     */   }
/*     */ 
/*     */   public static void main(String[] args) {
/* 170 */     new SpectrumFrame().setVisible(true);
/*     */   }
/*     */ 
/*     */   private class SpectrumPanel extends JPanel
/*     */   {
/*     */     SpectrumPanel()
/*     */     {
/* 127 */       setPreferredSize(new Dimension(SpectrumFrame.this.width, SpectrumFrame.this.height));
/*     */     }
/*     */ 
/*     */     public void paintComponent(Graphics g)
/*     */     {
/* 132 */       super.paintComponent(g);
/* 133 */       if (SpectrumFrame.this.map == null) {
/* 134 */         return;
/*     */       }
/* 136 */       int fontSize = 18;
/*     */ 
/* 138 */       for (int i = 0; i < 13; i++) {
/* 139 */         g.setColor(Color.BLACK);
/* 140 */         g.setFont(new Font("Serif", 1, fontSize));
/* 141 */         g.drawString(Card.Value.valueOf(12 - i).toString(), (i + 1) * SpectrumFrame.this.quadroSize + (SpectrumFrame.this.quadroSize - fontSize) / 2, (SpectrumFrame.this.quadroSize - fontSize) / 2 + fontSize);
/*     */ 
/* 143 */         g.drawString(Card.Value.valueOf(12 - i).toString(), 12, (i + 1) * SpectrumFrame.this.height / 14 + 24);
/*     */       }
/*     */ 
/* 146 */       for (int i = 0; i < 13; i++)
/* 147 */         for (int j = 0; j < 13; j++) {
/* 148 */           HoleCards cards = new HoleCards(new Card(Card.Value.valueOf(12 - i), Card.Suit.DIAMONDS), new Card(Card.Value.valueOf(12 - j), Card.Suit.CLUBS));
/*     */ 
/* 150 */           int color = 255 - (int)(255.0D * ((Double)SpectrumFrame.this.map.get(Integer.valueOf(cards.hashCodeForValues()))).doubleValue() / SpectrumFrame.this.maxValue);
/* 151 */           g.setColor(new Color(color, color, color));
/* 152 */           g.fillRect((i + 1) * SpectrumFrame.this.width / 14, (j + 1) * SpectrumFrame.this.height / 14, SpectrumFrame.this.width / 14, SpectrumFrame.this.height / 14);
/*     */         }
/*     */     }
/*     */   }
/*     */ }

/* Location:           /home/andriipanasiuk/workspace-private/mallorcatour/lib/MallorcaResources.jar
 * Qualified Name:     mallorcatour.game.core.SpectrumFrame
 * JD-Core Version:    0.6.2
 */