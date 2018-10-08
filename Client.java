import java.io.*;
import java.net.Socket;

public class Client {
    public static void main(String args[]) {
        int port = 12345;
        String ip = "127.0.0.1";

        int TIMEOUT = 2;
        int MAX_LENGTH = 100;

        for (int i = 0; i < args.length; i += 2) {
            String par = args[i];
            String arg = "";
            if (i+1 < args.length) {
                arg = args[i+1];
            } else {
                System.err.println("No argument to parameter: " + par);
                System.exit(1);
            }

            switch(par) {
                case "-a":
                    ip = arg;
                    break;
                case "-p":
                    try {
                        port = Integer.parseInt(arg);
                        if (port < 1024 || port > 65535) {
                            System.err.println("Port not in range from 1024 to 65535");
                            System.exit(1);
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Argument " + arg + " must be an integer.");
                        System.exit(1);
                    }
                    break;
                default:
                    System.err.println("Unrecognized parameter: " + par + ", available: a - server ip, p - port");
                    System.exit(1);
            }
        }

        try {
            BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
            Socket clientSocket = new Socket(ip, port);
            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            StringBuilder message = new StringBuilder(MAX_LENGTH);

            int read_char = inFromUser.read();
            while (read_char != -1) {
                message.append((char) read_char);
                if (message.length() == MAX_LENGTH) {
                    outToServer.writeBytes(message.toString());
                    message = new StringBuilder(MAX_LENGTH);
                }
            }

            message.append('\n');
            outToServer.writeBytes(message.toString());

            String response = inFromServer.readLine();
            System.out.println(response);

        } catch (IOException e) {
            System.out.println("IOException.");
            System.exit(1);
        }
    }
}
