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

    public String recomendarMaterias(String intereses) {
        return ragClient.queryMaterias(intereses);
    }
}

/*package com.grupo7.tesis.service;

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
}*/