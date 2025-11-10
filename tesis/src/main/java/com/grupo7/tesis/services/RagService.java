package com.grupo7.tesis.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RagService {

    private final RagCliente ragClient;

    @Autowired
    public RagService(RagCliente ragClient) {
        this.ragClient = ragClient;
    }

    public String obtenerRespuestaRag(String pregunta) {
        return ragClient.queryRag(pregunta);
    }

    public String recomendarMaterias(String intereses,
                                     Object creditos,
                                     Integer creditosMin,
                                     Integer creditosMax,
                                     String tipo) {
        return ragClient.queryMaterias(intereses, creditos, creditosMin, creditosMax, tipo);
    }

    public String recomendarMaterias(String intereses, Object creditos, String tipo) {
        return ragClient.queryMaterias(intereses, creditos, null, null, tipo);
    }
}
