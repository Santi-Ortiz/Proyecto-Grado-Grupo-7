package com.grupo7.tesis.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.stereotype.Controller;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grupo7.tesis.model.MateriaJson;
import com.grupo7.tesis.service.pensumService;

@Controller
public class pensumController {

    @Autowired
    private pensumService pensumService;

    @GetMapping("/pensum")
    public String mostrarPensum(Model model) throws Exception {
        Map<Integer, List<MateriaJson>> materiasPorSemestre = pensumService.obtenerMateriasPorSemestre();

        // Serializar requisitos a JSON
        ObjectMapper mapper = new ObjectMapper();
        materiasPorSemestre.values().forEach(lista -> {
            for (MateriaJson materia : lista) {
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
        //conexiones.forEach(c -> System.out.println(c.get("origen") + " -> " + c.get("destino")));
        return "pensum";
    }


    @GetMapping("/api/pensum")
    @ResponseBody
    public List<MateriaJson> obtenerPensumJson() throws Exception {
        return pensumService.obtenerPensum(); 
    }
    
}
