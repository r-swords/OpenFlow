import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;

public abstract class Node {
    static final int PACKETSIZE = 65536;
    static final byte MESSAGE = 1;
    static final byte WAITING = 2;
    static final byte ROUTE_REQUEST = 3;
    static final byte ROUTE_RESPONSE = 4;


    DatagramSocket socket;
    Listener listener;
    CountDownLatch latch;

    Node() {
        latch= new CountDownLatch(1);
        listener= new Listener();
        listener.setDaemon(true);
        listener.start();
    }

    public String getMessage(DatagramPacket packet) {
        byte[] messageArray = new byte[packet.getData().length];
        System.arraycopy(packet.getData(), 2, messageArray, 0,
                packet.getData().length-2);
        return new String(messageArray).trim();
    }

    public DatagramPacket createPacket(byte type, byte[] message, InetSocketAddress dstAddress) {
        byte[] data = new byte[message.length + 2];
        data[0] = type;
        data[1] = (byte) message.length;
        System.arraycopy(message,0, data, 2, message.length);
        return new DatagramPacket(data, data.length, dstAddress);
    }


    public abstract void onReceipt(DatagramPacket packet) throws IOException, ClassNotFoundException, InterruptedException;

    /**
     *
     * Listener thread
     *
     * Listens for incoming packets on a datagram socket and informs registered receivers about incoming packets.
     */
    class Listener extends Thread {

        /*
         *  Telling the listener that the socket has been initialized
         */
        public void go() {
            latch.countDown();
        }

        /*
         * Listen for incoming packets and inform receivers
         */
        public void run() {
            try {
                latch.await();
                // Endless loop: attempt to receive packet, notify receivers, etc
                while(true) {
                    DatagramPacket packet = new DatagramPacket(new byte[PACKETSIZE], PACKETSIZE);
                    socket.receive(packet);

                    onReceipt(packet);
                }
            } catch (Exception e) {if (!(e instanceof SocketException)) e.printStackTrace();}
        }
    }
}
