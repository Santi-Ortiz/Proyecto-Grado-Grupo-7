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

    public String recomendarMaterias(String intereses, Integer creditos) {
        int c = creditos != null ? creditos : 0;
        return ragClient.queryMaterias(intereses, c);
    }
}