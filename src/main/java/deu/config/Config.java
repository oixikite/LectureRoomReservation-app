package deu.config;

public class Config {
    public Server server = new Server();

    public static class Server {
        public String host;
        public int port;
    }
}