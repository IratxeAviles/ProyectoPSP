package org.egibide;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BBDD {

    private List<Incidencia> incidencias = new ArrayList<>();
    private static HashMap<String, String> usuarios = new HashMap<>();

    // Se guarda la incidencia desde el hilo y se saca por consola el código que se le ha dado
    public synchronized void guardarIncidencia(Incidencia incidencia) {
        incidencia.setCodigo(incidencias.size() + 1);
        incidencias.add(incidencia);
        System.out.println("Incidencia guardada con código " + incidencias.size());
    }

    // Función para realizar el hash de una contraseña usando SHA-256 (Codigo de la solución compartida del ejercicio 3)
    public synchronized String hashContrasena(String contrasena) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(contrasena.getBytes());

            // Convertir el hash a una cadena hexadecimal
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Error " + e.getMessage());
            return null;
        }
    }

    // Función para registrar un nuevo usuario (Codigo de la solución compartida del ejercicio 3)
    public synchronized boolean registrarUsuario(String nombreUsuario, String contrasena) {
        // Hasheamos la contraseña antes de almacenarla
        String contrasenaHasheada = hashContrasena(contrasena);
        usuarios.put(nombreUsuario, contrasenaHasheada);
        System.out.println("Un nuevo usuario ha sido registrado: " + nombreUsuario);

        return usuarios.containsKey(nombreUsuario); // Devolvemos si se ha registrado o no el usuario
    }

    // Función para iniciar sesión verificando la contraseña (Codigo de la solución compartida del ejercicio 3)
    public synchronized boolean iniciarSesion(String nombreUsuario, String contrasena) {
        // Comprobamos si el usuario existe
        if (usuarios.containsKey(nombreUsuario)) {
            // Hasheamos la contrasena ingresada y la comparamos con la almacenada
            String contrasenaHasheada = hashContrasena(contrasena);
            //Accedemos a la informacion almacenada de ese usuario, es decir, el resumen de la contrasena
            System.out.println(usuarios.get(nombreUsuario));
            return contrasenaHasheada.equals(usuarios.get(nombreUsuario));
        } else {
            System.out.println("Usuario no encontrado.");
            return false;
        }
    }
}
