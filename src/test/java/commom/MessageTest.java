package commom;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MessageTest {
    private final User foo = new User("foo");

    @Test
    void createMessage() {

        var msg = Message.createConnectMessage(foo);
        assertEquals(Message.Type.CONNECT, msg.getType());
    }

    @Test
    void createMessage2() {
        var msg = Message.createConnectMessage(foo);
        assertEquals(new User("foo"), msg.getUser());
    }

    @Test
    void createNormal() {
        var msg = Message.createMessage("aaa", foo);
        assertEquals(new User("foo"), msg.getUser());
        assertEquals(new Message(Message.Type.MESSAGE, "aaa", foo), msg);
    }
}