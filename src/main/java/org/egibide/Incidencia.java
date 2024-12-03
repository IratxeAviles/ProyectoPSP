package org.egibide;

import java.io.Serializable;

public class Incidencia implements Serializable {
    private int id;
    private String descripcion;
    private String lugar;
    private String empleado;
    private Prioridad prioridad;

    public Incidencia(String descripcion, String lugar, String empleado) {
        this.descripcion = descripcion;
        this.lugar = lugar;
        this.empleado = empleado;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getLugar() {
        return lugar;
    }

    public void setLugar(String lugar) {
        this.lugar = lugar;
    }

    public String getEmpleado() {
        return empleado;
    }

    public void setEmpleado(String empleado) {
        this.empleado = empleado;
    }

    public Prioridad getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(Prioridad prioridad) {
        this.prioridad = prioridad;
    }

    @Override
    public String toString() {
        return "Incidencia{" +
                "descripcion='" + descripcion + '\'' +
                ", lugar='" + lugar + '\'' +
                ", empleado='" + empleado + '\'' +
                '}';
    }
}
