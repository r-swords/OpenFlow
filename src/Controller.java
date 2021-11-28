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
            table.add(new RouteNode(new InetSocketAddress("e1", 51510), "e3", new InetSocketAddress("172.20.99.7", 51510), new InetSocketAddress("r1", 51510)));
            table.add(new RouteNode(new InetSocketAddress("172.20.99.7", 51510), "e1", new InetSocketAddress("e1", 51510), new InetSocketAddress("172.20.99.3", 51510)));
            table.add(new RouteNode(new InetSocketAddress("r4", 51510), "e1", new InetSocketAddress("e1", 51510), new InetSocketAddress("r1", 51510)));
            table.add(new RouteNode(new InetSocketAddress("r3", 51510), "e2", new InetSocketAddress("e2", 51510), new InetSocketAddress("r2", 51510)));
            table.add(new RouteNode(new InetSocketAddress("e2", 51510), "e1", new InetSocketAddress("r4", 51510), new InetSocketAddress("r2", 51510)));
            table.add(new RouteNode(new InetSocketAddress("e2", 51510), "e3", new InetSocketAddress("r4", 51510), new InetSocketAddress("r2", 51510)));
            table.add(new RouteNode(new InetSocketAddress("172.20.99.3", 51510), "e2", new InetSocketAddress("172.20.99.4", 51510), new InetSocketAddress("r3", 51510)));
            table.add(new RouteNode(new InetSocketAddress("172.20.99.7", 51510), "e2", new InetSocketAddress("172.20.99.4", 51510), new InetSocketAddress("r3", 51510)));
            table.add(new RouteNode(new InetSocketAddress("172.20.99.4", 51510), "e1", new InetSocketAddress("172.20.99.3", 51510), new InetSocketAddress("r4", 51510)));
            table.add(new RouteNode(new InetSocketAddress("172.20.99.4", 51510), "e3", new InetSocketAddress("172.20.99.7", 51510), new InetSocketAddress("r4", 51510)));
            table.add(new RouteNode(new InetSocketAddress("172.20.99.3", 51510), "e3", new InetSocketAddress("e3", 51510), new InetSocketAddress("r5", 51510)));
            table.add(new RouteNode(new InetSocketAddress("r4", 51510), "e3", new InetSocketAddress("e3", 51510), new InetSocketAddress("r5", 51510)));
            table.add(new RouteNode(new InetSocketAddress("e3", 51510), "e2", new InetSocketAddress("r3", 51510), new InetSocketAddress("r5", 51510)));
            table.add(new RouteNode(new InetSocketAddress("e3", 51510), "e1", new InetSocketAddress("172.20.99.3", 51510), new InetSocketAddress("r5", 51510)));
        }
        catch (SocketException e){
            e.printStackTrace();
        }
    }

    public synchronized void findRoute(RouteNode node, DatagramPacket packet){
        int j = 0;
        terminal.println(node.source.getHostString());
        for(RouteNode i : table){
            System.out.println(j);
            if(i.dest.equals(node.dest))System.out.println("same dest");
            if(i.source.equals(node.source))System.out.println("same source");
            if(i.currentAddress.equals(node.currentAddress))System.out.println("same current");
            if(i.dest.equals(node.dest) && i.source.equals(node.source) && i.currentAddress.equals(node.currentAddress)){
                terminal.println("Found route");
                sendRoute(i, packet);
                break;
            }
            j++;
        }
    }

    public synchronized void sendRoute(RouteNode node, DatagramPacket packet){
        try {
            byte[] data = createRouteNodeArray(node);
            byte[] packetData = new byte[data.length+1];
            packetData[0] = packet.getData()[2];
            System.arraycopy(data,0,packetData,1,data.length);
            DatagramPacket send = createPacket(ROUTE_RESPONSE, packetData, node.currentAddress);
            socket.send(send);
            terminal.println("Route sent.\n------\n");
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void addTable(){
        String newElement = terminal.read("Enter new element: ");
        terminal.println("Enter new element: " + newElement);
        String[] elementArray = newElement.split(" ");
        if(elementArray.length == 5) {
            table.add(new RouteNode(new InetSocketAddress(elementArray[0], Integer.parseInt(elementArray[1])), elementArray[2], new InetSocketAddress(elementArray[3], 51510), new InetSocketAddress(elementArray[4], 51510)));
            terminal.println("Added to table.\n------\n");
        }
        else terminal.println("Invalid input.\n------\n");
    }

    public void removeTable(){
        String element = terminal.read("Enter element: ");
        terminal.println("Enter element: " + element + "\n------\n");
        String[] elementArray = element.split(" ");
        if(elementArray.length == 5){
            RouteNode remove = new RouteNode(new InetSocketAddress(elementArray[0], Integer.parseInt(elementArray[1])), elementArray[2], new InetSocketAddress(elementArray[3], 51510), new InetSocketAddress(elementArray[4], 51510));
            table.removeIf(i -> i.source.equals(remove.source) && i.dest.equals(remove.dest) && i.nextAddress.equals(remove.nextAddress) && i.currentAddress.equals(remove.currentAddress));
            byte[] data = createRouteNodeArray(remove);
            DatagramPacket sendPacket = createPacket(REMOVE, data, remove.currentAddress);
            try {
                socket.send(sendPacket);
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }
        else terminal.println("Invalid input.\n------\n");
    }

    public void start(){
        terminal.println("waiting");
        while(true) {
            String action = terminal.read("'ADD' or 'REMOVE' from the forwarding table: ");
            terminal.println("'ADD' or 'REMOVE' from the forwarding table: " + action);
            if (action.equalsIgnoreCase("add")) addTable();
            else if (action.equalsIgnoreCase("remove")) removeTable();
            else terminal.println("Invalid input.\n------\n");
        }
    }
    

    @Override
    public synchronized void onReceipt(DatagramPacket packet) {
        terminal.println("Received packet");
        byte[] data = packet.getData();
        byte[] object = new byte[data.length - 3];
        System.arraycopy(data,3,object,0,object.length);
        if(data[0] == ROUTE_REQUEST){
            terminal.println("Route request.");
            RouteNode request = extractNode(object);
            findRoute(request, packet);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        (new Controller()).start();
    }
}
