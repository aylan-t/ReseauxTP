import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

public class Server {
    private static ServerSocket Listener;

    // Application serveur
    public static void main(String[] args) throws Exception {
        // Compteur incrémenté à chaque connexion d'un client au serveur
        int clientNumber = 0;

        // Adresse et port du serveur
        String serverAddress = "127.0.0.1";
        int serverPort = 5000;

        // Création de la connexion pour communiquer avec les clients
        Listener = new ServerSocket();
        Listener.setReuseAddress(true);
        InetAddress serverIP = InetAddress.getByName(serverAddress);

        // Association de l'adresse et du port à la connexion
        Listener.bind(new InetSocketAddress(serverIP, serverPort));
        System.out.format("The server is running on %s:%d%n", serverAddress, serverPort);

        try {
            // À chaque fois qu'un nouveau client se connecte, on exécute la fonction `run` de `ClientHandler`
            while (true) {
                // Important : la fonction accept() est bloquante et attend qu'un prochain client se connecte
                // Une nouvelle connexion : on incrémente le compteur
                new ClientHandler(Listener.accept(), clientNumber++).start();
            }
        } finally {
            // Fermeture de la connexion
            Listener.close();
        }
    }
}
