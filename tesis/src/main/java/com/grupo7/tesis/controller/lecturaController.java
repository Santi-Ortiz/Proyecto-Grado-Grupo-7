package com.grupo7.tesis.controller;

import com.grupo7.tesis.model.*;
import com.grupo7.tesis.service.ProyeccionService;
import com.grupo7.tesis.service.SimulacionService;
import com.grupo7.tesis.service.lecturaService;
import com.grupo7.tesis.service.pensumService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

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
    @ResponseBody
    public Progreso procesarPDFSubido(@RequestParam("archivo") MultipartFile archivo) {
        if (archivo.isEmpty() || !archivo.getOriginalFilename().endsWith(".pdf")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El archivo debe ser un PDF válido.");
        }

        List<Materia> materias = lecturaService.obtenerMateriasDesdeArchivo(archivo);

        List<Materia> cursosElectivaBasicas = lecturaService.convertirTextoElectivasATabla(
                lecturaService.extraerTextoElectivaBasicasBruto(archivo));

        List<Materia> cursosEnfasis = lecturaService.convertirTextoElectivasATabla(
                lecturaService.extraerTextoEnfasisBruto(archivo));

        List<Materia> cursosSeguridad = lecturaService.convertirTextoElectivasATabla(
                lecturaService.extraerTextoDesarrolloYSeguridadBruto(archivo));

        List<Materia> cursosComplementariaLenguas = lecturaService.convertirTextoElectivasATabla(
                lecturaService.extraerTextoComplementariaLenguasBruto(archivo));

        List<Materia> cursosComplementariaInfo = lecturaService.convertirTextoElectivasATabla(
                lecturaService.extraerTextoComplementariaInformacionBruto(archivo));

        List<Materia> tablaElectivas = lecturaService.convertirTextoElectivasATabla(
                lecturaService.extraerTextoElectivasBruto(archivo));

        List<Materia> cursosIA = lecturaService.convertirTextoElectivasATabla(
                lecturaService.extraerTextoInteligenciaArtificialBruto(archivo));

        List<Materia> tablaDesarrolloComputacion = lecturaService.convertirTextoElectivasATabla(
                lecturaService.extraerTextoDesarrolloSeguridadAComputacionBruto(archivo));

        List<Materia> tablaDesarrolloGestion = lecturaService.convertirTextoElectivasATabla(
                lecturaService.extraerTextoDesarrolloYGestionBruto(archivo));

        List<Materia> tablaComputacionVisual = lecturaService.convertirTextoElectivasATabla(
                lecturaService.extraerTextoComputacionVisualBruto(archivo));

        List<Materia> tablaCVtoIA = lecturaService.convertirTextoElectivasATabla(
                lecturaService.extraerTextoComputacionVisualAInteligenciaArtificialBruto(archivo));

        List<Materia> tablaSIGtoIA = lecturaService.convertirTextoElectivasATabla(
                lecturaService.extraerTextoSistemasGestionAInteligenciaArtificialBruto(archivo));

        List<String> lineasRequisitosGrado = lecturaService.extraerLineasRequisitosGrado(archivo);

        Progreso progreso = lecturaService.obtenerResumenAcademico(
                materias,
                tablaElectivas,
                cursosComplementariaLenguas,
                cursosComplementariaInfo,
                cursosEnfasis,
                cursosElectivaBasicas,
                cursosSeguridad,
                cursosIA,
                tablaDesarrolloComputacion,
                tablaDesarrolloGestion,
                tablaComputacionVisual,
                tablaCVtoIA,
                tablaSIGtoIA);

        progreso.setMaterias(materias);
        progreso.setLineasRequisitosGrado(lineasRequisitosGrado);

        double porcentaje = (progreso.getCreditosPensum() * 100.0) / 138.0;
        progreso.setPorcentaje(porcentaje);

        /*
         * Proyeccion proyeccion = proyeccionService.generarProyeccion(
         * 8,
         * 20,
         * 10,
         * 1,
         * 0);
         * 
         * model.addAttribute("semestreProyeccion", proyeccion.getSemestre());
         * model.addAttribute("creditosProyeccion", proyeccion.getCreditos());
         * model.addAttribute("materiasProyeccion", proyeccion.getMaterias());
         * model.addAttribute("tipoMatriculaProyeccion", proyeccion.getTipoMatricula());
         * model.addAttribute("dobleProgramaProyeccion", proyeccion.getDoblePrograma());
         * 
         * List<MateriaJson> materiasPensum = pensumService.obtenerPensum();
         * model.addAttribute("materiasPensum", materiasPensum);
         * 
         * Simulacion simulacion = simulacionService.generarSimulacion(progreso,
         * proyeccion, materiasPensum);
         * model.addAttribute("simulacion", simulacion);
         */

        return progreso;
    }

}
