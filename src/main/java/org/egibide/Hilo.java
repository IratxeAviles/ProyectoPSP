package org.egibide;

import javax.crypto.Cipher;
import javax.net.ssl.SSLSocket;
import java.io.*;
import java.security.*;

public class Hilo extends Thread {
    // Hacer un random para elegir que prioridad dar a cada incidencia y pasarla al usuario
    private final SSLSocket clienteConectado;
    private final PublicKey clavepub;
    private final PrivateKey clavepriv;

    public Hilo(SSLSocket clienteConectado, PublicKey clavepub, PrivateKey clavepriv) {
        this.clienteConectado = clienteConectado;
        this.clavepub = clavepub;
        this.clavepriv = clavepriv;
    }

    @Override
    public void run() {
        try {
            ObjectOutputStream salida = new ObjectOutputStream(clienteConectado.getOutputStream());
            ObjectInputStream entrada = new ObjectInputStream(clienteConectado.getInputStream());

            salida.writeObject(clavepub);
            PublicKey claveEmpleado = (PublicKey) entrada.readObject();

            byte[] incidenciaCifrada = (byte[]) entrada.readObject();
            byte[] firma = (byte[]) entrada.readObject();

            Incidencia incidencia = descifrar(incidenciaCifrada, clavepriv);
            if (comprobarFirma(claveEmpleado, incidencia, firma)) {
                System.out.println(incidencia);
            }

            // Cerrar conexión
            clienteConectado.close();
            salida.close();
            entrada.close();

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }

    }

    public static byte[] cifrar(String mensaje, PrivateKey clavepriv) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, clavepriv);
        return cipher.doFinal(mensaje.getBytes());
    }

    public static Incidencia descifrar(byte[] incidencia, PrivateKey clavepriv) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, clavepriv);
        byte[] incidenciaDescifrada = cipher.doFinal(incidencia);
        return (Incidencia) deserializeObject(incidenciaDescifrada);
    }

    public static boolean comprobarFirma(PublicKey clavePublica, Incidencia incidencia, byte[] firma) throws Exception {
        Signature verificada = Signature.getInstance("SHA1withRSA");
        verificada.initVerify(clavePublica);
        verificada.update(serializeObject(incidencia));
        boolean check = verificada.verify(firma);

        if (!check) {
            System.out.println("Se ha bloqueado una incidencia de una firma no verificada");
        }
        return check;
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

    // Metodo de la solución del ejercicio 8 UDP
    public static Object deserializeObject(byte[] data) throws Exception {
        java.io.ByteArrayInputStream byteStream = new java.io.ByteArrayInputStream(data);
        java.io.ObjectInputStream objectStream = new java.io.ObjectInputStream(byteStream);
        return objectStream.readObject();
    }
}
