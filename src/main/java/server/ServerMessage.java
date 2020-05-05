package server;

import commom.Message;

import java.util.UUID;

final class ServerMessage {
    private final UUID uuid;
    private final Message message;

    ServerMessage(UUID uuid, Message msg) {
        this.uuid = uuid;
        this.message = msg;
    }

    Message getMessage() {
        return message;
    }

    UUID getUuid() {
        return uuid;
    }

    @Override
    public String toString() {
        return "ServerMessage{" +
                "uuid=" + uuid +
                ", message=" + message +
                '}';
    }
}
