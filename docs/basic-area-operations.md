# Basic Area Operations

In this section, you will learn how to manually create or delete an area, and more.

## Enabling rendering things

By default, Loquat does not display any information for you. Use the following command to toggle it:

(Make sure you have the operator permission to use any Loquat commands)

```
/loquat outline
```

## Selecting a box

Just as the Wooden Axe is used in WorldEdit, we use the Spectral Arrow by default to select things in Loquat.

Click two blocks with the Spectral Arrow to select a box. You can select multiple boxes at the same time.

Shift-right-click with the Spectral Arrow to reset your selection.

## Creating a box area

When you have only one box selected, you can create an area with the following command:

```
/loquat create
```

You can also directly create an area with two coordinates:

```
/loquat create box <x1> <y1> <z1> <x2> <y2> <z2>
```

## Selecting an area

Shift-click a block with the Spectral Arrow to select all the areas containing the block. Shift-click again to unselect
them.

Then you can use `@s` to refer to the selected areas in commands. The full instructions for area selectors can be found
in [this page](area-selectors.md).

Shift-right-click with the Spectral Arrow to reset your selection.

### Commands

You can also select areas through the `/loquat select` command, with various conditions.

Use the following command to unselect all the areas:

```
/loquat unselect @s
```

## Deleting an area

Use the following command to delete all the selected areas:

```
/loquat delete @s
```

## Viewing list areas

View all the selected areas:

```
/loquat list @s
```

View all the areas within 10 blocks of the player:

```
/loquat list @a[distance=..10]
```

## Emptying blocks in an area

Use the following command to empty all the blocks in the selected areas:

```
/loquat empty @s
```

## Replacing area box

Use the following command to replace the box of the selected area with selected box:

```
/loquat replace
```
