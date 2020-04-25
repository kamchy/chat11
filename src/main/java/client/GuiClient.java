package client;

import commom.Message;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Consumer;
import java.util.logging.Logger;


public final class GuiClient {
    private static final String TITLE_FORMAT = "%s:%s - %s";
    public static final int WIDTH = 400;
    public static final int HEIGHT = 300;
    public static final Color BGCOLOR = Color.getColor("fafa00");
    private final String host;
    private final String port;
    private final String username;
    private final JFrame frame;
    private final JTextArea textarea = new JTextArea();
    private final JTextField textField = new JTextField();
    private final JScrollPane scrollPaneWithArea = new JScrollPane(textarea);
    private final JButton send = new JButton("Send");
    private final ServerProxy proxy;
    private Logger logger = Logger.getLogger(GuiClient.class.getName());

    interface  ServerProxy {
        void send(String text);
        void setAddLineCallback(Consumer<Message> lineConsumer);
        void setAddClientConsumer(Consumer<String> clientConsumer);
        void setRemoveClientConsumer(Consumer<String> clientConsumer);
        void disconnect(String username);
    }

    public GuiClient(String host, String port, String username, ServerProxy proxy) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.proxy = proxy;

        this.frame = new JFrame();

        frame.setTitle(String.format(TITLE_FORMAT, host, port, username));
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                disconnect(proxy, username);
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

    private void disconnect(ServerProxy proxy, String username) {
        logger.info("closing window");
        proxy.disconnect(username);
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
        send.addActionListener((e) -> sendTextToServer());
        proxy.setAddLineCallback(formatMessage(this::addToTextArea));
        proxy.setAddClientConsumer(addToTextAreaWith("Client added: "));
        proxy.setRemoveClientConsumer(addToTextAreaWith("Client removed: "));
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
        textarea.setBackground(BGCOLOR);
        pane.add(scrollPaneWithArea, BorderLayout.CENTER);
        JPanel commitLine = new JPanel(new BorderLayout());
        commitLine.add(textField, BorderLayout.CENTER);
        commitLine.add(send, BorderLayout.EAST);
        pane.add(commitLine, BorderLayout.SOUTH);

        return pane;
    }



    public void show() {
        try {
            SwingUtilities.invokeAndWait(() -> frame.setVisible(true));
            System.out.printf("%s  - %s", System.nanoTime(), "After invoke and wait");
        } catch (InterruptedException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

}
