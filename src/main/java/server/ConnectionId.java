package server;

import java.util.Objects;

final class ConnectionId<T> {
    private final T value;

    ConnectionId(T value) {
        Objects.requireNonNull(value);
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConnectionId<?> that = (ConnectionId<?>) o;

        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
