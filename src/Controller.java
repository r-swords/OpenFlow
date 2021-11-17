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
            listener.go();
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

    public synchronized void findRoute(RouteNode node, DatagramPacket packet){
        for(RouteNode i : table){
            if(i.dest.equals(node.dest) && i.source.equals(node.source) && i.currentAddress.equals(node.currentAddress)){
                sendRoute(i, packet);
                break;
            }
        }
    }

    public synchronized void sendRoute(RouteNode node, DatagramPacket packet){
        try {
            terminal.println("send packet");
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            ObjectOutput output = new ObjectOutputStream(outStream);
            output.writeObject(node);
            output.close();
            byte[] data = outStream.toByteArray();
            byte[] packetData = new byte[data.length+1];
            packetData[0] = packet.getData()[2];
            System.arraycopy(data,0,packetData,1,data.length);
            DatagramPacket send = createPacket(ROUTE_RESPONSE, packetData, node.currentAddress);
            socket.send(send);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }

    public synchronized void start() throws InterruptedException {
        terminal.println("waiting");
        while(true){
            this.wait();
        }
    }

    @Override
    public synchronized void onReceipt(DatagramPacket packet) throws IOException{
        terminal.println("Received packet");
        byte[] data = packet.getData();
        byte[] object = new byte[data.length - 3];
        System.arraycopy(data,3,object,0,object.length);
        byte[] h = new byte[1];
        if(data[0] == ROUTE_REQUEST){
            try {
                ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(object));
                RouteNode request = (RouteNode) inputStream.readObject();
                inputStream.close();
                findRoute(request, packet);
            }
            catch (ClassNotFoundException e){
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        new Controller().start();
    }
}
