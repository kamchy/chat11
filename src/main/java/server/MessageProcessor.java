package server;

import commom.Message;
import commom.User;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class MessageProcessor implements  Runnable{
    private final BlockingQueue<ServerMessage> messageQueue;


    private final ServerUserRepository userRepository;
    private AtomicInteger counter = new AtomicInteger(1);

    MessageProcessor(BlockingQueue<ServerMessage> messagesFromClients, ServerUserRepository userRepository) {
        this.messageQueue = Objects.requireNonNull(messagesFromClients);
        this.userRepository = Objects.requireNonNull(userRepository);
    }

    synchronized  UUID addChannel(OutputStream outputStream) throws IOException {
        return userRepository.addChannel(outputStream);
    }

    @Override
    public void run() {
        Thread.currentThread().setName("MessageProcessor");
        while (true) {
            try {
                var msg = messageQueue.take();
                var userMessage = msg.getMessage();
                switch (msg.getMessage().getType()) {
                    case CONNECT:
                        addUser(msg);
                        sendExitsingUsers();
                        break;
                    case MESSAGE:
                        sendToAll(msg);
                        break;
                    case DISCONNECT:
                        removeUser(msg.getUuid());
                        sendExitsingUsers();
                        break;
                    case STATUS:
                        updateUser(msg);
                        sendExitsingUsers();
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + userMessage.getType());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    private void updateUser(ServerMessage msg) {
        userRepository.updateUser(msg.getUuid(), msg.getMessage().getUser());
    }

    private void sendExitsingUsers() {
        userRepository.forEachChannel((uuid, oos) -> {
            var ul = userRepository.getUserList(uuid);
            sendToUser(oos, Message.createUserListMessage(ul, ul.currentUser().orElse(User.EMPTY)));
        });
    }

    private synchronized void addUser(ServerMessage msg) {
        userRepository.addUser(msg.getUuid(), msg.getMessage().getUser());
    }

    synchronized  private void removeUser(UUID uuid) {
        userRepository.remove(uuid);
    }

    private void sendToAll(ServerMessage msg) {
        var sender = userRepository.getUser(msg.getUuid());
        userRepository.forEachChannel((uuid, oos) ->
                sendToUser(oos, Message.withUser(msg.getMessage(), sender)));
    }

    private void sendToUser(ObjectOutputStream channel, Message msg) {
        try {
            channel.writeObject(msg);
            channel.flush();
        } catch (IOException e) {
            System.err.println(e);
        }
    }
}
