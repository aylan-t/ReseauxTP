import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class ServerTP {
    public static final int MIN_PORT = 5000;
    public static final int MAX_PORT = 5050;
    public static final int MAX_RECENT_MESSAGES = 15;
    public static final String MESSAGES_FILE = "messages.txt";
    public static final String CREDENTIALS_FILE = "userCredentials.csv";
    public static List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());
    private static ServerSocket listener;

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        String serverAddress;
        int serverPort = 0;
        while (true) {
            System.out.print("Entrez l'adresse IP du serveur: ");
            serverAddress = scanner.nextLine();
            if (isValidIPAddress(serverAddress)) {
                break;
            } else {
                System.out.println("Adresse IP invalide. Veuillez entrer une adresse IP valide.");
            }
        }
        while (true) {
            System.out.print("Entrez le port d'écoute (entre " + MIN_PORT + " et " + MAX_PORT + "): ");
            String portStr = scanner.nextLine();
            try {
                serverPort = Integer.parseInt(portStr);
                if (isValidPort(serverPort)) {
                    break;
                } else {
                    System.out.println("Port invalide. Veuillez entrer un port entre " + MIN_PORT + " et " + MAX_PORT + ".");
                }
            } catch (NumberFormatException e) {
                System.out.println("Port invalide. Veuillez entrer un nombre.");
            }
        }
        scanner.close();
        listener = new ServerSocket();
        listener.setReuseAddress(true);
        InetAddress serverIP = InetAddress.getByName(serverAddress);
        listener.bind(new InetSocketAddress(serverIP, serverPort));
        System.out.format("Le serveur est lancé sur %s:%d%n", serverAddress, serverPort);
        int clientNumber = 0;
        try {
            while (true) {
                ClientHandler handler = new ClientHandler(listener.accept(), clientNumber++);
                clients.add(handler);
                handler.start();
            }
        } finally {
            listener.close();
        }
    }

    public static boolean isValidIPAddress(String ip) {
        String[] parts = ip.split("\\.");
        if (parts.length != 4) return false;
        try {
            for (String part : parts) {
                int num = Integer.parseInt(part);
                if (num < 0 || num > 255) return false;
            }
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidPort(int port) {
        return port >= MIN_PORT && port <= MAX_PORT;
    }

    public static void broadcast(String message) {
        synchronized(clients) {
            for (ClientHandler ch : clients) {
                ch.sendMessage(message);
            }
        }
    }

    public static LinkedList<String> getRecentMessages() {
        LinkedList<String> recentLines = new LinkedList<>();
        File file = new File(MESSAGES_FILE);
        if (!file.exists()) {
            return recentLines;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                recentLines.add(line);
                if (recentLines.size() > MAX_RECENT_MESSAGES) {
                    recentLines.removeFirst();
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur de lecture du fichier " + e.getMessage());
        }
        return recentLines;
    }

    public static void appendMessage(String message) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(MESSAGES_FILE, true))) {
            writer.write(message);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Erreur lors de l'écriture du fichier: " + e.getMessage());
        }
    }
}
