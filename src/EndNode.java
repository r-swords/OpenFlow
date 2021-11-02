import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

public class EndNode extends Node {

    Terminal terminal;
    InetSocketAddress dstAddress;

    EndNode (String name, int srcPort){
        try {
            terminal = new Terminal(name);
            socket = new DatagramSocket(srcPort);
            switch(name){
                case("e1"):
                    dstAddress = new InetSocketAddress("localhost", 50004);
                    break;
                case("e2"):
                    dstAddress = new InetSocketAddress("localhost", 50005);
                    break;
                case("e3"):
                    dstAddress = new InetSocketAddress("localhost", 50008);
                    break;
            }
            listener.go();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void start() throws IOException {
        while(true) {
            String message = terminal.read("Send message: ");
            terminal.print("Send message: " + message);
            byte[] messageArray = message.getBytes(StandardCharsets.UTF_8);
            DatagramPacket sendPacket = new DatagramPacket(messageArray, messageArray.length, dstAddress);
            socket.send(sendPacket);
        }
    }

    @Override
    public void onReceipt(DatagramPacket packet) {
        byte[] messageArray = packet.getData();
        String message = new String(messageArray).trim();
        String[] print = message.split("/");
        terminal.println(print[1]);
    }

    public static void main(String[] args) {
        try {
            (new EndNode(args[0], Integer.parseInt(args[1]))).start();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}
