/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.frames;

/**
 * Hidden tray dialog for playing in PokerStart
 * @author Andrew
 */
import java.awt.AWTException;
import java.awt.CheckboxMenuItem;
import java.awt.Image;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.imageio.ImageIO;
import javax.swing.JColorChooser;
import javax.swing.JDialog;

import mallorcatour.bot.neural.NeuralBotFactory;
import mallorcatour.robot.ManageKeySettings;
import mallorcatour.robot.hardwaremanager.KeyboardHookManager;
import mallorcatour.robot.ps.PSTableDirector;
import mallorcatour.tools.Log;
import br.com.wagnerpaz.javahook.NativeKeyboardEvent;
import br.com.wagnerpaz.javahook.NativeKeyboardListener;
import fellomen.bot.FellOmenFactory;

public class GameTrayFrame extends JDialog {

    private final static String DHCP_ICON_PATH = "assets/dhcp.png";
    private final static String VALENCIA_ICON_PATH = "assets/valencia.gif";
    private final static String VALENCIA_TRAY_MESSAGE = "Valencia";
    private final static String DHCP_TRAY_MESSAGE = "DHCP Server";

    static {
        try {
            OutputStream output = new FileOutputStream("log.txt");
            final PrintStream printOut = new PrintStream(output);
            OutputStream error = new FileOutputStream("error.txt");
            final PrintStream printError = new PrintStream(error);
            System.setOut(printOut);
            System.setErr(printError);
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

                public void run() {
                    Log.d("Exit");
                    printOut.flush();
                    printOut.close();
                    printError.flush();
                    printError.close();
                }
            }));
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }
    private MenuItem isActivatedItem;
    private CheckboxMenuItem fellOmenItem, sparItem, mathBotItem;
    private SystemTray systemTray = SystemTray.getSystemTray();
    private final PSTableDirector gameDirector;
    private boolean humanManage = false;

    public GameTrayFrame() throws IOException, AWTException {
        gameDirector = new PSTableDirector(
                Toolkit.getDefaultToolkit().getScreenSize());
        gameDirector.setFLBotFactory(new FellOmenFactory());
        gameDirector.setNLBotFactory(new NeuralBotFactory());
        KeyboardHookManager.addListener(new NativeKeyboardListener() {

            public boolean keyPressed(NativeKeyboardEvent nke) {
                if (nke.getKeyCode() == ManageKeySettings.EXIT_KEY) {
                    System.exit(0);
                } else if (nke.getKeyCode() == ManageKeySettings.START_KEY) {
                    gameDirector.start();
                    isActivatedItem.setLabel("Activated");
                    return false;
                } else if (nke.getKeyCode() == ManageKeySettings.PAUSE_RESUME_KEY) {
                    if (!gameDirector.isPaused()) {
                        gameDirector.pauseGame();
                    } else {
                        gameDirector.resumeGame();
                    }
                    return false;
                } else if (nke.getKeyCode() == ManageKeySettings.STOP_KEY) {
                    gameDirector.stop();
                    return false;
                } else if (nke.getKeyCode() == ManageKeySettings.CHANGE_INTERACTOR_KEY) {
                    gameDirector.changeInteractor(!humanManage);
                    humanManage = !humanManage;
                    return false;
                }
                return true;
            }

            public boolean keyReleased(NativeKeyboardEvent nke) {
                if (nke.getKeyCode() == ManageKeySettings.EXIT_KEY) {
                    return false;
                } else if (nke.getKeyCode() == ManageKeySettings.START_KEY) {
                    return false;
                } else if (nke.getKeyCode() == ManageKeySettings.PAUSE_RESUME_KEY) {
                    return false;
                } else if (nke.getKeyCode() == ManageKeySettings.STOP_KEY) {
                    return false;
                } else if (nke.getKeyCode() == ManageKeySettings.CHANGE_INTERACTOR_KEY) {
                    return false;
                }
                return true;
            }
        });
        getContentPane().add(new JColorChooser());
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        Image image = ImageIO.read(new File(VALENCIA_ICON_PATH));
        TrayIcon trayIcon = new TrayIcon(image, VALENCIA_TRAY_MESSAGE);

        trayIcon.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (!isVisible()) {
                    setVisible(true);
                }
            }
        });
        PopupMenu popupMenu = new PopupMenu();
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                dispose();
                System.exit(0);
            }
        });
        sparItem = new CheckboxMenuItem("SB");
        sparItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                uncheckAll();
                sparItem.setEnabled(false);
            }
        });
        mathBotItem = new CheckboxMenuItem("MB");
        mathBotItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                uncheckAll();
                mathBotItem.setEnabled(false);
            }
        });
        fellOmenItem = new CheckboxMenuItem("FO");
        fellOmenItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                uncheckAll();
                fellOmenItem.setEnabled(false);
                gameDirector.setFLBotFactory(new FellOmenFactory());
            }
        });

        Menu createFixedLimitItem = new Menu("F");
        Menu createNoLimitItem = new Menu("N");

        createFixedLimitItem.add(fellOmenItem);
        createFixedLimitItem.add(mathBotItem);
        createFixedLimitItem.add(sparItem);

        isActivatedItem = new MenuItem("Disactivated");

        popupMenu.add(createNoLimitItem);
        popupMenu.add(createFixedLimitItem);
        popupMenu.add(isActivatedItem);
        popupMenu.add(exitItem);

        trayIcon.setPopupMenu(popupMenu);
        systemTray.add(trayIcon);
    }

    private void uncheckAll() {
        sparItem.setState(true);
        fellOmenItem.setState(true);
        mathBotItem.setState(true);
    }

    public static void main(String[] args) throws IOException, AWTException {
        GameTrayFrame trayWindow = new GameTrayFrame();
        trayWindow.pack();
        trayWindow.setLocationRelativeTo(null);
        trayWindow.setVisible(true);
    }
}
