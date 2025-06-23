package com.grupo7.tesis.controller;

import com.grupo7.tesis.model.*;
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

        String textoElectivaBasicas = lecturaService.extraerTextoElectivaBasicasBruto(archivo);
        model.addAttribute("textoElectivaBasicas", textoElectivaBasicas);

        List<Materia> cursosElectivaBasicas = lecturaService.convertirTextoElectivasATabla(textoElectivaBasicas);
        model.addAttribute("cursosElectivaBasicas", cursosElectivaBasicas);

        String textoEnfasis = lecturaService.extraerTextoEnfasisBruto(archivo);
        model.addAttribute("textoEnfasis", textoEnfasis);

        List<Materia> cursosEnfasis = lecturaService.convertirTextoElectivasATabla(textoEnfasis);
        model.addAttribute("cursosEnfasis", cursosEnfasis);

        String textoComplementariaLenguas = lecturaService.extraerTextoComplementariaLenguasBruto(archivo);
        model.addAttribute("textoComplementariaLenguas", textoComplementariaLenguas);

        List<Materia> cursosComplementariaLenguas = lecturaService.convertirTextoElectivasATabla(textoComplementariaLenguas);
        model.addAttribute("cursosComplementariaLenguas", cursosComplementariaLenguas);

        String textoComplementariaInfo = lecturaService.extraerTextoComplementariaInformacionBruto(archivo);
        model.addAttribute("textoComplementariaInformacion", textoComplementariaInfo);

        List<Materia> cursosComplementariaInfo = lecturaService.convertirTextoElectivasATabla(textoComplementariaInfo);
        model.addAttribute("cursosComplementariaInformacion", cursosComplementariaInfo);

        String textoElectivas = lecturaService.extraerTextoElectivasBruto(archivo);
        model.addAttribute("textoElectivas", textoElectivas);

        List<Materia> tablaElectivas = lecturaService.convertirTextoElectivasATabla(textoElectivas);
        model.addAttribute("cursosElectivas", tablaElectivas);

        if (materias.isEmpty()) {
            model.addAttribute("error", "No se pudieron extraer datos del PDF.");
        } else {
            model.addAttribute("materias", materias);

            //Progreso del estudiante
            Progreso progreso = lecturaService.obtenerResumenAcademico(materias, tablaElectivas, cursosComplementariaLenguas, cursosComplementariaInfo, cursosEnfasis, cursosElectivaBasicas);
            model.addAttribute("promedio", progreso.getPromedio());
            model.addAttribute("materiasCursadas", progreso.getMateriasCursadas());
            model.addAttribute("materiasCursando", progreso.getTotalCursando());
            model.addAttribute("materiasFaltantes", progreso.getMateriasFaltantes());
            model.addAttribute("listaMateriasFaltantes", progreso.getListaMateriasFaltantes());
            model.addAttribute("totalMaterias", progreso.getTotalMaterias());
            model.addAttribute("totalFaltantes", progreso.getTotalFaltantes());
            model.addAttribute("porcentaje", (progreso.getTotalCreditos() * 100.0) / 138.0);
            model.addAttribute("faltanElectiva", progreso.getFaltanElectiva());
            model.addAttribute("faltanComplementaria", progreso.getFaltanComplementaria());
            model.addAttribute("faltanEnfasis", progreso.getFaltanEnfasis());
            model.addAttribute("faltanElectivaBasicas", progreso.getFaltanElectivaBasicas());
        }

        return "lecturaInforme";
    }

}
