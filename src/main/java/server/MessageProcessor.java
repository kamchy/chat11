package server;

import commom.Message;
import commom.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;

public class MessageProcessor implements  Runnable{
    private static final Logger logger = LoggerFactory.getLogger(MessageProcessor.class);
    private final BlockingQueue<ServerMessage> queue;
    private final Map<UUID, ServerUser> userMap = new TreeMap<>();

    MessageProcessor(BlockingQueue<ServerMessage> messagesFromClients) {
        this.queue = messagesFromClients;
    }

    synchronized  UUID addChannell(OutputStream outputStream) throws IOException {
        var uuid = UUID.randomUUID();
        logger.info("Add null user with id: " + uuid);
        userMap.put(uuid, new ServerUser(null, uuid, new ObjectOutputStream(outputStream)));
        return uuid;
    }

    @Override
    public void run() {
        while (true) {
            try {
                var msg = queue.take();
                logger.info("Loop:  " + msg);
                var usermsg = msg.getMessage();
                switch (msg.getMessage().getType()) {
                    case CONNECT:
                        addUser(msg);
                        send(msg);
                        break;
                    case MESSAGE:
                        send(msg);
                        break;
                    case DISCONNECT:
                        logger.info("Got disconnect message from user "+ usermsg.getUser());
                        send(msg);
                        removeUser(msg.getUuid());
                        break;
                    case INVALID:
                        logger.warn("Invalid message received: " + usermsg.getContent());
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + usermsg.getType());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    private synchronized void addUser(ServerMessage msg) {
        var userMsg = msg.getMessage();
        var uuid = msg.getUuid();
        if (userMap.containsKey(uuid)) {
            var userWithUpdatedName = withUpdatedName(userMsg.getUser());
            userMap.get(uuid).updateUser(userWithUpdatedName);
        }
    }

    synchronized  private void removeUser(UUID uuid) {
        userMap.remove(uuid);
    }

    private synchronized void send(ServerMessage msg) {
        logger.info("Sending " + msg);
        userMap.values().forEach(su -> {
            try {
                ServerUser serverUser = userMap.get(msg.getUuid());
                var sourceUser = serverUser != null ? serverUser.getUser().orElse(msg.getMessage().getUser()) : msg.getMessage().getUser();
                // overwrite user if exists in map (might not exist if was removed, then e original user (ugly)
                sendToUser(su, msg.withUser(sourceUser));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void sendToUser(ServerUser su, Message msg) throws IOException {
        logger.info("Sending message " + msg + " to user " + su);
        su.getOs().writeObject(msg);
        su.getOs().flush();
    }

    private User withUpdatedName(User user) {
        var cnt = userMap.values().stream()
                .filter(su ->
                        su.getUser().isPresent() &&
                        su.getUser().get().getName().startsWith(user.getName()))
                .count();
        return cnt == 0 ? user : new User(user.getName() + "_" + cnt);
    }

}
