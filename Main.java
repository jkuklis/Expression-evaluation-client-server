public class Main {
    public static void main(String args[]) {
        int port = 12345;
        int threads_count = 3;

        for (int i = 0; i < args.length; i += 2) {
            String par = args[i];
            String arg = args[i+1];

            switch(par) {
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
                case "-t":
                    try {
                        threads_count = Integer.parseInt(arg);
                        if (threads_count < 1) {
                            System.err.println("Threads count must be positive.");
                            System.exit(1);
                        }
                        if (threads_count > 1000) {
                            System.err.println("Threads count must be less than 1000.");
                            System.exit(1);
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Argument " + arg + " must be an integer.");
                        System.exit(1);
                    }
                    break;
                default:
                    System.err.println("Unrecognized parameter: " + par + ", available: p - port, t - number of threads");
                    System.exit(1);
            }
        }

        Server server = new Server(port, threads_count);

        server.run();
    }
}
