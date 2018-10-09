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

    private enum State {
        START, NUMBER, BEFORE_OPERATOR, AFTER_OPERATOR, ERROR
    }

    private class Monitor {

        private int free_threads;
        private Boolean to_take;
        private State state;

        public Monitor(int threads_count) {
            this.free_threads = threads_count;
            this.to_take = false;
            this.state = State.START;
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
                    BigInteger val = new BigInteger("0");
                    BigInteger expr = new BigInteger("0");

                    Boolean plus = true;
                    int read_char = inFromClient.read();
                    while (read_char != -1) {
                        char c = (char)read_char;
//                        System.out.println(c);
                        if (c == '\n') {
                            break;
                        }

                        switch(c) {
                            case '+':
                            case '-':
                                if (state == State.NUMBER || state == State.BEFORE_OPERATOR) {
                                    state = State.AFTER_OPERATOR;
                                    if (plus) {
                                        val = val.add(expr);
                                    } else {
                                        val = val.subtract(expr);
                                    }
                                    expr = new BigInteger("0");
                                    plus = (c == '+');
                                } else {
                                    state = State.ERROR;
                                }
                                break;
                            case ' ':
                            case '\t':
                                if (state == State.NUMBER) {
                                    state = State.BEFORE_OPERATOR;
                                }
                                break;
                            default:
                                if (Character.isDigit(c) && state != State.BEFORE_OPERATOR) {
                                    expr = expr.multiply(new BigInteger("10"));
                                    expr = expr.add(new BigInteger(Character.toString(c)));
                                    state = State.NUMBER;
                                } else {
                                    state = State.ERROR;
                                }

                        }

                        if (state == State.ERROR) {
                            break;
                        }

                        read_char = inFromClient.read();
                    }

                    if (plus) {
                        val = val.add(expr);
                    } else {
                        val = val.subtract(expr);
                    }

                    System.out.println(expr.toString());
                    System.out.println(val.toString());
                    System.out.println(state.toString());

                    String message;
                    if (state == State.ERROR || state == state.AFTER_OPERATOR) {
                        message = "ERROR";
                    } else {
                        message = val.toString();
                    }

//                    outToClient.writeBytes(message + "\n");

                } catch (IOException e) {
                    System.out.println("IOException in worker.");
                    System.exit(1);
                }
            }
        }
    }
}
