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


# Todo
## Inventory:
- [x] SimpleInventory abstract
- [ ] PageableInventory abstract
- [ ] AnvilInventory abstract
- [x] ClickableItem
## API
- [x] Instance handler solve the problem and todos
- [ ] Hologram handler
- [ ] Block handler (state's from blocks)
- [ ] Pre - Sub-fix api
- [ ] ConfigPath
## Extensions
- [ ] Extension handler
- [ ] Load extension
- [ ] Inject dependencies into extensions
- [ ] List jars
- [ ] Implements json file reading (extension.json)