package net.minestom.testing;

import net.kyori.adventure.translation.GlobalTranslator;
import net.minestom.server.ServerFacade;
import net.minestom.server.adventure.MinestomAdventure;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.packet.server.ComponentHoldingServerPacket;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.player.PlayerConnection;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

final class TestConnectionImpl implements TestConnection {
    private final Env env;
    private final ServerFacade process;
    private final PlayerConnectionImpl playerConnection;

    private final List<IncomingCollector<ServerPacket>> incomingTrackers = new CopyOnWriteArrayList<>();

    TestConnectionImpl(Env env) {
        this.env = env;
        this.process = env.process();
        this.playerConnection = new PlayerConnectionImpl(env.process());
    }

    @Override
    public @NotNull CompletableFuture<Player> connect(@NotNull Instance instance, @NotNull Pos pos) {
        // Use player provider to disable queued chunk sending
        process.getConnectionManager().setPlayerProvider(TestPlayerImpl::new);

        playerConnection.setConnectionState(ConnectionState.LOGIN);
        var player = process.getConnectionManager().createPlayer(playerConnection, UUID.randomUUID(), "RandName");
        player.getEventNode().addListener(AsyncPlayerConfigurationEvent.class, event -> {
            event.setSpawningInstance(instance);
            event.getPlayer().setRespawnPoint(pos);
        });

        // Force the player through the entirety of the login process manually
        process.getConnectionManager().doConfiguration(player, true);
        process.getConnectionManager().transitionConfigToPlay(player);
        process.getConnectionManager().updateWaitingPlayers();
        return CompletableFuture.completedFuture(player);
    }

    @Override
    public @NotNull <T extends ServerPacket> Collector<T> trackIncoming(@NotNull Class<T> type) {
        var tracker = new IncomingCollector<>(type);
        this.incomingTrackers.add(IncomingCollector.class.cast(tracker));
        return tracker;
    }

    final class PlayerConnectionImpl extends PlayerConnection {

        PlayerConnectionImpl(ServerFacade serverFacade) {
            super(serverFacade);
        }

        @Override
        public void sendPacket(@NotNull SendablePacket packet) {
            final var serverPacket = this.extractPacket(packet);
            for (var tracker : incomingTrackers) {
                if (tracker.type.isAssignableFrom(serverPacket.getClass())) tracker.packets.add(serverPacket);
            }
        }

        private ServerPacket extractPacket(final SendablePacket packet) {
            if (!(packet instanceof ServerPacket serverPacket)) return SendablePacket.extractServerPacket(getConnectionState(), packet);

            final Player player = getPlayer();
            if (player == null) return serverPacket;

            if (MinestomAdventure.AUTOMATIC_COMPONENT_TRANSLATION && serverPacket instanceof ComponentHoldingServerPacket) {
                serverPacket = ((ComponentHoldingServerPacket) serverPacket).copyWithOperator(component ->
                        GlobalTranslator.render(component, Objects.requireNonNullElseGet(player.getLocale(), MinestomAdventure::getDefaultLocale)));
            }

            return serverPacket;
        }

        @Override
        public @NotNull SocketAddress getRemoteAddress() {
            return new InetSocketAddress("localhost", 25565);
        }

        @Override
        public void disconnect() {

        }
    }

    final class IncomingCollector<T extends ServerPacket> implements Collector<T> {
        private final Class<T> type;
        private final List<T> packets = new CopyOnWriteArrayList<>();

        public IncomingCollector(Class<T> type) {
            this.type = type;
        }

        @Override
        public @NotNull List<T> collect() {
            incomingTrackers.remove(this);
            return List.copyOf(packets);
        }
    }
}
