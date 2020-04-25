package server;

import commom.User;

import java.io.ObjectOutputStream;
import java.util.Optional;
import java.util.UUID;

class ServerUser {
    private User user;
    private final UUID id;
    private final ObjectOutputStream os;

    ServerUser(User user, UUID id, ObjectOutputStream os) {
        this.user = user;
        this.id = id;
        this.os = os;
    }

    public UUID getId() {
        return id;
    }

    Optional<User> getUser() {
        return Optional.ofNullable(user);
    }

    ObjectOutputStream getOs() {
        return os;
    }

    void updateUser(User withUpddatedName) {
        user = withUpddatedName;
    }

    @Override
    public String toString() {
        return "ServerUser{" +
                "user=" + user +
                ", id=" + id +
                '}';
    }
}
