import client.ServerFacade;
import ui.PreloginREPL;

public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("♕ 240 Chess Client:");

        ServerFacade server = new ServerFacade();

        PreloginREPL prelogin = new PreloginREPL(server);
        prelogin.run();
        System.out.println("Exited");

    }
}