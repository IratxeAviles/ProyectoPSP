package org.egibide;

import org.egibide.Certificado.Hilo;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.IOException;

public class Sistema {
    public static void main(String[] args) {
        int puerto = 6000;
        System.setProperty("javax.net.ssl.keyStore", "src/main/java/org/egibide/Certificado/CertificadoSSL.jks");
        System.setProperty("javax.net.ssl.keyStorePassword", "12345Abcde");
        SSLServerSocketFactory sfact = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        SSLServerSocket servidorSSL = null;
        try {
            servidorSSL = (SSLServerSocket) sfact.createServerSocket(puerto);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Servidor conectado, esperando incidencias...");

        while (true) {
            Hilo empleado = new Hilo(servidorSSL);
            empleado.start();
        }
    }


}
