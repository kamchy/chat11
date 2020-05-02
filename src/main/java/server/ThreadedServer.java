package server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ThreadedServer implements Runnable{
    private static Logger logger = LoggerFactory.getLogger("server");
    private final int port;


    private ThreadedServer(int port) {
        this.port = port;
    }

    public void run() {
        BlockingQueue<ServerMessage> messagesFromClients = new LinkedBlockingQueue<>();
        MessageProcessor messageProcessor = new MessageProcessor(messagesFromClients);
        var mp = new Thread(messageProcessor);
        mp.start();

        try {
            var ss = new ServerSocket(this.port);
            logger.info("Server socket created on port " + this.port) ;
            while (true) {
                var conn = ss.accept();
                var clientId = messageProcessor.addChannel(conn.getOutputStream());
                var cli = new ClientThread(conn.getInputStream(), messagesFromClients, clientId);
                cli.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static ThreadedServer create(int port) {
        return new ThreadedServer(port);
    }


    public static void main(String[] args) throws InterruptedException {
        int port = Integer.parseInt(args[0]);
        startServerAtPort(port);
    }

    public static void startServerAtPort(int port) throws InterruptedException {
        var threadedServer = new Thread(ThreadedServer.create(port));
        threadedServer.start();
        threadedServer.join();
    }
}
