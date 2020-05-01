package client;

import commom.Message;
import commom.User;
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


public final class GuiClient {
    private static final String TITLE_FORMAT = "%s:%s - %s";
    public static final int WIDTH = 400;
    public static final int HEIGHT = 300;
    public static final Color COLOR_TEXTAREA_BG = Color.getColor("fafa00");
    private final String host;
    private final String port;
    private final String username;
    private final JFrame frame;
    private final JTextArea textarea = new JTextArea();
    private final JTextField textField = new JTextField();
    private final JScrollPane scrollPaneWithArea = new JScrollPane(textarea);
    private final JButton sendButton = new JButton("Send");
    private DefaultListModel<User> userListModel  = new DefaultListModel<>();
    private final JList<User> userJList = new JList<>(userListModel);
    private final JSplitPane messagesAndUsersPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    private final ImageIcon frameIcon = new ImageIcon(getClass().getResource("icon.png"));

    private final ServerProxy proxy;
    private Logger logger = LoggerFactory.getLogger(GuiClient.class);

    interface  ServerProxy {
        void send(String text);
        void disconnect();

        void setAddLineCallback(Consumer<Message> lineConsumer);
        void setAddClientConsumer(Consumer<String> clientConsumer);
        void setRemoveClientConsumer(Consumer<String> clientConsumer);
    }

    public GuiClient(String host, String port, String username, ServerProxy proxy) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.proxy = proxy;

        this.frame = new JFrame();

        frame.setTitle(String.format(TITLE_FORMAT, host, port, username));
        frame.setIconImage(frameIcon.getImage());
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                proxy.disconnect();
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
        proxy.setAddLineCallback(formatMessage(this::addToTextArea));
        proxy.setAddClientConsumer(s -> userListModel.addElement(new User(s))); // TODO change api
        proxy.setRemoveClientConsumer(s -> userListModel.removeElement(new User(s)));
    }

    private Consumer<Message> formatMessage(Consumer<String> stringConsumer) {
        return message -> stringConsumer.accept(
                String.format("[%s] %s", message.getUser().getName(), message.getContent()));

    }

    private void sendTextToServer() {
        proxy.send(textField.getText());
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



    public void show() {
        try {
            SwingUtilities.invokeAndWait(() -> frame.setVisible(true));
            messagesAndUsersPane.setDividerLocation(0.6);
            System.out.printf("%s  - %s", System.nanoTime(), "After invoke and wait");
        } catch (InterruptedException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

}
