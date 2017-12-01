package mallorcatour.core.game;

import java.util.List;

import mallorcatour.core.game.interfaces.GameContext;

public class PlayerGameInfoAdapter implements GameContext {

	private final GameContext gameInfo;
	private final OpenPlayerInfo villainInfo;
	private final OpenPlayerInfo heroInfo;

	public PlayerGameInfoAdapter(GameContext gameInfo, OpenPlayerInfo heroInfo, OpenPlayerInfo villainInfo) {
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
		return gameInfo.getAmountToCall(hero);
	}

	@Override
	public double getPot(PokerStreet street) {
		return gameInfo.getPot(street);
	}
}