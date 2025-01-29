import javax.naming.AuthenticationException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class ClientTP {
    private static Socket socket;
    private static String adresseIP;
    private static int port;

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        boolean validInput = false;

        while (!validInput) {
            try {
                receuillirInfos();
                validInput = true;
            } catch (Exception e) {
                System.out.println(e.getMessage());
                System.out.println("Veuillez réessayer.\n");
            }
        }

        try {
            socket = new Socket(adresseIP, port);
            System.out.format("Connecté au serveur sur [%s:%d]\n", adresseIP, port);

            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            System.out.println("Veuillez entrer votre nom d'utilisateur:");
            String nomUtilisateur = sc.nextLine();
            System.out.println("Veuillez entrer votre mot de passe:");
            String mdp = sc.nextLine();

            out.writeUTF(nomUtilisateur);
            out.writeUTF(mdp);

            String reponseAuth = in.readUTF();
            if (verifierReponse(reponseAuth)) {
                throw new AuthenticationException();
            }
            else {
                int taille = in.read();
                for (int i = 0; i < taille ; i++) {
                    String historiqueMessages = in.readUTF();
                    System.out.println(historiqueMessages);
                }

                boolean msgValide;
                do {
                    System.out.println("Veuillez écrire un message (Limite de 200 charactères)");
                    String message = sc.nextLine();
                    msgValide = validerMessage(message);
                    out.writeUTF(message);
                } while (!msgValide);
                String message;
                do {
                    String lastMessage = in.readUTF();
                    System.out.println(lastMessage);
                    message = sc.nextLine();
                    out.writeUTF(message + "\n");
                } while (!(message.equals("Z")));
            }

            socket.close();
        } catch (IOException e) {
            System.out.println("Erreur lors de la connexion au serveur: " + e.getMessage());
        } catch (AuthenticationException e) {
            System.out.println("Erreur dans la saisie du mot de passe");
        }
    }

    public static void receuillirInfos() throws Exception {
        Scanner sc = new Scanner(System.in);

        System.out.println("Veuillez saisir l'adresse IP de votre serveur:");
        adresseIP = sc.nextLine();
        try {
            verifierIP(adresseIP);
        } catch (UnknownHostException e) {
            throw new Exception("Adresse IP invalide.");
        }

        System.out.println("Veuillez saisir le port de votre serveur (5000 ou 5050):");
        try {
            port = Integer.parseInt(sc.nextLine());
            if (port != 5000 && port != 5050) {
                throw new Exception("Port invalide. Utilisez 5000 ou 5050.");
            }
        } catch (NumberFormatException e) {
            throw new Exception("Le port doit être un nombre entier.");
        }
    }

    public static void verifierIP(String adresseIP) throws UnknownHostException {
        InetAddress.getByName(adresseIP);
    }

    public static boolean verifierReponse(String reponseAuth) {
        return reponseAuth.toLowerCase().contains("erreur mdp");
    }

    public static boolean validerMessage(String message) {
        return (message != null) && (message.length() < 200);
    }
}