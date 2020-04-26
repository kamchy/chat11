package client;

import commom.Message;
import commom.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.Consumer;

public class Client {

    private static final String INPUT_TERMINAL = "q";

    public static void main(String[] args) {
        var host = args[0];
        var port = args[1];
        var username = args[2];

        startClientWith(host, port, username);
    }

    public static void startGuiClientWith(String host, String port, String username) {
        BlockingQueue<Message> inQueue = new ArrayBlockingQueue<>(10);
        OutputMessageHandler outputMessageHandler = new ConsoleConsumer(inQueue, new User(username));
        SimpleServerProxy simpleServerProxy = new SimpleServerProxy(outputMessageHandler);
        GuiClient gc = new GuiClient(host, port, username, simpleServerProxy);
        gc.show();
        startSendREceive(host, port,  simpleServerProxy, outputMessageHandler, null);
    }

    public static void startClientWith(String host, String port, String username) {
        BlockingQueue<Message> inQueue = new ArrayBlockingQueue<>(10);
        OutputMessageHandler outputMessageHandler = new ConsoleConsumer(inQueue, new User(username));
        Consumer<Message> incomingMessageHandler = new ConsoleReceiverHandler();
        InputLoop inputLoop = new ConsoleLoop();
        startSendREceive(host, port, incomingMessageHandler, outputMessageHandler, inputLoop);
    }


    private static void startSendREceive(String host, String port, Consumer<Message> incomingMessageHandler,
                                         OutputMessageHandler outputMessageHandler, InputLoop inputLoop) {
        try (Socket s = new Socket(host, Integer.parseInt(port))){
            var rt = new Thread(new Receiver(s.getInputStream(), incomingMessageHandler));
            rt.start();


            var st = new Thread(new Sender(s.getOutputStream(), outputMessageHandler));
            st.start();

            if (inputLoop != null) {
                inputLoop.loop(outputMessageHandler);
            }

            st.join();
            rt.join();
        } catch (IOException | InterruptedException e) {
            System.err.print("IOException when creating client socket" + e.getMessage());
        }
    }


    interface  InputLoop {
        void loop(OutputMessageHandler outputMessageHandler);
    }

    interface OutputMessageHandler {
        void onString(String s);
        BlockingQueue<Message> getQueue();

        void disconnect(String username);
    }

    private static final class Receiver implements Runnable {

        private final Consumer<Message> handler;
        private InputStream is;

        Receiver(InputStream is, Consumer<Message> hander) {
            this.is = is;
            this.handler = hander;
        }

        @Override
        public void run() {
            try (var iss = new ObjectInputStream(this.is)) {
                while (true) {
                    var m = (Message) iss.readObject();
                    handler.accept(m);
                }
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Exception when running receiver: " + e.getMessage());
            }

        }

    }

    private static final class Sender implements Runnable {
        private final BlockingQueue<Message> blockingQueue;
        private OutputStream oss;
        private static Logger logger = LoggerFactory.getLogger(Sender.class);

        Sender(OutputStream os, OutputMessageHandler consumer) {
            this.oss = os;
            this.blockingQueue = consumer.getQueue();
        }

        @Override
        public void run() {
            logger.info("Started sender");

            try (var oss = new ObjectOutputStream(this.oss)) {
                while (true) {
                    logger.info("Sender waits...");
                    Message obk = blockingQueue.take();
                    logger.info("Sender took from queue: " + obk);
                    if (obk.getType().equals(Message.Type.DISCONNECT)) {
                        logger.info("Sending disconnect mesage!!!");
                    }
                    oss.writeObject(obk);
                    oss.flush();
                }
            } catch (IOException | InterruptedException e) {
                System.err.println("Sender exception: " + e.getMessage());
            }
        }
    }

    private static class ConsoleReceiverHandler implements Consumer<Message> {
        private void showMessage(String format, Object...args) {
            System.out.format(format + "\n", args);
        }

        @Override
        public void accept(Message message) {
            showMessage("%s\n", message);
        }

        @Override
        public Consumer<Message> andThen(Consumer<? super Message> after) {
            return message -> {
                accept(message);
                after.accept(message);
            };
        }
    }

    private static class ConsoleConsumer implements OutputMessageHandler {

        private final BlockingQueue<Message> q;
        private final User self;
        private boolean exited = false;
        private Logger logger = LoggerFactory.getLogger(ConsoleConsumer.class);

        ConsoleConsumer(BlockingQueue<Message> inQueue, User self) {
            this.q = inQueue;
            this.self = self;
            try {
                q.put(Message.createConnectMessage(self));
                q.put(Message.createMessage("hello", self));
            } catch (InterruptedException e) {
                System.err.print("cannot send connect message");
            }
        }

        @Override
        public void onString(String s) {
            try {
                if (s.equals(INPUT_TERMINAL)) {
                    q.put(Message.createDisonnectMessage(self));
                    System.out.print("No more input would be sent from user " + self );
                    exited = true;
                } else {
                    if (!exited) {
                        q.put(Message.createMessage(s, self));
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        public BlockingQueue<Message> getQueue() {
            return q;
        }

        @Override
        public void disconnect(String username) {
            logger.info("Disconnect in ConsoleConsumer with username" + username);
            onString(INPUT_TERMINAL);
        }
    }

    private static class ConsoleLoop implements InputLoop {

        @Override
        public void loop(OutputMessageHandler handler) {
            try(BufferedReader r = new BufferedReader(new InputStreamReader(System.in))) {
                var line = r.readLine();
                while (!line.equals(INPUT_TERMINAL)) {
                    handler.onString(line);
                    line = r.readLine();
                }
            } catch (IOException e) {
                System.out.print("Exception: " + e.getMessage());
            }
        }
    }

    private static class SimpleServerProxy implements GuiClient.ServerProxy, Consumer<Message> {
        private final OutputMessageHandler outputHandler;
        private Consumer<Message> lineCallback;
        private Consumer<String> removeClientCallback;
        private Consumer<String> addClientCallback;
        private Logger logger = LoggerFactory.getLogger(SimpleServerProxy.class);

        public SimpleServerProxy(OutputMessageHandler outputMessageHandler) {
            this.outputHandler = outputMessageHandler;
        }

        @Override
        public void send(String text) {
            outputHandler.onString(text);
        }

        @Override
        public void setAddLineCallback(Consumer<Message> lineConsumer) {
            this.lineCallback = lineConsumer;

        }

        @Override
        public void setAddClientConsumer(Consumer<String> clientConsumer) {
            this.addClientCallback = clientConsumer;
        }

        @Override
        public void setRemoveClientConsumer(Consumer<String> clientConsumer) {
            this.removeClientCallback = clientConsumer;
        }

        @Override
        public void disconnect(String username) {
            logger.info("Proxy - disconnecting with username " + username);
            outputHandler.disconnect(username);
        }

        @Override
        public void accept(Message message) {
            switch (message.getType()) {
                case MESSAGE:
                case INVALID:
                    lineCallback.accept(message);
                    break;
                case CONNECT:
                    addClientCallback.accept(message.getUser().getName());
                    break;
                case DISCONNECT:
                    removeClientCallback.accept(message.getUser().getName());
                    break;
            }
        }

        @Override
        public Consumer<Message> andThen(Consumer<? super Message> after) {
            throw new UnsupportedOperationException();
        }
    }
}

