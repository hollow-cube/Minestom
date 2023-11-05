# ByteMC - Minestom

### Description
Will be released soon as possible. The bytemc team is on the way to implements all basic functions.


# Documentation
1. FakePlayers
    ```kotlin
    FakePlayer.initPlayer(PlayerSkin, Instance, Pos, Consumer<FakePlayer>)
    ```
    Also manipulate `FakePlayers` with skin layers:
    ```kotlin
    fakePlayer.meta.allowAllSkinLayers()
    ```
2. Instance
    ```kotlin
    // Disable time:
    Instance.disableTimeRotation()
    ```
3. Extension
    ```kotlin
   // Return path:
   Extension.getConfigPath()
    ```
4. Inventory
   ```kotlin
   // ClickableItem
   ClickableItem(ItemStack).subscribe(Consumer<Player>, ClickType)
   
   // Singleton inventory:
   class Class: SingletonInventory(String, InventoryType, Boolean) { 
        fill(ClickableItem)
        fill(Int, ClickableItem)
        fill(Int, Int, ClickableItem)
    }
   
   // Pageable inventory:
   class Class: PageableInventory<Object>(String, InventoryType, Boolean, List<Object>) { 
        fill(ClickableItem)
        fill(Int, ClickableItem)
        fill(Int, Int, ClickableItem)
        
        override fun construct(Object): ClickableItem
    }
   
   // Anvil inventory:
   class Class: AnvilInventory(String, Player) { 
        override fun onSubmit(Player, String)
    }
   ```
5. Hologram
   ```kotlin
   Hologram(Instance, Point, List<String>)
   // Methods
   spawn()
   destroy()
   ```

6. Point
    ``` java
    point.blockCenter() // return the middle of a block
    ```

7. Direction
    ```java
    Direction.NORTH.getChestFacing(); // get chest face direction
    Direction.NORTH.getSignFacing(); // get sign face direction
    Direction.NORTH.rotate(new Vec(0,0,0)); // rotate a direciton 
    Direction.fromDirection(new Vec(0,0,0)); // translate a point to direction
    ```
8. ClickableEntity
    ```kotlin
    ClickableEntity(EntityType.VILLAGER, Consumer {
        // click action
    }).modify {
        // set type or something else
    }.spawn(it.player.position, it.player.instance)
    ```
9. ItemStack
    ```kotlin
    // Player Head
   ItemStack.of(String)
   ItemStack.of(String, Int)
   ItemStack.of(PlayerSkin, Int)
    ```
10. HeadDisplay
    ```kotlin
    TODO("Currently there is only Space and Alphabetic-Letters implemented!")
    TODO("Currently there is only HeadSize.BIG and HeadSize.MEDIUM implemented!")
    // Player Head
    HeadDisplay(String, Instance, Pos, Direction)
    HeadDisplay(String, Instance, Pos, Direction, HeadSize)
    HeadDisplay(String, Instance, Pos, Direction, HeadSize, Boolean)
    // Methods
    spawn()
    destroy()
    ```

# Todo
## API
- clickable block
- clickable Item
- clickable entities
- Minecraft Colors