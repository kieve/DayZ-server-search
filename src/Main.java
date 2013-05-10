import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static final String MASTER_SERVER =
        "http://gstadmin.gamespy.net/masterserver/index.aspx?gamename=arma2oapc&fields=%5Cpassword"
        + "%5Chostname%5C%5Cnumplayers&overridemaster=&filter=";

    public static void main(String[] args) {
        System.out.println("USAGE:");
        System.out.println("find A,B,C,...,Z\tSearch for players seperated by commas. Only use "
        		           + "spaces if they exist in the name");
        System.out.println("refresh         \tRefresh the serverlist.\n");
        System.out.println("exit            \tExit");
        Scanner in = new Scanner(System.in);
        System.out.println("Loading server list.");
        ArrayList<Server> servers = loadServerList();
        System.out.println("Done.");

        String line;
        while (!(line = in.nextLine()).equals("exit")) {
            if (line.startsWith("find ")) {
                String[] people = line.substring(5).split(",");
                System.out.println("Checking...");
                List<String> result = new ThreadedQuery(servers, people).start();
                for (String s: result) {
                    System.out.println(s);
                }
                System.out.println("Done.");
            } else if (line.equals("refresh")) {
                System.out.println("Loading server list.");
                servers = loadServerList();
                System.out.println("Done.");
            } else {
                System.out.println("Unknown command.");
            }
        }
        in.close();
    }

    public static ArrayList<Server> loadServerList() {
        URL url;
        InputStream is = null;
        BufferedReader br;

        StringBuilder sb = new StringBuilder();
        ArrayList<Server> servers = new ArrayList<Server>();

        try {
            url = new URL(MASTER_SERVER);
            is = url.openStream();
            br = new BufferedReader(new InputStreamReader(is));

            while (!(sb.append(br.readLine()).toString().equals("null"))) {
                if (sb.toString().startsWith("<tr><td>")) {
                    int temp = sb.indexOf("<tr><td>");
                    sb.delete(temp, temp + 8); // Remove the leading table tags

                    temp = sb.indexOf("</td></tr>");
                    sb.delete(temp, temp + 10); // Remove the trailing table tags

                    temp = sb.indexOf("</td><td></td><td>");
                    sb.replace(temp, temp + 18, "�"); // Replace this annoying blank column

                    while ((temp = sb.indexOf("</td><td>")) != -1) {
                        sb.replace(temp, temp + 9, "�"); // Split up the important columns
                    }
                    try {
                        Server result = Server.createFromStringArray(sb.toString().split("�"));
                        if (!result.hasPassword()
                                //&& result.getHostname().contains("DayZ")
                                //&& result.getHostname().contains("1.7.1.5")
                                ){
                            servers.add(result);
                        }
                    } catch (Exception e) {
                        System.out.println("Should probably use a new REGEX for the strings");
                        e.printStackTrace();
                    }
                }
                sb.setLength(0);
            }
        } catch (MalformedURLException mue) {
             mue.printStackTrace();
        } catch (IOException ioe) {
             ioe.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException ioe) {
                // Ignore, failing to close means nothing to us.
            }
        }
        return servers;
    }

    public static String bytesToHex(byte[] bytes) {
        final char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for ( int j = 0; j < bytes.length; j++ ) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
