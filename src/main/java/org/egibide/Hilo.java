package org.egibide;

import javax.crypto.Cipher;
import javax.net.ssl.SSLSocket;
import java.io.*;
import java.security.*;

public class Hilo extends Thread {
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

            byte[] descripcion = (byte[]) entrada.readObject();
            byte[] lugar = (byte[]) entrada.readObject();
            byte[] nombre = (byte[]) entrada.readObject();

            byte[] firma = (byte[]) entrada.readObject();

            String sNombre = descifrar(nombre, clavepriv);
            if (comprobarFirma(claveEmpleado, sNombre, firma)) {
                String sDescripcion = descifrar(descripcion, clavepriv);
                String sLugar = descifrar(lugar, clavepriv);
                System.out.println("Nueva incidencia: " + sDescripcion + ". Localizacion: " + sLugar + ". Reportado por: " + sNombre);
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

    public static String descifrar(byte[] mensaje, PrivateKey clavepriv) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, clavepriv);
        byte[] mensajeDescifrado = cipher.doFinal(mensaje);
        return new String(mensajeDescifrado);
    }

    public static boolean comprobarFirma(PublicKey clavePublica, String mensaje, byte[] firma) throws Exception {
        Signature verificada = Signature.getInstance("SHA1withRSA");
        verificada.initVerify(clavePublica);
        verificada.update(mensaje.getBytes());
        boolean check = verificada.verify(firma);

        if (!check) {
            System.out.println("Se ha bloqueado una incidencia de una firma no verificada");
        }
        return check;
    }
}
