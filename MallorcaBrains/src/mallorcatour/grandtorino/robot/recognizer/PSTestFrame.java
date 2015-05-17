/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * PSMFrame.java
 *
 * Created on 15.10.2012, 18:14:26
 */
package mallorcatour.grandtorino.robot.recognizer;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.util.List;
import mallorcatour.game.core.Card;
import mallorcatour.grandtorino.robot.recognizer.assets.MinSizeCardAssets;
import mallorcatour.util.FrameUtils;
import mallorcatour.util.ImageUtils;
import mallorcatour.util.Log;
import mallorcatour.util.ReaderUtils;

/**
 *
 * @author Andrew
 */
public class PSTestFrame extends javax.swing.JFrame {

    private PSTableRecognizer tableRecognizer;

    /** Creates new form PSMFrame */
    public PSTestFrame() {
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton5 = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jButton5.setText("Recognize information from table PS ");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        jButton1.setText("Recognize cards");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("Temp");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jButton1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButton5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(86, 86, 86)
                        .addComponent(jButton2)))
                .addContainerGap(36, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(33, 33, 33)
                .addComponent(jButton5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 14, Short.MAX_VALUE)
                .addComponent(jButton2)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        String[] pathes = FrameUtils.openFileChooserForMultipleFiles(this, "./");
        tableRecognizer = new PSTableRecognizer();
        for (int i = 0; i < pathes.length; i++) {
            String path = pathes[i];
            long start = System.currentTimeMillis();
            tableRecognizer.scrapTable(path);
            Log.d("Top left point: " + tableRecognizer.getTopLeftPosition() + "");
            Log.d("Time of finding PS table: " + (System.currentTimeMillis() - start) + " ms");
//            start = System.currentTimeMillis();
//            HoleCards holeCards = tableRecognizer.getHoleCards();
//            Log.d("Time of recognizing hole cards: " + (System.currentTimeMillis() - start) + " ms");
            start = System.currentTimeMillis();
            List<Card> boardCards = tableRecognizer.getBoardCards();
            Log.d("Time of recognizing board cards: " + (System.currentTimeMillis() - start) + " ms");
            start = System.currentTimeMillis();
            boolean heroOnButton = tableRecognizer.isHeroOnButton();
            Log.d("Time of recognizing button: " + (System.currentTimeMillis() - start) + " ms");
            start = System.currentTimeMillis();
            double villainStack = tableRecognizer.getVillainStack();
            Log.d("Time of recognizing villain stack: " + (System.currentTimeMillis() - start) + " ms");
            start = System.currentTimeMillis();
            double heroStack = tableRecognizer.getHeroStack();
            Log.d("Time of recognizing hero stack: " + (System.currentTimeMillis() - start) + " ms");
            start = System.currentTimeMillis();
            long handNumber = tableRecognizer.getHandNumber();
            Log.d("Time of recognizing hand number: " + (System.currentTimeMillis() - start) + " ms");
            start = System.currentTimeMillis();
            double villainBet = tableRecognizer.getVillainBet();
            Log.d("Time of recognizing villain bet: " + (System.currentTimeMillis() - start) + " ms");
            start = System.currentTimeMillis();
            double heroBet = tableRecognizer.getHeroBet();
            Log.d("Time of recognizing hero bet: " + (System.currentTimeMillis() - start) + " ms");
            start = System.currentTimeMillis();
            double pot = tableRecognizer.getPot();
            Log.d("Time of recognizing pot: " + (System.currentTimeMillis() - start) + " ms");
            Log.d("<-------------------------->");
            Log.d("File: " + path);
            Log.d("Hand number: " + handNumber);
            Log.d("Villain stack: " + villainStack);
            Log.d("Hero stack: " + heroStack);
            Log.d("Villain bet: " + villainBet);
            Log.d("Hero bet: " + heroBet);
            Log.d("Pot = " + pot);
//            Log.d("Hole cards: " + holeCards);
            Log.d("Board cards: " + boardCards);
            Log.d("Hero on button: " + heroOnButton);
        }
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        String[] pathes = FrameUtils.openFileChooserForMultipleFiles(this, "./");
        PSCardRecognizer cardRecognizer = new PSCardRecognizer(new MinSizeCardAssets());
        for (int i = 0; i < pathes.length; i++) {
            String path = pathes[i];
            BufferedImage image = ImageUtils.fromFile(path);
            long start = System.currentTimeMillis();
            List<Card> boardCards = cardRecognizer.getCards(image, 5);
            int count = cardRecognizer.getCardsCount(image);
            Log.d("Time of recognizing cards: " + (System.currentTimeMillis() - start) + " ms");
            Log.d("Recognized cards: " + boardCards);
            Log.d("Cards count: " + count);
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        String[] pathes = FrameUtils.openFileChooserForMultipleFiles(this, "./");
        for (String path : pathes) {
            BufferedReader reader = ReaderUtils.initReader(path);
            String buf;
            int line = 0;
            while ((buf = ReaderUtils.readLine(reader)) != null) {
                line++;
                if (buf.contains("quart")) {
                    Log.d("Needed file: " + path);
                    return;
                }
            }
        }
        Log.d("End of process");

    }//GEN-LAST:event_jButton2ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new PSTestFrame().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton5;
    // End of variables declaration//GEN-END:variables
}