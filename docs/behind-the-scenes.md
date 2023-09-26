# Behind the Scenes

In this section, you will learn how to keep your RPG or mini dungeon game clean, fancy, and modularized.

## Area persistent data

You can assign data to an area during the generation process.

We can use this feature to randomize the spawner in our dungeon.

```js hl_lines="16 22 23 24"
// server script
ServerEvents.recipes(event => {
	LoquatPlacements.register("any_key_you_like", new LoquatTreeNodePlacer("pack:big_dungeon", ctx => {
		// add monster rooms
		let monsterRooms = []
		// ctx.root is the start room according to the structure json file
		let lastRoom = ctx.root
		for (let i = 0; i < 5; i++) {
			// lastRoom.addChild(target_name, template_pool_name)
			monsterRooms.push(lastNode = lastRoom.addChild("door", "pack:monster"))
		}
		// add boss room
		lastRoom.addChild("door", "pack:boss")
		// add treasure room
		monsterRooms[ctx.random.nextInt(monsterRooms.length)].addChild("door", "pack:treasure")
		let spawners = ["zombies", "skeletons", "creepers"]
		monsterRooms.forEach(room => {
			// we don't want the entrance and exit to be too close, so we set a minimum distance
			room.minEdgeDistance = 8
			// we don't want two rooms are the same, so we make a unique group id for them
			room.uniqueGroup = "monster"
			room.data = {
				spawner: "pack:" + spawners[ctx.random.nextInt(spawners.length)]
			}
		})
	}))
})

LoquatEvents.playerEnteredArea(event => {
	let data = event.area.persistentData
	let tags = event.area.tags
	if (data.contains("spawner") && !tags.contains("spawned")) {
		event.player.runCommandSilent(`loquat spawn ${data.spawner} ${event.area.uuid}`)
		tags.add("spawned")
		event.areaManager.setChanged([event.area])
	}
})
```

## The aftercare

When the players finish the level, we want to be able to clean up all the stuff that we generated, for future
generations. Here is the solution:

First we need to tag all the areas that will be generated along with our structure:

```js
// server script
ServerEvents.recipes(event => {
	LoquatPlacements.register("any_key_you_like", new LoquatTreeNodePlacer("pack:big_dungeon", ctx => {
		ctx.globalTags.add("big_dungeon")
		// ...
	}))
})
```

Then write code to clean them up if necessary:

```js
// server script
function clearDungeon() {
	// pick a random player
	// make sure there is at least one player online
	let player = Utils.server.players.stream().findAny().get()
	player.runCommandSilent(`execute in minecraft:overworld run loquat select_tag big_dungeon`)
	player.runCommandSilent(`execute in minecraft:overworld run loquat empty selection`)
	player.runCommandSilent(`execute in minecraft:overworld run loquat delete selection`)
}
```

## Dynamic processor assignment

A [processor list](https://minecraft.wiki/w/Custom_structure#Processor_List) is a list of processors used to affect blocks in structures, such as replacing blocks.

You can assign a processor list to a node (piece) during the generation process.

```js
// server script
node.lowPriorityProcessors.add(ResourceLocation.tryParse('pack:replace_diamond'))
// or node.highPriorityProcessors, depending on whether you want to
// process blocks before or after the structure is rotated and mirrored
```

data/pack/worldgen/processor_list/replace_diamond.json:

```json
{
  "processors": [
    {
      "processor_type": "minecraft:rule",
      "rules": [
        {
          "input_predicate": {
            "block": "minecraft:diamond_block",
            "predicate_type": "minecraft:block_match"
          },
          "location_predicate": {
            "predicate_type": "minecraft:always_true"
          },
          "output_state": {
            "Name": "minecraft:obsidian"
          }
        }
      ]
    }
  ]
}
```
