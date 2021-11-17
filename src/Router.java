import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

public class Router extends Node{
    Terminal terminal;
    ArrayList<RouteNode> table;
    InetSocketAddress waitingAddress;
    boolean waiting;
    String name;
    InetSocketAddress dstAddress;
    byte waitingCount;
    HashMap<Byte, String> waitingMessages;

    Router(String name){
        try {
            this.name = name;
            terminal = new Terminal(name);
            socket = new DatagramSocket(51510);
            listener.go();
            table = new ArrayList<>();
            waiting = false;
            dstAddress = new InetSocketAddress("controller", 51510);
            waitingCount = 0;
            waitingMessages = new HashMap<>();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void start() throws IOException, InterruptedException {
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
            waitingCount++;
            String message = getMessage(packet);
            String[] stArr = message.split("/");
            RouteNode node = new RouteNode((InetSocketAddress)packet.getSocketAddress(),stArr[0], null, new InetSocketAddress(name, 51510));
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            ObjectOutput output = new ObjectOutputStream(outStream);
            output.writeObject(node);
            output.close();
            byte[] data = outStream.toByteArray();
            byte[] packetData = new byte[data.length+1];
            packetData[0] = waitingCount;
            System.arraycopy(data,0,packetData,1,data.length);
            DatagramPacket send = createPacket(ROUTE_REQUEST, packetData, dstAddress);
            socket.send(send);
            waitingMessages.put(waitingCount, message);
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    public synchronized RouteNode getNewNode(DatagramPacket packet) {
        byte[] data = packet.getData();
        byte[] object = new byte[data.length - 3];
        System.arraycopy(data,3,object,0,object.length);
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

    public void sendToNewNode(DatagramPacket packet){
        try {
            byte count = packet.getData()[2];
            RouteNode node = getNewNode(packet);
            String message = waitingMessages.get(count);
            DatagramPacket sendPacket = createPacket(MESSAGE, message.getBytes(StandardCharsets.UTF_8), node.nextAddress);
            socket.send(sendPacket);
            waitingMessages.remove(count);
            if(waitingMessages.isEmpty()) waitingCount = 0;
        }
        catch (IOException e){
            e.printStackTrace();
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
                waiting = message.equals("waiting");
                break;
            case(ROUTE_RESPONSE):
                sendToNewNode(packet);
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        (new Router(args[0])).start();
    }
}
