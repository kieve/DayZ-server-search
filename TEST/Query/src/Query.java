import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class Query {
    private static final String randomId = "\u0033\u0044\u0033\u0044";
    private static final String options = "\u00ff\u00ff\u00ff\u0001";
    private static final String infoRequest = "\u00fe\u00fd\u0000";
    private static final String challengeRequest = "\u00fe\u00fd\u0009";
    private static final byte[] challenge = (challengeRequest + randomId + options).getBytes();
    
    public static final int TIMEOUT = 500;

    public static String getServerInfo(Server server) {
        String ip = server.getIp();
        int port = server.getPort();
        DatagramSocket socket = null;
        DatagramPacket packet1 = null;
        DatagramPacket packet2 = null;
        byte[] challengeResponse;
        byte[] challengeNumber;
        String number = "";
        
        InetAddress address = null;
        try {
            address = InetAddress.getByName(ip);

            socket = new DatagramSocket();
            socket.setSoTimeout(TIMEOUT);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        
        int retries = 2;
        while (retries >= 0) {
            packet1 = new DatagramPacket(challenge, challenge.length, address, port);
            try {
                socket.send(packet1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            challengeResponse = new byte[64];
            packet1 = new DatagramPacket(challengeResponse, challengeResponse.length);
            try {
                socket.receive(packet1);
                retries = -1;
            } catch (SocketTimeoutException e) {
                // We'll try again.
                retries--;
            }  catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        challengeNumber = new byte[13];
        for (int i = 5; i < 16; i++) {
            challengeNumber[i-5] = packet1.getData()[i];
        }
        number = byteToString(challengeNumber).replace("\u0000", "");
        byte[] temp = {};
        try {
            temp = ByteBuffer.allocate(4).putInt(Integer.parseInt(number)).array();
        }  catch (NumberFormatException e) {
            return "";
        }
        String response = infoRequest + randomId + byteToString(temp) + options;
        
        byte[] res = response.getBytes();
        
        retries = 2;
        while (retries >= 0) {
            packet1 = new DatagramPacket(res, res.length, address, port);
            try {
                socket.send(packet1);
            } catch (IOException e) {
                e.printStackTrace();
            }
    
            byte[] buf1 = new byte[1400];
            byte[] buf2 = new byte[1400];
            packet1 = new DatagramPacket(buf1, buf1.length);
            packet2 = new DatagramPacket(buf2, buf2.length);
            try {
                socket.receive(packet1);
                retries = -1;
            } catch (SocketTimeoutException e) {
                // Try sending again.
                retries--;
            }catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        
        try {
            socket.receive(packet2);
        } catch (SocketTimeoutException e) {
            // Do nothing
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return parsePlayers(byteToString(packet1.getData()) + byteToString(packet2.getData()));
    }
    
    private static String parsePlayers(String s) {
        int startIndex = s.indexOf("player_\u0000\u0000") + 9;
        if (startIndex == 8) {
            return "";
        }
        int endIndex = s.indexOf("team_\u0000\u0000");
        
        String result = "";
        if (endIndex == -1) {
            result = s.substring(startIndex);
        } else {
            result = s.substring(startIndex, endIndex);
        }
        result = result.replace("\u0000", ", ");
        return result;
    }
    
    private static String byteToString(byte[] bytes) {
        return new String(bytes, 0, bytes.length);
    }
}
