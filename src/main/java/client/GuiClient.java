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
    private static final int WIDTH = 600;
    private static final int HEIGHT = 400;
    private static final Color COLOR_TEXTAREA_BG = Color.getColor("fafa00");
    private final String host;
    private final String port;
    private final JFrame frame;
    private final JTextArea textarea = new JTextArea();
    private final JTextField textField = new JTextField();
    private final JScrollPane scrollPaneWithArea = new JScrollPane(textarea);
    private final JButton sendButton = new JButton("Send");
    private final JButton changeStatusButton = new JButton("Status");
    private final Consumer<Message> messageConsumer;
    private final DefaultListModel<User> userListModel  = new DefaultListModel<>();
    private final UserListCellRenderer userListCellRenderer = new UserListCellRenderer();
    private final JList<User> userJList = new JList<>(userListModel);
    private final JSplitPane messagesAndUsersPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);


    private final Logger logger = LoggerFactory.getLogger("client");
    private User currentUser;


    public GuiClient(String host, String port, String username, Consumer<Message> messageConsumer) {
        this.host = host;
        this.port = port;
        this.currentUser = new User(username);
        this.messageConsumer = loggingConsumer(messageConsumer);

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

    private Consumer<Message> loggingConsumer(Consumer<Message> messageConsumer) {
        return m -> {
            logger.debug(m.toString());
            messageConsumer.accept(m);
        };
    }

    private void addToTextArea(String line) {
        SwingUtilities.invokeLater(() -> {
            textarea.append(line + "\n");
            scrollPaneWithArea.scrollRectToVisible(textarea.getVisibleRect());
        });
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
        sendButton.setAction(Actions.createSendMessageAction(() -> sendTextToServer()));
        changeStatusButton.setAction(Actions.createChangeStatusAction(() -> updateStatus()));
        userJList.setCellRenderer(userListCellRenderer);

    }


    @Override
    public void accept(Message message) {
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
            this.currentUser = userlist.currentUser().orElseGet(() -> User.EMPTY);
            frame.setTitle(formatTitle(currentUser));


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
        messageConsumer.accept(Message.createMessage(textField.getText(), currentUser));
        textField.setText("");
    }

    private void updateStatus() {
        var s = JOptionPane.showInputDialog(frame.getContentPane(), "New status", currentUser.getStatus());
        currentUser = new User(currentUser.getName(), s);
        messageConsumer.accept(Message.createStatusUpdate(currentUser));
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
        var buttons = new JPanel();
        buttons.add(sendButton);
        buttons.add(changeStatusButton);
        commitLine.add(buttons, BorderLayout.EAST);
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

    private class UserListCellRenderer implements ListCellRenderer<User> {

        @Override
        public Component getListCellRendererComponent(JList<? extends User> list, User value, int index, boolean isSelected, boolean cellHasFocus) {
            JPanel pa = new JPanel(new BorderLayout());
            pa.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));


            int baseSize = 12;
            int smaller = 10;
            Color currentColor = Color.getColor("darkgray");
            Color userLabelsFG = value.equals(currentUser) ? Color.black: currentColor;
            int fontStyle = value.equals(currentUser) ? Font.BOLD : Font.PLAIN;

            var usernameLabel =  new JLabel();
            usernameLabel.setForeground(userLabelsFG);
            usernameLabel.setFont(new Font("SansSerif", fontStyle, baseSize));
            usernameLabel.setText(value.getName());
            pa.add(usernameLabel, BorderLayout.NORTH);

            var statusLabel = new JLabel(value.getStatus());
            statusLabel.setFont(new Font("SansSerif", fontStyle, smaller));
            statusLabel.setForeground(userLabelsFG);
            pa.add(statusLabel, BorderLayout.CENTER);

            return pa;
        }

    }
}
