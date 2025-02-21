import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ClientHandler extends Thread {
    private Socket socket;
    private int clientNumber;
    private String username;
    private static final int MAX_MESSAGE_LENGTH = 200;

    public ClientHandler(Socket socket, int clientNumber) {
        this.socket = socket;
        this.clientNumber = clientNumber;
        System.out.println("Nouvelle connexion avec client#" + clientNumber + " à " + socket);
    }

    public void run() {
        try {
            DataInputStream in  = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeUTF("Entrez votre nom d'utilisateur:");
            String receivedUsername = in.readUTF();
            boolean authenticated = false;
            for (int attempts = 0; attempts < 3; attempts++) {
                out.writeUTF("Entrez votre mot de passe:");
                String receivedPassword = in.readUTF();
                if (checkAuthentication(receivedUsername, receivedPassword)) {
                    authenticated = true;
                    break;
                } else {
                    if (attempts < 2)
                        out.writeUTF("Mot de passe incorrect. Réessayez.");
                    else
                        out.writeUTF("Erreur dans la saisie du mot de passe. Fin de la connexion.");
                }
            }
            if (!authenticated) {
                return;
            }
            this.username = receivedUsername;
            out.writeUTF("Authentification réussie ! Bienvenue " + username);
            out.writeUTF("Voici les derniers messages :");
            for (String msg : ServerTP.getRecentMessages()) {
                out.writeUTF(msg);
            }
            out.writeUTF("Vous pouvez maintenant envoyer vos messages (tapez 'exit' pour quitter).");
            while (true) {
                String clientMessage = in.readUTF();
                if (clientMessage.equalsIgnoreCase("exit")) {
                    out.writeUTF("Déconnexion...");
                    break;
                }
                if (clientMessage.length() > MAX_MESSAGE_LENGTH) {
                    out.writeUTF("Message trop long, maximum " + MAX_MESSAGE_LENGTH + " caractères autorisés.");
                    continue;
                }
                String formattedMessage = formatMessage(clientMessage);
                ServerTP.appendMessage(formattedMessage);
                ServerTP.broadcast(formattedMessage);
            }
        } catch (IOException e) {
            System.err.println("Erreur lors du traitement du client#" + clientNumber + ": " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("Impossible de fermer le socket: " + e.getMessage());
            }
            System.out.println("Connexion avec client#" + clientNumber + " fermée.");
        }
    }

    private boolean checkAuthentication(String username, String password) {
        File file = new File(ServerTP.CREDENTIALS_FILE);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            List<String> lines = java.nio.file.Files.readAllLines(file.toPath());
            for (String line : lines) {
                String[] parts = line.split(",");
                if (parts.length >= 2 && parts[0].equals(username)) {
                    return parts[1].equals(password);
                }
            }
            String entry = username + "," + password + System.lineSeparator();
            java.nio.file.Files.write(file.toPath(), entry.getBytes(), java.nio.file.StandardOpenOption.APPEND);
            return true;
        } catch (IOException e) {
            System.err.println("Erreur lors de l'authentification : " + e.getMessage());
            return false;
        }
    }

    private String formatMessage(String message) {
        String clientIP = socket.getInetAddress().getHostAddress();
        int clientPort = socket.getPort();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd@HH:mm:ss");
        String dateTime = LocalDateTime.now().format(formatter);
        return String.format("[%s - %s:%d - %s]: %s", username, clientIP, clientPort, dateTime, message);
    }

    public void sendMessage(String message) {
        try {
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeUTF(message);
        } catch (IOException e) {
            System.err.println("Erreur d'envoi au client#" + clientNumber + ": " + e.getMessage());
        }
    }
}
