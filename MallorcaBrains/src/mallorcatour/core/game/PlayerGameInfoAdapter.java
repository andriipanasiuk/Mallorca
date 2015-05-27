package mallorcatour.core.game;

import java.util.List;

import mallorcatour.core.game.interfaces.IGameInfo;
import mallorcatour.core.game.interfaces.IPlayerGameInfo;

public class PlayerGameInfoAdapter implements IPlayerGameInfo {

	private final IGameInfo gameInfo;
	private final OpenPlayerInfo villainInfo;
	private final OpenPlayerInfo heroInfo;

	public PlayerGameInfoAdapter(IGameInfo gameInfo, OpenPlayerInfo heroInfo, OpenPlayerInfo villainInfo) {
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
	public OpenPlayerInfo getVillain() {
		return villainInfo;
	}

	@Override
	public double getAmountToCall() {
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
	public OpenPlayerInfo getHero() {
		return heroInfo;
	}

	@Override
	public boolean onButton(String name) {
		if (name.equals(heroInfo.name)) {
			return heroInfo.isOnButton;
		} else if (name.equals(villainInfo.name)) {
			return villainInfo.isOnButton;
		} else {
			throw new RuntimeException("Incorrect name: " + name);
		}
	}

	@Override
	public PlayerInfo getHero(String hero) {
		return heroInfo.name.equals(hero) ? heroInfo : villainInfo;
	}

	@Override
	public PlayerInfo getVillain(String hero) {
		return heroInfo.name.equals(hero) ? villainInfo : heroInfo;
	}

	@Override
	public double getAmountToCall(String hero) {
		return Math.max(0, getVillain(hero).bet - getHero(hero).bet);
	}
}