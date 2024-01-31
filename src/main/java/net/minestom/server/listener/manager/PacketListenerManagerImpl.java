package net.minestom.server.listener.manager;

import net.minestom.server.ServerFacade;
import net.minestom.server.event.GlobalEventHandlerProvider;
import net.minestom.server.event.player.PlayerPacketEvent;
import net.minestom.server.exception.ExceptionHandlerProvider;
import net.minestom.server.extras.MojangAuthProvider;
import net.minestom.server.listener.*;
import net.minestom.server.listener.common.KeepAliveListener;
import net.minestom.server.listener.common.PluginMessageListener;
import net.minestom.server.listener.common.ResourcePackListener;
import net.minestom.server.listener.common.SettingsListener;
import net.minestom.server.listener.preplay.ConfigListener;
import net.minestom.server.listener.preplay.HandshakeListener;
import net.minestom.server.listener.preplay.LoginListener;
import net.minestom.server.listener.preplay.StatusListener;
import net.minestom.server.network.ConnectionManagerProvider;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.client.common.*;
import net.minestom.server.network.packet.client.configuration.ClientFinishConfigurationPacket;
import net.minestom.server.network.packet.client.handshake.ClientHandshakePacket;
import net.minestom.server.network.packet.client.login.ClientEncryptionResponsePacket;
import net.minestom.server.network.packet.client.login.ClientLoginAcknowledgedPacket;
import net.minestom.server.network.packet.client.login.ClientLoginPluginResponsePacket;
import net.minestom.server.network.packet.client.login.ClientLoginStartPacket;
import net.minestom.server.network.packet.client.play.*;
import net.minestom.server.network.packet.client.status.PingPacket;
import net.minestom.server.network.packet.client.status.StatusRequestPacket;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.network.socket.ServerProvider;
import net.minestom.server.timer.SchedulerManagerProvider;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class PacketListenerManagerImpl implements PacketListenerManager {

    private final static Logger LOGGER = LoggerFactory.getLogger(PacketListenerManagerImpl.class);

    private final Map<Class<? extends ClientPacket>, PacketPrePlayListenerConsumer>[] listeners = new Map[ConnectionState.values().length];
    private final GlobalEventHandlerProvider globalEventHandlerProvider;
    private final ExceptionHandlerProvider exceptionHandlerProvider;

    public PacketListenerManagerImpl(ServerFacade serverFacade) {
        this(serverFacade, serverFacade, serverFacade, serverFacade, serverFacade, serverFacade);
    }

    public PacketListenerManagerImpl(GlobalEventHandlerProvider globalEventHandlerProvider, ExceptionHandlerProvider exceptionHandlerProvider, MojangAuthProvider mojangAuthProvider, ConnectionManagerProvider connectionManagerProvider, SchedulerManagerProvider schedulerManagerProvider, ServerProvider serverProvider) {
        this.globalEventHandlerProvider = globalEventHandlerProvider;
        this.exceptionHandlerProvider = exceptionHandlerProvider;
        for (int i = 0; i < listeners.length; i++) {
            listeners[i] = new ConcurrentHashMap<>();
        }

        setListener(ConnectionState.HANDSHAKE, ClientHandshakePacket.class, HandshakeListener::listener);

        setListener(ConnectionState.STATUS, StatusRequestPacket.class, (packet, connection) -> StatusListener.requestListener(connectionManagerProvider.getConnectionManager(), globalEventHandlerProvider.getGlobalEventHandler(), serverProvider.getServer(), packet, connection));
        setListener(ConnectionState.STATUS, PingPacket.class, (packet, connection) -> StatusListener.pingListener(globalEventHandlerProvider.getGlobalEventHandler(), schedulerManagerProvider.getSchedulerManager(), packet, connection));

        setListener(ConnectionState.LOGIN, ClientLoginStartPacket.class, (packet, connection) -> LoginListener.loginStartListener(mojangAuthProvider.getMojangAuth(), connectionManagerProvider.getConnectionManager(), packet, connection));
        setListener(ConnectionState.LOGIN, ClientEncryptionResponsePacket.class, (packet, connection) -> LoginListener.loginEncryptionResponseListener(mojangAuthProvider.getMojangAuth(), exceptionHandlerProvider.getExceptionHandler(), connectionManagerProvider.getConnectionManager(), packet, connection));
        setListener(ConnectionState.LOGIN, ClientLoginPluginResponsePacket.class, (packet, connection) -> LoginListener.loginPluginResponseListener(exceptionHandlerProvider.getExceptionHandler(), connectionManagerProvider.getConnectionManager(), packet, connection));
        setListener(ConnectionState.LOGIN, ClientLoginAcknowledgedPacket.class, (packet, connection) -> LoginListener.loginAckListener(connectionManagerProvider.getConnectionManager(), packet, connection));

        setConfigurationListener(ClientSettingsPacket.class, SettingsListener::listener);
        setConfigurationListener(ClientPluginMessagePacket.class, PluginMessageListener::listener);
        setConfigurationListener(ClientKeepAlivePacket.class, KeepAliveListener::listener);
        setConfigurationListener(ClientPongPacket.class, (packet, player) -> {/* empty */});
        setConfigurationListener(ClientResourcePackStatusPacket.class, ResourcePackListener::listener);
        setConfigurationListener(ClientFinishConfigurationPacket.class, ConfigListener::finishConfigListener);

        setPlayListener(ClientKeepAlivePacket.class, KeepAliveListener::listener);
        setPlayListener(ClientCommandChatPacket.class, ChatMessageListener::commandChatListener);
        setPlayListener(ClientChatMessagePacket.class, ChatMessageListener::chatMessageListener);
        setPlayListener(ClientClickWindowPacket.class, WindowListener::clickWindowListener);
        setPlayListener(ClientCloseWindowPacket.class, WindowListener::closeWindowListener);
        setPlayListener(ClientConfigurationAckPacket.class, PlayConfigListener::configAckListener);
        setPlayListener(ClientPongPacket.class, WindowListener::pong);
        setPlayListener(ClientEntityActionPacket.class, EntityActionListener::listener);
        setPlayListener(ClientHeldItemChangePacket.class, PlayerHeldListener::heldListener);
        setPlayListener(ClientPlayerBlockPlacementPacket.class, BlockPlacementListener::listener);
        setPlayListener(ClientSteerVehiclePacket.class, PlayerVehicleListener::steerVehicleListener);
        setPlayListener(ClientVehicleMovePacket.class, PlayerVehicleListener::vehicleMoveListener);
        setPlayListener(ClientSteerBoatPacket.class, PlayerVehicleListener::boatSteerListener);
        setPlayListener(ClientPlayerPacket.class, PlayerPositionListener::playerPacketListener);
        setPlayListener(ClientPlayerRotationPacket.class, PlayerPositionListener::playerLookListener);
        setPlayListener(ClientPlayerPositionPacket.class, PlayerPositionListener::playerPositionListener);
        setPlayListener(ClientPlayerPositionAndRotationPacket.class, PlayerPositionListener::playerPositionAndLookListener);
        setPlayListener(ClientTeleportConfirmPacket.class, PlayerPositionListener::teleportConfirmListener);
        setPlayListener(ClientPlayerDiggingPacket.class, PlayerDiggingListener::playerDiggingListener);
        setPlayListener(ClientAnimationPacket.class, AnimationListener::animationListener);
        setPlayListener(ClientInteractEntityPacket.class, UseEntityListener::useEntityListener);
        setPlayListener(ClientUseItemPacket.class, UseItemListener::useItemListener);
        setPlayListener(ClientStatusPacket.class, PlayStatusListener::listener);
        setPlayListener(ClientSettingsPacket.class, SettingsListener::listener);
        setPlayListener(ClientCreativeInventoryActionPacket.class, CreativeInventoryActionListener::listener);
        setPlayListener(ClientCraftRecipeRequest.class, RecipeListener::listener);
        setPlayListener(ClientTabCompletePacket.class, TabCompleteListener::listener);
        setPlayListener(ClientPluginMessagePacket.class, PluginMessageListener::listener);
        setPlayListener(ClientPlayerAbilitiesPacket.class, AbilitiesListener::listener);
        setPlayListener(ClientResourcePackStatusPacket.class, ResourcePackListener::listener);
        setPlayListener(ClientAdvancementTabPacket.class, AdvancementTabListener::listener);
        setPlayListener(ClientSpectatePacket.class, SpectateListener::listener);
        setPlayListener(ClientEditBookPacket.class, BookListener::listener);
        setPlayListener(ClientChatSessionUpdatePacket.class, (packet, player) -> {/* empty */});
        setPlayListener(ClientChunkBatchReceivedPacket.class, ChunkBatchListener::batchReceivedListener);
    }

    @Override
    public <T extends ClientPacket> void processClientPacket(@NotNull T packet, @NotNull PlayerConnection connection) {
        final ConnectionState state = connection.getConnectionState();
        final Class clazz = packet.getClass();
        PacketPrePlayListenerConsumer<T> packetListenerConsumer = listeners[state.ordinal()].get(clazz);

        // Listener can be null if none has been set before, call PacketConsumer anyway
        if (packetListenerConsumer == null) {
            LOGGER.warn("Packet " + clazz + " does not have any default listener! (The issue comes from Minestom)");
            return;
        }

        // Event
        if (state == ConnectionState.PLAY) {
            PlayerPacketEvent playerPacketEvent = new PlayerPacketEvent(connection.getPlayer(), packet);
            globalEventHandlerProvider.getGlobalEventHandler().call(playerPacketEvent);
            if (playerPacketEvent.isCancelled()) {
                return;
            }
        }

        // Finally execute the listener
        try {
            packetListenerConsumer.accept(packet, connection);
        } catch (Exception e) {
            // Packet is likely invalid
            exceptionHandlerProvider.getExceptionHandler().handleException(e);
        }
    }

    @Override
    public <T extends ClientPacket> void setListener(@NotNull ConnectionState state, @NotNull Class<T> packetClass, @NotNull PacketPrePlayListenerConsumer<T> consumer) {
        this.listeners[state.ordinal()].put(packetClass, consumer);
    }

    @Override
    public <T extends ClientPacket> void setPlayListener(@NotNull Class<T> packetClass, @NotNull PacketPlayListenerConsumer<T> consumer) {
        setListener(ConnectionState.PLAY, packetClass, (packet, playerConnection) -> consumer.accept(packet, playerConnection.getPlayer()));
    }

    @Override
    public <T extends ClientPacket> void setConfigurationListener(@NotNull Class<T> packetClass, @NotNull PacketPlayListenerConsumer<T> consumer) {
        setListener(ConnectionState.CONFIGURATION, packetClass, (packet, playerConnection) -> consumer.accept(packet, playerConnection.getPlayer()));
    }

    @Override
    @Deprecated
    public <T extends ClientPacket> void setListener(@NotNull Class<T> packetClass, @NotNull PacketPlayListenerConsumer<T> consumer) {
        setPlayListener(packetClass, consumer);
    }
}
