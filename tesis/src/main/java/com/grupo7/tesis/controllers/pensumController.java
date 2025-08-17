package com.grupo7.tesis.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grupo7.tesis.models.Materia;
import com.grupo7.tesis.services.pensumService;

@RestController
@RequestMapping ("/api/pensum")
public class pensumController {

    @Autowired
    private pensumService pensumService;

    @GetMapping("/pensum")
    public String mostrarPensum(Model model) throws Exception {
        Map<Integer, List<Materia>> materiasPorSemestre = pensumService.obtenerMateriasPorSemestre();

        // Serializar requisitos a JSON
        ObjectMapper mapper = new ObjectMapper();
        materiasPorSemestre.values().forEach(lista -> {
            for (Materia materia : lista) {
                try {
                    materia.setRequisitosJson(mapper.writeValueAsString(materia.getRequisitos()));
                } catch (Exception e) {
                    materia.setRequisitosJson("[]");
                }
            }
        });

        // Calcular conexiones y pasarlas como JSON al modelo
        List<Map<String, String>> conexiones = pensumService.calcularConexionesValidas(materiasPorSemestre);
        model.addAttribute("materiasPorSemestre", materiasPorSemestre);
        model.addAttribute("conexiones", mapper.writeValueAsString(conexiones));
        // conexiones.forEach(c -> System.out.println(c.get("origen") + " -> " +
        // c.get("destino")));
        return "pensum";
    }

    @GetMapping
    @ResponseBody
    public List<Materia> obtenerPensumJson() throws Exception {
        return pensumService.obtenerPensum();
    }

}
