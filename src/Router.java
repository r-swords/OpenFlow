import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class Router extends Node{
    Terminal terminal;
    ArrayList<RouteNode> table;

    Router(String name, int srcPort){
        try {
            terminal = new Terminal(name);
            socket = new DatagramSocket(srcPort);
            listener.go();
            table = new ArrayList<>();
            switch(name){
                case("r1"):
                    table.add(new RouteNode(new InetSocketAddress("localhost", 50001), "e2", new InetSocketAddress("localhost", 50006)));
                    table.add(new RouteNode(new InetSocketAddress("localhost", 50001), "e3", new InetSocketAddress("localhost", 50008)));
                    table.add(new RouteNode(new InetSocketAddress("localhost", 50008), "e1", new InetSocketAddress("localhost", 50001)));
                    table.add(new RouteNode(new InetSocketAddress("localhost", 50007), "e1", new InetSocketAddress("localhost", 50001)));
                    break;
                case("r2"):
                    table.add(new RouteNode(new InetSocketAddress("localhost", 50006), "e2", new InetSocketAddress("localhost", 50002)));
                    table.add(new RouteNode(new InetSocketAddress("localhost", 50002), "e1", new InetSocketAddress("localhost", 50007)));
                    table.add(new RouteNode(new InetSocketAddress("localhost", 50002), "e3", new InetSocketAddress("localhost", 50007)));
                    break;
                case("r3"):
                    table.add(new RouteNode(new InetSocketAddress("localhost", 50004), "e2", new InetSocketAddress("localhost", 50005)));
                    table.add(new RouteNode(new InetSocketAddress("localhost", 50008), "e2", new InetSocketAddress("localhost", 50005)));
                    break;
                case("r4"):
                    table.add(new RouteNode(new InetSocketAddress("localhost", 50005), "e1", new InetSocketAddress("localhost", 50004)));
                    table.add(new RouteNode(new InetSocketAddress("localhost", 50005), "e3", new InetSocketAddress("localhost", 50008)));
                    break;
                case("r5"):
                    table.add(new RouteNode(new InetSocketAddress("localhost", 50004), "e3", new InetSocketAddress("localhost", 50003)));
                    table.add(new RouteNode(new InetSocketAddress("localhost", 50007), "e3", new InetSocketAddress("localhost", 50003)));
                    table.add(new RouteNode(new InetSocketAddress("localhost", 50003), "e2", new InetSocketAddress("localhost", 50006)));
                    table.add(new RouteNode(new InetSocketAddress("localhost", 50003), "e1", new InetSocketAddress("localhost", 50004)));
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public synchronized void start(){
        terminal.println("waiting for packets");
    }

    @Override
    public void onReceipt(DatagramPacket packet) throws IOException {
        terminal.println("Received packet");
        String message = getMessage(packet);
        String[] stArr = message.split("/");
        for(RouteNode i: table){
            if(i.dest.equals(stArr[0]) &&
                    i.source.equals((InetSocketAddress) packet.getSocketAddress())){
                DatagramPacket sendPacket = createPacket(MESSAGE, message, i.nextAddress);
                socket.send(sendPacket);
                break;
            }
        }
    }

    public static void main(String[] args){
        (new Router(args[0], Integer.parseInt(args[1]))).start();
    }
}
