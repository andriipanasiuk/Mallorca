/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.core.game.situation;

import mallorcatour.core.equilator.PokerEquilatorBrecher;
import mallorcatour.core.equilator.StreetEquity;
import mallorcatour.core.equilator.preflop.EquilatorPreflop;
import mallorcatour.core.game.Action;
import mallorcatour.core.game.Card;
import mallorcatour.core.game.LimitType;
import mallorcatour.core.game.PokerStreet;
import mallorcatour.core.game.interfaces.IGameInfo;
import mallorcatour.core.game.interfaces.IGameObserver;

/**
 *
 * @author Andrew
 */
public class SituationHandler implements ISituationHandler, IGameObserver {

    protected IGameInfo gameInfo;  // general game information
    protected Card holeCard1, holeCard2, flop1, flop2, flop3, turn, river;
    protected int heroActionCount, countOfHeroAggressive, villainActionCount,
            countOfOppAggressive;
    protected boolean wasHeroPreviousAggressive, wasVillainPreviousAggressive;
    protected String heroName, villainName;
    private double strength, positivePotential, negativePotential;
    protected LimitType limitType;
    private final boolean needFullPotentialOnFlop;

    public SituationHandler(LimitType limitType) {
        this.limitType = limitType;
        needFullPotentialOnFlop = false;
    }

	public SituationHandler(LimitType limitType, boolean needFullPotentialOnFlop) {
		this.limitType = limitType;
		this.needFullPotentialOnFlop = needFullPotentialOnFlop;
	}

    @Override
	public void onHoleCards(Card c1, Card c2, String heroName, String villainName) {
        this.holeCard1 = c1;
		this.holeCard2 = c2;
		StreetEquity equity = EquilatorPreflop.equityVsRandom(holeCard1, holeCard2);
		strength = equity.strength;
		positivePotential = equity.positivePotential;
		negativePotential = equity.negativePotential;
        this.heroName = heroName;
        this.villainName = villainName;
    }

    private LocalSituation getHeroSituation() {
        LocalSituation result = null;
        boolean isOnButton;
        if (heroName.equals(gameInfo.getButtonName())) {
            isOnButton = true;
        } else {
            isOnButton = false;
        }
        double toCall = gameInfo.getHeroAmountToCall();
        double pot = gameInfo.getPotSize();
        double effectiveStack = gameInfo.getBankRollAtRisk();
        double potOdds = toCall / (toCall + pot);
        double stackProportion = gameInfo.getBankRoll(heroName)
                / (gameInfo.getBankRoll(heroName) + gameInfo.getBankRoll(villainName));

        if (gameInfo.isPreFlop()) {
            result = new LocalSituation(LocalSituation.PREFLOP, limitType);
        } else if (gameInfo.isFlop()) {
            result = new LocalSituation(LocalSituation.FLOP, limitType);
        } else if (gameInfo.isTurn()) {
            result = new LocalSituation(LocalSituation.TURN, limitType);
        } else if (gameInfo.isRiver()) {
            result = new LocalSituation(LocalSituation.RIVER, limitType);
        }
        result.setHeroAggresionActionCount(countOfHeroAggressive);
        result.setHeroActionCount(heroActionCount);
        result.setVillainAggresionActionCount(countOfOppAggressive);
        result.setVillainActionCount(villainActionCount);
        result.wasHeroPreviousAggresive(wasHeroPreviousAggressive);
        result.wasOpponentPreviousAggresive(wasVillainPreviousAggressive);
        result.setStrength(strength);
        result.setPositivePotential(positivePotential);
        result.setNegativePotential(negativePotential);
        result.setStackProportion(stackProportion);
        result.setPotOdds(potOdds);
        result.isOnButton(isOnButton);
        result.setPotToStackOdds((pot + toCall) / (pot + toCall + effectiveStack));
        result.setFLPotSize(1 - (2 * gameInfo.getBigBlindSize()) / pot);
        result.canRaise(gameInfo.canHeroRaise());
        return result;
    }

    @Override
    public LocalSituation onHeroSituation() {
        LocalSituation result = getHeroSituation();
        return result;
    }

    @Override
	public void onHeroActed(Action action) {
        if (action.isAggressive()) {
            wasHeroPreviousAggressive = true;
            countOfHeroAggressive++;
        } else if (action.isPassive()) {
            wasHeroPreviousAggressive = false;
        }
        heroActionCount++;
    }

    @Override
	public void onStageEvent(PokerStreet street) {
        if (street == PokerStreet.FLOP) {
            flop1 = gameInfo.getBoard().get(0);
            flop2 = gameInfo.getBoard().get(1);
			flop3 = gameInfo.getBoard().get(2);
			StreetEquity flopEquity;
			if (needFullPotentialOnFlop) {
				flopEquity = PokerEquilatorBrecher.equityOnFlopFull(holeCard1, holeCard2, flop1, flop2, flop3, true);
			} else {
				flopEquity = PokerEquilatorBrecher.equityOnFlop(holeCard1, holeCard2, flop1, flop2, flop3);
			}
            strength = flopEquity.strength;
            positivePotential = flopEquity.positivePotential;
            negativePotential = flopEquity.negativePotential;
        } else if (street == PokerStreet.TURN) {
            turn = gameInfo.getBoard().get(3);
            StreetEquity turnEquity = PokerEquilatorBrecher.equityOnTurn(holeCard1,
                    holeCard2, flop1, flop2, flop3, turn);
            strength = turnEquity.strength;
            positivePotential = turnEquity.positivePotential;
            negativePotential = turnEquity.negativePotential;
        } else if (street == PokerStreet.RIVER) {
            river = gameInfo.getBoard().get(4);
            strength = PokerEquilatorBrecher.strengthOnRiver(holeCard1, holeCard2,
                    flop1, flop2, flop3, turn, river);
            positivePotential = 0;
            negativePotential = 0;
        }
    }

    /* (non-Javadoc)
	 * @see mallorcatour.core.game.situation.IGameObserver#onHandStarted(mallorcatour.core.game.interfaces.IGameInfo)
	 */
    @Override
	public void onHandStarted(IGameInfo gameInfo) {
        this.gameInfo = gameInfo;
        heroActionCount = 0;
        countOfHeroAggressive = 0;
        villainActionCount = 0;
        countOfOppAggressive = 0;
        wasHeroPreviousAggressive = false;
        wasVillainPreviousAggressive = false;
    }

    /* (non-Javadoc)
	 * @see mallorcatour.core.game.situation.IGameObserver#onVillainActed(mallorcatour.core.game.Action, double)
	 */
    @Override
	public void onVillainActed(Action action, double toCall) {
        if (action.isAggressive()) {
            wasVillainPreviousAggressive = true;
            villainActionCount++;
            countOfOppAggressive++;
        } else if (action.isPassive()) {
            wasVillainPreviousAggressive = false;
            villainActionCount++;
        }
    }

    /* (non-Javadoc)
	 * @see mallorcatour.core.game.situation.IGameObserver#onHandEnded()
	 */
    @Override
	public void onHandEnded() {
        //do nothing
    }
}
