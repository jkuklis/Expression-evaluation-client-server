public class Server_Main {
    public static void main(String args[]) {
        int port = 12345;
        int threads_count = 3;

        String parameter = "";
        String argument = "";

        for (int i = 0; i < args.length; i += 2) {
            parameter = args[i];
            if (i+1 < args.length) {
                argument = args[i+1];
            } else {
                System.err.println("No argument to parameter: " + parameter);
                System.exit(2);
            }

            switch(parameter) {
                case "-p":
                    try {
                        port = Integer.parseInt(argument);
                        if (port < 1024 || port > 65535) {
                            System.err.println("Port not in range from 1024 to 65535");
                            System.exit(2);
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Argument " + argument + " must be an integer.");
                        System.exit(2);
                    }
                    break;
                case "-t":
                    try {
                        threads_count = Integer.parseInt(argument);
                        if (threads_count < 1) {
                            System.err.println("Threads count must be positive.");
                            System.exit(2);
                        }
                        if (threads_count > 1000) {
                            System.err.println("Threads count must be at most 1000.");
                            System.exit(2);
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Argument " + argument + " must be an integer.");
                        System.exit(2);
                    }
                    break;
                default:
                    System.err.println("Unrecognized parameter: " + parameter + ", available: p - port, t - number of threads");
                    System.exit(2);
            }
        }

        Server server = new Server(port, threads_count);

        server.run();
    }
}
