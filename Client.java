import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class Client {
    private final int TIMEOUT = 2;
    private final int MAX_LENGTH = 100;

    private int port;
    private String ip;

    public Client(int port, String ip) {
        this.port = port;
        this.ip = ip;
    }

    public void run() {
        try {
            BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));

            Socket clientSocket = new Socket(ip, port);
            clientSocket.setSoTimeout(TIMEOUT * 1000);

            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            StringBuilder message = new StringBuilder(MAX_LENGTH);

            try {
                int read_char = inFromUser.read();
                while (read_char != -1) {
                    char c = (char) read_char;
                    if (c == '\n') {
                        break;
                    }
                    message.append(c);
                    if (message.length() == MAX_LENGTH) {
                        System.out.println(message);
                        outToServer.writeBytes(message.toString());
                        message = new StringBuilder(MAX_LENGTH);
                    }

                    read_char = inFromUser.read();
                }

                System.out.println(message);
                message.append('\n');
                outToServer.writeBytes(message.toString());

                String response = inFromServer.readLine();
                System.out.println(response);

            } catch (SocketTimeoutException e) {
                System.out.println("TIMEOUT");
                System.exit(1);
            }

        } catch (IOException e) {
            System.out.println("IOException.");
            System.exit(2);
        }
    }
}
