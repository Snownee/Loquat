# Manipulating Jigsaws

This is where the main event begins - generating a randomized dungeon or whatever you want using all the knowledge you
learned for Loquat.

!!! note

		In my imagination, there are many many illustrations here. But I'm too lazy. So please use your imagination instead.

## The preparation

!!! info

		The vanilla data pack can be found [here](https://github.com/misode/mcmeta/tree/data).

Here I assume you already have some knowledge of how to use data pack
to [generate a jigsaw structure](https://minecraft.fandom.com/wiki/Custom_world_generation). So let's do a quick
overview:

### 1. Create our structure type

!!! example

		data/pack/worldgen/structure/big_dungeon.json:
        ```json
        {
          "type": "minecraft:jigsaw",
          "biomes": "#minecraft:is_overworld",
          "max_distance_from_center": 80,
          "project_start_to_heightmap": "WORLD_SURFACE_WG",
          "size": 1,
          "spawn_overrides": {},
          "start_height": {
            "absolute": 40
          },
          "start_pool": "pack:start",
          "step": "surface_structures",
          "terrain_adaptation": "none",
          "use_expansion_hack": false
        }
        ```

### 2. Build and add the rooms

Now we need to build every part of our structure, place jigsaw blocks so that they can connect with each other, and save
them as `.nbt` files using the Structure Block. Remember to create an area with the same size as the room, so that we
can clear the whole structure in world when we want to.

All the `.nbt` files should be placed in the `data/{namespace}}/structures/` directory. In our case, it
is `data/pack/structures/`.

### 3. Create template pools

!!! example

		data/pack/worldgen/template_pool/start.json:
        ```json
        {
          "name": "pack:start",
          "elements": [
            {
              "element": {
                "element_type": "minecraft:single_pool_element",
                "location": "pack:start",
                "processors": "minecraft:empty",
                "projection": "rigid"
              },
              "weight": 1
            }
          ],
          "fallback": "minecraft:empty"
        }
        ```

Now you should be able to generate your structure using the `/place structure` command.

## The placement script

Here we need to get the generator to connect these rooms in a way that we are happy with. Ideally, we want our dungeon
to start with a `start` room, followed by five `monster` rooms, and a `boss` room at the end. We also want to have
a `treasure` room to be placed as a branch for one of the `monster` rooms.

This is the KubeJS script being used:

```js
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
		monsterRooms.forEach(room => {
			// we don't want the entrance and exit to be too close, so we set a minimum distance
			room.minEdgeDistance = 8
			// we don't want two rooms are the same, so we make a unique group id for them
			room.uniqueGroup = "monster"
		})
	}))
})
```

Reload the scripts and run `/place structure pack:big_dungeon`, ta-da!

## Placing the structure

### By command

Generally, we use KubeJS to execute the `/place structure` command, to place the structure. Here is an example:

!!! example

        ```js
        // server script
        BlockEvents.rightClicked('minecraft:diamond_block', event => {
            if (event.hand != 'MAIN_HAND') {
                return
            }
            event.server.runCommandSilent(`execute in minecraft:overworld positioned 0 0 0 run place structure pack:big_dungeon`)
        })
        ```

!!! warning "Note"

        Do NOT use `/execute run place structure <structure> <x> <y> <z>`, it is not supported.

        Use `/execute positioned <x> <y> <z> run place structure <structure>` instead.

### During world generation

You can define a [structure set](https://minecraft.fandom.com/wiki/Custom_world_generation/structure_set) to randomly
place the structure like vanilla does.

!!! example

        data/pack/worldgen/structure_set/big_dungeon.json:
        ```json
        {
          "placement": {
            "type": "minecraft:random_spread",
            "salt": 100,
            "separation": 8,
            "spacing": 15
          },
          "structures": [
            {
              "structure": "pack:big_dungeon",
              "weight": 1
            }
          ]
        }
        ```

## Fallback piece

So it looks like our dungeon has been bui... Wait, how does the player get out of the dungeon?

Recall that we have a "branch room", which means that the monster room may have multiple exits, which means that there
may be exits that are not used. We need to place a "lid" to block the player's way out when the exit has no target room.

First you need to create a barrier structure and the template pool that can perfectly cover the exit of the room and fit
into the corresponding jigsaw block.

Then modify the placement script:

```js
// server script
ServerEvents.recipes(event => {
	LoquatPlacements.register("any_key_you_like", new LoquatTreeNodePlacer("pack:big_dungeon", ctx => {
		// omit the code above
		// ...
		// use node(the root).walk() to iterate all child nodes
		ctx.root.walk(room => {
			room.fallbackNodeProvider = joint => {
				if (joint === "minecraft:door")
					// LoquatTreeNode is also the type of ctx.root and room
					return new LoquatTreeNode("pack:barrier");
				return null;
			}
		})
	}))
})
```
