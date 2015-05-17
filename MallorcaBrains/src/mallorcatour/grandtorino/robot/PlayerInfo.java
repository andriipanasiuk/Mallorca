/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.grandtorino.robot;

/**
 * Class for representing player's info  from table(nickname, stack and bet)
 * @author Andrew
 */
public class PlayerInfo {

    private String name;
    private boolean isOnButton;
    double stack;
    double bet;

    public PlayerInfo(String name, boolean isOnButton) {
        this.name = name;
        this.isOnButton = isOnButton;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    public void changeName(String name) {
        this.name = name;
    }

    /**
     * @return the stack
     */
    public double getStack() {
        return stack;
    }

    /**
     * @return the bet
     */
    public double getBet() {
        return bet;
    }

    /**
     * @return the isOnButton
     */
    public boolean isOnButton() {
        return isOnButton;
    }
}
