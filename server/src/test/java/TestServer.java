import net.bytemc.minestom.server.ByteServer;
import net.bytemc.minestom.server.display.head.HeadDisplay;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.utils.Direction;

public final class TestServer {

    public TestServer() {
        var instance = ByteServer.getInstance().getInstanceHandler().getSpawnInstance();

        // HeadDisplay
        var head = new HeadDisplay("Love you Marco Polo", instance, new Pos(1, 2, 1), settings -> {
            settings.withDirection(Direction.NORTH);
        });
        head.spawn();
    }
}
