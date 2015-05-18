/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mallorcatour.windowfinder;

import br.com.wagnerpaz.javahook.NativeKeyboardEvent;
import br.com.wagnerpaz.javahook.NativeKeyboardListener;
import java.awt.event.KeyEvent;

import mallorcatour.robot.hardwaremanager.KeyboardHookManager;
import mallorcatour.util.Log;

/**
 *
 * @author Andrew
 */
public class KeyboardTester {

    public static void main(String[] args){
        KeyboardHookManager.addListener(new NativeKeyboardListener() {

            public boolean keyPressed(NativeKeyboardEvent nke) {
                if(nke.getKeyCode() == KeyEvent.VK_F){
                    Log.d(nke.getWhen() + "");
                    Log.d("f");
                }
                return true;
            }

            public boolean keyReleased(NativeKeyboardEvent nke) {
                return true;
            }
        });
    }
}
