import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class Client {
    private final int TIMEOUT = 2;
    private final int MAX_LENGTH = 1000;

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
                int read_value = inFromUser.read();
                while (read_value != -1) {
                    char read_char = (char) read_value;
                    if (read_char == '\n') {
                        break;
                    }
                    message.append(read_char);
                    if (message.length() == MAX_LENGTH) {
                        outToServer.writeBytes(message.toString());
                        message = new StringBuilder(MAX_LENGTH);
                    }

                    read_value = inFromUser.read();
                }

                message.append('\n');
                outToServer.writeBytes(message.toString());

                String response = inFromServer.readLine();
                System.out.println(response);

            } catch (SocketTimeoutException e) {
                System.out.println("TIMEOUT");
                System.exit(1);
            }

        } catch (IOException e) {
            System.err.println("IOException.");
            System.exit(2);
        }
    }
}
