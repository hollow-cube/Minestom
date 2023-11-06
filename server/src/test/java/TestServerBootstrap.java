import net.bytemc.minestom.server.ByteServerBootstrap;

public final class TestServerBootstrap {

    public static void main(String[] args) {
        ByteServerBootstrap.main(args);
        new TestServer();
    }
}
