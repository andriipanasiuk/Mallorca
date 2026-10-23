use std::fmt;

pub type Card = u8;

const RANKS: &[u8; 13] = b"23456789TJQKA";
const SUITS: &[u8; 4] = b"cdhs";

#[derive(Debug, Clone, PartialEq, Eq)]
pub enum CardError {
    BadLength(String),
    BadRank(char),
    BadSuit(char),
}

impl fmt::Display for CardError {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        match self {
            CardError::BadLength(s) => write!(f, "card must be two characters: {s:?}"),
            CardError::BadRank(c) => write!(f, "unknown rank {c:?}"),
            CardError::BadSuit(c) => write!(f, "unknown suit {c:?}"),
        }
    }
}

impl std::error::Error for CardError {}

pub fn rank(c: Card) -> u8 {
    c / 4
}

pub fn suit(c: Card) -> u8 {
    c % 4
}

pub fn parse_card(s: &str) -> Result<Card, CardError> {
    let mut chars = s.chars();
    let (Some(r), Some(u), None) = (chars.next(), chars.next(), chars.next()) else {
        return Err(CardError::BadLength(s.to_string()));
    };
    let rank = RANKS
        .iter()
        .position(|&b| b as char == r)
        .ok_or(CardError::BadRank(r))?;
    let suit = SUITS
        .iter()
        .position(|&b| b as char == u)
        .ok_or(CardError::BadSuit(u))?;
    Ok((rank * 4 + suit) as Card)
}

pub fn parse_cards(s: &str) -> Result<Vec<Card>, CardError> {
    s.split_whitespace().map(parse_card).collect()
}

pub fn format_card(c: Card) -> String {
    let mut out = String::with_capacity(2);
    out.push(RANKS[rank(c) as usize] as char);
    out.push(SUITS[suit(c) as usize] as char);
    out
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn parses_ace_of_spades() {
        assert_eq!(parse_card("As"), Ok(51));
    }

    #[test]
    fn parses_deuce_of_clubs() {
        assert_eq!(parse_card("2c"), Ok(0));
    }

    #[test]
    fn format_round_trips_all_cards() {
        for c in 0..52u8 {
            assert_eq!(parse_card(&format_card(c)), Ok(c));
        }
    }

    #[test]
    fn rejects_garbage() {
        assert!(parse_card("Xx").is_err());
        assert!(parse_card("A").is_err());
        assert!(parse_card("Asd").is_err());
    }

    #[test]
    fn parses_card_lists() {
        assert_eq!(parse_cards("As Kd").unwrap().len(), 2);
        assert!(parse_cards("As Zz").is_err());
    }

    #[test]
    fn rank_and_suit_split() {
        assert_eq!(rank(51), 12);
        assert_eq!(suit(51), 3);
        assert_eq!(rank(0), 0);
        assert_eq!(suit(0), 0);
    }
}
