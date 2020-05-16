package server;

import commom.User;
import commom.UserList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class ServerUserRepository {
    private final Map<UUID, ObjectOutputStream> oosMap = new TreeMap<>();
    private final Map<UUID, User> userMap = new TreeMap<>();
    private final AtomicInteger counter = new AtomicInteger(0);
    public static final Logger logger = LoggerFactory.getLogger("server");

    public synchronized void addUser(UUID uuid, User user) {
        if (!oosMap.containsKey(uuid)) {
            throw new UnsupportedOperationException("There is no channel for user " + uuid);
        }
        boolean shouldRename = userMap.values().stream().anyMatch(Predicate.isEqual(user));
        var name = shouldRename ? user.getName() + counter.incrementAndGet() : user.getName();
        userMap.computeIfAbsent(uuid, (u) -> new User(name));
        logger.info("Added {}", shouldRename ? String.format("%s (connected as %s)", name, user.getName()) : name);
    }

    public synchronized void forEachChannel(BiConsumer<UUID, ObjectOutputStream> channellFn) {
        oosMap.entrySet().forEach(e -> channellFn.accept(e.getKey(), e.getValue()));
    }
    public synchronized void remove(UUID uuid) {
        logger.info("Removed {}", getUser(uuid));
        oosMap.remove(uuid);
        userMap.remove(uuid);
    }

    public synchronized UUID addChannel(OutputStream outputStream) throws IOException {
        var uuid = UUID.randomUUID();
        oosMap.put(uuid, new ObjectOutputStream(outputStream));
        return uuid;

    }

    public Optional<ObjectOutputStream> getChannel(UUID uuid) {
        return Optional.ofNullable(oosMap.get(uuid));
    }


    public synchronized UserList getUserList(UUID uuid) {
        var vals = new ArrayList<>(userMap.values());
        return new UserList(vals.indexOf(userMap.get(uuid)), vals);
    }

    public User getUser(UUID uuid) {
        return userMap.getOrDefault(uuid, User.EMPTY);
    }

    public synchronized void updateUser(UUID uuid, User user) {
        userMap.computeIfPresent(uuid, (uuid1, user1) -> user);
    }
}
