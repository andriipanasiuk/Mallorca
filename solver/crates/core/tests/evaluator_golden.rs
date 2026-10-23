use mallorca_core::cards::parse_cards;
use mallorca_core::eval::rank;

#[test]
fn matches_phe_on_every_fixture_line() {
    let fixture = include_str!("fixtures/evaluator.txt");
    let mut checked = 0;
    for (n, line) in fixture.lines().enumerate() {
        let (cards, expected) = line.split_once(',').expect("comma");
        let cards = parse_cards(cards).expect("cards");
        let expected: u16 = expected.parse().expect("rank");
        assert_eq!(rank(&cards), expected, "line {}: {line}", n + 1);
        checked += 1;
    }
    assert_eq!(checked, 60_000);
}
