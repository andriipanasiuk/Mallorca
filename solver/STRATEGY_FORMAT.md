# Strategy file format, version 1

The contract between the trainer (`mallorca-trainer export`) and the browser bot. Everything is little-endian. The
reference implementation is `crates/core/src/strategy.rs`; the bot reader is written from this file.

## Layout

| Offset | Size | Field | Meaning |
| --- | --- | --- | --- |
| 0 | 4 | `magic` | ASCII `MLCS` |
| 4 | 2 | `format_version` | u16, `1`. A reader rejects any other value |
| 6 | 32 | `fingerprint` | SHA-256 over the abstraction part of the preset, the serialized tree and the bucket tables of the training run |
| 38 | 4 | `small_blind` | u32 |
| 42 | 4 | `big_blind` | u32 |
| 46 | 4 | `stack` | u32, effective stack in chips |
| 50 | 1 | `streets` | u8 bitmask of included streets: bit 0 preflop, bit 1 flop, bit 2 turn, bit 3 river. Bit 0 is always set, bits 4 to 7 are zero |
| 51 | 8 | `iterations` | u64, training iterations of the exported checkpoint |
| 59 | 4 | `checkpoint` | u32, number of the exported checkpoint |
| 63 | 4 | `tree_length` | u32, byte length of the tree that follows |
| 67 | `tree_length` | `tree` | serialized tree, see below |
| next | 4 | `buckets_length` | u32, byte length of the bucket section that follows |
| next | `buckets_length` | `buckets` | bucket tables, see below |
| next | rest of file | `probabilities` | one byte per action per infoset, see below |

The probabilities section has no length field: it runs to the end of the file. Its length must equal the value
computed in "Probabilities"; a reader treats any other length as a corrupt file.

## Tree

The same bytes as the trainer's tree serialization. All integers are little-endian.

```
buckets      4 x u16   bucket count per street: preflop (169), flop, turn, river
node_count   u32
nodes        node_count entries, node id = index, root = 0
```

A node starts with a one-byte tag.

- Tag 0, decision: `seat u8`, `street u8` (0 preflop .. 3 river), `committed 2 x u32`, `street_bets 2 x u32`,
  `action_count u8` (at least 1), then `action_count` actions. An action is a kind byte, an optional `u32`, and the
  child node id `u32`. Kind 0 fold, 1 check, 2 call, 3 raise to (followed by the raise-to amount as `u32`
  chips), 4 all-in. The `u32` of a raise comes before the child id. A child id is always greater than the id of
  its parent.
- Tag 1, fold terminal: `folder u8`, `committed 2 x u32`.
- Tag 2, showdown terminal: `committed 2 x u32`.

## Buckets

The section holds one table for each included postflop street, in street order (flop, turn, river). A file with
`streets = 0b0011` therefore holds the flop table only.

```
for each included postflop street:
    length   u32
    table    length bytes
```

The table of a street maps the canonical hand class index of the hole cards plus board to a bucket number, one byte
per class. Expected lengths: flop 1286792, turn 13960050, river 123156254. The sections' `buckets_length` equals the
sum of `4 + length` over the tables. Preflop has no table: the bucket is the preflop class index, 0 to 168.

## Probabilities

Walk the tree nodes in id order. For every decision node whose street is included in `streets`, write one block for
each bucket `0 .. buckets[street]`, in increasing bucket order. A block is `action_count` bytes, one per action in
the node's action order. Decision nodes of excluded streets contribute nothing.

- The bytes of a block sum to exactly 255. A byte is the action's probability times 255, rounded by largest
  remainder (ties go to the lower action index). An infoset that training never reached holds the uniform
  distribution rounded the same way.
- Block start for a node and bucket `b`: the sum, over earlier included decision nodes, of `buckets[street] *
  action_count`, plus `b * action_count`.
- Total length: the sum over included decision nodes of `buckets[street] * action_count`.

## Validity

A reader rejects the file when: the magic differs, the version is not 1, the data ends inside the header, tree or
bucket section, `streets` lacks bit 0 or sets a bit above bit 3, the tree does not deserialize, a table has the
wrong length or the bucket section has leftover bytes, or the probabilities length differs from the total above.
