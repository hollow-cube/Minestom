package net.minestom.server.listener.preplay;

import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.server.ClientPingServerEvent;
import net.minestom.server.event.server.ServerListPingEvent;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.network.packet.client.status.PingPacket;
import net.minestom.server.network.packet.client.status.StatusRequestPacket;
import net.minestom.server.network.packet.server.status.PongPacket;
import net.minestom.server.network.packet.server.status.ResponsePacket;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.network.socket.Server;
import net.minestom.server.ping.ServerListPingType;
import net.minestom.server.timer.SchedulerManager;
import org.jetbrains.annotations.NotNull;

public final class StatusListener {

    public static void requestListener(ConnectionManager connectionManager, GlobalEventHandler globalEventHandler, Server server, @NotNull StatusRequestPacket packet, @NotNull PlayerConnection connection) {
        final ServerListPingType pingVersion = ServerListPingType.fromModernProtocolVersion(connection.getProtocolVersion());
        final ServerListPingEvent statusRequestEvent = new ServerListPingEvent(connectionManager, server, connection, pingVersion);
        globalEventHandler.callCancellable(statusRequestEvent, () ->
                connection.sendPacket(new ResponsePacket(pingVersion.getPingResponse(statusRequestEvent.getResponseData()))));
    }

    public static void pingListener(GlobalEventHandler globalEventHandler, SchedulerManager schedulerManager, @NotNull PingPacket packet, @NotNull PlayerConnection connection) {
        final ClientPingServerEvent clientPingEvent = new ClientPingServerEvent(connection, packet.number());
        globalEventHandler.call(clientPingEvent);

        if (clientPingEvent.isCancelled()) {
            connection.disconnect();
        } else {
            if (clientPingEvent.getDelay().isZero()) {
                connection.sendPacket(new PongPacket(clientPingEvent.getPayload()));
                connection.disconnect();
            } else {
                schedulerManager.buildTask(() -> {
                    connection.sendPacket(new PongPacket(clientPingEvent.getPayload()));
                    connection.disconnect();
                }).delay(clientPingEvent.getDelay()).schedule();
            }
        }
    }

}
