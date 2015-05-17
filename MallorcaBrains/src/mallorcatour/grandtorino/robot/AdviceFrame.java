/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.grandtorino.robot;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.lang.management.ManagementFactory;
import javax.swing.JComponent;
import javax.swing.JDialog;
import mallorcatour.util.Log;
import mallorcatour.util.ThreadUtils;

/**
 *
 * @author Andrew
 */
public class AdviceFrame extends JDialog {

    private Dimension size;

    /** Creates new form HumanActionFrame */
    public AdviceFrame() {
        setUndecorated(true);
        setAlwaysOnTop(true);
        size = new Dimension(3, 3);
        setSize(size);
        setPreferredSize(size);
        pack();
    }

    public void setColor(Color color) {
        getContentPane().setBackground(color);
    }

    class ImagePanel extends JComponent {

        private Image image;

        public ImagePanel(Image image) {
            this.image = image;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            g.drawImage(image, 0, 0, AdviceFrame.this.getWidth(),
                    AdviceFrame.this.getHeight(), null);
        }
    }

    public static void main(String[] args) {
        AdviceFrame fr = new AdviceFrame();
        double res = ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage();
        String res1 = ManagementFactory.getOperatingSystemMXBean().getArch();
        Log.d(res1 + " " + res);
        fr.setLocation(100, 100);
        fr.setColor(Color.yellow);
        fr.setVisible(true);
        ThreadUtils.sleep(1000);
        fr.setSize(500, 100);

    }
}
