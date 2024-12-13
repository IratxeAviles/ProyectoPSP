package org.egibide;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.security.*;

public class Sistema {
    public static void main(String[] args) {
        BBDD bbdd = new BBDD();
        int puerto = 6565;

        // Uso del certificado en la parte del servidor
        System.setProperty("javax.net.ssl.keyStore", "src/main/java/org/egibide/Certificado/CertificadoSSL.jks");
        System.setProperty("javax.net.ssl.keyStorePassword", "12345Abcde");
        SSLServerSocketFactory sfact = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        SSLServerSocket servidorSSL = null;
        try {
            servidorSSL = (SSLServerSocket) sfact.createServerSocket(puerto);

            // Creacion de clave publica y privada para el cifrado asimetrico
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            KeyPair par = keyGen.generateKeyPair();
            PrivateKey clavepriv = par.getPrivate();
            PublicKey clavepub = par.getPublic();

            System.out.println("Servidor conectado, esperando incidencias...");
            SSLSocket empleadoConectado = null;

            while (true) { // El servidor nunca se detiene
                try {
                    // Cuando recibe una peticion de un empleado, lanza un hilo para poder ser una aplicación multihilo
                    empleadoConectado = (SSLSocket) servidorSSL.accept();
                    Hilo empleado = new Hilo(empleadoConectado, clavepub, clavepriv, bbdd);
                    empleado.start();
                } catch (IOException e) {
                    System.out.println("Error al conectar el empleado");
                }
            }
        } catch (IOException e) {
            System.out.println("Error al crear el servidor: " + e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Error al crear claves");
        }
    }
}
