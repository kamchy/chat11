import client.Client;
import server.ThreadedServer;

public class Main {
    public static void main(String[] args) {
        try {
            var option = args[0];
            if (option.equals("-s")) {
                var port = 8881;
                if (args.length > 1) {
                    port = Integer.parseInt(args[1]);
                }
                ThreadedServer.startServerAtPort(port);
            } else if (option.startsWith("-c")) {
                var host = args[1];
                var port = args[2];
                var name = args[3];
                if (option.equals("-cg")) {
                    Client.startGuiClientWith(host, port, name);
                } else {
                    Client.startClientWith(host, port, name);
                }
            } else {
                System.out.println("Invalid options");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.printf("Usage: [one of]\njava -jar chat11.jar -s [port]\n" +
                    "java -jar chat11.jar -c host port username\n" +
                    "java -jar chat11.jar -cg host port username\n");
            System.exit(0);
        }
     }
}
