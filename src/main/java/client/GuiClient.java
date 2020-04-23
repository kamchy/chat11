package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Consumer;


public final class GuiClient {
    private static final String TITLE_FORMAT = "%s:%s - %s";
    private final String host;
    private final String port;
    private final String username;
    private final JFrame frame;
    private final JTextArea textarea = new JTextArea();
    private final JTextField textField = new JTextField();
    private final JButton send = new JButton("Send");
    private final ServerProxy proxy;

    interface  ServerProxy {
        void send(String text);
        void setAddLineCallback(Consumer<String> lineConsumer);

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
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(new Dimension(800, 600));
        frame.setContentPane(createContentPane());
        configureComponents();
    }

    private void addToTextArea(String line) {
        SwingUtilities.invokeLater(() -> textarea.append(line + "\n"));
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
        proxy.setAddLineCallback(this::addToTextArea);
        proxy.setAddClientConsumer(addToTextAreaWith("Client added: "));
        proxy.setRemoveClientConsumer(addToTextAreaWith("Client removed: "));
    }

    private void sendTextToServer() {
        proxy.send(textField.getText());
        textField.setText("");
    }

    private Container createContentPane() {
        var pane = new JPanel();


        pane.setLayout(new BorderLayout());
        pane.add(textarea, BorderLayout.CENTER);
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
