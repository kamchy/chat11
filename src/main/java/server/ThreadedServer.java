package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

public class ThreadedServer implements Runnable{
    private static Logger logger = Logger.getLogger(ThreadedServer.class.getName());
    private final int port;


    private ThreadedServer(int port) {
        this.port = port;

    }

    public void run() {
        BlockingQueue<ServerMessage> messagesFromClients =  new LinkedBlockingQueue<>();
        MessageProcessor messageProcessor = new MessageProcessor(messagesFromClients);
        var mp = new Thread(messageProcessor);
        mp.start();

        try {

            var ss = new ServerSocket(this.port);
            logger.info("Server socket created on port " + this.port) ;
            while (true) {
                var conn = ss.accept();
                logger.info("accepted  connection "  + conn.getInetAddress()) ;
                var clientId = messageProcessor.addUser(conn.getOutputStream());
                logger.info("Connected " + clientId) ;
                var cli = new ClientThread(conn.getInputStream(), messagesFromClients, clientId);
                cli.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static ThreadedServer create(int port) {
        logger.info("Created server on " + port);
        return new ThreadedServer(port);
    }

    public static void main(String[] args) throws InterruptedException {
        var threadedServer = new Thread(ThreadedServer.create(8881));
        threadedServer.start();
        threadedServer.join();
    }
}
