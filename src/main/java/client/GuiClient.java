package client;

import commom.Message;
import commom.User;
import commom.UserList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Consumer;


public final class GuiClient implements Client.LoopingConsumer {
    private static final String TITLE_FORMAT = "%s:%s - %s";
    private static final int WIDTH = 400;
    private static final int HEIGHT = 300;
    private static final Color COLOR_TEXTAREA_BG = Color.getColor("fafa00");
    private final String host;
    private final String port;
    private final String username;
    private final JFrame frame;
    private final JTextArea textarea = new JTextArea();
    private final JTextField textField = new JTextField();
    private final JScrollPane scrollPaneWithArea = new JScrollPane(textarea);
    private final JButton sendButton = new JButton("Send");
    private final Consumer<Message> messageConsumer;
    private final DefaultListModel<User> userListModel  = new DefaultListModel<>();
    private final JList<User> userJList = new JList<>(userListModel);
    private final JSplitPane messagesAndUsersPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);


    private final Logger logger = LoggerFactory.getLogger("client");


    public GuiClient(String host, String port, String username, Consumer<Message> messageConsumer) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.messageConsumer = messageConsumer;

        this.frame = new JFrame();

        frame.setTitle(String.format(TITLE_FORMAT, host, port, username));
        frame.setIconImage(new ImageIcon(getClass().getResource("icon.png")).getImage());
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                messageConsumer.accept(Message.createDisonnectMessage(new User(username)));
            }

            @Override
            public void windowActivated(WindowEvent e) {
                textField.requestFocus();
            }
        });

        frame.setSize(new Dimension(WIDTH, HEIGHT));
        frame.setContentPane(createContentPane());
        configureComponents();

    }

    private void addToTextArea(String line) {
        SwingUtilities.invokeLater(() -> {
            textarea.append(line + "\n");
            scrollPaneWithArea.scrollRectToVisible(textarea.getVisibleRect());
        });
    }

    private Consumer<String> addToTextAreaWith(String prefix) {
        return (line) -> addToTextArea(prefix + line);
    }

    private void configureComponents() {
        textField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendTextToServer();
                }
            }
        });
        sendButton.addActionListener((e) -> sendTextToServer());
        userJList.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> new JLabel(value.getName()));
    }


    @Override
    public void accept(Message message) {
        logger.info(message.toString());
        switch (message.getType()) {
            case MESSAGE:
                formatMessage(this::addToTextArea).accept(message);
                break;
            case USERLIST:
                updateUsers(message.getUserlist());

        }
    }

    private void updateUsers(UserList userlist) {
        SwingUtilities.invokeLater(() -> {
            userListModel.removeAllElements();
            userListModel.addAll(userlist.getUsers());
            frame.setTitle(formatTitle(userlist.currentUser().orElseGet(() -> User.EMPTY)));
        });
    }

    private String formatTitle(User u) {
        return "Chat11 - " + u.getName();

    }

    private Consumer<Message> formatMessage(Consumer<String> stringConsumer) {
        return message -> stringConsumer.accept(
                String.format("[%s] %s", message.getUser().getName(), message.getContent()));

    }

    private void sendTextToServer() {
        messageConsumer.accept(Message.createMessage(textField.getText(), new User(username)));
        textField.setText("");
    }

    private Container createContentPane() {
        var pane = new JPanel();
        pane.setLayout(new BorderLayout());
        textarea.setEditable(false);
        textarea.setBackground(COLOR_TEXTAREA_BG);
        userJList.setBackground(COLOR_TEXTAREA_BG);

        messagesAndUsersPane.add(scrollPaneWithArea);
        scrollPaneWithArea.setAlignmentX(JSplitPane.CENTER_ALIGNMENT);
        messagesAndUsersPane.add(userJList);
        userJList.setAlignmentX(JSplitPane.RIGHT_ALIGNMENT);
        messagesAndUsersPane.setDividerLocation(0.8);
        pane.add(messagesAndUsersPane, BorderLayout.CENTER);
        JPanel commitLine = new JPanel(new BorderLayout());
        commitLine.add(textField, BorderLayout.CENTER);
        commitLine.add(sendButton, BorderLayout.EAST);
        pane.add(commitLine, BorderLayout.SOUTH);

        return pane;
    }



    private void show() {
        try {
            SwingUtilities.invokeAndWait(() -> frame.setVisible(true));
            messagesAndUsersPane.setDividerLocation(0.6);
        } catch (InterruptedException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void loop() {
        show();
    }
}
