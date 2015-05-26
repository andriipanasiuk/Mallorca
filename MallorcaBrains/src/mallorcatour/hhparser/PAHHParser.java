/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.hhparser;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import mallorcatour.core.game.Action;
import mallorcatour.core.game.Card;
import mallorcatour.core.game.Flop;
import mallorcatour.core.game.HoleCards;
import mallorcatour.core.game.LimitType;
import mallorcatour.core.game.OpenPlayerInfo;
import mallorcatour.hhparser.core.IHandHandler;
import mallorcatour.hhparser.core.ITournamentHandler;
import mallorcatour.util.DateUtils;
import mallorcatour.util.ReaderUtils;
import mallorcatour.util.StringUtils;

/**
 *
 * @author Andrew
 */
public class PAHHParser {

    private static final String START = "Start: ";
    private static final String GAME_TYPE = "Game Type: ";
    private static final String NUMBER_OF_PLAYERS = "Number of Players: ";
    private static final String TOURNAMENT_SUMMARY = "Tournament Summary";
    @SuppressWarnings("unused")
	private static final String NEW_HAND = "Poker Academy Pro";
    private static final String NEW_HAND_POKER_GENIUS = "Poker Genius";
    private static final String END_HAND = "****";
    private static final String FLOP = "FLOP:  ";
    private static final String TURN = "TURN:  ";
    private static final String RIVER = "RIVER:  ";
    private static final String CHECKS = "checks";
    private static final String CALLS = "calls";
    private static final String FOLDS = "folds";
    private static final String RAISES = "raises";
    private static final String BETS = "bets";
    private static final String NO_LIMIT = "No Limit";
    private static final String LIMIT = "Limit";

    public static void parseTournamentHH(String filename, ITournamentHandler handler) {
        boolean isFirstInFile = true;
        String winnerName = null;
        BufferedReader reader = ReaderUtils.initReader(filename);

        String buffer = ReaderUtils.readLine(reader);
        while (buffer != null) {
            // tournament start
            if (buffer.contains(TOURNAMENT_SUMMARY)) {
                if (!isFirstInFile) {
                    handler.onTournamentEnd(winnerName);
                } else {
                    isFirstInFile = false;
                }
                parseTopTournament(reader, handler);
            } //new hand
            else {
                processHandParsing(buffer, handler, reader);
            }
            // new line reading
            buffer = ReaderUtils.readLine(reader);
        }
        handler.onTournamentEnd(winnerName);
    }

    private static void processHandParsing(String buffer, IHandHandler handler,
            BufferedReader reader) {
        if (buffer.startsWith(NEW_HAND_POKER_GENIUS)) {
            parseNewHand(buffer, reader, handler);

        } //flop, turn and river
        else if (buffer.contains(FLOP)) {
            Flop flop = parseFlop(buffer);
            handler.onFlop(flop.first, flop.second, flop.third);
        } else if (buffer.contains(TURN)) {
            handler.onTurn(parseTurn(buffer));
        } else if (buffer.contains(RIVER)) {
            handler.onRiver(parseRiver(buffer));
        } //player actions
        else if (buffer.contains(CHECKS)) {
            String name = StringUtils.between(buffer, 0, CHECKS).trim();
            handler.onPlayerActed(name, Action.checkAction());
        } else if (buffer.contains(CALLS)) {
            String name = StringUtils.between(buffer, 0, CALLS).trim();
            handler.onPlayerActed(name, Action.callAction(parseNumber(buffer)));
        } else if (buffer.contains(RAISES)) {
            String name = StringUtils.between(buffer, 0, RAISES).trim();
            handler.onPlayerActed(name, Action.raiseAction(parseNumber(buffer)));
        } else if (buffer.contains(BETS)) {
            String name = StringUtils.between(buffer, 0, BETS).trim();
            handler.onPlayerActed(name, Action.betAction(parseNumber(buffer)));
        } else if (buffer.contains(FOLDS)) {
            String name = StringUtils.between(buffer, 0, FOLDS).trim();
            handler.onPlayerActed(name, Action.foldAction());
        } else if (buffer.startsWith(END_HAND)) {
            handler.onHandEnded();
        }
    }

    public static void parseHandHistory(String filename, IHandHandler handler) {
        BufferedReader reader = ReaderUtils.initReader(filename);

        String buffer = ReaderUtils.readLine(reader);
        while (buffer != null) {
            // new hand
            processHandParsing(buffer, handler, reader);
            // new line reading
            buffer = ReaderUtils.readLine(reader);
        }
    }

    private static int parseNumber(String buffer) {
        return Integer.parseInt(StringUtils.between(buffer, "$", " ").replaceAll(",", ""));
    }

