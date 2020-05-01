package client;

import commom.Message;
import commom.User;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Client {

    private static final String INPUT_TERMINAL = "q";

    public static void main(String[] args) {
        var host = args[0];
        var port = args[1];
        var username = args[2];

        startClientWith(host, port, username);
    }

    public static void startGuiClientWith(String host, String port, String username) {
        startThreadsAndClientFor(host, port, username, (messageQueue) -> new GuiClient(host, port, username, messageQueue));
    }

    public static void startClientWith(String host, String port, String username) {
        startThreadsAndClientFor(host, port, username, (messageQueue) -> new ConsoleClient(username, messageQueue));
    }

    private static void startThreadsAndClientFor(String host, String port, String username, Function<MessageQueue, LoopingConsumer> consumerFactory) {
        BlockingQueue<Message> inQueue = new ArrayBlockingQueue<>(10);
        User user = new User(username);
        var messageQueue = new MessageQueue(inQueue, user);
        startSendReceive(host, port,  consumerFactory.apply(messageQueue), messageQueue);

    }



    private static void startSendReceive(String host, String port,
                                         LoopingConsumer loopingConsumer,
                                         Supplier<Message> outputMessageHandler) {
        try (Socket s = new Socket(host, Integer.parseInt(port))){
            var rt = new Thread(new Receiver(s.getInputStream(), loopingConsumer));
            rt.start();


            var st = new Thread(new Sender(s.getOutputStream(), outputMessageHandler));
            st.start();

            loopingConsumer.loop();

            st.join();
            rt.join();
        } catch (IOException | InterruptedException e) {
            System.err.print("IOException when creating client socket" + e.getMessage());
        }
    }



    interface LoopingConsumer extends InputLoop, Consumer<Message> {}
    private interface  InputLoop {
        void loop();
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
        private final Supplier<Message> supplier;
        private OutputStream oss;
        private static Logger logger = LoggerFactory.getLogger(Sender.class);

        Sender(OutputStream os, Supplier<Message> messageSupplier) {
            this.oss = os;
            this.supplier = messageSupplier;
        }

        @Override
        public void run() {
            logger.info("Started sender");

            try (var oss = new ObjectOutputStream(this.oss)) {
                while (true) {
                    logger.info("Sender waits...");
                    Message obk = supplier.get();
                    logger.info("Sender took from queue: " + obk);
                    if (obk.getType().equals(Message.Type.DISCONNECT)) {
                        logger.info("Sending disconnect mesage!!!");
                    }
                    oss.writeObject(obk);
                    oss.flush();
                }
            } catch (IOException e) {
                System.err.println("Sender exception: " + e.getMessage());
            }
        }
    }



    private static class MessageQueue implements Consumer<Message>, Supplier<Message> {

        private final BlockingQueue<Message> q;
        private final User self;
        private boolean exited = false;
        private Logger logger = LoggerFactory.getLogger(MessageQueue.class);

        MessageQueue(BlockingQueue<Message> inQueue, User self) {
            this.q = inQueue;
            this.self = self;
            try {
                q.put(Message.createConnectMessage(self));
            } catch (InterruptedException e) {
                System.err.print("cannot send connect message");
            }
        }

        @Override
        public void accept(Message message) {
            try {
                if (!exited) {
                    q.put(message);
                    exited =  message.getType().equals(Message.Type.DISCONNECT);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        @NotNull
        @Override
        public Consumer<Message> andThen(@NotNull Consumer<? super Message> after) {
            return null;
        }

        @Override
        public Message get() {
            try {
                return q.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private static class ConsoleClient implements LoopingConsumer {

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
                while (!line.equals(INPUT_TERMINAL)) {
                    messageConsumer.accept(Message.createMessage(line, user));
                    line = r.readLine();
                }
                messageConsumer.accept(Message.createDisonnectMessage(user));
            } catch (IOException e) {
                System.err.print("Exception: " + e.getMessage());
            }
        }
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


}

