package org.egibide;

import javax.crypto.Cipher;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.security.*;

public class Empleado {
    public static void main(String[] args) {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        String host = "localhost";
        int puerto = 6565;
        System.out.println("Programa empleado iniciado...");
        System.setProperty("javax.net.ssl.trustStore", "src/main/java/org/egibide/Certificado/UsuarioCertificadoSSL");
        System.setProperty("javax.net.ssl.trustStorePassword", "12345Abcde");
        SSLSocketFactory sfact = (SSLSocketFactory) SSLSocketFactory.getDefault();
        SSLSocket empleado = null;
        try {
            empleado = (SSLSocket) sfact.createSocket(host, puerto);

            ObjectInputStream entrada = new ObjectInputStream(empleado.getInputStream());
            ObjectOutputStream salida = new ObjectOutputStream(empleado.getOutputStream());

            // Creacion de clave publica y privada
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            KeyPair par = keyGen.generateKeyPair();
            PrivateKey clavepriv = par.getPrivate();
            PublicKey clavepub = par.getPublic();

            PublicKey claveServidor = (PublicKey) entrada.readObject();
            salida.writeObject(clavepub);

            // ---- Escribir incidencia ----
            System.out.print("Escriba la descripción de la incidencia: ");
            String descripcion = br.readLine();
            System.out.print("Escriba el lugar de su incidencia: ");
            String lugar = br.readLine();
            System.out.print("Escriba su nombre: ");
            String nombre = br.readLine();

            // ---- Enviar incidencia -----
            byte[] descripcionCifrado = cifrar(descripcion, claveServidor);
            salida.writeObject(descripcionCifrado);

            byte[] lugarCifrado = cifrar(lugar, claveServidor);
            salida.writeObject(lugarCifrado);

            byte[] nombreCifrado = cifrar(nombre, claveServidor);
            byte[] firma = firmarIncidencia(nombre, clavepriv);
            salida.writeObject(nombreCifrado);
            salida.writeObject(firma);

            System.out.println("Incidencia enviada");

            // Desconectar
            entrada.close();
            salida.close();
            empleado.close();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static byte[] cifrar(String mensaje, PublicKey claveServidor) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, claveServidor);
        return cipher.doFinal(mensaje.getBytes());
    }

    public static byte[] firmarIncidencia(String nombre, PrivateKey clavepriv) throws Exception {
        Signature dsa = Signature.getInstance("SHA1withRSA");
        dsa.initSign(clavepriv);
        dsa.update(nombre.getBytes());
        return dsa.sign();
    }
}
