import type { Card } from '../cards/index.ts';
import { fullDeck } from '../cards/index.ts';
import { pheEvaluator } from '../equity/index.ts';
import { getOptimal } from './adviceFromMap.ts';
import type { AggressionInfo, GameContext } from './handState.ts';
import { HandState, PREFLOP, RIVER } from './handState.ts';
import { holeCardsHash } from './javaCard.ts';
import { preflopStrengthVsSpectrum } from './preflopEquilator.ts';
import type { ActionDistribution, GameSolver } from './profit.ts';
import { call, check, createBetAction, createRaiseAction, fold, RandomVariable } from './profit.ts';
import type { HoleCards } from './spectrum.ts';
import { Spectrum } from './spectrum.ts';
import type { StreetName, StrengthManager, StrengthMap } from './strength.ts';
import type { VillainModel } from './villainModel.ts';

const STREET_NAMES: readonly StreetName[] = ['preflop', 'flop', 'turn', 'river'];
const NEARLY_ZERO = 0.0001;
const DEFAULT_POSITIVE_POTENTIAL = 0.1;
const DEFAULT_NEGATIVE_POTENTIAL = 0.1;
const FIRST_BET_PERCENT = 0.75;
const FLOP_SIZE = 3;
const TURN_SIZE = 4;

interface Outcomes {
  wins: number;
  draws: number;
  count: number;
}

export class SpectrumEquilator {
  private key = '';
  private readonly outcomes = new Map<number, Outcomes>();
  private heroRanks: number[] = [];
  private runouts: Card[][] = [];

  strengthVsSpectrum(hero: HoleCards, board: readonly Card[], spectrum: Spectrum): number {
    if (board.length < FLOP_SIZE) {
      throw new Error('Cannot calculate for empty board');
    }
    this.prepare(hero, board);
    let count = 0;
    let wins = 0;
    let draw = 0;
    for (const { cards, weight } of spectrum.entries()) {
      if (hero.includes(cards[0]) || hero.includes(cards[1])) {
        continue;
      }
      if (board.includes(cards[0]) || board.includes(cards[1])) {
        continue;
      }
      const outcome = this.outcomesOf(cards, board.length);
      for (let i = 0; i < outcome.wins; i++) {
        wins += weight;
      }
      for (let i = 0; i < outcome.draws; i++) {
        draw += weight;
      }
      for (let i = 0; i < outcome.count; i++) {
        count += weight;
      }
    }
    return (draw / 2 + wins) / count;
  }

  private prepare(hero: HoleCards, board: readonly Card[]): void {
    const key = [...hero, ...board].join(',');
    if (key === this.key) {
      return;
    }
    this.key = key;
    this.outcomes.clear();
    const live = fullDeck().filter((card) => !hero.includes(card) && !board.includes(card));
    this.runouts = [];
    if (board.length === FLOP_SIZE) {
      for (let t = 0; t < live.length; t++) {
        for (let r = t + 1; r < live.length; r++) {
          this.runouts.push([...board, live[t] as Card, live[r] as Card]);
        }
      }
    } else if (board.length === TURN_SIZE) {
      this.runouts = live.map((card) => [...board, card]);
    } else {
      this.runouts = [[...board]];
    }
    this.heroRanks = this.runouts.map((runout) => pheEvaluator.rank([...hero, ...runout]));
  }

  private outcomesOf(cards: HoleCards, boardSize: number): Outcomes {
    const hash = holeCardsHash(cards[0], cards[1]);
    let result = this.outcomes.get(hash);
    if (result !== undefined) {
      return result;
    }
    const repeats = boardSize === FLOP_SIZE ? 2 : 1;
    const [first, second] = cards;
    const villain: Card[] = [first, second, 0, 0, 0, 0, 0];
    let wins = 0;
    let draws = 0;
    let count = 0;
    for (let b = 0; b < this.runouts.length; b++) {
      const runout = this.runouts[b] as Card[];
      let blocked = false;
      for (let i = boardSize; i < runout.length; i++) {
        const card = runout[i] as Card;
        blocked ||= card === first || card === second;
        villain[i + 2] = card;
      }
      if (blocked) {
        continue;
      }
      for (let i = 0; i < boardSize; i++) {
        villain[i + 2] = runout[i] as Card;
      }
      villain.length = runout.length + 2;
      const villainRank = pheEvaluator.rank(villain);
      const heroRank = this.heroRanks[b] as number;
      if (heroRank > villainRank) {
        wins += repeats;
      } else if (heroRank === villainRank) {
        draws += repeats;
      }
      count += repeats;
    }
    result = { wins, draws, count };
    this.outcomes.set(hash, result);
    return result;
  }
}

