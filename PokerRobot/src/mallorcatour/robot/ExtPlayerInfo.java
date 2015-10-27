/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.robot;

import mallorcatour.core.game.BasePlayerInfo;

/**
 * Class for representing player's info from table(nickname, stack and bet)
 * 
 * @author Andrew
 */
public class ExtPlayerInfo extends BasePlayerInfo {

	public boolean isOnButton;
	public double bet;

	public ExtPlayerInfo(String name, boolean isOnButton) {
		super(name);
		this.isOnButton = isOnButton;
	}

	public ExtPlayerInfo(String name) {
		super(name);
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
