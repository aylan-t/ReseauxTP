import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ClientTP {
    private static Socket socket;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String serverIP;
        int serverPort;
        while (true) {
            System.out.print("Entrez l'adresse IP du serveur: ");
            serverIP = scanner.nextLine();
            if (ServerTP.isValidIPAddress(serverIP)) {
                break;
            } else {
                System.out.println("Adresse IP invalide. Réessayez.");
            }
        }
        while (true) {
            System.out.print("Entrez le port du serveur (entre " + ServerTP.MIN_PORT + " et " + ServerTP.MAX_PORT + "): ");
            String portStr = scanner.nextLine();
            try {
                serverPort = Integer.parseInt(portStr);
                if (ServerTP.isValidPort(serverPort)) {
                    break;
                } else {
                    System.out.println("Port invalide. Réessayez.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Port invalide. Réessayez.");
            }
        }
        try {
            socket = new Socket(serverIP, serverPort);
            DataInputStream in  = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            String prompt = in.readUTF();
            System.out.println(prompt);
            System.out.print("Votre nom d'utilisateur: ");
            String username = scanner.nextLine();
            out.writeUTF(username);
            String pwdPrompt = in.readUTF();
            System.out.println(pwdPrompt);
            System.out.print("Votre mot de passe: ");
            String password = scanner.nextLine();
            out.writeUTF(password);
            while (true) {
                String response = in.readUTF();
                if (response.startsWith("Mot de passe incorrect")) {
                    System.out.println(response);
                    String newPrompt = in.readUTF();
                    System.out.println(newPrompt);
                    System.out.print("Votre mot de passe: ");
                    String newPassword = scanner.nextLine();
                    out.writeUTF(newPassword);
                } else if (response.startsWith("Erreur dans la saisie")) {
                    System.out.println(response);
                    socket.close();
                    scanner.close();
                    return;
                } else if (response.startsWith("Authentification réussie")) {
                    System.out.println(response);
                    break;
                } else {
                    System.out.println("Réponse inattendue du serveur: " + response);
                }
            }
            System.out.println(in.readUTF());
            String line;
            while (true) {
                line = in.readUTF();
                if (line.startsWith("Vous pouvez maintenant envoyer vos messages")) {
                    System.out.println(line);
                    break;
                }
                System.out.println(line);
            }
            Thread readThread = new Thread(() -> {
                try {
                    while (true) {
                        String msg = in.readUTF();
                        System.out.println(msg);
                    }
                } catch (IOException e) {
                    System.out.println("Connexion fermée par le serveur.");
                }
            });
            readThread.start();
            while (true) {
                String message = scanner.nextLine();
                out.writeUTF(message);
                if (message.equalsIgnoreCase("exit")) {
                    break;
                }
            }
            socket.close();
            scanner.close();
        } catch (IOException e) {
            System.err.println("Erreur lors de la connexion au serveur: " + e.getMessage());
        }
    }
}
