# ByteMC - Minestom

> **Wiki**\
> If you need help just look into our wiki:\
> https://github.com/bytemcnetzwerk/minestom/wiki


### Description
> **Progress**\
> We are currently in the process of adding functions. Therefore, it is not guaranteed that the current version will be stable.
> If you have ideas write us a message: https://discord.gg/bZ7hGX4pPt

# Dependencies

> **Warning**
>
> You need following repository `bytemc-public`
> 
Repository:
```xml
<repository>
    <id>bytemc-public</id>
    <url>https://artifactory.bytemc.de/artifactory/bytemc-public</url>
</repository>
```

Our own bytemc-server-minestom fork: 
```xml 
<dependency>
    <groupId>net.bytemc</groupId>
    <artifactId>server</artifactId>
    <version>VERSION</version>
</dependency>
```

Only our modifyed minestom version:
```xml  
<dependency>
    <groupId>net.bytemc</groupId>
    <artifactId>minestom</artifactId>
    <version>VERSION</version>
</dependency>
```

# Todo
## Not implemented
- [ ] HeadDisplay size [SMALL, VERY_SMALL]
- [ ] HeadDisplay rotation [UP, DOWN]
- [ ] HeadDisplay string [SYMBOLS]
- [ ] Clickable Hologram
- [ ] Edit Hologram lines

## API
- [ ] ClickableBlock
- [ ] Minecraft Colors

### Methods - THAT I HAVE NOT IMPLEMENTED INTO THE WIKI (TODO)

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