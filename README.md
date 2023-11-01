# ByteMC - Minestom

### Description
Will be released soon as possible. The bytemc team is on the way to implements all basic functions.


# Documentation
1. FakePlayers
    ```java
    FakePlayer.initPlayer(PlayerSkin, Instance, Pos, Consumer<FakePlayer>);
    ```
    Also manipulate `FakePlayers` with skin layers:
    ```java
    fakePlayer.meta.allowAllSkinLayers(); 
    ```
2. Instance
    ```java
    // Disable time:
    Instance.disableTimeRotation();
    ```
3. Extension
    ```java
   // Return path:
   Extension.getConfigPath();
    ```

# Todo
## Inventory:
- [ ] AnvilInventory abstract
## API
- [ ] Hologram handler
- [ ] Block handler (state's from blocks)