package client;

import commom.Message;
import commom.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.function.Consumer;
import java.util.stream.Collectors;

class ConsoleClient implements Client.LoopingConsumer {

    private final User user;
    private final Consumer<Message> messageConsumer;

    public ConsoleClient(String username, Consumer<Message> typedMessageConsumer) {
        this.user = new User(username);
        this.messageConsumer = typedMessageConsumer;
    }

    @Override
    public void loop() {
        try(BufferedReader r = new BufferedReader(new InputStreamReader(System.in))) {
            var line = r.readLine();
            while (!line.equals(Client.INPUT_TERMINAL)) {
                messageConsumer.accept(Message.createMessage(line, user));
                line = r.readLine();
            }
            messageConsumer.accept(Message.createDisonnectMessage(user));
        } catch (IOException e) {
            System.err.print("Console client exception: " + e);
        }
    }
    private void showMessage(String format, Object...args) {
        System.out.format(format, args);
    }

    @Override
    public void accept(Message message) {
        switch (message.getType()) {
            case MESSAGE:
                showMessage("[%s]: %s\n",
                        message.getUser().getName(), message.getContent());
                break;
            case CONNECT:
                showMessage("Connected %s\n", message.getUser().getName());
            case DISCONNECT:
                showMessage("Disconnected %s", message.getUser().getName());
                break;
            case USERLIST:
                showMessage("Users: \n%s\n",
                        message.getUserlist().getUsers().stream()
                                .map(User::getName)
                                .collect(Collectors.joining(",")));
                break;
            case STATUS:
                showMessage("[%s] changed status to [%s]\n", message.getUser().getName(), message.getUser().getStatus());
                break;
        }

    }
}
