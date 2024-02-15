# Area Properties

This will introduce you to the properties that every area has. You can manage them through KubeJS or commands.

## UUID

Every area has a unique ID. You can use it in KubeJS or commands to specify an area.

## Tags

Tags can be used to identify areas. If you hold down Shift key, you can see the tags of the area in the world.

### Commands

Add a tag to the [selected areas](basic-area-operations.md#selecting-an-area):

```
/loquat tag <areas> add <tag>
```

Remove a tag from the selected areas:

```
/loquat tag <areas> remove <tag>
```

Select all the areas with given tag:

```
/loquat select @a[tag=foo]
```

Select all the areas without given tag:

```
/loquat select @a[tag=!foo]
```

### KubeJS

Get a stream of area by tag:

```js
let manager = LoquatAreaManager.of(level)
let areas = manager.byTag('foo').toList()
let area = manager.byTag('foo').findFirst().orElse(null)
area.tags.add('bar')
manager.setChanged([area])
```

## Attached Data

You can attach data to an area as `CompoundTag`. The data can be seen through the `/loquat list` command.

### Commands

Select areas with given data:

```
/loquat select @a[nbt={foo:bar}]
```

### KubeJS

```js
let manager = LoquatAreaManager.of(level)
let area = manager.byTag('foo').findFirst().orElse(null)
area.persistentData.foo = "bar"
manager.setChanged([area])
```

## Zones

Zones are named boxes inside an area. They can be used to mark special places in the area.

### Commands

Add selected boxes as zones to the selected area:

("0" if no name is specified)

```
/loquat zone <area> add [name]
```

Remove zones from the selected area:

```
/loquat zone <area> remove <name>
```

### KubeJS

It is recommended to use commands to modify zones.

Get a zone by name:

```js
let manager = LoquatAreaManager.of(level)
let area = manager.byTag('foo').findFirst().orElse(null)
let aabbs = area.zones.get('bar').aabbs
```

## Restrictions

Restrictions are used to restrict the player's behavior in the area.

Currently, there are four types of restrictions: `enter`, `exit`, `place`, and `destroy`.

### Commands

Add a restriction to the selected area:

```
/loquat restrict <players> <type> <true|false>
```

The `players` and `type` arguments can be `*` to represent all players and all types.
