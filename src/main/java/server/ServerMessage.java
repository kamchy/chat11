package server;

import commom.Message;
import commom.User;

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

    Message withUser(User user) {
        return new Message(message.getType(), message.getContent(), user);
    }
}
