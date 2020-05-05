package server;

import commom.User;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ServerUserRepositoryTest {
    @Test
    void checkCreateChannel() throws IOException {
        var sr = new ServerUserRepository();
        ObjectOutputStream outputStream = new ObjectOutputStream(new ByteArrayOutputStream(10));
        var id = sr.addChannel(outputStream);
        assertTrue(sr.getChannel(id).isPresent());
    }

    @Test
    void shouldGetUserlistGiveEmptyListWhenEmpty() {
        var sr = new ServerUserRepository();
        UUID uuid = UUID.randomUUID();
        var ul = sr.getUserList(uuid);
        assertEquals(-1, ul.getIndex());
        assertTrue(ul.getUsers().isEmpty());
    }

    @Test
    void shouldAddUserSucceedAfterChannelCreated() throws IOException {
        var sr = new ServerUserRepository();
        ObjectOutputStream outputStream = new ObjectOutputStream(new ByteArrayOutputStream(10));
        UUID uuid = sr.addChannel(outputStream);
        String name = "foo";
        sr.addUser(uuid, new User(name));

        var ul = sr.getUserList(uuid);
        assertTrue(ul.currentUser().isPresent());
        ul.currentUser().ifPresent(u -> assertEquals(u.getName(), name));
        assertEquals(0, ul.getIndex() );

    }

    @Test
    void shouldAddUserThrowWhenNoChaneCreated() throws IOException {
        var sr = new ServerUserRepository();
        UUID uuid = UUID.randomUUID();
        assertThrows(UnsupportedOperationException.class, ()-> sr.addUser(uuid, new User("foo")));

    }

}