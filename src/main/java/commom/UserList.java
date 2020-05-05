package commom;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class UserList implements Serializable {
    private final List<User> users;
    private final int curr;

    public UserList(int currentIndex, List<User> users) {
        this.users = Objects.requireNonNull(users);
        this.curr = users.isEmpty() ? -1 : Objects.checkIndex(currentIndex, users.size());
    }

    public int getIndex() {
        return curr;
    }

    public List<User> getUsers() {
        return users;
    }

    public Optional<User> currentUser() {
        return curr >= 0 ? Optional.ofNullable(users.get(curr)) : Optional.empty();
    }
}
