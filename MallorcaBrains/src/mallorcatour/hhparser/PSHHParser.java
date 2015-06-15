/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mallorcatour.hhparser;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mallorcatour.core.game.Action;
import mallorcatour.core.game.Card;
import mallorcatour.core.game.Flop;
import mallorcatour.core.game.LimitType;
import mallorcatour.core.game.OpenPlayerInfo;
import mallorcatour.hhparser.core.IHandHandler;
import mallorcatour.tools.DateUtils;
import mallorcatour.tools.ReaderUtils;
import mallorcatour.tools.StringUtils;

/**
 *
 * @author Andrew
 */
public class PSHHParser {

    private static final String START = "Start: ";
    private static final String GAME_TYPE = "Game Type: ";
    private static final String NUMBER_OF_PLAYERS = "Number of Players: ";
    private static final String TOURNAMENT_SUMMARY = "Tournament Summary";
    private static final String NEW_HAND = "PokerStars";
    private static final String IS_SITTING_OUT = "is sitting out";

    private static final String HOLE_CARDS = "HOLE CARDS";
    private static final String FLOP = "FLOP";
    private static final String TURN = "TURN";
    private static final String RIVER = "RIVER";
    private static final String SUMMARY = "SUMMARY";
    private static final String CHECKS = "checks";
    private static final String CALLS = "calls";
    private static final String FOLDS = "folds";
    private static final String RAISES = "raises";
    private static final String BETS = "bets";
    private static final String NO_LIMIT = "No Limit";
    private static final String LIMIT = "Limit";
    private static final String HAND_NUMBER = "Hand #";
    private static final Pattern BLINDS_PATTERN = Pattern.compile("\\(\\${0,1}(.+)/\\${0,1}(.+)\\)");
    private static final Pattern PLAYER_INFO_PATTERN = Pattern.compile("Seat (\\d+): (.+)\\s\\((.+) in");
    private static final Pattern HOLE_CARDS_PATTERN = Pattern.compile("Dealt to (.+) \\[(.+) (.+)\\]");
    private static final Pattern RAISE_AMOUNT_PATTERN = Pattern.compile("\\s(\\d+[\\,\\d+]*[\\.\\d+]{0,1})\\s|$");
    private static final Pattern FLOP_PATTERN = Pattern.compile("\\[(..) (..) (..)\\]");
    private static final Pattern TURN_PATTERN = Pattern.compile("\\[.. .. ..\\] \\[(..)\\]");
    private static final Pattern RIVER_PATTERN = Pattern.compile("\\[.. .. .. ..\\] \\[(..)\\]");
    private static final Pattern DATE_PATTERN = Pattern.compile("- (\\d{4}/\\d{2}/\\d{2} \\d{1,2}:\\d{2}:\\d{2}) ");
    private static final String PS_DATE_FORMAT = "yyyy/MM/dd HH:mm:ss";

//    public static void parseTournamentHH(String filename, ITournamentHandler handler) {
//        boolean isFirstInFile = true;
//        String winnerName = null;
//        BufferedReader reader = ReaderUtils.initReader(filename);
//
//        String buffer = ReaderUtils.readLine(reader);
//        while (buffer != null) {
//            // tournament start
//            if (buffer.contains(TOURNAMENT_SUMMARY)) {
//                if (!isFirstInFile) {
//                    handler.onTournamentEnd(winnerName);
//                } else {
//                    isFirstInFile = false;
//                }
//                parseTopTournament(reader, handler);
//            } //new hand
//            else {
//                processHandParsing(buffer, handler, reader);
//            }
//            // new line reading
//            buffer = ReaderUtils.readLine(reader);
//        }
//        handler.onTournamentEnd(winnerName);
//    }
    private static void processHandParsing(String buffer, IHandHandler handler,
            BufferedReader reader) {
        if (buffer.contains(NEW_HAND)) {
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
            String name = StringUtils.between(buffer, 0, ":").trim();
            handler.onPlayerActed(name, Action.checkAction());
        } else if (buffer.contains(CALLS)) {
            String name = StringUtils.between(buffer, 0, ":").trim();
            double amount = Double.parseDouble(StringUtils.between(buffer, CALLS + " ", " "));
            handler.onPlayerActed(name, Action.callAction(amount));
        } else if (buffer.contains(RAISES)) {
            String name = StringUtils.between(buffer, 0, ":").trim();
            double amount = Double.parseDouble(StringUtils.between(buffer, RAISES + " ", " to"));
            handler.onPlayerActed(name, Action.raiseAction(amount));
        } else if (buffer.contains(BETS)) {
            String name = StringUtils.between(buffer, 0, ":").trim();
            double amount = Double.parseDouble(StringUtils.between(buffer, BETS + " ", " "));
            handler.onPlayerActed(name, Action.betAction(amount));
        } else if (buffer.contains(FOLDS)) {
            String name = StringUtils.between(buffer, 0, ":").trim();
            handler.onPlayerActed(name, Action.fold());
        } else if (buffer.contains(SUMMARY)) {
            handler.onHandEnded();
        }
    }

    public static void parseHandHistory(String filename, IHandHandler handler) {
        BufferedReader reader = ReaderUtils.initReader(filename);

        String buffer = ReaderUtils.readLineFrom(reader);
        while (buffer != null) {
            // new hand
            processHandParsing(buffer, handler, reader);
            // new line reading
            buffer = ReaderUtils.readLineFrom(reader);
        }
    }

