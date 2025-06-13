package com.grupo7.tesis.controller;

import com.grupo7.tesis.model.Materia;
import com.grupo7.tesis.service.lecturaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
public class lecturaController {

    @Autowired
    private lecturaService lecturaService;

    @GetMapping("/historial")
    public String mostrarFormulario(Model model) {
        model.addAttribute("materias", null);
        return "lecturaInforme";
    }

    @PostMapping("/subir-pdf")
    public String procesarPDFSubido(@RequestParam("archivo") MultipartFile archivo, Model model) {
        if (archivo.isEmpty() || !archivo.getOriginalFilename().endsWith(".pdf")) {
            model.addAttribute("error", "El archivo debe ser un PDF válido.");
            return "lecturaInforme";
        }

        List<Materia> materias = lecturaService.obtenerMateriasDesdeArchivo(archivo);
        if (materias.isEmpty()) {
            model.addAttribute("error", "No se pudieron extraer datos del PDF.");
        } else {
            model.addAttribute("materias", materias);
        }

        return "lecturaInforme";
    }

}
