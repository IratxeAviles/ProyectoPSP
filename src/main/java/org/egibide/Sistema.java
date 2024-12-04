package org.egibide;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.security.*;

public class Sistema {
    public static void main(String[] args) {
        BBDD bbdd = new BBDD();
        int puerto = 6565;
        System.setProperty("javax.net.ssl.keyStore", "src/main/java/org/egibide/Certificado/CertificadoSSL.jks");
        System.setProperty("javax.net.ssl.keyStorePassword", "12345Abcde");
        SSLServerSocketFactory sfact = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        SSLServerSocket servidorSSL = null;
        try {
            servidorSSL = (SSLServerSocket) sfact.createServerSocket(puerto);

            // Creacion de clave publica y privada
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            KeyPair par = keyGen.generateKeyPair();
            PrivateKey clavepriv = par.getPrivate();
            PublicKey clavepub = par.getPublic();

            System.out.println("Servidor conectado, esperando incidencias...");
            SSLSocket clienteConectado = null;

            while (true) {
                try {
                    clienteConectado = (SSLSocket) servidorSSL.accept();
                    Hilo empleado = new Hilo(clienteConectado, clavepub, clavepriv, bbdd);
                    empleado.start();
                } catch (IOException e) {
                    System.out.println("Error al conectar el empleado");
                }
            }
        } catch (IOException e) {
            System.out.println("Error al crear el servidor");
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Error al crear claves");
        }
    }
}
