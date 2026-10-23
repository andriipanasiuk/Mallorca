declare module 'phe' {
  export function evaluateCardCodes(codes: readonly number[]): number;
  export function handRank(value: number): number;
  export const ranks: {
    readonly STRAIGHT_FLUSH: number;
    readonly FOUR_OF_A_KIND: number;
    readonly FULL_HOUSE: number;
    readonly FLUSH: number;
    readonly STRAIGHT: number;
    readonly THREE_OF_A_KIND: number;
    readonly TWO_PAIR: number;
    readonly ONE_PAIR: number;
    readonly HIGH_CARD: number;
  };
}
