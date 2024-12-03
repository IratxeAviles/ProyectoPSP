package org.egibide;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;

public class Empleado {
    public static void main(String[] args) {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        String host = "localhost";
        int puerto = 6000;
        System.out.println("Programa cliente iniciado...");
        System.setProperty("javax.net.ssl.trustStore", "src/main/java/org/egibide/Certificado/UsuarioCertificadoSSL");
        System.setProperty("javax.net.ssl.trustStorePassword", "12345Abcde");
        SSLSocketFactory sfact = (SSLSocketFactory) SSLSocketFactory.getDefault();
        SSLSocket cliente = null;
        try {
            cliente = (SSLSocket) sfact.createSocket(host, puerto);

            ObjectInputStream entrada = new ObjectInputStream(cliente.getInputStream());
            ObjectOutputStream salida = new ObjectOutputStream(cliente.getOutputStream());

            KeyGenerator keyGenerator = null;
            keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(256);
            SecretKey claveSecreta = keyGenerator.generateKey();

            System.out.print("Escriba su incidencia a continuación: ");
            String mensaje = br.readLine();
            byte[] mensajeCifrado = cifrarTexto(mensaje, claveSecreta);
            salida.writeObject(mensajeCifrado);
            salida.writeObject(claveSecreta);

            System.out.println("Incidencia enviada");

            entrada.close();
            salida.close();
            cliente.close();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static byte[] cifrarTexto(String textoOriginal, SecretKey claveSecreta) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, claveSecreta);

        return cipher.doFinal(textoOriginal.getBytes());
    }
}
