/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * WindowFinderFrame.java
 *
 * Created on 20.12.2012, 16:18:43
 */
package mallorcatour.windowfinder;

import br.com.wagnerpaz.javahook.NativeKeyboardEvent;
import br.com.wagnerpaz.javahook.NativeKeyboardListener;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.util.List;

import mallorcatour.core.game.Card;
import mallorcatour.robot.hardwaremanager.KeyboardHookManager;
import mallorcatour.robot.ps.recognizer.PSTableObserver;
import mallorcatour.robot.ps.recognizer.PSTableRecognizer;
import mallorcatour.robot.recognizer.ITableListener;
import mallorcatour.tools.Log;
import mallorcatour.util.mp3player.Alarm;

/**
 *
 * @author Andrew
 */
public class PSFinderFrame extends javax.swing.JFrame {

    private static long time = 0;
    private PSTableObserver tableObserver;
    private PSTableRecognizer tableRecognizer = new PSTableRecognizer();

    /** Creates new form WindowFinderFrame */
    public PSFinderFrame() {
        initComponents();
        tableObserver = new PSTableObserver(new ITableListener() {

            public void onTableVisible(Point topLeftPoint, Dimension dimension) {
                Log.d("Top left: " + topLeftPoint);
                Log.d("Dimension: " + dimension);
                if (tableRecognizer.isSupportedDimension(dimension)) {
                    Alarm.stop();
                    tableRecognizer.onTableVisible(topLeftPoint, dimension);
                } else {
                    Alarm.alarm();
                }
            }

            public void onTableInvisible() {
                Alarm.alarm();
            }
        },
                Toolkit.getDefaultToolkit().getScreenSize());
        KeyboardHookManager.addListener(new NativeKeyboardListener() {

            public boolean keyPressed(NativeKeyboardEvent nke) {
                if (nke.getKeyCode() == KeyEvent.VK_F4) {
                    tableObserver.traceTable();
                    return false;
                } else if (nke.getKeyCode() == KeyEvent.VK_F5) {
                    tableObserver.stopTracing();
                    return false;
                } else if (nke.getKeyCode() == KeyEvent.VK_F6) {
                    logRecognizing();
                    return false;
                }
                return true;
            }

            public boolean keyReleased(NativeKeyboardEvent nke) {
                return true;
            }
        });
    }

    private void logRecognizing() {
        long start = System.currentTimeMillis();
        tableRecognizer.scrapTable();
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
        int boardCardsCount = tableRecognizer.getCardsCount();
        boolean chat = tableRecognizer.checkChat();
        Log.d("Time of recognizing pot: " + (System.currentTimeMillis() - start) + " ms");
        Log.d("<-------------------------->");
        Log.d("Hand number: " + handNumber);
        Log.d("Chat normal: " + chat);
        Log.d("Villain stack: " + villainStack);
        Log.d("Hero stack: " + heroStack);
        Log.d("Villain bet: " + villainBet);
        Log.d("Hero bet: " + heroBet);
        Log.d("Pot = " + pot);
//            Log.d("Hole cards: " + holeCards);
        Log.d("Board cards count: " + boardCardsCount);
        Log.d("Board cards: " + boardCards);
        Log.d("Hero on button: " + heroOnButton);

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new PSFinderFrame().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
