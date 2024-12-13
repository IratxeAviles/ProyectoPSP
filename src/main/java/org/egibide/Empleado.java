package org.egibide;

import javax.crypto.*;
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

        String nombre = ""; // Para saber fácilmente quien inicia sesion

        // Uso del certificado en la parte del "cliente"
        System.setProperty("javax.net.ssl.trustStore", "src/main/java/org/egibide/Certificado/UsuarioCertificadoSSL");
        System.setProperty("javax.net.ssl.trustStorePassword", "12345Abcde");
        SSLSocketFactory sfact = (SSLSocketFactory) SSLSocketFactory.getDefault();
        SSLSocket empleado;
        try {
            empleado = (SSLSocket) sfact.createSocket(host, puerto);

            ObjectInputStream entrada = new ObjectInputStream(empleado.getInputStream());
            ObjectOutputStream salida = new ObjectOutputStream(empleado.getOutputStream());

            // Creacion de clave publica y privada para el cifrado asimetrico
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            KeyPair par = keyGen.generateKeyPair();
            PrivateKey clavepriv = par.getPrivate();
            PublicKey clavepub = par.getPublic();

            // Creacion de clave secreta para el cifrado simetrico
            KeyGenerator keyGenerator;
            keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(256);
            SecretKey claveSecreta = keyGenerator.generateKey();

            // Se pasa la clave publica del empleado y se recibe la del servidor para empezar la conversacion cifrada
            PublicKey claveServidor = (PublicKey) entrada.readObject();
            salida.writeObject(clavepub);
            salida.writeObject(claveSecreta);

            // ---- Inicio del programa ----
            int opcion = 0;
            boolean login = false;
            do {
                System.out.println("1. Registro 2. Inicio de Sesion 3. Salir");
                System.out.print("Introduce una opción:");
                try {
                    opcion = Integer.parseInt(br.readLine());
                    salida.writeObject(opcion);
                    switch (opcion) { // Registro
                        case 1:
                            // Pedimos nombe, apellido, edad (> 18), email ([a-zA-Z0-9]+[@][a-zA-Z]+[.][a-z]{2,3}, usuario (=>5 caracteres) y contraseña (=>5 caracteres))
                            // region PedirDatos
                            do {
                                System.out.println("Introduce tu nombre para registrarte: ");
                                nombre = br.readLine();
                                if (!nombre.matches("[a-zA-Z]{5,}")) {
                                    System.out.println("Usuario no valido, tiene que tener 5 o mas letras");
                                }
                            } while (!nombre.matches("[a-zA-Z]{5,}"));

                            String nuevoApellido;
                            do {
                                System.out.println("Introduce tu apellido: ");
                                nuevoApellido = br.readLine();
                                if (!nuevoApellido.matches("[a-zA-Z]{5,}")) {
                                    System.out.println("Usuario no valido, tiene que tener 5 o mas letras");
                                }
                            } while (!nuevoApellido.matches("[a-zA-Z]{5,}"));

                            int nuevaEdad = 0;
                            do {
                                System.out.println("Introduce tu edad: ");
                                try {
                                    nuevaEdad = Integer.parseInt(br.readLine());

                                    if (nuevaEdad < 18) {
                                        System.out.println("La edad debe ser mayor o igual que 18");
                                    }
                                } catch (NumberFormatException e) {
                                    System.out.println("Respuesta no valida");
                                }
                            } while (nuevaEdad < 18);

                            String nuevoEmail;
                            do {
                                System.out.println("Introduce tu email: ");
                                nuevoEmail = br.readLine();
                                if (!nuevoEmail.matches("[a-zA-Z0-9.]+@[a-zA-Z]+[.][a-z]{2,3}")) {
                                    System.out.println("Email no válido, tiene que incluir @ y .");
                                }
                            } while (!nuevoEmail.matches("[a-zA-Z0-9.]+@[a-zA-Z]+[.][a-z]{2,3}"));

                            String nuevoUsuario;
                            do {
                                System.out.println("Introduce el usuario que usaras para iniciar sesion: ");
                                nuevoUsuario = br.readLine();
                                if (!nuevoUsuario.matches("[a-zA-Z0-9]{5,}")) {
                                    System.out.println("Usuario no valido, tiene que tener 5 o mas caracteres");
                                }
                            } while (!nuevoUsuario.matches("[a-zA-Z0-9]{5,}"));

                            String nuevaContrasena;
                            do {
                                System.out.println("Introduce la contraseña que usaras para inciar sesion: ");
                                nuevaContrasena = br.readLine();

                                if ((!nuevaContrasena.matches("[a-zA-Z0-9]{5,}"))) {
                                    System.out.println("La contraseña debe tener 5 o más caracteres");
                                }
                            } while (!nuevaContrasena.matches("[a-zA-Z0-9]{5,}"));
                            // endregion

                            Usuario usuario = new Usuario(nombre, nuevoApellido, nuevaEdad, nuevoEmail, nuevoUsuario, getHash(nuevaContrasena));
                            salida.writeObject(cifrarUsuario(usuario, claveSecreta));
                            login = true;
                            break;
                        case 2: // Inicio de sesion
                            do {
                                System.out.println("Introduce tu usuario: ");
                                String registro = br.readLine();
                                System.out.println("Introduce tu contraseña: ");
                                String contrasena = br.readLine();
                                salida.writeObject(cifrar(registro, clavepub));
                                salida.writeObject(cifrar(getHash(contrasena), clavepub));

                                if ((boolean) entrada.readObject()) {
                                    login = true;
                                    System.out.println("Sesion iniciada correctamente");
                                } else {
                                    System.out.println("Usuario o contraseña incorrectos");
                                }
                            } while (!login);

                            break;
                        case 3: // Salir de la aplicación
                            System.out.println(entrada.readObject());
                            break;
                        default: // Error
                            System.out.println("Opción no válida");
                            break;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Valor incorrecto");
                }
            } while (!login || opcion != 3); // salir del bucle si se ha hecho login o se ha elegido la tercera opcion

            // --- Envio de incidencia ---
            if (opcion != 3) {
                // ---- Escribir incidencia ----
                System.out.print("Escriba la descripción de la incidencia: ");
                String descripcion = br.readLine();
                System.out.print("Escriba el lugar de su incidencia: ");
                String lugar = br.readLine();
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
        Cipher cipher;
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

    public static byte[] cifrarClaveSecreta(SecretKey claveSecreta, PublicKey claveServidor) {
        Cipher cipher;
        try {
            cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, claveServidor);
            return cipher.doFinal(serializeObject(claveSecreta));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException |
                 BadPaddingException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return null;
    }

    public static byte[] cifrarUsuario(Usuario usuario, SecretKey claveSecreta) {
        Cipher cipher;
        try {
            cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, claveSecreta);
            return cipher.doFinal(serializeObject(usuario));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException |
                 BadPaddingException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return null;
    }

    public static byte[] cifrarIncidencia(Incidencia incidencia, PublicKey claveServidor) {
        Cipher cipher;
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
        Cipher cipher;
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
        Signature dsa;
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

    public static String getHash(String contrasena) {
        MessageDigest md;
        try {
            // Usamos el algoritmo SHA-256 para hashea la contraseña
            md = MessageDigest.getInstance("SHA-256");
            byte[] contrasenaSegura = md.digest(contrasena.getBytes());

            // Convertimos los bytes a hexadecimal para poder pasarlo a String
            StringBuilder sb = new StringBuilder();
            for (byte b : contrasenaSegura) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString(); // Lo pasamos a String para devolverlo
        } catch (NoSuchAlgorithmException e) {
            System.out.println("No se ha podido encontrar el SHA-256");
        }
        return null;
    }

    // Metodo de la solucion del ejercicio 8 UDP
    public static byte[] serializeObject(Object obj) {
        java.io.ByteArrayOutputStream byteStream = new java.io.ByteArrayOutputStream();

        ObjectOutputStream objectStream;
        try {
            objectStream = new ObjectOutputStream(byteStream);
            objectStream.writeObject(obj);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return byteStream.toByteArray();
    }
}
