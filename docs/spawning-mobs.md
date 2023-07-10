# Spawning Mobs

!!! tip

	This feature requires Lychee.

## Commands

Start a mob spawning session in the selected area:

```
/loquat spawn <spawner_id>
```

Start a mob spawning session in area with given UUID:

```
/loquat spawn <spawner_id> <area_uuid> [difficulty_id]
```

## Defining spawners

Spawner is where you define waves, and the monster types and the duration of each wave.

All spawners should be located in the `loquat_spawners` directory in a data pack.

### Spawner specification

- `waves`: A list of _Wave_.
- `difficulty` (optional): The default _Difficulty_ id. If not specified, it will be `"default"`.

### Wave specification

- `wait` (optional): The wait time before the wave starts in seconds. If not specified, it will be 0.
- `timeout` (optional): The timeout of the wave in seconds. If time runs out, the next wave will immediately start.
- `contextual` (optional): [_Contextual
  Condition_](https://lycheetweaker.readthedocs.io/en/latest/contextual-condition/) or a list of _Contextual
  Condition_. If the conditions are not met, the wave will be skipped.
- `post`: [_Post Action_](https://lycheetweaker.readthedocs.io/en/latest/post-action/) or a list of _Post Action_. You
  should use the `loquat:spawn` action to spawn mobs, added by Loquat.

!!! example

        ```json
        {
          "waves": [
            {
              "timeout": 30,
              "post": [
                {
                  "type": "loquat:spawn",
                  "count": 2,
                  "mob": "husk"
                }
              ]
            },
            {
              "wait": 1,
              "post": [
                {
                  "type": "loquat:spawn",
                  "count": 2,
                  "mob": {
                    "type": "rabbit",
                    "randomize": false,
                    "attrs": {
                      "hp": 20
                    },
                    "nbt": {
                      "RabbitType": 99
                    }
                  }
                }
              ]
            }
          ]
        }
        ```

### `loquat:spawn` action

- `count`: The number of mobs to spawn.
- `mob`: The mob to spawn. Can be a string, or a _Mob Entry_.
- `zone` (optional): The zone to spawn mobs in. If not specified, it will be `"0"`.

### Mob Entry specification

- `type`: The entity type id of the mob.
- `randomize` (optional): Whether to randomize the spawning, like giving random equipment to the mob, or transforming
  normal
  zombie to chicken jockey. If not specified, it will be `true`.
- `attrs` (optional): A map of attributes to set on the mob. The key can
  be `hp`, `movement_speed`, `damage`, `attack_speed`, `armor`, `armor_toughness`, `knockback_resistance`, `knockback`,
  etc.
- `nbt` (optional): A map of NBT tags to set on the mob.

## Defining difficulties

A _Difficulty_ is a manager that determines the amount of mobs to spawn, and scales the mob attributes based on the
context.

All difficulties should be located in the `loquat_difficulties` directory in a data pack. The namespaced will be
ignored.

### Difficulty specification

- `provider`: A command that returns a number that determines which level to use. In the example below, it returns the
  number of players in the server.
- `levels`: A list of _Difficulty Level_. If the number returned is greater than the number of levels, the last level
  will be used.

### Difficulty Level specification

- `hp` (optional): The scale of the mob's health. If not specified, it will be 1.
- `amount` (optional): The scale of the amount of mobs to spawn. If not specified, it will be 1.

!!! example

        data/loquat/loquat_difficulties/default.json:
        ```json
        {
          "provider": "execute if entity @a",
          "levels": [
            {},
            {
              "amount": 2
            },
            {
              "amount": 2,
              "hp": 1.5
            },
            {
              "amount": 2,
              "hp": 2
            }
          ]
        }
        ```
    
        data/loquat/loquat_difficulties/boss.json:
        ```json
        {
          "provider": "execute if entity @a",
          "levels": [
            {},
            {
              "hp": 2
            },
            {
              "hp": 3
            },
            {
              "hp": 4
            },
            {
              "hp": 5
            },
            {
              "hp": 6
            }
          ]
        }
        ```

!!! tip

    [JSON Fragment](https://lycheetweaker.readthedocs.io/en/latest/fragment/) is supported in spawner and difficulty to help you reuse code.
