import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class Query {
    public static final byte[] randomId = {
        (byte) 0x33,
        (byte) 0x44,
        (byte) 0x33,
        (byte) 0x44
    };

    public static final byte[] options = {
        (byte) 0xff,
        (byte) 0xff,
        (byte) 0xff,
        (byte) 0x01
    };

    public static final byte[] infoRequest = {
        (byte) 0xfe,
        (byte) 0xfd,
        (byte) 0x00
    };

    public static final byte[] challengeRequest = {
        (byte) 0xfe,
        (byte) 0xfd,
        (byte) 0x09
    };

    public static final byte[] challenge = concat(challengeRequest, randomId, options);
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
        byte[] challenge = {};
        try {
            challenge = ByteBuffer.allocate(4).putInt(Integer.parseInt(number)).array();
        }  catch (NumberFormatException e) {
            return "";
        }

        byte[] res = concat(infoRequest, randomId, challenge, options);

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
        socket.close();
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

    private static byte[] concat(byte[]... bytes) {
        byte[] result = concat(bytes[0], bytes[1]);
        for (int i = 2; i < bytes.length; i++) {
            result = concat(result, bytes[i]);
        }
        return result;
    }

    private static byte[] concat(byte[] a, byte[] b) {
        byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }
}