class AggressionInfoBuffer implements AggressionInfo {
  heroActionCount: number;
  villainActionCount: number;
  heroAggressionActionCount: number;
  villainAggressionActionCount: number;
  wasHeroPreviousAggressive: boolean;
  wasVillainPreviousAggressive: boolean;

  constructor(info: AggressionInfo) {
    this.heroActionCount = info.heroActionCount;
    this.villainActionCount = info.villainActionCount;
    this.heroAggressionActionCount = info.heroAggressionActionCount;
    this.villainAggressionActionCount = info.villainAggressionActionCount;
    this.wasHeroPreviousAggressive = info.wasHeroPreviousAggressive;
    this.wasVillainPreviousAggressive = info.wasVillainPreviousAggressive;
  }

  plusHeroAction(): this {
    this.heroActionCount++;
    return this;
  }

  plusHeroAggressiveAction(): this {
    this.heroAggressionActionCount++;
    return this;
  }

  plusVillainAction(aggressive: boolean): this {
    if (aggressive) {
      this.villainAggressionActionCount++;
    }
    this.villainActionCount++;
    return this;
  }
}

function raisePercent(toCall: number, street: number): number {
  return toCall !== 0 || street === PREFLOP ? 1 : FIRST_BET_PERCENT;
}

function villainSituationWithoutStrength(
  street: number,
  villainOnButton: boolean,
  toCall: number,
  pot: number,
  effectiveStack: number,
  info: AggressionInfo,
  wasVillainPreviousAggressive: boolean,
  wasHeroPreviousAggressive: boolean,
): HandState {
  const potOdds = toCall / (toCall + pot);
  const result = new HandState(street);
  result.positivePotential = DEFAULT_POSITIVE_POTENTIAL;
  result.negativePotential = DEFAULT_NEGATIVE_POTENTIAL;
  result.setAggressionInfoOnlyCount(info, true);
  result.wasVillainPreviousAggressive = wasHeroPreviousAggressive;
  result.wasHeroPreviousAggressive = wasVillainPreviousAggressive;
  result.potOdds = potOdds;
  result.amountToCall = toCall;
  result.isHeroOnButton = villainOnButton;
  result.potToStackOdds = (pot + toCall) / (pot + toCall + effectiveStack);
  result.canRaise = effectiveStack !== 0;
  return result;
}

interface Hand {
  readonly game: GameContext;
  readonly hero: HoleCards;
  readonly board: readonly Card[];
  readonly street: number;
  readonly onButton: boolean;
  readonly strengthMap: StrengthMap | undefined;
}

export class NLGameSolver implements GameSolver {
  private readonly villainModel: VillainModel;
  private readonly equilator = new SpectrumEquilator();

  constructor(villainModel: VillainModel) {
    this.villainModel = villainModel;
  }

  onFirstActionPreflop(
    game: GameContext,
    info: AggressionInfo,
    effectiveStack: number,
    pot: number,
    spectrum: Spectrum,
    hero: HoleCards,
    strength: StrengthManager,
    isHeroOnButton: boolean,
    bigBlind: number,
  ): ActionDistribution {
    const hand = this.hand(game, hero, isHeroOnButton, strength);
    const wasVillainPreviousAggressive = false;
    const result: ActionDistribution = [];
    result.push({ action: fold(), profit: RandomVariable.constant(0) });
    const toCall = bigBlind / 2;
    const callProfit = this.heroActionProfit(
      hand,
      info,
      0,
      effectiveStack,
      pot + toCall,
      toCall,
      spectrum,
      wasVillainPreviousAggressive,
      false,
    );
    result.push({ action: call(toCall), profit: callProfit });
    const raise = createRaiseAction(toCall, pot, effectiveStack, raisePercent(toCall, PREFLOP));
    const raiseAmount = raise.amount;
    const aggressiveProfit = this.heroActionProfit(
      hand,
      info,
      raiseAmount,
      effectiveStack - raiseAmount,
      pot + toCall + raiseAmount,
      toCall + raiseAmount,
      spectrum,
      wasVillainPreviousAggressive,
      true,
    );
    result.push({ action: raise, profit: aggressiveProfit });
    return result;
  }

