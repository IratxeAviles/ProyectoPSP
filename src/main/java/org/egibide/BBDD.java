package org.egibide;

import java.util.ArrayList;
import java.util.List;

public class BBDD {

    private List<Incidencia> incidencias = new ArrayList();

    public synchronized void guardarIncidencia(Incidencia incidencia) {
        incidencia.setCodigo(incidencias.size() + 1);
        incidencias.add(incidencia);
        System.out.println("Incidencia guardada con código " + incidencias.size());
    }
}
