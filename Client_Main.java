public class Client_Main {
    public static void main(String args[]) {
        int port = 12345;
        String ip = "localhost";

        String par = "";
        String arg = "";

        for (int i = 0; i < args.length; i += 2) {
            par = args[i];
            if (i + 1 < args.length) {
                arg = args[i + 1];
            } else {
                System.err.println("No argument to parameter: " + par);
                System.exit(2);
            }

            switch (par) {
                case "-a":
                    ip = arg;
                    break;
                case "-p":
                    try {
                        port = Integer.parseInt(arg);
                        if (port < 1024 || port > 65535) {
                            System.err.println("Port not in range from 1024 to 65535");
                            System.exit(2);
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Argument " + arg + " must be an integer.");
                        System.exit(2);
                    }
                    break;
                default:
                    System.err.println("Unrecognized parameter: " + par + ", available: a - server ip, p - port");
                    System.exit(2);
            }
        }

        Client client = new Client(port, ip);

        client.run();
    }
}