  onFirstAction(
    game: GameContext,
    info: AggressionInfo,
    effectiveStack: number,
    pot: number,
    spectrum: Spectrum,
    hero: HoleCards,
    strength: StrengthManager,
    wasVillainPreviousAggressive: boolean,
    isHeroOnButton: boolean,
  ): ActionDistribution {
    const hand = this.hand(game, hero, isHeroOnButton, strength);
    const result: ActionDistribution = [];
    result.push({ action: fold(), profit: RandomVariable.constant(0) });
    const checkProfit = this.heroActionProfit(
      hand,
      info,
      0,
      effectiveStack,
      pot,
      0,
      spectrum,
      wasVillainPreviousAggressive,
      false,
    );
    result.push({ action: check(), profit: checkProfit });
    const bet = createBetAction(pot, effectiveStack, FIRST_BET_PERCENT);
    const betAmount = bet.amount;
    const aggressiveProfit = this.heroActionProfit(
      hand,
      info,
      betAmount,
      effectiveStack - betAmount,
      pot + betAmount,
      betAmount,
      spectrum,
      wasVillainPreviousAggressive,
      true,
    );
    result.push({ action: bet, profit: aggressiveProfit });
    return result;
  }

  onSecondAction(
    game: GameContext,
    info: AggressionInfo,
    effectiveStack: number,
    pot: number,
    toCall: number,
    spectrum: Spectrum,
    hero: HoleCards,
    strength: StrengthManager,
    isHeroOnButton: boolean,
  ): ActionDistribution {
    const hand = this.hand(game, hero, isHeroOnButton, strength);
    return this.secondActionRecursive(hand, info, effectiveStack, pot, toCall, 0, spectrum);
  }

  protected calculatePassiveProfit(
    game: GameContext,
    pot: number,
    heroInvestment: number,
    street: number,
    hero: HoleCards,
    board: readonly Card[],
    spectrum: Spectrum,
    considerPreviousPot = false,
  ): RandomVariable {
    let win = pot - heroInvestment;
    if (!considerPreviousPot) {
      const previousStreetPot = street !== PREFLOP ? game.potOnStreet(street - 1) : 0;
      win -= previousStreetPot;
    }
    const lose = heroInvestment;
    const strength =
      board.length > 0
        ? this.equilator.strengthVsSpectrum(hero, board, spectrum)
        : preflopStrengthVsSpectrum(hero, spectrum);
    const result = new RandomVariable();
    result.add(strength, win);
    result.add(1 - strength, -lose);
    return result;
  }

  private hand(
    game: GameContext,
    hero: HoleCards,
    onButton: boolean,
    strength: StrengthManager,
  ): Hand {
    const street = game.street;
    const strengthMap = this.villainModel.needsEquity
      ? strength.map(STREET_NAMES[street] ?? 'preflop')
      : undefined;
    return { game, hero, board: [...game.board], street, onButton, strengthMap };
  }

  private secondActionRecursive(
    hand: Hand,
    info: AggressionInfo,
    effectiveStack: number,
    pot: number,
    toCall: number,
    heroInvestment: number,
    spectrum: Spectrum,
  ): ActionDistribution {
    const result: ActionDistribution = [];
    result.push({ action: fold(), profit: RandomVariable.constant(-heroInvestment) });
    const passiveProfit = this.calculatePassiveProfit(
      hand.game,
      pot + toCall,
      heroInvestment + toCall,
      hand.street,
      hand.hero,
      hand.board,
      spectrum,
    );
    result.push({ action: call(toCall), profit: passiveProfit });
    if (effectiveStack === 0) {
      return result;
    }
    const raise = createRaiseAction(toCall, pot, effectiveStack, raisePercent(toCall, hand.street));
    const amount = raise.amount;
    const aggressiveProfit = this.heroActionProfit(
      hand,
      info,
      amount,
      effectiveStack - amount,
      pot + toCall + amount,
      heroInvestment + toCall + amount,
      spectrum,
      toCall !== 0,
      true,
    );
    result.push({ action: raise, profit: aggressiveProfit });
    return result;
  }

