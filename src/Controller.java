import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;

public class Controller extends Node {
    ArrayList<RouteNode> table;
    Terminal terminal;

    Controller (){
        try {
            terminal = new Terminal("Controller");
            socket = new DatagramSocket(51510);
            table = new ArrayList<>();
            table.add(new RouteNode(new InetSocketAddress("e1", 50000), "e2", new InetSocketAddress("r1", 51510), new InetSocketAddress("e1", 51510)));
            table.add(new RouteNode(new InetSocketAddress("e1", 50000), "e3", new InetSocketAddress("r1", 51510), new InetSocketAddress("e1", 51510)));
            table.add(new RouteNode(new InetSocketAddress("e2", 50000), "e1", new InetSocketAddress("r2", 51510), new InetSocketAddress("e2", 51510)));
            table.add(new RouteNode(new InetSocketAddress("e2", 50000), "e3", new InetSocketAddress("r2", 51510), new InetSocketAddress("e2", 51510)));
            table.add(new RouteNode(new InetSocketAddress("e3", 50000), "e1", new InetSocketAddress("r5", 51510), new InetSocketAddress("e3", 51510)));
            table.add(new RouteNode(new InetSocketAddress("e3", 50000), "e2", new InetSocketAddress("r5", 51510), new InetSocketAddress("e3", 51510)));
            table.add(new RouteNode(new InetSocketAddress("e1", 51510), "e2", new InetSocketAddress("r3", 51510), new InetSocketAddress("r1", 51510)));
            table.add(new RouteNode(new InetSocketAddress("e1", 51510), "e3", new InetSocketAddress("r5", 51510), new InetSocketAddress("r1", 51510)));
            table.add(new RouteNode(new InetSocketAddress("r5", 51510), "e1", new InetSocketAddress("e1", 51510), new InetSocketAddress("r1", 51510)));
            table.add(new RouteNode(new InetSocketAddress("r4", 51510), "e1", new InetSocketAddress("e1", 51510), new InetSocketAddress("r1", 51510)));
            table.add(new RouteNode(new InetSocketAddress("r3", 51510), "e2", new InetSocketAddress("e2", 51510), new InetSocketAddress("r2", 51510)));
            table.add(new RouteNode(new InetSocketAddress("e2", 51510), "e1", new InetSocketAddress("r4", 51510), new InetSocketAddress("r2", 51510)));
            table.add(new RouteNode(new InetSocketAddress("e2", 51510), "e3", new InetSocketAddress("r4", 51510), new InetSocketAddress("r2", 51510)));
            table.add(new RouteNode(new InetSocketAddress("r1", 51510), "e2", new InetSocketAddress("r2", 51510), new InetSocketAddress("r3", 51510)));
            table.add(new RouteNode(new InetSocketAddress("r5", 51510), "e2", new InetSocketAddress("r2", 51510), new InetSocketAddress("r3", 51510)));
            table.add(new RouteNode(new InetSocketAddress("r2", 51510), "e1", new InetSocketAddress("r1", 51510), new InetSocketAddress("r4", 51510)));
            table.add(new RouteNode(new InetSocketAddress("r2", 51510), "e3", new InetSocketAddress("r5", 51510), new InetSocketAddress("r4", 51510)));
            table.add(new RouteNode(new InetSocketAddress("r1", 51510), "e3", new InetSocketAddress("e3", 51510), new InetSocketAddress("r5", 51510)));
            table.add(new RouteNode(new InetSocketAddress("r4", 51510), "e3", new InetSocketAddress("e3", 51510), new InetSocketAddress("r5", 51510)));
            table.add(new RouteNode(new InetSocketAddress("e3", 51510), "e2", new InetSocketAddress("r3", 51510), new InetSocketAddress("r5", 51510)));
            table.add(new RouteNode(new InetSocketAddress("e3", 51510), "e1", new InetSocketAddress("r1", 51510), new InetSocketAddress("r5", 51510)));
        }
        catch (SocketException e){
            e.printStackTrace();
        }
    }

    public void findRoute(RouteNode node){
        for(RouteNode i : table){
            if(i.dest.equals(node.dest) && i.source.equals(node.source) && i.currentAddress.equals(node.currentAddress)){
                sendRoute(i);
                break;
            }
        }
    }

    public void sendRoute(RouteNode node){
        try {
            terminal.println("send packet");
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            ObjectOutput output = new ObjectOutputStream(outStream);
            output.writeObject(node);
            output.close();
            byte[] packetData = outStream.toByteArray();
            DatagramPacket send = createPacket(ROUTE_RESPONSE, packetData, node.currentAddress);
            socket.send(send);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onReceipt(DatagramPacket packet) throws IOException{
        terminal.println("Received packet");
        byte[] data = packet.getData();
        byte[] object = new byte[data[1]];
        System.arraycopy(data,2,object,0,object.length);
        if(data[0] == ROUTE_REQUEST){
            try {
                ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(object));
                RouteNode request = (RouteNode) inputStream.readObject();
                inputStream.close();
                findRoute(request);
            }
            catch (ClassNotFoundException e){
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        new Controller();
    }
}
