import net.bytemc.minestom.server.ByteServer;
import net.bytemc.minestom.server.display.head.HeadDisplay;
import net.minestom.server.coordinate.Pos;

public final class TestServer {

    public TestServer() {
        var instance = ByteServer.getInstance().getInstanceHandler().getSpawnInstance();

        // HeadDisplay
        var head = new HeadDisplay("Test Marco Polo", instance, new Pos(1, 2, 1), settings -> {

        });
        head.spawn();
    }
}
