# Mr. President — RotP addon (mod id `rotp_mrpresident`)

Player-usable Mr. President (JoJo Part 5 / YBA abilities) built on the room systems that
Ripples of the Past already ships (since snapshot 250430-a) in `com.github.standobyte.jojo.mrpresident`
(pocket dimension `jojo:mr_president`, room structure, room table, teleporters, Turtle Key item, exit gem).
No new dimension is created. Requires a RotP build that has them (checked against snapshot 250430-a, the first public build with them;
`main_mod_version` in `gradle.properties`).

Get the stand: `/stand give <player> rotp_mrpresident:mr_president`

## Abilities
| Hotbar | Ability | What it does |
|---|---|---|
| RMB | Enter Room / Exit Room | Step into your own room, or back out. A floating 3D key marks the spot where you went in |
| LMB | Kidnap | Key strike: drags the target into the room for 20 s (enemies or allies) |
| LMB | Empty Room | Throws everybody out, you too if you are inside |
| LMB | Shake Key | Everybody inside (you too, non-lethal for you) takes damage + stun, camera shake |
| RMB | Restock Supplies | Heal, regeneration, absorption and food for everybody inside |
| RMB | Turtle Form | Rendered as Coco Jumbo, -75% damage, very slow, turtle hitbox, entities falling on you drop into the room, 45 s |
| RMB | Remove Key / Insert Key | Key out: room locked and everybody expelled, you get the Turtle Key item. Hold it to put it back |

## Layout
- `room/RoomService` - enter/exit/expel/occupants/kidnap timers on top of the base-mod room and teleporters
- `room/OwnerSavedData` - per owner: exit position outside, kidnap timers (stored in the base room dimension)
- `room/RoomFurnisher` - bed, sofa, TV, fridge with drinks, lamps placed into the base room on first build
- `room/RoomKeys` - the base mod's Turtle Key, assigned to the player
- `effect/TurtleFormEffect`, `effect/KeyRemovedEffect` - vanilla effects (synced + saved by vanilla)
- `action/*` - one class per ability, shared plumbing in `RoomAction`
- `event/MrPresidentEvents` - damage reduction, hitbox, exit tracking, timers, exit-gem fix, logout expel
- `client/*`, `network/*` - turtle form rendering, camera shake packet

## Not implemented
- Peeking out through the key's gem (needs the outside world rendered from another dimension).

## Key weapon
The Turtle Key pulled out with *Remove Key* is a weapon: 9 attack damage, 1.8 attack speed. While held in the main hand by its
owner it also lets him hit Stands directly.

## Authors
Gocimenya, inkegg
