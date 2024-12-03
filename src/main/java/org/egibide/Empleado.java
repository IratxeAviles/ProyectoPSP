package org.egibide;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.security.*;

public class Empleado {
    // FALTA EL LOGIN
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
            Incidencia incidencia = new Incidencia(descripcion, lugar, nombre);

            // ---- Enviar incidencia -----
            byte[] incidenciaCifrada = cifrar(incidencia, claveServidor);
            byte[] firma = firmarIncidencia(incidencia, clavepriv);
            salida.writeObject(incidenciaCifrada);
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

    public static byte[] cifrar(Incidencia incidencia, PublicKey claveServidor) throws RuntimeException {
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, claveServidor);
            return cipher.doFinal(serializeObject(incidencia));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException |
                 BadPaddingException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return null;
    }

    public static byte[] firmarIncidencia(Incidencia incidencia, PrivateKey clavepriv) {
        Signature dsa = null;
        try {
            dsa = Signature.getInstance("SHA1withRSA");
            dsa.initSign(clavepriv);
            dsa.update(serializeObject(incidencia));
            return dsa.sign();
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return null;
    }

    // Metodo de la solucion del ejercicio 8 UDP
    public static byte[] serializeObject(Object obj) {
        java.io.ByteArrayOutputStream byteStream = new java.io.ByteArrayOutputStream();

        ObjectOutputStream objectStream = null;
        try {
            objectStream = new ObjectOutputStream(byteStream);
            objectStream.writeObject(obj);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return byteStream.toByteArray();
    }
}
