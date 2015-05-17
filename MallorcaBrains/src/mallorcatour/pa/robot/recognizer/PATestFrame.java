/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * PSMFrame.java
 *
 * Created on 15.10.2012, 18:14:26
 */
package mallorcatour.pa.robot.recognizer;

import mallorcatour.util.Log;

/**
 *
 * @author Andrew
 */
public class PATestFrame extends javax.swing.JFrame {

    private PATableRecognizer tableRecognizer;

    /** Creates new form PSMFrame */
    public PATestFrame() {
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

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jButton5.setText("Recognize information from table PA ");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(jButton5)
                .addContainerGap(36, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(33, 33, 33)
                .addComponent(jButton5)
                .addContainerGap(82, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        //  PokerPreferences.LOG_IMAGE_RECOGNITION = false;
        tableRecognizer = new PATableRecognizer();
        long start = System.currentTimeMillis();
        tableRecognizer.reset();
        Log.d("Time of finding PA table: " + (System.currentTimeMillis() - start) + " ms");
        start = System.currentTimeMillis();
        double pot = tableRecognizer.getPot();
        Log.d("Time of recognizing pot: " + (System.currentTimeMillis() - start) + " ms");
        Log.d("<-------------------------->");
        Log.d("Pot = " + pot);
    }//GEN-LAST:event_jButton5ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new PATestFrame().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton5;
    // End of variables declaration//GEN-END:variables
}