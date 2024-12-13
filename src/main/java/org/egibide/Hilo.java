package org.egibide;

import javax.crypto.*;
import javax.net.ssl.SSLSocket;
import java.io.*;
import java.security.*;
import java.util.Random;

public class Hilo extends Thread {
    private final SSLSocket empleadoConectado;
    private final PublicKey clavepub;
    private final PrivateKey clavepriv;
    private BBDD bbdd;

    public Hilo(SSLSocket empleadoConectado, PublicKey clavepub, PrivateKey clavepriv, BBDD bbdd) {
        this.empleadoConectado = empleadoConectado;
        this.clavepub = clavepub;
        this.clavepriv = clavepriv;
        this.bbdd = bbdd;
    }

    @Override
    public void run() {
        try {
            ObjectOutputStream salida = new ObjectOutputStream(empleadoConectado.getOutputStream());
            ObjectInputStream entrada = new ObjectInputStream(empleadoConectado.getInputStream());

            // Se pasa la clave publica del servidor y se recibe la del empleado para empezar la conversacion cifrada
            salida.writeObject(clavepub);
            PublicKey claveEmpleado = (PublicKey) entrada.readObject();
            SecretKey claveSecreta = (SecretKey) entrada.readObject();

            int opcion = 0;
            boolean login = false;

            do {
                opcion = (int) entrada.readObject();
                switch (opcion) {
                    case 1:
                        byte[] usuarioCifrado = (byte[]) entrada.readObject();
                        Usuario usuario = descifrarUsuario(usuarioCifrado, claveSecreta);
                        bbdd.guardarUsuario(usuario);
                        login = true;
                        break;
                    case 2:
                        byte[] usuarioBytes = (byte[]) entrada.readObject();
                        Usuario usuarioLogin = descifrarUsuario(usuarioBytes, claveSecreta);
                        if (bbdd.comprobarLogin(usuarioLogin)) {
                            System.out.println("Usuario " + usuarioLogin.getNombre() + " logueado correctamente");
                            salida.writeObject(true);
                        } else {
                            System.out.println("Login incorrecto");
                            salida.writeObject(false);
                        }
                        login = true;
                        break;
                    case 3:
                        salida.writeObject("¡Adios!");
                        break;
                    default:
                        break;
                }
            } while (login || opcion != 3);

            // Se recibe la incidencia cifrada junto con la firma digital
            byte[] incidenciaCifrada = (byte[]) entrada.readObject();
            byte[] firma = (byte[]) entrada.readObject();

            // Primero se descifra la incidencia para poder comprobar la firma digital, si es válida continúa el código, sino no se guarda la incidencia
            Incidencia incidencia = descifrar(incidenciaCifrada, clavepriv);
            if (comprobarFirma(claveEmpleado, incidencia, firma)) {
                System.out.println(incidencia);

                Random random = new Random();
                switch (random.nextInt(3)) { // Random del 0 al 2 para poder elegir entre prioridad Leve / Moderada / Urgente y responder al empleado según el caso
                    case 0:
                        incidencia.setPrioridad(Prioridad.Leve); // Se guarda la prioridad en la incidencia
                        salida.writeObject(cifrar("Incidencia leve recibida. Se tardará en responder en un máximo de 1 semana", claveEmpleado));
                        bbdd.guardarIncidencia(incidencia); // Se usa el metodo de la clase BBDD para guardar la incidencia en una lista y saber que codigo le tocara
                        break;
                    case 1:
                        incidencia.setPrioridad(Prioridad.Moderada);
                        salida.writeObject(cifrar("Incidencia moderada recibida. Se tardará en responder en un máximo de 2 días laborales", claveEmpleado));
                        bbdd.guardarIncidencia(incidencia);
                        break;
                    case 2:
                        incidencia.setPrioridad(Prioridad.Urgente);
                        salida.writeObject(cifrar("Incidencia urgente recibida. Se tardará en responder en un máximo de 2 horas", claveEmpleado));
                        bbdd.guardarIncidencia(incidencia);
                        break;
                    default:
                        break;
                }
            } else {
                salida.writeObject(cifrar("Incidencia cancelada. La incidencia está vacía o la firma no es válida", claveEmpleado));
            }

            // Cerrar conexión
            empleadoConectado.close();
            salida.close();
            entrada.close();

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }

    }

    public static byte[] cifrar(String mensaje, PublicKey claveEmpleado) {
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, claveEmpleado);
            return cipher.doFinal(mensaje.getBytes());
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException |
                 BadPaddingException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return null;
    }

    public static Incidencia descifrar(byte[] incidencia, PrivateKey clavepriv) {
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, clavepriv);
            byte[] incidenciaDescifrada = cipher.doFinal(incidencia);
            return (Incidencia) deserializeObject(incidenciaDescifrada);
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
        return null;

    }

    public static Usuario descifrarUsuario(byte[] usuario, SecretKey claveSecreta) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, claveSecreta);
            byte[] usuarioDescifrado = cipher.doFinal(usuario);
            return (Usuario) deserializeObject(usuarioDescifrado);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        return null;
    }

    public static boolean comprobarFirma(PublicKey clavePublica, Incidencia incidencia, byte[] firma) {
        Signature verificada = null;
        try {
            verificada = Signature.getInstance("SHA1withRSA");

            verificada.initVerify(clavePublica);
            verificada.update(serializeObject(incidencia));
            boolean check = verificada.verify(firma);

            if (!check) {
                System.out.println("Se ha bloqueado una incidencia de una firma no verificada");
            }
            return check;
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return false;
    }

    // Metodo cogido de la solucion del ejercicio 8 UDP de Eider
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

    // Metodo cogido de la solución del ejercicio 8 UDP de Eider
    public static Object deserializeObject(byte[] data) {
        java.io.ByteArrayInputStream byteStream = new java.io.ByteArrayInputStream(data);
        ObjectInputStream objectStream = null;
        try {
            objectStream = new ObjectInputStream(byteStream);
            return objectStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return null;
    }
}