    private static Flop parseFlop(String flop) {
        int from = flop.indexOf(FLOP) + FLOP.length();
        Card flop1 = Card.valueOf(flop.substring(from, from + 2));
        Card flop2 = Card.valueOf(flop.substring(from + 3, from + 5));
        Card flop3 = Card.valueOf(flop.substring(from + 6, from + 8));
        return new Flop(flop1, flop2, flop3);
    }

    private static Card parseTurn(String turn) {
        int from = turn.indexOf(TURN) + TURN.length();
        Card turnCard = Card.valueOf(turn.substring(from + 9, from + 11));
        return turnCard;
    }

    private static Card parseRiver(String river) {
        int from = river.indexOf(RIVER) + RIVER.length();
        Card riverCard = Card.valueOf(river.substring(from + 12, from + 14));
        return riverCard;
    }

	/**
	 * Reader will be on the line "****..** after method has finished
	 */
	private static void parseTopTournament(BufferedReader reader, ITournamentHandler handler) {
        int playerCount = 0;
        String description = null;
        Date startingDate = null;
        String buffer = ReaderUtils.readLine(reader);
        while (!buffer.startsWith(END_HAND)) {
            if (buffer.startsWith(START)) {
                startingDate = DateUtils.parsePADate(buffer.substring(START.length(), buffer.indexOf("EE") - 2));
            } else if (buffer.startsWith(GAME_TYPE)) {
                description = buffer.substring(GAME_TYPE.length());
            } else if (buffer.startsWith(NUMBER_OF_PLAYERS)) {
                playerCount = Integer.parseInt(buffer.substring(NUMBER_OF_PLAYERS.length()));
            }
            buffer = ReaderUtils.readLine(reader);
        }
        handler.onTournamentStart(startingDate, playerCount, description);
    }

    private static void parseNewHand(String buffer, BufferedReader reader, IHandHandler handler) {
        double smallBlind = 0, bigBlind = 0;
        List<OpenPlayerInfo> players = new ArrayList<>();
        Date startingDate = null;
        LimitType limitType = null;
        String playerOnButton = null;

        long id = Long.parseLong(StringUtils.between(buffer, "#", " ").replaceAll(",", ""));
        // blinds and limitType parsing
        buffer = ReaderUtils.readLine(reader);
        if (buffer.contains(NO_LIMIT)) {
            limitType = LimitType.NO_LIMIT;
            smallBlind = Double.parseDouble(StringUtils.between(buffer, "($", "/"));
            bigBlind = Double.parseDouble(StringUtils.between(buffer, "/$", " NL"));
        } else if (buffer.contains(LIMIT)) {
            limitType = LimitType.FIXED_LIMIT;
            smallBlind = Double.parseDouble(StringUtils.between(buffer, "($", "/")) / 2;
            bigBlind = Double.parseDouble(StringUtils.between(buffer, "/$", ")")) / 2;
        }

        // startingDate parsing
        ReaderUtils.skipLines(reader, 1);
        buffer = ReaderUtils.readLine(reader);
        startingDate = DateUtils.parsePADate(StringUtils.between(buffer, 0, " (EE"));

        //player infos parsing
        ReaderUtils.skipLines(reader, 1);
        buffer = ReaderUtils.readLine(reader);
        while (!buffer.equals("")) {
            String name = parseName(buffer);
            if (buffer.contains("*")) {
                playerOnButton = name;
            }
            int stack = parseNumber(buffer);
            HoleCards cards = parseHoleCards(buffer);
            players.add(new OpenPlayerInfo(name, stack, cards.first, cards.second));
            buffer = ReaderUtils.readLine(reader);
        }
        handler.onHandStarted(id, startingDate, players, playerOnButton, smallBlind,
                bigBlind, limitType);
    }

    private static String parseName(String buffer) {
        String from = null;
        String to = null;
        if (buffer.contains(")")) {
            from = ") ";
        } else if (buffer.contains("}")) {
            from = "} ";
        } else {
            throw new RuntimeException("Parse name. There is some problem in " + buffer);
        }
        if (buffer.contains("*")) {
            to = " *";
        } else {
            to = "  ";
        }
        return StringUtils.between(buffer, from, to);
    }

    private static HoleCards parseHoleCards(String withHoleCards) {
        int from1 = withHoleCards.indexOf(" ", withHoleCards.indexOf("$")) + 2;
        Card card1 = Card.valueOf(withHoleCards.substring(from1, from1 + 2));
        Card card2 = Card.valueOf(withHoleCards.substring(from1 + 3, from1 + 5));
        return new HoleCards(card1, card2);
    }
}
