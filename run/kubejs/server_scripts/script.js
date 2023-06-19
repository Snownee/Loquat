// priority: 0

console.info('Hello, World! (You will see this line every time server resources reload)')

ServerEvents.recipes(event => {
	LoquatPlacements.register("test", new LoquatTreeNodePlacer("loquat:test", ctx => {
		let rooms = []
		let random = ctx.random
		ctx.globalTags.add("111")
		let lastNode = ctx.root;
		for (let i = 0; i < 5; i++) {
			rooms.push(lastNode = lastNode.addChild("door", "loquat:room"));
		}
		rooms.forEach($ => {
			$.data = {
				foo: "bar"
			}
			$.uniqueGroup = "room";
			$.minEdgeDistance = 8;
		});
		rooms[random.nextInt(rooms.length)].addChild("door", "loquat:treasure");
		lastNode = lastNode.addChild("door", "loquat:end");
		lastNode.lowPriorityProcessors.add(ResourceLocation.tryParse('loquat:replace_diamond'))
		ctx.root.walk($ => {
			$.tags.add("test")
			$.fallbackNodeProvider = joint => {
				if (joint === "minecraft:door")
					return new LoquatTreeNode("loquat:barrier");
				return null;
			}
		})
	}));

	LoquatPlacements.register("lushcave", new LoquatTreeNodePlacer("pack:lushcave", ctx => {
		let rooms = []
		let random = ctx.random
		let lastNode = ctx.root;
		for (let i = 0; i < 3; i++) {
			rooms.push(lastNode = lastNode.addChild("door", "pack:lushcave/room"));
		}
		rooms.forEach($ => {
			$.uniqueGroup = "room";
			$.minEdgeDistance = 8;
		});
		// rooms[random.nextInt(rooms.length)].addChild("door", "loquat:treasure");
		// lastNode.addChild("door", "loquat:end");
		ctx.root.walk($ => {
			$.tags.add("test")
			$.fallbackNodeProvider = joint => {
				if (joint === "minecraft:door")
					return new LoquatTreeNode("pack:lushcave/barrier");
				return null;
			}
		})
	}));
})

LoquatEvents.playerEnteredArea(event => {
	let tags = event.area.tags
	if (tags.contains("mob_room") && !tags.contains("spawned")) {
		event.player.runCommandSilent(`loquat spawn loquat:1 ${event.area.uuid}`)
		tags.add("spawned")
		event.areaManager.setChanged([event.area])
	}
})

BlockEvents.rightClicked('minecraft:diamond_block', event => {
	if (event.hand != 'MAIN_HAND') {
		return
	}
	console.log('hi')
	if (event.block.level.overworld) {
		Java.loadClass("snownee.kiwi.loader.Platform").getServer().getLevel('minecraft:the_end')
		event.player.runCommandSilent(`execute in loquat:void positioned 0 0 0 run place structure loquat:test`)
		event.player.runCommandSilent(`execute in loquat:void run tp 0 100 0`)
	} else {
		event.player.runCommandSilent(`execute in loquat:void run loquat select_tag dungeon`)
		event.player.runCommandSilent(`execute in loquat:void run loquat empty selection`)
		event.player.runCommandSilent(`execute in loquat:void run loquat delete selection`)
		event.player.runCommandSilent(`execute in overworld run tp 0 0 0`)
	}
})

BlockEvents.rightClicked('minecraft:emerald_block', event => {
	if (event.hand != 'MAIN_HAND') {
		return
	}
	let areaManager = LoquatAreaManager.of(event.level)
	let area = areaManager.byTag('aaa').findFirst().get()
	area.persistentData.foo = "bar"
	areaManager.setDirty()
})