    private static Flop parseFlop(String flop) {
        Matcher m = FLOP_PATTERN.matcher(flop);
        if (m.find()) {
            Card flop1 = Card.valueOf(m.group(1));
            Card flop2 = Card.valueOf(m.group(2));
            Card flop3 = Card.valueOf(m.group(3));
            return new Flop(flop1, flop2, flop3);
        } else {
            throw new RuntimeException();
        }
    }

    private static Card parseTurn(String turn) {
        Matcher m = TURN_PATTERN.matcher(turn);
        if (m.find()) {
            Card card = Card.valueOf(m.group(1));
            return card;
        } else {
            throw new RuntimeException();
        }
    }

    private static Card parseRiver(String river) {
        Matcher m = RIVER_PATTERN.matcher(river);
        if (m.find()) {
            Card card = Card.valueOf(m.group(1));
            return card;
        } else {
            throw new RuntimeException();
        }
    }

    //After method works reader will be on the line "****..**"
//    private static void parseTopTournament(BufferedReader reader, ITournamentHandler handler) {
//        int playerCount = 0;
//        String description = null;
//        Date startingDate = null;
//        String buffer = ReaderUtils.readLine(reader);
//        while (!buffer.startsWith(END_HAND)) {
//            if (buffer.startsWith(START)) {
//                startingDate = DateUtils.parsePADate(buffer.substring(START.length(), buffer.indexOf("EE") - 2));
//            } else if (buffer.startsWith(GAME_TYPE)) {
//                description = buffer.substring(GAME_TYPE.length());
//            } else if (buffer.startsWith(NUMBER_OF_PLAYERS)) {
//                playerCount = Integer.parseInt(buffer.substring(NUMBER_OF_PLAYERS.length()));
//            }
//            buffer = ReaderUtils.readLine(reader);
//        }
//        handler.onTournamentStart(startingDate, playerCount, description);
//    }
    private static double parseNewHand(String buffer, BufferedReader reader, IHandHandler handler) {
        double smallBlind = 0, bigBlind = 0;
        List<OpenPlayerInfo> players = new ArrayList<>();
        Date startingDate = null;
        LimitType limitType = null;
        String playerOnButton = null;
        int indexFrom = buffer.indexOf(HAND_NUMBER);
        int indexTo = buffer.indexOf(":", indexFrom);
        long id = Long.parseLong(buffer.substring(indexFrom + HAND_NUMBER.length(), indexTo));
        // blinds and limitType parsing
        Matcher m = BLINDS_PATTERN.matcher(buffer);
        if (m.find()) {
            smallBlind = Double.parseDouble(m.group(1));
            bigBlind = Double.parseDouble(m.group(2));
        } else {
            throw new RuntimeException();
        }
        if (buffer.contains(NO_LIMIT)) {
            limitType = LimitType.NO_LIMIT;
        } else if (buffer.contains(LIMIT)) {
            limitType = LimitType.FIXED_LIMIT;
            smallBlind /= 2;
            bigBlind /= 2;
        }

        // startingDate parsing
        m = DATE_PATTERN.matcher(buffer);
        if (m.find()) {
            startingDate = DateUtils.parseDate(m.group(1), PS_DATE_FORMAT);
        } else {
            throw new RuntimeException("No date in top of hand: " + buffer);
        }

        //button seat parsing
        buffer = ReaderUtils.readLineFrom(reader);
        int buttonSeatNumber = Integer.parseInt(StringUtils.between(buffer, "Seat #", " "));

        //player infos parsing
        buffer = ReaderUtils.readLineFrom(reader);
        while (buffer.startsWith("Seat ")) {
            Matcher matcher = PLAYER_INFO_PATTERN.matcher(buffer);
            if (matcher.find()) {
                int seat = Integer.parseInt(matcher.group(1));
                String name = matcher.group(2);
                if (seat == buttonSeatNumber) {
                    playerOnButton = name;
                }
                int stack = Integer.parseInt(matcher.group(3));
                boolean isSittingOut = buffer.contains(IS_SITTING_OUT);
                players.add(new OpenPlayerInfo(name, stack, isSittingOut));
            } else {
                throw new RuntimeException();
            }
            buffer = ReaderUtils.readLineFrom(reader);
        }
        while (!buffer.contains(HOLE_CARDS)) {
            buffer = ReaderUtils.readLineFrom(reader);
        }
        buffer = ReaderUtils.readLineFrom(reader);
        m = HOLE_CARDS_PATTERN.matcher(buffer);
        if (m.find()) {
            String heroName = m.group(1);
            Card c1 = Card.valueOf(m.group(2));
            Card c2 = Card.valueOf(m.group(3));
            for (OpenPlayerInfo playerInfo : players) {
                if (playerInfo.getName().equals(heroName)) {
                    playerInfo.setHoleCards(c1, c2);
                }
            }
        } else {
            throw new RuntimeException();
        }
        handler.onHandStarted(id, startingDate, players, playerOnButton, smallBlind,
                bigBlind, limitType);
        return bigBlind;
    }

    public static void main(String[] args) {
        String d = "ilona 309: calls 10.05 sdf";
        Matcher m = RAISE_AMOUNT_PATTERN.matcher(d);
        if (m.find()) {
            System.out.println(m.group(1));
        }
    }
}
