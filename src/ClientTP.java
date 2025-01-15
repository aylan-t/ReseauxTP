import org.w3c.dom.ls.LSOutput;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;
import java.util.regex.Pattern;

public class ClientTP {
    private static Socket socket;
    private static String adresseIP;
    private static int port;

    public static void main(String[] args) throws Exception {
        // Adresse et port du serveur
        receuillirInfos();
        socket = new Socket(adresseIP, port);
        System.out.format("Serveur lancé sur [%s:%d]", adresseIP, port);

        DataInputStream in = new DataInputStream(socket.getInputStream());
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
    }
    public static void receuillirInfos() throws IOException {
        Scanner sc = new Scanner(System.in);
        adresseIP = "";
        port = 0;
        System.out.println("Veuillez saisir l'addresse IP de votre client");
        try {
            adresseIP = sc.nextLine();
            if (!verifierIP(adresseIP)) {
                throw new Exception(e);
            }
        } catch (Exception e) {
            System.out.println(e);
        }

        System.out.println("Veuillez saisir le port de votre client");
        try {
            port = sc.nextInt();
            if (port != 5000 && port != 5050) {
                throw new Exception(e);
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    public static boolean verifierIP(String adresseIP) {
        if (adresseIP == null) {
            return false;
        }
        String regexIP = "^(25[0-5]|2[0-4][0-9]|[0-1]?[0-9][0-9]?)\\." +
                "(25[0-5]|2[0-4][0-9]|[0-1]?[0-9][0-9]?)\\." +
                "(25[0-5]|2[0-4][0-9]|[0-1]?[0-9][0-9]?)\\." +
                "(25[0-5]|2[0-4][0-9]|[0-1]?[0-9][0-9]?)$";
        return Pattern.compile(regexIP).matcher(adresseIP).matches();
    }
}
