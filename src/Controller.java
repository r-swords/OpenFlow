import java.io.IOException;

public class Controller {
    public static void main(String[] args) throws IOException {
        new Router("r1", 50004).start();
        new Router("r2", 50005).start();
        new Router("r3", 50006).start();
        new Router("r4", 50007).start();
        new Router("r5", 50008).start();
        new EndNode("e1", 50001).start();
        new EndNode("e2", 50002).start();
        new EndNode("e3", 50003).start();
    }
}
