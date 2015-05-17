/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.game.situation;

import mallorcatour.interfaces.ISituationHandler;
import mallorcatour.game.core.Card;
import mallorcatour.game.core.Action;
import mallorcatour.game.core.PokerStreet;
import mallorcatour.core.bot.IGameInfo;
import mallorcatour.core.bot.LimitType;

/**
 *
 * @author Andrew
 */
public class NoStrengthSituationHandler implements ISituationHandler {

    protected IGameInfo gameInfo;  // general game information
    protected int countOfHeroActions, countOfHeroAggressive, countOfOppActions,
            countOfOppAggressive;
    protected boolean wasHeroPreviousAggressive, wasVillainPreviousAggressive;
    protected String heroName, villainName;
    private double strength, positivePotential, negativePotential;
    protected LimitType limitType;

    public NoStrengthSituationHandler(LimitType limitType) {
        this.limitType = limitType;
    }

    /**
     * An event called to tell us our hole cards and seat number
     * @param c1 your first hole card
     * @param c2 your second hole card
     * @param seat your seat number at the table
     */
    public void onHoleCards(Card c1, Card c2, String heroName, String villainName) {
        this.heroName = heroName;
        this.villainName = villainName;
        strength = -1;
        positivePotential = LocalSituation.DEFAULT_POTENTIAL;
        negativePotential = LocalSituation.DEFAULT_POTENTIAL;
    }

    private LocalSituation getHeroSituation() {
        LocalSituation result = null;
        boolean isOnButton;
        if (heroName.equals(gameInfo.getButtonName())) {
            isOnButton = true;
        } else {
            isOnButton = false;
        }
        //potOdds
        double toCall = gameInfo.getHeroAmountToCall();
        double pot = gameInfo.getPotSize();
        double effectiveStack = gameInfo.getBankRollAtRisk();
        double potOdds = toCall / (toCall + pot);
        double stackProportion = gameInfo.getBankRoll(heroName)
                / (gameInfo.getBankRoll(heroName) + gameInfo.getBankRoll(villainName));

        if (gameInfo.isPreFlop()) {
            result = new LocalSituation(LocalSituation.PREFLOP, limitType);
        } else if (gameInfo.isFlop()) {
            //equities
            result = new LocalSituation(LocalSituation.FLOP, limitType);
            result.setPositivePotential(positivePotential);
            result.setNegativePotential(negativePotential);
        } else if (gameInfo.isTurn()) {
            //equities
            result = new LocalSituation(LocalSituation.TURN, limitType);
            result.setPositivePotential(positivePotential);
            result.setNegativePotential(negativePotential);
        } else if (gameInfo.isRiver()) {
            //equities
            result = new LocalSituation(LocalSituation.RIVER, limitType);
        }
        result.setLocalAggresion(countOfHeroActions != 0 ? (double) countOfHeroAggressive / countOfHeroActions : 0);
        result.setLocalOpponentAggresion(countOfOppActions != 0 ? (double) countOfOppAggressive / countOfOppActions : 0);
        result.wasHeroPreviousAggresive(wasHeroPreviousAggressive);
        result.wasOpponentPreviousAggresive(wasVillainPreviousAggressive);
        result.setStrength(strength);
        result.setStackProportion(stackProportion);
        result.setPotOdds(potOdds);
        result.isOnButton(isOnButton);
        result.setPotToStackOdds((pot + toCall) / (pot + toCall + effectiveStack));
        result.setFLPotSize(1 - (2 * gameInfo.getBigBlindSize()) / pot);
        result.canRaise(gameInfo.canHeroRaise());
        return result;
    }

    public LocalSituation onHeroSituation() {
        LocalSituation result = getHeroSituation();
        return result;
    }

    public void onHeroActed(Action action) {
        if (action.isAggressive()) {
            wasHeroPreviousAggressive = true;
            countOfHeroAggressive++;
        } else if (action.isPassive()) {
            wasHeroPreviousAggressive = false;
        }
        countOfHeroActions++;
    }

    /**
     * A new betting round has started.
     */
    public void onStageEvent(PokerStreet street) {
        //do nothing
    }

    /**
     * A new game has been started.
     * @param gi the game stat information
     */
    public void onHandStarted(IGameInfo gameInfo) {
        this.gameInfo = gameInfo;
        countOfHeroActions = 0;
        countOfHeroAggressive = 0;
        countOfOppActions = 0;
        countOfOppAggressive = 0;
        wasHeroPreviousAggressive = false;
        wasVillainPreviousAggressive = false;
    }

    /**
     * An villain action has been observed.
     */
    public void onVillainActed(Action action, double toCall) {
        if (action.isAggressive()) {
            wasVillainPreviousAggressive = true;
            countOfOppActions++;
            countOfOppAggressive++;
        } else if (action.isPassive()) {
            wasVillainPreviousAggressive = false;
            countOfOppActions++;
        }
    }

    public void beforeHeroAction() {
        //do nothing
    }

    public void onHandEnded() {
        //do nothing
    }
}