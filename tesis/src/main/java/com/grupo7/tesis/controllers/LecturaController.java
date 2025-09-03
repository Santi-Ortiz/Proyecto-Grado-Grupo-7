package com.grupo7.tesis.controllers;

import com.grupo7.tesis.dtos.*;
import com.grupo7.tesis.models.*;
import com.grupo7.tesis.services.LecturaService;
import com.grupo7.tesis.services.EstudianteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.io.IOException;

@RestController
public class LecturaController {

        @Autowired
        private LecturaService lecturaService;
        @Autowired
        private EstudianteService estudianteService;

        @GetMapping("/historial")
        public String mostrarFormulario(Model model) {
                model.addAttribute("materias", null);
                return "lecturaInforme";
        }

        @PostMapping("/guardarInforme")
        @ResponseBody
        public Progreso guardarInformeAvance(@RequestParam("archivo") MultipartFile archivo) {
        if (archivo.isEmpty() || !archivo.getOriginalFilename().endsWith(".pdf")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El archivo debe ser un PDF válido.");
        }
        try {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String correo = authentication.getName();

                Estudiante estudiante = estudianteService.obtenerEstudiantePorCorreo(correo);
                if (estudiante == null) {
                throw new RuntimeException("Estudiante autenticado no encontrado");
                }

                Pensum pensum = estudiante.getPensum();
                if (pensum == null) {
                throw new RuntimeException("El estudiante no tiene un pensum asociado");
                }

                byte[] archivoBytes = archivo.getBytes();
                lecturaService.guardarInformeAvance(archivoBytes, estudiante, pensum);

                return procesarPDFSubido();

        } catch (IOException e) {
                throw new RuntimeException("Error al guardar el archivo", e);
        }
        }

        @PostMapping("/subir-pdf")
        @ResponseBody
        public Progreso procesarPDFSubido() {

                List<MateriaDTO> materias = lecturaService.obtenerMateriasDesdeArchivo();
                
                List<MateriaDTO> cursosElectivaBasicas = lecturaService.convertirTextoElectivasATabla(
                                lecturaService.extraerTextoElectivaBasicas());

                List<MateriaDTO> cursosEnfasis = lecturaService.convertirTextoElectivasATabla(
                                lecturaService.extraerTextoEnfasis());

                List<MateriaDTO> cursosSeguridad = lecturaService.convertirTextoElectivasATabla(
                                lecturaService.extraerTextoDesarrolloYSeguridad());

                List<MateriaDTO> cursosComplementariaLenguas = lecturaService.convertirTextoElectivasATabla(
                                lecturaService.extraerTextoComplementariaLenguas());

                List<MateriaDTO> cursosComplementariaInfo = lecturaService.convertirTextoElectivasATabla(
                                lecturaService.extraerTextoComplementariaInformacion());

                List<MateriaDTO> tablaElectivas = lecturaService.convertirTextoElectivasATabla(
                                lecturaService.extraerTextoElectivas());

                List<MateriaDTO> cursosIA = lecturaService.convertirTextoElectivasATabla(
                                lecturaService.extraerTextoInteligenciaArtificial());

                List<MateriaDTO> tablaDesarrolloComputacion = lecturaService.convertirTextoElectivasATabla(
                                lecturaService.extraerTextoDesarrolloSeguridadAComputacion());

                List<MateriaDTO> tablaDesarrolloGestion = lecturaService.convertirTextoElectivasATabla(
                                lecturaService.extraerTextoDesarrolloYGestion());

                List<MateriaDTO> tablaComputacionVisual = lecturaService.convertirTextoElectivasATabla(
                                lecturaService.extraerTextoComputacionVisual());

                List<MateriaDTO> tablaCVtoIA = lecturaService.convertirTextoElectivasATabla(
                                lecturaService.extraerTextoComputacionVisualAInteligenciaArtificial());

                List<MateriaDTO> tablaSIGtoIA = lecturaService.convertirTextoElectivasATabla(
                                lecturaService.extraerTextoSistemasGestionAInteligenciaArtificial());

                List<String> lineasRequisitosGrado = lecturaService.extraerLineasRequisitosGrado();

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

                return progreso;
        }
        

}
