package commom;

import java.io.Serializable;
import java.util.Objects;

public final class Message implements Serializable {

    private final Type type;
    private final String content;
    private final User user;
    private UserList userlist;

    private Message(Type valueOf, String content, User user) {
        this.type = valueOf;
        this.content = content;
        this.user = user;
        this.userlist = null;
    }

    private  Message(UserList userList, User sender) {
        this(Type.USERLIST, "", userList.currentUser().orElseGet(() -> User.EMPTY));
        this.userlist = userList;
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

    public static Message createUserListMessage(UserList userList, User source) {
        return new Message(userList, source);
    }

    public static Message withUser(Message message, User sender) {
        return message.getType().equals(Type.USERLIST) ? new Message(message.userlist, sender) : new Message(message.getType(), message.getContent(), sender);
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

    public UserList getUserlist() {
        return userlist;
    }

    public enum Type {
        CONNECT, MESSAGE, DISCONNECT, USERLIST;

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
