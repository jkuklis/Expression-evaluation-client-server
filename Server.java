import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;

import static java.lang.Thread.sleep;

public class Server {
    private int port;
    private int threads_count;

    private ServerSocket socket;
    private Socket connectionSocket;
    private Monitor monitor;

    public Server(int port, int threads_count) {
        this.port = port;
        this.threads_count = threads_count;
        this.monitor = new Monitor(threads_count);
    }

    public void run() {
        try {
            socket = new ServerSocket(port);
        } catch (IOException e) {
            System.err.println("Socket not opened.");
            System.exit(1);
        }

        Thread listener = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    monitor.listen();
                } catch (InterruptedException e) {
                    System.out.println("Interrupted exception in listener");
                    System.exit(1);
                }
            }
        });

        listener.start();

        for (int i = 0; i < threads_count; i++) {
            Thread worker = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        monitor.work();
                    } catch (InterruptedException e) {
                        System.out.println("Interrupted exception in worker.");
                        System.exit(1);
                    }
                }
            });
            worker.start();
        }

        try {
            listener.join();
        } catch (InterruptedException e) {
            System.out.println("Failure joining threads.");
            System.exit(1);
        }
    }

    private class Monitor {
        private int free_threads;
        private Boolean to_take;

        public Monitor(int threads_count) {
            this.free_threads = threads_count;
            this.to_take = false;
        }

        public void listen() throws InterruptedException {
            while (true) {
                try {
                    synchronized (this) {
                        if (to_take) {
                            wait();
                        }
                        System.out.println("awaiting connection");
                        connectionSocket = socket.accept();
                        System.out.println("accepted");

                        to_take = true;
                        notify();
                    }
                } catch (IOException e) {
                    System.err.println("Accept a connection failure.");
                    System.exit(1);
                }
                sleep(1000);
            }
        }

        public void work() throws InterruptedException {
            while (true) {
                Socket priv_socket;
                synchronized (this) {
                    System.out.println("worker ready");
                    while (to_take == false) {
                        System.out.println("worker checking");
                        wait();
                    }
                    System.out.println("worker working");

                    priv_socket = connectionSocket;

                    to_take = false;
                    notify();
                }
                try {
                    BufferedReader inFromClient = new BufferedReader(new InputStreamReader(priv_socket.getInputStream()));
                    DataOutputStream outToClient = new DataOutputStream(priv_socket.getOutputStream());
                    String clientSentence = inFromClient.readLine();
                    System.out.println("Received: " + clientSentence);
                    BigInteger val = new BigInteger("0");
                    BigInteger expr = new BigInteger("0");
                    Boolean already = false;
                    Boolean started = false;
                    Boolean operator = false;
                    Boolean plus = false;
                    Boolean error = false;
                    Boolean space = false;
                    for (int i = 0; i < clientSentence.length(); i++) {
                        char c = clientSentence.charAt(i);
                        switch (c) {
                            case '+':
                            case '-':
                                if (operator || !already) {
                                    error = true;
                                    break;
                                }

                                plus = (c == '+');
                                break;
                            case ' ':
                            case '\t':
                                space = true;
                                if (operator) {
                                    if (plus) {
                                        val.add(expr);
                                    } else {
                                        val.subtract(expr);
                                    }
                                } else {
                                    val.add(expr);
                                }
                                break;
                            default:
                                if (Character.isDigit(c)) {
                                    if (space && !operator) {
                                        error = true;
                                    }
                                    already = true;

                                } else {
                                    error = true;
                                }

                        }
                    }
                    if (operator) {
                        error = true;
                    }

                    String message;
                    if (error) {
                        message = "ERROR";
                    } else {
                        message = val.toString();
                    }
                    System.out.println("Sending: " + message);


                } catch (IOException e) {
                    System.out.println("IOException in worker.");
                    System.exit(1);
                }
                sleep(1000);
            }
        }
    }
}
