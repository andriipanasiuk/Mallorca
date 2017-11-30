package mallorcatour.core.equilator;

import mallorcatour.core.game.Card;
import mallorcatour.core.game.state.StreetEquity;
import mallorcatour.equilator.PokerEquilatorBrecher;

/**
 * Песочница для проверок корректности эквилятора на картах, где присутствует флэш.
 */
public class EquilatorFlushTest {

	public static Card[] getFlushDro3() {
		Card card1 = Card.valueOf("Ad");
		Card card2 = Card.valueOf("Kd");
		Card flop1 = Card.valueOf("Qd");
		Card flop2 = Card.valueOf("Td");
		Card flop3 = Card.valueOf("8s");
		return new Card[] { card1, card2, flop1, flop2, flop3 };
	}

	public static Card[] getFlushDro1() {
		Card card1 = Card.valueOf("Ad");
		Card card2 = Card.valueOf("Kd");
		Card flop1 = Card.valueOf("Ks");
		Card flop2 = Card.valueOf("Td");
		Card flop3 = Card.valueOf("8d");
		return new Card[] { card1, card2, flop1, flop2, flop3 };
	}

	public static Card[] getFlushDro4() {
		Card card1 = Card.valueOf("Qd");
		Card card2 = Card.valueOf("Jd");
		Card flop1 = Card.valueOf("Ks");
		Card flop2 = Card.valueOf("Td");
		Card flop3 = Card.valueOf("8d");
		return new Card[] { card1, card2, flop1, flop2, flop3 };
	}

	public static Card[] getFlushDro6() {
		Card card1 = Card.valueOf("2d");
		Card card2 = Card.valueOf("3d");
		Card flop1 = Card.valueOf("Ks");
		Card flop2 = Card.valueOf("Td");
		Card flop3 = Card.valueOf("8d");
		return new Card[] { card1, card2, flop1, flop2, flop3 };
	}

	public static Card[] getFlushDro2() {
		Card card1 = Card.valueOf("Qd");
		Card card2 = Card.valueOf("Td");
		Card flop1 = Card.valueOf("Kd");
		Card flop2 = Card.valueOf("Ts");
		Card flop3 = Card.valueOf("8d");
		return new Card[] { card1, card2, flop1, flop2, flop3 };
	}

	public static Card[] getFlushDro5() {
		Card card1 = Card.valueOf("Qd");
		Card card2 = Card.valueOf("9d");
		Card flop1 = Card.valueOf("Kd");
		Card flop2 = Card.valueOf("Ts");
		Card flop3 = Card.valueOf("8d");
		return new Card[] { card1, card2, flop1, flop2, flop3 };
	}

	public static Card[] getBackdoorFlushDro() {
		Card card1 = Card.valueOf("Ac");
		Card card2 = Card.valueOf("Kd");
		Card flop1 = Card.valueOf("Qd");
		Card flop2 = Card.valueOf("Td");
		Card flop3 = Card.valueOf("8s");
		return new Card[] { card1, card2, flop1, flop2, flop3 };
	}

	public static void testFlushDro(Card[] cards) {
		StreetEquity equity = PokerEquilatorBrecher.equityOnFlopFull(false, cards);
		StreetEquity equity2 = PokerEquilatorBrecher.equityOnFlopFull(true, cards);
		System.out.println("-------");
		double winWillWin = equity.winWillWin();
		double winWillLose = equity.winWillLose();
		double loseWillWin = equity.loseWillWin();
		double loseWillLose = equity.loseWillLose();
		double winWillWin2 = equity2.winWillWin();
		double winWillLose2 = equity2.winWillLose();
		double loseWillWin2 = equity2.loseWillWin();
		double loseWillLose2 = equity2.loseWillLose();
		System.out.println(winWillWin + " -> " + winWillWin2 + " " + percent(winWillWin, winWillWin2));
		System.out.println(winWillLose + " -> " + winWillLose2 + " " + percent(winWillLose, winWillLose2));
		System.out.println(loseWillWin + " -> " + loseWillWin2 + " " + percent(loseWillWin, loseWillWin2));
		System.out.println(loseWillLose + " -> " + loseWillLose2 + " " + percent(loseWillLose, loseWillLose2));
	}

	private static String percent(double one, double two) {
		if (one == 0) {
			return "+100%";
		}
		int percent = (int) ((two - one) / one * 100);
		if (percent > 0) {
			return "+" + percent + "%";
		} else {
			return percent + "%";
		}
	}

	public void test() {
		PokerEquilatorBrecher.LOGGING = false;
		testFlushDro(getFlushDro1());
		testFlushDro(getFlushDro2());
		testFlushDro(getFlushDro3());
		testFlushDro(getFlushDro4());
		testFlushDro(getFlushDro5());
		testFlushDro(getFlushDro6());
	}
}
