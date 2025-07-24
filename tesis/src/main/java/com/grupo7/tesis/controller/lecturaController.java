package com.grupo7.tesis.controller;

import com.grupo7.tesis.model.*;
import com.grupo7.tesis.service.ProyeccionService;
import com.grupo7.tesis.service.SimulacionService;
import com.grupo7.tesis.service.lecturaService;
import com.grupo7.tesis.service.pensumService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Controller
public class lecturaController {

    @Autowired
    private lecturaService lecturaService;

    @Autowired
    private ProyeccionService proyeccionService;

    @Autowired
    private SimulacionService simulacionService;

    @Autowired
    private pensumService pensumService;

    @GetMapping("/historial")
    public String mostrarFormulario(Model model) {
        model.addAttribute("materias", null);
        return "lecturaInforme";
    }

    @PostMapping("/subir-pdf")
    public String procesarPDFSubido(@RequestParam("archivo") MultipartFile archivo, Model model) throws Exception {
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

        String textoSeguridad = lecturaService.extraerTextoDesarrolloYSeguridadBruto(archivo);
        model.addAttribute("textoSeguridad", textoSeguridad);

        List<Materia> cursosSeguridad = lecturaService.convertirTextoElectivasATabla(textoSeguridad);
        model.addAttribute("cursosSeguridad", cursosSeguridad);

        String textoComplementariaLenguas = lecturaService.extraerTextoComplementariaLenguasBruto(archivo);
        model.addAttribute("textoComplementariaLenguas", textoComplementariaLenguas);

        List<Materia> cursosComplementariaLenguas = lecturaService
                .convertirTextoElectivasATabla(textoComplementariaLenguas);
        model.addAttribute("cursosComplementariaLenguas", cursosComplementariaLenguas);

        String textoComplementariaInfo = lecturaService.extraerTextoComplementariaInformacionBruto(archivo);
        model.addAttribute("textoComplementariaInformacion", textoComplementariaInfo);

        List<Materia> cursosComplementariaInfo = lecturaService.convertirTextoElectivasATabla(textoComplementariaInfo);
        model.addAttribute("cursosComplementariaInformacion", cursosComplementariaInfo);

        String textoElectivas = lecturaService.extraerTextoElectivasBruto(archivo);
        model.addAttribute("textoElectivas", textoElectivas);

        List<Materia> tablaElectivas = lecturaService.convertirTextoElectivasATabla(textoElectivas);
        model.addAttribute("cursosElectivas", tablaElectivas);

        String textoIA = lecturaService.extraerTextoInteligenciaArtificialBruto(archivo);
        model.addAttribute("textoIA", textoIA);

        List<Materia> cursosIA = lecturaService.convertirTextoElectivasATabla(textoIA);
        model.addAttribute("cursosIA", cursosIA);

        String textoDesarrolloComputacion = lecturaService.extraerTextoDesarrolloSeguridadAComputacionBruto(archivo);
        model.addAttribute("textoDesarrolloComputacion", textoDesarrolloComputacion);

        List<Materia> tablaDesarrolloComputacion = lecturaService
                .convertirTextoElectivasATabla(textoDesarrolloComputacion);
        model.addAttribute("cursosDesarrolloComputacion", tablaDesarrolloComputacion);

        String textoDesarrolloGestion = lecturaService.extraerTextoDesarrolloYGestionBruto(archivo);
        model.addAttribute("textoDesarrolloGestion", textoDesarrolloGestion);

        List<Materia> tablaDesarrolloGestion = lecturaService.convertirTextoElectivasATabla(textoDesarrolloGestion);
        model.addAttribute("cursosDesarrolloGestion", tablaDesarrolloGestion);

        String textoComputacionVisual = lecturaService.extraerTextoComputacionVisualBruto(archivo);
        model.addAttribute("textoComputacionVisual", textoComputacionVisual);

        List<Materia> tablaComputacionVisual = lecturaService.convertirTextoElectivasATabla(textoComputacionVisual);
        model.addAttribute("cursosComputacionVisual", tablaComputacionVisual);

        String textoCVtoIA = lecturaService.extraerTextoComputacionVisualAInteligenciaArtificialBruto(archivo);
        model.addAttribute("textoCVtoIA", textoCVtoIA);

        List<Materia> tablaCVtoIA = lecturaService.convertirTextoElectivasATabla(textoCVtoIA);
        model.addAttribute("cursosCVtoIA", tablaCVtoIA);

        String textoSIGtoIA = lecturaService.extraerTextoSistemasGestionAInteligenciaArtificialBruto(archivo);
        model.addAttribute("textoSIGtoIA", textoSIGtoIA);

        List<Materia> tablaSIGtoIA = lecturaService.convertirTextoElectivasATabla(textoSIGtoIA);
        model.addAttribute("cursosSIGtoIA", tablaSIGtoIA);

        List<String> lineasRequisitosGrado = lecturaService.extraerLineasRequisitosGrado(archivo);
        model.addAttribute("lineasRequisitosGrado", lineasRequisitosGrado);

        if (materias.isEmpty()) {
            model.addAttribute("error", "No se pudieron extraer datos del PDF.");
        } else {
            model.addAttribute("materias", materias);

            // Progreso del estudiante
            Progreso progreso = lecturaService.obtenerResumenAcademico(materias, tablaElectivas,
                    cursosComplementariaLenguas, cursosComplementariaInfo, cursosEnfasis, cursosElectivaBasicas,
                    cursosSeguridad, cursosIA, tablaDesarrolloComputacion, tablaDesarrolloGestion,
                    tablaComputacionVisual, tablaCVtoIA, tablaSIGtoIA);
            model.addAttribute("promedio", progreso.getPromedio());
            model.addAttribute("materiasCursadas", progreso.getMateriasCursadas());
            model.addAttribute("materiasCursando", progreso.getTotalCursando());
            model.addAttribute("materiasFaltantes", progreso.getMateriasFaltantes());
            model.addAttribute("listaMateriasFaltantes", progreso.getListaMateriasFaltantes());
            model.addAttribute("totalMaterias", progreso.getTotalMaterias());
            model.addAttribute("totalFaltantes", progreso.getTotalFaltantes());
            model.addAttribute("porcentaje", (progreso.getCreditosPensum() * 100.0) / 138.0);
            model.addAttribute("creditosPensum", progreso.getCreditosPensum());
            model.addAttribute("creditosExtra", progreso.getCreditosExtra());
            model.addAttribute("totalCreditosCursados", progreso.getTotalCreditos());
            model.addAttribute("faltanElectiva", progreso.getFaltanElectiva());
            model.addAttribute("faltanComplementaria", progreso.getFaltanComplementaria());
            model.addAttribute("faltanEnfasis", progreso.getFaltanEnfasis());
            model.addAttribute("faltanElectivaBasicas", progreso.getFaltanElectivaBasicas());
            model.addAttribute("semestreActual", progreso.getSemestre());

            Proyeccion proyeccion = proyeccionService.generarProyeccion(
                    8,
                    20,
                    10);

            model.addAttribute("semestreProyeccion", proyeccion.getSemestre());
            model.addAttribute("creditosProyeccion", proyeccion.getCreditos());
            model.addAttribute("materiasProyeccion", proyeccion.getMaterias());
            // model.addAttribute("tipoMatriculaProyeccion", proyeccion.getTipoMatricula());
            // model.addAttribute("dobleProgramaProyeccion", proyeccion.getDoblePrograma());

            List<MateriaJson> materiasPensum = pensumService.obtenerPensum();
            model.addAttribute("materiasPensum", materiasPensum);

            /*
             * Simulacion simulacion = simulacionService.generarSimulacion(progreso,
             * proyeccion, materiasPensum);
             * model.addAttribute("simulacion", simulacion);
             */

            /*
             * Simulacion simulacionCombinatorias =
             * simulacionService.generarSimulacionCombinatorias(progreso, proyeccion,
             * materiasPensum);
             * model.addAttribute("simulacion", simulacionCombinatorias);
             */

            Map<Integer, Simulacion> simulacionMultiSemestre = simulacionService
                    .generarSimulacionMultiSemestreOptimizada(progreso, proyeccion, proyeccion.getSemestre(), materiasPensum);

            model.addAttribute("simulacion", simulacionMultiSemestre);

            int totalCreditos = 0;
            int totalMaterias = 0;

            model.addAttribute("totalCreditosMulti", totalCreditos);
            model.addAttribute("totalMateriasMulti", totalMaterias);

        }
        return "lecturaInforme";
    }
}
