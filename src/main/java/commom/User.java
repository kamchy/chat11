package commom;

import java.io.Serializable;
import java.util.Objects;

public final class User implements Serializable {
    public static User EMPTY = new User("EMPTY", "");
    private final String name;
    private final String status;

    public User(String name) {
        this(name, "");
    }
    public User(String name, String status) {
        this.name = name;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", status='" + status + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (!Objects.equals(name, user.name)) return false;
        return Objects.equals(status, user.status);
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (status != null ? status.hashCode() : 0);
        return result;
    }

    public String getStatus() {
        return status;
    }
}
