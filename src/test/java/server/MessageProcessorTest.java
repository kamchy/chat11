package server;

import commom.Message;
import commom.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;

class MessageProcessorTest {

    @Test
    void checkMessageFromClientsNotNUll() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            new MessageProcessor(null, new ServerUserRepository());
        });
    }

    @Test
    void checkUserRepositoryNotNull() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            new MessageProcessor(new ArrayBlockingQueue<>(10) , null);
        });

    }

    @Test
    void checkUserCanChangeStatus() throws IOException {
        ServerUserRepository repo = new ServerUserRepository();
        ArrayBlockingQueue<ServerMessage> messagesFromClients = new ArrayBlockingQueue<>(10);
        MessageProcessor mp = new MessageProcessor(messagesFromClients, repo);
        UUID uuid = repo.addChannel(new ByteArrayOutputStream(1));
        User user = new User("foo");
        mp.process(new ServerMessage(uuid, Message.createConnectMessage(user)));
        String status = "hello world";
        User withStatus = new User(user.getName(), status);
        mp.process(new ServerMessage(uuid, Message.createStatusUpdate(withStatus)));
        Assertions.assertEquals(status, repo.getUser(uuid).getStatus());
    }

}