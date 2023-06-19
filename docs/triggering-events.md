# Triggering Events

Use KubeJS to listen and react to the following events:

## Player enters an area

```js
// server script
LoquatEvents.playerEnteredArea(event => {
	let tags = event.area.tags
	if (tags.contains("mob_room") && !tags.contains("spawned")) {
		event.player.runCommandSilent(`loquat spawn loquat:1 ${event.area.uuid}`)
		tags.add("spawned")
		event.areaManager.setChanged([event.area])
	}
})
```

## Player leaves an area

```js
// server script
LoquatEvents.playerLeftArea(event => {
	// ...
})
```

## UUID

You can use UUID to specify a specific area in event. UUID can be copied from the `/loquat nearby` command.

```js
// server script
LoquatEvents.playerEnteredArea("00000000-0000-0000-0000-000000000000", event => {
	// ...
})
```
