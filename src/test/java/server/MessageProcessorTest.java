package server;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
}