import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class Router extends Node{
    Terminal terminal;
    ArrayList<RouteNode> table;
    InetSocketAddress waitingAddress;
    boolean waiting;
    RouteNode newNode;
    String name;
    InetSocketAddress dstAddress;

    Router(String name, int srcPort){
        try {
            this.name = name;
            terminal = new Terminal(name);
            socket = new DatagramSocket(srcPort);
            listener.go();
            table = new ArrayList<>();
            waiting = false;
            dstAddress = new InetSocketAddress("controller", 51510);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public synchronized void start(){
        terminal.println("waiting for packets");
    }

    public synchronized void forwardMessage(DatagramPacket packet) throws IOException, InterruptedException {
        String message = getMessage(packet);
        String[] stArr = message.split("/");
        if(stArr[0].equals(name)){
            if(waiting){
                DatagramPacket sendPacket = createPacket(MESSAGE, message.getBytes(StandardCharsets.UTF_8), waitingAddress);
                socket.send(sendPacket);
            }
        }
        boolean found = false;
        for (RouteNode i : table) {
            if (i.dest.equals(stArr[0]) &&
                    i.source.equals((InetSocketAddress) packet.getSocketAddress())) {
                DatagramPacket sendPacket = createPacket(MESSAGE, message.getBytes(StandardCharsets.UTF_8), i.nextAddress);
                socket.send(sendPacket);
                found = true;
                break;
            }
        }
        if(!found){
            terminal.println("not found");
            sendRouteRequest(packet);
        }
    }

    public synchronized void sendRouteRequest(DatagramPacket packet){
        try {
            String message = getMessage(packet);
            String[] stArr = message.split("/");
            RouteNode node = new RouteNode((InetSocketAddress)packet.getSocketAddress(),stArr[0], null, new InetSocketAddress(name, 51510));
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            ObjectOutput output = new ObjectOutputStream(outStream);
            output.writeObject(node);
            output.close();
            byte[] packetData = outStream.toByteArray();
            DatagramPacket send = createPacket(ROUTE_RESPONSE, packetData, dstAddress);
            socket.send(send);
            terminal.println("packet sent");
            this.wait();
            send = createPacket(MESSAGE,message.getBytes(StandardCharsets.UTF_8),newNode.nextAddress);
            socket.send(send);
            table.add(newNode);
        }
        catch(IOException | InterruptedException e){
            e.printStackTrace();
        }
    }

    public synchronized RouteNode getNewNode(DatagramPacket packet) {
        byte[] data = packet.getData();
        byte[] object = new byte[data[1]];
        System.arraycopy(data,2,object,0,object.length);
        try {
            ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(object));
            RouteNode request = (RouteNode) inputStream.readObject();
            inputStream.close();
            return request;
        }
        catch(IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }


    @Override
    public synchronized void onReceipt(DatagramPacket packet) throws IOException, InterruptedException {
        terminal.println("Received packet");
        String message = getMessage(packet);
        switch(packet.getData()[0]) {
            case(MESSAGE):
                forwardMessage(packet);
                break;
            case(WAITING):
                waitingAddress = (InetSocketAddress) packet.getSocketAddress();
                waiting = (message.equals(waiting))? true: false;
                break;
            case(ROUTE_RESPONSE):
                newNode = getNewNode(packet);
                this.notify();
        }
    }

    public static void main(String[] args){
        (new Router(args[0], 51510)).start();
    }
}
