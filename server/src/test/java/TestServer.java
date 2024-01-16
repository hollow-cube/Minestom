import net.bytemc.minestom.server.ByteServer;
import net.bytemc.minestom.server.clickable.ClickableEntity;
import net.bytemc.minestom.server.display.head.HeadDisplay;
import net.bytemc.minestom.server.display.head.misc.HeadSize;
import net.bytemc.minestom.server.inventory.anvil.AnvilInventory;
import net.bytemc.minestom.server.utils.FlyingItem;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.entity.fakeplayer.FakePlayer;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.utils.Direction;

public final class TestServer {

    public TestServer() {
        var instance = ByteServer.getInstance().getInstanceHandler().getSpawnInstance();

        // HeadDisplay
        var head = new HeadDisplay("Love you Marco Polo", instance, new Pos(1, 2, 1), settings -> {
            settings.withDirection(Direction.NORTH);
            settings.withHeadSize(HeadSize.BIG);
        });
        head.spawn();

        var head2 = new HeadDisplay("Welcome to ByteMC.DE", instance, new Pos(1, 2.25, 1), settings -> {
            settings.withDirection(Direction.NORTH);
            settings.withHeadSize(HeadSize.MID);
        });
        head2.spawn();

        var head3 = new HeadDisplay("0 Player online", instance, new Pos(1, 1.25, 1), settings -> {
            settings.withDirection(Direction.WEST);
            settings.withHeadSize(HeadSize.SMALL);
            //settings.withAdditionDistance(0.5);
            settings.withSpacer(false);
        });
        head3.spawn();

        var entity = new ClickableEntity(EntityType.ALLAY, player -> {
            player.sendMessage("Click! ClickableEntity");
        });
        entity.modify(it -> {
            it.setNoGravity(true);
        });
        entity.spawn(new Pos(1, 10, 1), instance);


        var schemPos = new Pos(0, 0, 0);
        instance.setBlock(schemPos, Block.DIAMOND_BLOCK);
        MinecraftServer.getGlobalEventHandler().addListener(PlayerBlockBreakEvent.class, event -> {
            FlyingItem flyingItem = new FlyingItem(ItemStack.of(Material.ITEM_FRAME), event.getPlayer().getInstance(), event.getPlayer().getPosition().add(0, 2, 0));

            flyingItem.spawn();

            /*schematic.build(Rotation.NONE, block -> {
                return block;
            }).apply(instance, 0, 15, 0, () -> {

            });*/


            /*try {
                SchematicWriter.write(schematic, Path.of("schematics/test.dat"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }*/
        });

        MinecraftServer.getGlobalEventHandler().addListener(PlayerSpawnEvent.class, event -> {
            event.getPlayer().setGameMode(GameMode.CREATIVE);

            AnvilInventory.open(event.getPlayer(), "Test", (player, s) -> {
                player.sendMessage(s);
            });
        });
    }
}
