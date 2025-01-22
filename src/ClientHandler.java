import javax.security.sasl.AuthenticationException;
import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Scanner;

public class ClientHandler extends Thread { // Pour traiter la demande de chaque client sur un socket particulier
    private Socket socket;
    private String csvPath;
    private String messagePath;
    private int clientNumber;
    private int recentMessagesSize = 15;

    public ClientHandler(Socket socket, String csvPath, String messagePath, int clientNumber) {
        this.socket = socket;
        this.csvPath = csvPath;
        this.messagePath = messagePath;
        this.clientNumber = clientNumber;
        System.out.println("New connection with client#" + clientNumber + " at " + socket);
    }

    public void run() { // Création de thread qui envoie un message à un client
        try {
            DataOutputStream out = new DataOutputStream(socket.getOutputStream()); // Création de canal d'envoi
            DataInputStream in = new DataInputStream(socket.getInputStream());
            String username = in.readUTF();
            String mdp = in.readUTF();
            if(usernameExists(username)){
                if (!validateCreds(username, mdp)) {
                    throw new AuthenticationException();
                }
                LinkedList<String> recentMessages = getRecentLines(messagePath);//Messages a output
                for (String message : recentMessages) {
                    out.writeUTF(message);
                }
            }
            else {
                //Creation du user
                writeUser(username, mdp);
            }
        } catch (AuthenticationException e) {
            System.out.println("erreur mdp");
        } catch (IOException e){
            System.out.println("Error handling client# " + clientNumber + ": " + e);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Couldn't close a socket, what's going on?");
            }
            System.out.println("Connection with client# " + clientNumber + " closed");
        }
    }

    public boolean usernameExists(String username) {
        try(Scanner sc = new Scanner(new File(csvPath))) {
            while (sc.hasNextLine()) {
                String[] line = sc.nextLine().split(",");
                if (line.length>0 && line[0].contains(username)) {
                    return true;
                }
            }

        } catch (Exception e) {
            System.out.println("Erreur de lecture du fichier");
        }
        return false;
    }

    public boolean validateCreds(String username, String password) {
        try(Scanner sc = new Scanner(new File(csvPath))) {
            while (sc.hasNextLine()) {
                String[] line = sc.nextLine().split(",");
                if (line.length>0 && line[0].contains(username) && line[1].contains(password)) {
                    return true;
                }
            }

        } catch (Exception e) {
            System.out.println("Erreur de lecture du fichier");
        }
        return false;
    }

    public void writeUser(String username, String mdp) {
        try(FileWriter writer = new FileWriter((csvPath))) {
            String entry = username+","+mdp;
            writer.append(entry);
            writer.append("\n");
        }
        catch (IOException e) {
            System.out.println("Erreur de lecture du fichier");
        }
    }

    public LinkedList<String> getRecentLines(String filePath) {
        LinkedList<String> recentLines = new LinkedList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                recentLines.add(line);

                if (recentLines.size() > recentMessagesSize) {
                    recentLines.removeFirst();
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur de lecture du fichier " + e.getMessage());
        }
        return recentLines;
    }
}