  private heroActionProfit(
    hand: Hand,
    info: AggressionInfo,
    heroActionAmount: number,
    effectiveStack: number,
    pot: number,
    heroInvestment: number,
    spectrum: Spectrum,
    wasVillainPreviousAggressive: boolean,
    wasHeroPreviousAggressive: boolean,
  ): RandomVariable {
    const result = new RandomVariable();
    const villainToCall = heroActionAmount;
    const newInfo = new AggressionInfoBuffer(info).plusHeroAction();
    if (wasHeroPreviousAggressive) {
      newInfo.plusHeroAggressiveAction();
    }
    const villainSituation = villainSituationWithoutStrength(
      hand.street,
      !hand.onButton,
      villainToCall,
      pot,
      effectiveStack,
      newInfo,
      wasVillainPreviousAggressive,
      wasHeroPreviousAggressive,
    );
    const foldSpectrum = Spectrum.empty();
    const passiveSpectrum = Spectrum.empty();
    const aggressiveSpectrum = Spectrum.empty();
    this.processVillainSpectrums(
      spectrum,
      foldSpectrum,
      passiveSpectrum,
      aggressiveSpectrum,
      villainSituation,
      hand.strengthMap,
    );
    const villainSpectrumWeight = spectrum.summaryWeight();
    const foldWeight = foldSpectrum.summaryWeight();
    const passiveWeight = passiveSpectrum.summaryWeight();
    const aggressiveWeight = aggressiveSpectrum.summaryWeight();
    if (
      Math.abs(foldWeight + passiveWeight + aggressiveWeight - villainSpectrumWeight) > NEARLY_ZERO
    ) {
      throw new Error(
        `Equities must be equal in summary: fold ${foldWeight}, call ${passiveWeight}, raise ${aggressiveWeight}, summary ${villainSpectrumWeight}`,
      );
    }
    result.add(foldWeight / villainSpectrumWeight, pot - heroInvestment);
    if (!passiveSpectrum.isEmpty()) {
      const passiveActionChance = passiveWeight / villainSpectrumWeight;
      const ifVillainPassiveProfit = this.calculatePassiveProfit(
        hand.game,
        pot + villainToCall,
        heroInvestment,
        hand.street,
        hand.hero,
        hand.board,
        passiveSpectrum,
      );
      result.addVariable(passiveActionChance, ifVillainPassiveProfit);
    }
    if (aggressiveSpectrum.isEmpty()) {
      return result;
    }
    const villainRaise = createRaiseAction(
      villainToCall,
      pot,
      effectiveStack,
      raisePercent(villainToCall, hand.street),
    );
    const villainRaiseAmount = villainRaise.amount;
    const aggressiveChance = aggressiveWeight / villainSpectrumWeight;
    newInfo.plusVillainAction(true);
    const distribution = this.secondActionRecursive(
      hand,
      newInfo,
      effectiveStack - villainRaiseAmount,
      pot + villainToCall + villainRaiseAmount,
      villainRaiseAmount,
      heroInvestment,
      aggressiveSpectrum,
    );
    result.addVariable(aggressiveChance, getOptimal(distribution, hand.game));
    return result;
  }

  private processVillainSpectrums(
    spectrum: Spectrum,
    foldSpectrum: Spectrum,
    passiveSpectrum: Spectrum,
    aggressiveSpectrum: Spectrum,
    villainSituation: HandState,
    strengthMap: StrengthMap | undefined,
  ): void {
    for (const { cards, weight } of spectrum.entries()) {
      const equity = strengthMap?.get(cards);
      if (equity !== undefined) {
        villainSituation.strength = equity.strength;
        villainSituation.positivePotential = equity.positivePotential;
        villainSituation.negativePotential = equity.negativePotential;
      }
      const advice = this.villainModel.advise(villainSituation, cards);
      foldSpectrum.add(cards, weight * advice.fold);
      passiveSpectrum.add(cards, weight * advice.passive);
      aggressiveSpectrum.add(cards, weight * advice.aggressive);
    }
  }
}

export class ShowdownGameSolver extends NLGameSolver {
  protected override calculatePassiveProfit(
    game: GameContext,
    pot: number,
    heroInvestment: number,
    street: number,
    hero: HoleCards,
    board: readonly Card[],
    spectrum: Spectrum,
  ): RandomVariable {
    if (street === RIVER || street === PREFLOP) {
      return super.calculatePassiveProfit(
        game,
        pot,
        heroInvestment,
        street,
        hero,
        board,
        spectrum,
        true,
      );
    }
    throw new Error('ShowdownGameSolver supports only preflop and river');
  }
}
