package client;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.bundle.LanternaThemes;
import com.googlecode.lanterna.graphics.SimpleTheme;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import commom.Message;
import commom.User;

import javax.sound.sampled.Line;
import java.io.IOException;
import java.util.function.Consumer;

class CursesClient implements Client.LoopingConsumer {

    private final User user;
    private final Consumer<Message> messageConsumer;
    TextBox namesPanel = new TextBox("User names");
    TextBox messagesPanel = new TextBox("Messages");
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
        Screen screen = new TerminalScreen(terminal);
        screen.startScreen();
        textGUI = new MultiWindowTextGUI(screen);
        textGUI.setTheme(new SimpleTheme(TextColor.ANSI.WHITE, TextColor.ANSI.BLACK, SGR.BORDERED));

        Panel contentPanel = new Panel(new BorderLayout());

        Panel entryPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));
        TerminalSize terminalSize = terminal.getTerminalSize();

        int messagePanelWidth = (int)(terminalSize.getColumns() * 0.8);
        messagesPanel.setPreferredSize(terminalSize.withColumns(messagePanelWidth).withRelativeRows(-10));
        messagesPanel.setReadOnly(true);

        namesPanel.setReadOnly(true);
        namesPanel.setSize(terminalSize.withColumns(terminalSize.getColumns() - messagePanelWidth).withRelativeRows(-10));

        contentPanel.addComponent(namesPanel.setLayoutData(BorderLayout.Location.RIGHT).withBorder(Borders.singleLine("Users")));
        contentPanel.addComponent(messagesPanel.setLayoutData(BorderLayout.Location.CENTER).withBorder(Borders.singleLine("Messages")));
        contentPanel.addComponent(entryPanel.setLayoutData(BorderLayout.Location.BOTTOM));

        entry = new TextBox(new TerminalSize(messagePanelWidth, 1));
        entry.setTheme(new SimpleTheme(TextColor.ANSI.YELLOW_BRIGHT, TextColor.ANSI.BLACK_BRIGHT, SGR.BORDERED));


        Button sendButton = new Button("Send", () ->
                messageConsumer.accept(Message.createMessage(entry.getText(), user)));
        Button exitButton = new Button("Exit", () -> {
            messageConsumer.accept(Message.createDisonnectMessage(user));
            try {
                screen.stopScreen();
                System.exit(0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        Panel buttonsPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));
        entryPanel.addComponent(entry.setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.Center)).withBorder(Borders.singleLine("Your message")));
        entryPanel.addComponent(buttonsPanel.setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.End)).withBorder(Borders.singleLine()));
        buttonsPanel.addComponent(sendButton);
        buttonsPanel.addComponent(exitButton);

        window.setComponent(contentPanel);
        entry.takeFocus();
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
        messagesPanel.addLine(String.format(format, elems));
        messagesPanel.handleKeyStroke(new KeyStroke(KeyType.PageDown));
    }

    private void updateUsers(Message message) {
        for (int i = 0; i < namesPanel.getLineCount(); i++) {
            namesPanel.removeLine(i);
        }

        message.getUserlist().getUsers().stream()
                .map(User::getName).forEach(name -> namesPanel.addLine(name));

    }

}
