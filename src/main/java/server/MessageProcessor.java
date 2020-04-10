package server;

import commom.Message;
import commom.User;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;

public class MessageProcessor implements  Runnable{
    private static final Logger logger = Logger.getLogger(MessageProcessor.class.getName());
    private final BlockingQueue<ServerMessage> queue;
    private final Map<UUID, ServerUser> userMap = new TreeMap<>();

    MessageProcessor(BlockingQueue<ServerMessage> messagesFromClients) {
        this.queue = messagesFromClients;
    }

    synchronized  UUID addUser(OutputStream outputStream) throws IOException {
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
                        var uuid = msg.getUuid();
                        if (userMap.containsKey(uuid)) {
                            var  withUpddatedName = withUpddatedName(usermsg.getUser());
                            userMap.get(uuid).updateUser(withUpddatedName);
                        }
                        send(msg);
                        break;
                    case MESSAGE:
                        send(msg);
                        break;
                    case DISCONNECT:
                        userMap.remove(msg.getUuid()); // socket is already closed for this client
                        send(msg);
                        break;
                    case INVALID:
                        logger.warning("Invalid message received: " + usermsg.getContent());
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + usermsg.getType());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    private void send(ServerMessage msg) {
        logger.info("Sending " + msg);
        userMap.values().forEach(su -> {
            try {
                var messageSource = userMap.get(msg.getUuid()).getUser();
                if (messageSource.isPresent()) {
                    sendToUser(su, msg.withUser(messageSource.get())); // overwrite user
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void sendToUser(ServerUser su, Message msg) throws IOException {
        su.getOs().writeObject(msg);
        su.getOs().flush();
    }

    private User withUpddatedName(User user) {
        var cnt = userMap.values().stream().filter(su -> su.getUser().isPresent() && su.getUser().get().getName().startsWith(user.getName())).count();
        return cnt == 0 ? user : new User(user.getName() + "_" + cnt);
    }

}
