TODOs in no particular order:

**General:**\
Cleanroom-style room that is only safe inside.\
Textures, models\
Gameplay decisions (machine limits at tiers, what should be multiblocks, etc)

**Oxygen machine:**\
Working sound effect (higher pitch when breached and surroundings are lower pressure)\
Oxygenation visualization like ad astra?\
Make radius and max volume settings you can define for the machine in kubejs\
Make oxygen machines in general kubejs-able

**Space heater:**\
Visual effects?\
Radius visualization\
Energy consumption GUI\
Some kind of temperature gameplay that's not so binary? Gradient maybe? How to make it nice to play while also being interesting?

**Higgs Emitter (gravity machine):**\
Everything

**Low Pressure Chamber (Cleanroom for Europa)**\
Needs special flood fill handling for the walls

**Decompression:**\
Particle effects\
Breach force direction based on pressure differential against environment, or actually against the local pressure outside the breach (future-proof for Europa's varying pressure level with Y level)\
Stop decompression force when original hole is filled but there's still other holes (shift to new hole)

**Maybe:**\
Readout board with temperature, oxygen, pressure, gravity status?
Equip quark suit debug command


# Environment System Design Notes

Non-obvious design decisions and their rationale.

## Transition

Right now my machines and ad astra's machines both work as expected, so players have time to transition. In the future I can remove ad astra's codepath and get the actual performance benefits. 

## Machines

### Types

**Oxygen Distributors** use flood fill to fill a room with atmosphere.\
**Space Heaters** create a bubble of warm air\
**TODO Higgs Emitter**, I'm thinking maybe it spreads outwards along a floor surface and above it? That could be cool, could be annoying. Gotta think what would be fun for gameplay and what's possible to code.\
**TODO Low Pressure Chamber**, I'm thinking maybe it spreads outwards along a floor surface and above it? That could be cool, could be annoying. Gotta think what would be fun for gameplay and what's possible to code.

### Unloaded machines and machines just loaded from NBT are assumed to be working

When a machine's chunk unloads, its provider continues answering "yes" to oxygen/temperature queries. The machine isn't consuming resources during this time, but otherwise the player might take damage and dependent machines like the greenhouse would stop working.

I chose the maxmium horizontal dimensions (128 blocks aka 8 chunks) with the idea that hopefully when the player is around, the machine's room is entirely loaded. You can abuse this unloaded mechanic a little but not a lot.

### Sealed rooms use envelope (interior + shell) for oxygen queries

The "envelope" is the interior plus one layer of wall blocks. Queries use the envelope, not the
interior, for three reasons:
- A player standing with in a partial block still has oxygen.
- When a player breaks a wall block, they don't get damaged by the void left behind before the machine revalidates.
- Machines can check `hasOxygen` at their own position instead of checking all 6 neighbors.

## Flood fill tags

For the atmosphere room finding code, you can force blocks to be treated a certain way:
- `tfg:atmosphere_impassable`: forced FULL (e.g. Create fluid tanks that pretend to be 3/4ths blocks instead of full)
- `tfg:atmosphere_passable`: forced EMPTY (e.g. fences, pipes, wires, ladders, iron bars)
- `tfg:atmosphere_use_outline`: uses `getShape()` instead of `getCollisionShape()` for blocks
  where collision shape doesn't match the visual form (e.g. domum_ornamentum:blockpillar with full collision that should have open sides)

Checking order is impassable, passable, outline, so a specific block can override a tag category.

- `tfg:atmosphere_check_facades`: mark blocks that can have facades. Facades are a bit expensive to check, so I only want to look for them if I expect that they may be there. Currently I check for wires, cables, and pipes. These are also in `tfg:atmosphere_passable`, because by themselves they don't ever block air. The facade can still block air though, and the combination is handled correctly.

## Flood fill implementation details

The flood fill needs to decide whether air can pass through each block which has a bunch of tricky edge cases. I basically follow ad astra's approach, but it's a full rewrite for performance and because they had bugs in their implementation.

### PassInfo block type

1. **EMPTY**: air, tagged passable, or no full silhouette on any axis (eg fences, pipes). Air flows freely in all directions.
2. **FULL**: tagged impassable, or collision box is a full cube. (eg tree log block)
3. **COLLISION**: has some full faces/silhouettes but not all, needs direction-aware checking. (eg half slab)

### COLLISION type

COLLISION type blocks need to know which direction the air is coming from, to see whether the flood fill succeeds. To do so, we cache two bitmasks for each block, `fullFaces` and `fullSilhouettes`.

- **fullFaces**: Is the face on the incoming side sealed? A bottom slab has a sealed bottom face but open top and side faces.
- **fullSilhouettes**: Looking along the incoming axis, does the shape fill the entire cross-section? Aka what does its shadow look like if the sun were shining along this axis? A thin window pane has a full silhouette along one axis, but no fullFaces.

### COLLISION passability logic `isPassableFromDirections`

When processing a block, we know which directions we're entering from so far (can be multiple).

If all incoming faces are sealed > NO_OPEN_FACES (added to pending shell, not final shell: we might still visit it later from a different direction where it's open, e.g. a bottom slab visited from below is pending, but if later visited from the side it becomes passable).

A slab has a full silhouette top-to-bottom but not side-to-side. If any incoming direction has an open face AND an open silhouette > OPEN_SILHOUETTE (air passes through, but only out of faces that are open: a bottom slab accessed from the side lets air out the top and other sides, but not down).

Some blocks (glass panes, stone walls) have open faces but filled silhouettes. Because the front face is open, you'd think air always flows into the front and out sidewards to the perpendicular neighbors. However, when flowing into the neighboring pane, it would enter from the side and be allowed to exit on the backside. That would make a solid wall of window panes leak.

The solution is a best effort compromise that's friendly to the player: when a block has open faces but all silhouettes are filled, check perpendicular neighbors. If any neighbor is EMPTY or has an empty face toward the pane > PASSABLE_WINDOW_PANE (air flows around, but only out of open faces). Otherwise > BLOCKED_WINDOW_PANE (added to pending shell, but can be upgraded to passable if later visited from a different direction).

This means a wall of glass panes with solid blocks on all sides seals a room, but a single pane in open air doesn't.

### Face direction convention

All directions in the floodfill are "along the floodfill direction", so something like the block closed faces in the cache uses the direction from outwards in. In almost all cases this is the most convenient. For faces specifically, minecraft uses the opposite convention, and in some situations we have to reverse the directions. There's helper functions for it.

### Uncacheable blocks (NO_CACHE)

Some blocks need world context to compute their collision shape: airlock doors (reference controller block), moving pistons, shulker boxes, bellows. These can't be cached per BlockState alone so they're recomputed each time using the actual Level context.

### Facade blocks (CHECK_FACADES)

These blocks get their cached faces and silhouettes data written but also get marked separately as CHECK_FACADES. Then when we encounter them during the flood fill, we check if there's any facades on there and combine the facade data with the block itself. It's a bit like NO_CACHE but we don't need to do a full live collisionshape check, which for wires would depend on the surrounding blocks. That would make things more expensive and doesn't actually make a difference in the end.

### Async block access

The flood fill runs on a background thread to reduce lag while keeping fast responses. Normal block access goes through `ServerChunkCache.getChunk()` which bounces to the main thread, so `AsyncBlockReader` bypasses it by reading directly from `ChunkHolder` futures via mixin accessors. It only reads loaded chunks; a null return means the chunk is unloaded. It's set up on the main thread to capture the `ChunkMap` entry point, then all reads happen off thread.

This is safe because `PalettedContainer` is thread safe (Mojang's light engine reads it off thread), and stale reads are acceptable since we listen for blockchanges even during a revalidation. If a block changes during a scan, the machine's dirty flag gets set and a rescan is queued after the current one finishes.

For the facades check we need to get block entities and get their data. This is not guaranteed thread-safe, but we don't iterate so we don't get CME. The main risk is stale data but we don't care too much about that since the dirty flag should almost always catch this too.

There's a non-impactful race condition where we can only listen for the _old_ room's blocks, not the new room's, so if a block changes in the new room that was not present in the old room, we don't catch that. Practically you almost never notice in gameplay, as any subsequent block changes will still trigger revalidation.
