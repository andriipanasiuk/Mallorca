package mallorcatour.core.game;

import java.util.List;

import mallorcatour.core.game.interfaces.IGameInfo;
import mallorcatour.core.game.interfaces.IPlayerGameInfo;

public class PlayerGameInfoAdapter implements IPlayerGameInfo {

	private final IGameInfo gameInfo;
	private final PlayerInfo heroInfo, villainInfo;

	public PlayerGameInfoAdapter(IGameInfo gameInfo, PlayerInfo heroInfo, PlayerInfo villainInfo) {
		this.gameInfo = gameInfo;
		this.heroInfo = heroInfo;
		this.villainInfo = villainInfo;
	}

	@Override
	public double getBigBlindSize() {
		return gameInfo.getBigBlindSize();
	}

	@Override
	public PokerStreet getStage() {
		return gameInfo.getStage();
	}

	@Override
	public boolean isPreFlop() {
		return gameInfo.isPreFlop();
	}

	@Override
	public boolean isPostFlop() {
		return gameInfo.isPostFlop();
	}

	@Override
	public boolean isFlop() {
		return gameInfo.isFlop();
	}

	@Override
	public boolean isTurn() {
		return gameInfo.isTurn();
	}

	@Override
	public boolean isRiver() {
		return gameInfo.isRiver();
	}

	@Override
	public List<Card> getBoard() {
		return gameInfo.getBoard();
	}

	@Override
	public Flop getFlop() {
		return gameInfo.getFlop();
	}

	@Override
	public Card getTurn() {
		return gameInfo.getTurn();
	}

	@Override
	public Card getRiver() {
		return gameInfo.getRiver();
	}

	@Override
	public double getPotSize() {
		return gameInfo.getPotSize();
	}

	@Override
	public double getBankRollAtRisk() {
		return gameInfo.getBankRollAtRisk();
	}

	@Override
	public int getNumRaises() {
		throw new UnsupportedOperationException();
	}

	@Override
	public LimitType getLimitType() {
		return gameInfo.getLimitType();
	}

	@Override
	public PlayerInfo getVillain() {
		return villainInfo;
	}

	@Override
	public double getHeroAmountToCall() {
		return villainInfo.bet - heroInfo.bet;
	}

	@Override
	public boolean isVillainSitOut() {
		return villainInfo.isSittingOut();
	}

	@Override
	public boolean canHeroRaise() {
		return getBankRollAtRisk() > 0;
	}

	@Override
	public boolean onButton() {
		return heroInfo.isOnButton();
	}

	@Override
	public PlayerInfo getHero() {
		return heroInfo;
	}

}