package client;

import commom.Message;
import commom.User;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;

public class Client {

    private static final String INPUT_TERMINAL = "q";

    public static void main(String[] args) {
        //TODO nice optionals or args library
        System.out.println("args:" + Arrays.toString(args));
        var host = args[0];
        var port = args[1];
        var username = args[2];

        startClientWith(host, port, username);


    }

    public static void startClientWith(String host, String port, String username) {
        BlockingQueue<Message> inQueue = new ArrayBlockingQueue<>(10);
        OutputMessageHandler outputMessageHandler = new ConsoleConsumer(inQueue, new User(username));
        IncommingMessageHandler incommingMessageHandler = new ConsoleReceiverHandler();
        InputLoop inputLoop = new ConsoleLoop();

        startSendREceive(host, port, incommingMessageHandler, outputMessageHandler, inputLoop);
    }


    private static void startSendREceive(String host, String port, IncommingMessageHandler incomingMessageHandler,
                                         OutputMessageHandler outputMessageHandler, InputLoop inputLoop) {
        try (Socket s = new Socket(host, Integer.parseInt(port))){
            var rt = new Thread(new Receiver(s.getInputStream(), incomingMessageHandler));
            rt.start();

            var st = new Thread(new Sender(s.getOutputStream(), outputMessageHandler));
            st.start();

            inputLoop.loop(outputMessageHandler);

        } catch (IOException e) {
            incomingMessageHandler.onError(e.getMessage());
        }
    }

    interface IncommingMessageHandler {
        void onConnect(User u);

        void onDisconnect(User u);

        void onMessage(User u, String content);

        void onInvalid(Message m);

        void onError(String s);
    }

    interface  InputLoop {
        void loop(OutputMessageHandler outputMessageHandler);
    }

    interface OutputMessageHandler {
        void onString(String s) throws ConsoleConsumer.InputCompleteException;

        BlockingQueue<Message> getQueue();
    }

    private static final class Receiver implements Runnable {

        private final IncommingMessageHandler handler;
        private InputStream is;

        Receiver(InputStream is, IncommingMessageHandler hander) {
            this.is = is;
            this.handler = hander;
        }

        @Override
        public void run() {
            try (var iss = new ObjectInputStream(this.is)) {
                while (true) {
                    var m = (Message) iss.readObject();
                    switch (m.getType()) {
                        case CONNECT:
                            handler.onConnect(m.getUser());
                            break;
                        case DISCONNECT:
                            handler.onDisconnect(m.getUser());
                            break;
                        case MESSAGE:
                            handler.onMessage(m.getUser(), m.getContent());
                            break;
                        case INVALID:
                            handler.onInvalid(m);
                            break;
                    }
                }

            } catch (IOException | ClassNotFoundException e) {
                handler.onError("Receiver exiting:" + e.getMessage());
            }

        }

    }

    private static final class Sender implements Runnable {

        private final BlockingQueue<Message> blockingQueue;
        private OutputStream oss;
        private static Logger logger = Logger.getLogger(Sender.class.getName());

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
                    oss.writeObject(obk);
                    oss.flush();
                }
            } catch (IOException | InterruptedException e) {
                System.out.println("Sender exception: " + e.getMessage());
            }

        }
    }




    private static class ConsoleReceiverHandler implements IncommingMessageHandler {
        @Override
        public void onConnect(User u) {
            showMessage("Connected: %s\n", u.getName());
        }

        @Override
        public void onDisconnect(User u) {
            showMessage("Disconnected: %s\n", u.getName());
        }

        @Override
        public void onMessage(User u, String msg) {
            showMessage("Received [%s]: %s\n", u.getName(), msg);
        }

        @Override
        public void onInvalid(Message m){
            showMessage("NVALID %s\n", m);
        }

        @Override
        public void onError(String s) {
            showMessage(s);

        }

        private void showMessage(String format, Object...args) {
            System.out.format(format + "\n", args);
        }
    }

    private static class ConsoleConsumer implements OutputMessageHandler {

        private final BlockingQueue<Message> q;
        private final User self;

        ConsoleConsumer(BlockingQueue<Message> inQueue, User self) {
            this.q = inQueue;
            this.self = self;
            try {
                q.put(Message.createConnectMessage(self));
                q.put(Message.createMessage("hello", self));
            } catch (InterruptedException e) {
                System.out.print("cannot send connect message");
            }
        }

        @Override
        public void onString(String s) {
            try {
                if (s.equals(INPUT_TERMINAL)) {
                    q.put(Message.createDisonnectMessage(self));
                    System.out.print("Terminating...\n");
                    System.exit(0);
                }
                q.put(Message.createMessage(s, self));
            } catch (InterruptedException e) {
                e.printStackTrace();

            }
        }

        @Override
        public BlockingQueue<Message> getQueue() {
            return q;
        }

        private static class InputCompleteException extends Throwable {
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

            } catch (IOException | ConsoleConsumer.InputCompleteException e) {
                System.out.print("Exception: " + e.getMessage());
            }

        }
    }
}

