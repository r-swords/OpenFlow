import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

public class EndNode extends Node {

    Terminal terminal;
    InetSocketAddress dstAddress;
    boolean waiting;

    EndNode (String name, int srcPort){
        try {
            terminal = new Terminal(name);
            socket = new DatagramSocket(srcPort);
            waiting = false;
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

    public void waiting(){
        waiting = true;
        while(waiting){
            String quit = terminal.read("Enter 'quit' to stop listening");
            if(quit.equalsIgnoreCase("quit")) waiting = false;
        }
    }

    public void start() throws IOException {
        while(true) {
            String message = terminal.read("Send message, or enter 'WAITING': ");
            terminal.println("Send message, or enter 'WAITING': " + message);
            if(message.equalsIgnoreCase("waiting")) waiting();
            else {
                DatagramPacket sendPacket = createPacket(MESSAGE, message, dstAddress);
                socket.send(sendPacket);
            }
        }
    }

    @Override
    public void onReceipt(DatagramPacket packet) {
        if(waiting) {
            String message = getMessage(packet);
            String[] print = message.split("/");
            terminal.println(print[1]);
        }
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
