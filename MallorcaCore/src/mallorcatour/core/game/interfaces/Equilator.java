package mallorcatour.core.game.interfaces;

import mallorcatour.core.game.Card;
import mallorcatour.core.game.Flop;
import mallorcatour.core.game.state.StreetEquity;

public interface Equilator {
    double strengthOnRiver(Card holeCard1, Card holeCard2, Card flop1, Card flop2, Card flop3, Card turn, Card river);

    StreetEquity equityOnTurn(Card holeCard1, Card holeCard2, Card flop1, Card flop2, Card flop3, Card turn);

    StreetEquity equityOnFlop(Card holeCard1, Card holeCard2, Card flop1, Card flop2, Card flop3);

    StreetEquity equityOnFlopFull(Card holeCard1, Card holeCard2, Card flop1, Card flop2, Card flop3, boolean b);

    StreetEquity equityOnFlopFull(Card first, Card second, Flop flop, boolean b);

    StreetEquity equityOnFlop(Card first, Card second, Flop flop);

    double strengthOnTurn(Card first, Card second, Card flop1, Card flop2, Card flop3, Card turn);

    double strengthOnFlop(Card first, Card second, Card flop1, Card flop2, Card flop3);
}
