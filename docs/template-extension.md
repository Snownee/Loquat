# Template Extension

## Structure metadata

When you save a structure using the Structure Block, Loquat will also save all areas within the structure. When
the structure is loaded, Loquat will load them back as well.

Specially, Loquat will not save [restrictions](area-properties.md#restrictions). And the area UUID will be changed.

## Structure block interactions

### Setting structure position and size

**Clicking with the Spectral Arrow** sets the structure position to the selected box start. The same applies to the
size if the structure block is NOT in `LOAD` mode.

### Selecting areas within the structure

**Shift-clicking with the Spectral Arrow** selects all areas within the structure.

### Emptying the structure

**Shift-right-clicking with the Spectral Arrow** clears all areas and blocks within the structure.
