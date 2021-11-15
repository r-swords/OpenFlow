import java.io.Serializable;
import java.net.InetSocketAddress;

public class RouteNode implements Serializable {
    InetSocketAddress source;
    InetSocketAddress currentAddress;
    String dest;
    InetSocketAddress nextAddress;

    RouteNode(InetSocketAddress source, String dest, InetSocketAddress nextAddress, InetSocketAddress currentAddress){
        this.source = source;
        this.dest = dest;
        this.nextAddress = nextAddress;
        this.currentAddress = currentAddress;
    }
}
