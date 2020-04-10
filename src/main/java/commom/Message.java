package commom;

import java.io.Serializable;
import java.util.Objects;

public final class Message implements Serializable {

    private final Type type;
    private final String content;
    private final User user;

    public Message(Type valueOf, String content, User user) {
        this.type = valueOf;
        this.content = content;
        this.user = user;
    }

    public static Message createConnectMessage(User user) {
        return new Message(Type.CONNECT," ", user);
    }

    public static Message createDisonnectMessage(User user) {
        return new Message(Type.DISCONNECT," ", user);
    }

    public static Message createMessage(String message, User user) {
        return new Message(Type.MESSAGE, message, user);
    }


    private static Message invalid(String message) {
        return new Message(Type.INVALID, message, User.EMPTY);
    }

    public Type getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    public User getUser() {
        return user;
    }

    @Override
    public String toString() {
        return "Message{" +
                "type=" + type +
                ", content='" + content + '\'' +
                ", user=" + user +
                '}';
    }

    public Message withUser(User user) {
        return new Message(type, content, user);
    }

    public enum Type {
        CONNECT, MESSAGE, DISCONNECT, INVALID
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Message message = (Message) o;

        if (type != message.type) return false;
        if (!Objects.equals(content, message.content)) return false;
        return Objects.equals(user, message.user);
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (content != null ? content.hashCode() : 0);
        result = 31 * result + (user != null ? user.hashCode() : 0);
        return result;
    }
}
