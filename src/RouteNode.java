import java.net.InetSocketAddress;

public class RouteNode {
    InetSocketAddress source;
    String dest;
    InetSocketAddress nextAddress;

    RouteNode(InetSocketAddress source, String dest, InetSocketAddress nextAddress){
        this.source = source;
        this.dest = dest;
        this.nextAddress = nextAddress;
    }
}
