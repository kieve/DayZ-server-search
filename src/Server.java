
public class Server {
    /*
     * ROW MAP:
     * 
     * public IP
     * public port
     * private IP
     * private port
     * ICMPIP
     * flags
     * password
     * host name
     * number of players
     */
    public static final int PUBLIC_IP = 0;
    public static final int PUBLIC_PORT = 1;
    public static final int PRIVATE_IP = 2;
    public static final int PRIVATE_PORT = 3;
    public static final int ICMPIP = 4;
    public static final int FLAGS = 5;
    public static final int HAS_PASSWORD = 6;
    public static final int HOSTNAME = 7;
    public static final int NUMBER_PLAYERS = 8;

    private final String     g_ip;
    private final int        g_port;
    private final boolean    g_hasPassword;
    private final String     g_hostname;
    private final int        g_numPlayers;

    private final String g_string;

    public String getIp() { return g_ip; }
    public int getPort() { return g_port; }
    public boolean hasPassword() { return g_hasPassword; }
    public String getHostname() { return g_hostname; }
    public int getNumPlayers() { return g_numPlayers; }

    public Server(String ip, int port, boolean hasPassword, String hostname, int numPlayers) {
        g_ip = ip;
        g_port = port;
        g_hasPassword = hasPassword;
        g_hostname = hostname;
        g_numPlayers = numPlayers;

        g_string = new StringBuilder()
                          .append(g_ip).append(":").append(g_port)
                          .append(" | ").append(g_hasPassword)
                          .append(" | ").append(g_hostname)
                          .append(" | ").append(g_numPlayers)
                          .toString();
    }

    public static Server createFromStringArray(String[] array) {
        return new Server(array[PUBLIC_IP],
                          Integer.parseInt(array[PUBLIC_PORT]),
                          Boolean.parseBoolean(array[HAS_PASSWORD]),
                          array[HOSTNAME],
                          Integer.parseInt(array[NUMBER_PLAYERS]));
    }

    @Override
    public String toString() {
        return g_string;
    }
}
