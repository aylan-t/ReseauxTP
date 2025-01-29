import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;
import java.io.File;


public class ServerTP {
    private static ServerSocket listener;
    private Scanner scanner = new Scanner(System.in);
    private String serverAddress;
    private int serverPort;
    private static String credentialsPath;
    private static String messagesPath;
    private static File credentialsFile;

    // Application serveur
    public static void main(String[] args) throws Exception {
        int clientNumber = 0;

        ServerTP server = new ServerTP();
        server.setupServer();

        try {
            while (true) {
                new ClientHandler(listener.accept(), credentialsPath, credentialsFile,messagesPath, clientNumber++).start();
            }
        } finally {
            listener.close();
        }
    }

    private void setupServer() {
        setupAddress();
        setupPort();
        try {
            setupClientConnexion();
            credentialsPathCreation();
            messagesPathCreation();

        } catch (IOException e) {
            System.err.println("An error occurred while setting up the server");
        }

    }

    private void setupAddress() {
        System.out.println("Entrez une adresse IP valide (ex: 127.0.0.1)");
        String userInput = scanner.nextLine();
        if (!isAddressValid(userInput)) {
            System.out.println("L'adresse IP est invalide.");
            setupAddress();
        } else {
            serverAddress = userInput;
        }
    }

    private void setupPort() {
        System.out.println("Entrez un port valide entre 5000 et 5050");
        int userInput = scanner.nextInt();
        if (!isPortValid(userInput)){
            System.out.println("Le port est invalide.");
            setupPort();
        } else {
            serverPort = userInput;
        }
    }

    private void setupClientConnexion() throws IOException {
        listener = new ServerSocket();
        listener.setReuseAddress(true);
        InetAddress serverIP = InetAddress.getByName(serverAddress);
        listener.bind(new InetSocketAddress(serverIP, serverPort));
        System.out.format("Le serveur roule sur %s:%d%n", serverAddress, serverPort);
    }

    private void credentialsPathCreation() {
        File csvFile = new File("userCredentials.csv");
        try {
            if (!csvFile.exists()) {
                csvFile.createNewFile();
            }
        } catch (IOException e) {
            System.err.println("une erreur c'est produite lors de la creation du fichier");
        }
        credentialsPath = csvFile.getAbsolutePath();
        credentialsFile = csvFile;
    }

    private void messagesPathCreation() {
        File messagesFile = new File("messages.txt");
        try {
            if (!messagesFile.exists()) {
                messagesFile.createNewFile();
            }
        } catch (IOException e) {
            System.err.println("une erreur c'est produite lors de la creation du fichier");
        }
        messagesPath = messagesFile.getAbsolutePath();
    }

    public static boolean isAddressValid(String address) {
        try {
            InetAddress.getByName(address);
            return true;
        } catch (UnknownHostException e) {
            return false;
        }
    }

    public static boolean isPortValid(int port) {
        return 5000 <= port && port <= 5050;
    }
}