import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private int port;
    private int threads_count;

    private ServerSocket socket;
    private Socket connectionSocket;
    private Monitor monitor;

    public Server(int port, int threads_count) {
        this.port = port;
        this.threads_count = threads_count;
        this.monitor = new Monitor();
    }

    public void run() {
        try {
            socket = new ServerSocket(port);
        } catch (IOException e) {
            System.err.println("Socket not opened.");
            System.exit(2);
        }

        Thread listener = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    monitor.listen();
                } catch (InterruptedException e) {
                    System.err.println("Interrupted exception in listener");
                    System.exit(2);
                }
            }
        });

        Thread workers[] = new Thread[threads_count];

        for (int i = 0; i < threads_count; i++) {
            workers[i] = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        monitor.work();
                    } catch (InterruptedException e) {
                        System.err.println("Interrupted exception in worker.");
                        System.exit(2);
                    }
                }
            });
        }

        listener.start();
        for (int i = 0; i < threads_count; i++) {
            workers[i].start();
        }

        try {
            listener.join();
            for (int i = 0; i < threads_count; i++) {
                workers[i].join();
            }
        } catch (InterruptedException e) {
            System.err.println("Failure joining threads.");
            System.exit(2);
        }
    }

    private enum ExpressionState {
        START, NUMBER, BEFORE_OPERATOR, AFTER_OPERATOR, ERROR
    }

    private class Monitor {
        private Boolean connection_to_take;
        private ExpressionState state;

        public Monitor() {
            this.connection_to_take = false;
            this.state = ExpressionState.START;
        }

        public void listen() throws InterruptedException {
            while (true) {
                try {
                    synchronized (this) {
                        if (connection_to_take) {
                            wait();
                        }
                        connectionSocket = socket.accept();

                        connection_to_take = true;
                        notify();
                    }
                } catch (IOException e) {
                    System.err.println("Accept a connection failure.");
                    System.exit(2);
                }
            }
        }

        public void work() throws InterruptedException {
            while (true) {
                Socket worker_socket;
                synchronized (this) {
                    while (!connection_to_take) {
                        wait();
                    }

                    worker_socket = connectionSocket;

                    connection_to_take = false;
                    notifyAll();
                }

                read_and_respond(worker_socket);
            }
        }

        private void read_and_respond (Socket worker_socket) {
            try {
                BufferedReader inFromClient = new BufferedReader(new InputStreamReader(worker_socket.getInputStream()));
                DataOutputStream outToClient = new DataOutputStream(worker_socket.getOutputStream());

                BigInteger accumulator = new BigInteger("0");
                BigInteger built_expr = new BigInteger("0");

                Boolean plus = true;
                int read_value = inFromClient.read();

                while (read_value != -1) {
                    char read_char = (char)read_value;
                    if (read_char == '\n') {
                        break;
                    }

                    switch(read_char) {
                        case '+':
                        case '-':
                            if (state == ExpressionState.NUMBER || state == ExpressionState.BEFORE_OPERATOR) {
                                state = ExpressionState.AFTER_OPERATOR;
                                if (plus) {
                                    accumulator = accumulator.add(built_expr);
                                } else {
                                    accumulator = accumulator.subtract(built_expr);
                                }
                                built_expr = new BigInteger("0");
                                plus = (read_char == '+');
                            } else {
                                state = ExpressionState.ERROR;
                            }
                            break;
                        case ' ':
                        case '\t':
                            if (state == ExpressionState.NUMBER) {
                                state = ExpressionState.BEFORE_OPERATOR;
                            }
                            break;
                        default:
                            if (Character.isDigit(read_char) && state != ExpressionState.BEFORE_OPERATOR) {
                                built_expr = built_expr.multiply(new BigInteger("10"));
                                built_expr = built_expr.add(new BigInteger(Character.toString(read_char)));
                                state = ExpressionState.NUMBER;
                            } else {
                                state = ExpressionState.ERROR;
                            }

                    }

                    if (state == ExpressionState.ERROR) {
                        break;
                    }

                    read_value = inFromClient.read();
                }

                if (plus) {
                    accumulator = accumulator.add(built_expr);
                } else {
                    accumulator = accumulator.subtract(built_expr);
                }

                String message;
                if (state == ExpressionState.ERROR || state == ExpressionState.AFTER_OPERATOR) {
                    message = "ERROR";
                } else {
                    message = accumulator.toString();
                }

                outToClient.writeBytes(message + "\n");

            } catch (IOException e) {
                System.err.println("IOException in worker.");
                System.exit(2);
            }
        }
    }
}
