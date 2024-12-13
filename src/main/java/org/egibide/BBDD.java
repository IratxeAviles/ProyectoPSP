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

    // Se guarda el usuario desde el hilo
    public synchronized void guardarUsuario(Usuario usuario) {
        usuarios.put(usuario.getUsuario(), usuario.getContrasena());
        System.out.println("Un nuevo usuario ha sido registrado: " + usuario.getUsuario());
    }

    public boolean comprobarLogin(Usuario usuario) { // Para comprobar si el usuario y la contraseña es correcta
        if (usuarios.containsKey(usuario.getUsuario())) {
            // Hasheamos la contraseña ingresada y la comparamos con la almacenada
            String contrasenaHasheada = getHash(usuario.getContrasena());
            return contrasenaHasheada.equals(usuarios.get(usuario.getUsuario()));
        } else {
            return false;
        }
    }

    public static String getHash(String contrasena) {
        MessageDigest md = null;
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
}
