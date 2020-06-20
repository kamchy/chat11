import client.Client;
import server.ThreadedServer;

import java.io.PrintStream;

public class Main {
    public static void main(String[] args) {
        try {
            var option = args[0];
            if (option.equals("-h")) {
                printUsageAndExitWithCode(System.out, 0);
            }
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
                } else if (option.equals("-cc")) {
                    Client.startCursesClientWith(host, port, name);
                } else {
                    Client.startClientWith(host, port, name);
                }
            } else {
                System.out.println("Invalid options");
            }
        } catch (Exception e) {
            e.printStackTrace();
            printUsageAndExitWithCode(System.err, 1);
        }
     }

    private static void printUsageAndExitWithCode(PrintStream stream, int exitCode) {
        stream.printf("Usage: [one of]\n" +
                "java -jar chat11.jar -h                         prints this message\n" +
                "java -jar chat11.jar -s [port]                  stars server on port (deault: 8881)\n" +
                "java -jar chat11.jar -c host port username      starts console client that connects to host on port as username\n" +
                "java -jar chat11.jar -cc host port username     starts curses console client that connects to host on port as username\n" +
                "java -jar chat11.jar -cg host port username     starts swing gui client that connects to host on port as username\n");
        System.exit(exitCode);
    }
}
