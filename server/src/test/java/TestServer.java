import net.bytemc.minestom.server.ByteServer;
import net.bytemc.minestom.server.display.head.HeadDisplay;
import net.bytemc.minestom.server.display.head.misc.HeadSize;
import net.minestom.server.coordinate.Pos;
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
            settings.withDirection(Direction.NORTH);
            settings.withHeadSize(HeadSize.MID);
            settings.withAdditionDistance(0.5);
            settings.withSpacer(false);
        });
        head3.spawn();
    }
}
