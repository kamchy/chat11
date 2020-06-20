package client;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import commom.Message;
import commom.User;

import java.io.IOException;
import java.util.function.Consumer;

class CursesClient implements Client.LoopingConsumer {

    private final User user;
    private final Consumer<Message> messageConsumer;

    TextBox placeFor_names = new TextBox("placeFor names");
    TextBox place_for_messages = new TextBox("Place for messages");
    TextBox entry;
    WindowBasedTextGUI textGUI;
    final Window window;

    public CursesClient(String userName, Consumer<Message> messageConsumer) {
        this.user = new User(userName);
        this.messageConsumer = messageConsumer;
        window = new BasicWindow("Chat: " + userName);

        try {
            initialize();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void initialize() throws IOException {

        Terminal terminal = new DefaultTerminalFactory().createTerminal();
        terminal.setBackgroundColor(TextColor.ANSI.BLACK);
        Screen screen = new TerminalScreen(terminal);
        screen.startScreen();
        textGUI = new MultiWindowTextGUI(screen);

        Panel contentPanel = new Panel(new BorderLayout());
        Panel entryPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));
        place_for_messages.setSize(terminal.getTerminalSize().withRelativeColumns(-14));
        place_for_messages.setReadOnly(true);
        place_for_messages.setVerticalFocusSwitching(true);
        placeFor_names.setReadOnly(true);
        contentPanel.addComponent(place_for_messages.setLayoutData(BorderLayout.Location.CENTER).withBorder(Borders.singleLine("Messages")));
        placeFor_names.setSize(terminal.getTerminalSize().withRelativeColumns(10));
        contentPanel.addComponent(placeFor_names.setLayoutData(BorderLayout.Location.RIGHT));
        contentPanel.addComponent(entryPanel.setLayoutData(BorderLayout.Location.BOTTOM));

        entry = new TextBox(new TerminalSize(80, 1));
        entry.takeFocus();
        Button sendButton = new Button("Send", () -> messageConsumer.accept(Message.createMessage(entry.getText(), user)));
        Button exitButton = new Button("Exit", () -> {
            messageConsumer.accept(Message.createDisonnectMessage(user));
            try {
                screen.stopScreen();
                System.exit(0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        entryPanel.addComponent(entry.setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.Center)));
        entryPanel.addComponent(sendButton.setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.End)));
        entryPanel.addComponent(exitButton.setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.End)));

        window.setComponent(contentPanel);
        window.setPosition(TerminalPosition.OFFSET_1x1);
        window.setFixedSize(new TerminalSize(100,20));
    }

    @Override
    public void loop() {
        textGUI.addWindowAndWait(window);
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
                updateUsers(message);
                break;
            case STATUS:
                showMessage("[%s] changed status to [%s]\n", message.getUser().getName(), message.getUser().getStatus());
                break;
        }

    }

    private void showMessage(String format, Object... elems) {
        place_for_messages.addLine(String.format(format, elems));
    }

    private void updateUsers(Message message) {
        for (int i = 0; i < placeFor_names.getLineCount(); i++) {
            placeFor_names.removeLine(i);
        }

        message.getUserlist().getUsers().stream()
                .map(User::getName).forEach(name -> placeFor_names.addLine(name));

    }

}
