package org.egibide;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import java.io.*;

public class Hilo extends Thread {
    private SSLServerSocket servidorSSL;

    public Hilo(SSLServerSocket servidorSSL) {
        this.servidorSSL = servidorSSL;
    }

    @Override
    public void run() {
        SSLSocket clienteConectado = null;
        try {
            clienteConectado = (SSLSocket) servidorSSL.accept();
            ObjectOutputStream salida = new ObjectOutputStream(clienteConectado.getOutputStream());
            ObjectInputStream entrada = new ObjectInputStream(clienteConectado.getInputStream());

            byte[] mensaje = null;
            mensaje = (byte[]) entrada.readObject();
            SecretKey claveSecreta = (SecretKey) entrada.readObject();
            System.out.println("Nueva incidencia: " + descifrarTexto(mensaje, claveSecreta));

            clienteConectado.close();
            salida.close();
            entrada.close();

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }

    }

    public static String descifrarTexto(byte[] textoCifrado, SecretKey claveSecreta) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, claveSecreta);
        byte[] textoDecifrado = cipher.doFinal(textoCifrado);
        return new String(textoDecifrado);
    }
}
