package server;

import commom.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;

/**
 * Receives lines of text from connection's input stream and pushes to given queue
 * */
class ClientThread extends Thread {


    private final InputStream ins;
    private final BlockingQueue<server.ServerMessage> serverMessages;
    private final UUID uuid;
    private static Logger logger = LoggerFactory.getLogger(ClientThread.class);

    ClientThread(InputStream inputStream, BlockingQueue<server.ServerMessage> messagesFromClients, UUID uuid) {
        this.ins = inputStream;
        this.serverMessages = messagesFromClients;
        this.uuid = uuid;
    }

    @Override
    public void run() {
        try (var in = new ObjectInputStream(this.ins)) {
            while (true) {
                Object o = in.readObject();
                if (o instanceof Message) {
                    Message message = (Message) o;
                    logger.info(String.format("%s got message - %s", uuid, message));
                    synchronized (serverMessages) {
                        serverMessages.offer(new server.ServerMessage(uuid, message));
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
           logger.info(String.format("Client thread ends: %s\n", e));
        }

    }
}
