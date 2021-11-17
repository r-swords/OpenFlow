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

    EndNode (String name){
        try {
            terminal = new Terminal(name);
            socket = new DatagramSocket(50000);
            dstAddress = new InetSocketAddress(name, 51510);
            waiting = false;
            listener.go();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void waiting() throws IOException {
        DatagramPacket packet = createPacket(WAITING, "waiting".getBytes(StandardCharsets.UTF_8), dstAddress);
        socket.send(packet);
        waiting = true;
        while(waiting){
            String quit = terminal.read("Enter 'quit' to stop listening");
            if(quit.equalsIgnoreCase("quit")){
                packet = createPacket(WAITING, "quit".getBytes(StandardCharsets.UTF_8), dstAddress);
                socket.send(packet);
                waiting = false;
            }
        }
    }

    public void start() throws IOException {
        while(true) {
            String message = terminal.read("Send message, or enter 'WAITING': ");
            terminal.println("Send message, or enter 'WAITING': " + message);
            if(message.equalsIgnoreCase("waiting")) waiting();
            else {
                DatagramPacket sendPacket = createPacket(MESSAGE, message.getBytes(StandardCharsets.UTF_8), dstAddress);
                System.out.println("print");
                socket.send(sendPacket);
                System.out.println("print2");
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
            (new EndNode(args[0])).start();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}
