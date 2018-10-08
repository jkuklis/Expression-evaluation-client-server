import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Listener implements Runnable {
    private ServerSocket socket;

    public Listener(ServerSocket socket) {
        this.socket = socket;
    }

    public void run() {
        while (true) {
            try {
                Socket connectionSocket = socket.accept();
            } catch (IOException e) {
                System.err.println("Accept a connection failure.");
                System.exit(1);
            }
        }
    };
}
