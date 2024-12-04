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
    public static void main(String[] args) {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        String host = "localhost";
        int puerto = 6565;
        System.out.println("Programa empleado iniciado...");

        // Uso del certificado en la parte del "cliente"
        System.setProperty("javax.net.ssl.trustStore", "src/main/java/org/egibide/Certificado/UsuarioCertificadoSSL");
        System.setProperty("javax.net.ssl.trustStorePassword", "12345Abcde");
        SSLSocketFactory sfact = (SSLSocketFactory) SSLSocketFactory.getDefault();
        SSLSocket empleado = null;
        try {
            empleado = (SSLSocket) sfact.createSocket(host, puerto);

            ObjectInputStream entrada = new ObjectInputStream(empleado.getInputStream());
            ObjectOutputStream salida = new ObjectOutputStream(empleado.getOutputStream());

            // Creacion de clave publica y privada para la conversacion cifrada con el servidor
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            KeyPair par = keyGen.generateKeyPair();
            PrivateKey clavepriv = par.getPrivate();
            PublicKey clavepub = par.getPublic();

            // Se pasa la clave publica del empleado y se recibe la del servidor para empezar la conversacion cifrada
            PublicKey claveServidor = (PublicKey) entrada.readObject();
            salida.writeObject(clavepub);

            // ---- Inicio del programa ----
            int opcion = 0;
            boolean login = false;
            do {
                System.out.print("1. Registro 2. Inicio de Sesion 3. Salir");
                System.out.println("Introduce una opción:");
                try {
                    opcion = Integer.parseInt(br.readLine());
                    salida.writeObject(opcion);
                    switch (opcion) { // Registro
                        case 1:
                            System.out.println("Introduce tu email para registrarte: ");
                            String nuevoUsuario = br.readLine();

                            if (nuevoUsuario.matches("[a-zA-Z0-9]+[@][a-zA-Z]+[.][a-z]{2,3}")) {
                                System.out.println("Introduce una nueva contraseña: ");
                                String nuevaContrasena = br.readLine();
                                salida.writeObject(cifrar(nuevoUsuario, clavepub));
                                salida.writeObject(nuevaContrasena);
                            } else {
                                System.out.println("Usuario no válido, debe ser en formato email usando @ y .");
                            }
                            break;
                        case 2: // Inicio de sesion
                            System.out.println("Introduce tu usuario (email): ");
                            String usuario = br.readLine();
                            System.out.println("Introduce tu contraseña: ");
                            String contrasena = br.readLine();

                            break;
                        case 3: // Salir de la aplicación
                            break;
                        default: // Error
                            System.out.println("Opción no válida");
                            break;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Valor incorrecto");
                }
            } while (opcion == 2 && login == true || opcion == 3); // salir del bucle si se ha hecho login o se ha elegido la tercera opcion

            if (opcion != 3) {
                // ---- Escribir incidencia ----
                System.out.print("Escriba la descripción de la incidencia: ");
                String descripcion = br.readLine();
                System.out.print("Escriba el lugar de su incidencia: ");
                String lugar = br.readLine();
                System.out.print("Escriba su nombre: ");
                String nombre = br.readLine();
                Incidencia incidencia = new Incidencia(descripcion, lugar, nombre);

                // ---- Enviar incidencia -----
                byte[] incidenciaCifrada = cifrarIncidencia(incidencia, claveServidor);
                byte[] firma = firmarIncidencia(incidencia, clavepriv);
                salida.writeObject(incidenciaCifrada);
                salida.writeObject(firma);

                byte[] respuesta = (byte[]) entrada.readObject();

                System.out.println("Respuesta del sistema: " + descifrar(respuesta, clavepriv));
            }

            // Desconectar
            entrada.close();
            salida.close();
            empleado.close();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static byte[] cifrar(String texto, PublicKey claveServidor) {
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, claveServidor);
            return cipher.doFinal(serializeObject(texto));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException |
                 BadPaddingException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return null;
    }

    public static byte[] cifrarIncidencia(Incidencia incidencia, PublicKey claveServidor) {
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

    public static String descifrar(byte[] incidencia, PrivateKey clavepriv) {
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, clavepriv);
            byte[] incidenciaDescifrada = cipher.doFinal(incidencia);
            return new String(incidenciaDescifrada);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException |
                 BadPaddingException e) {
            System.out.println("Error: " + e.getMessage());
            return null;
        }
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
