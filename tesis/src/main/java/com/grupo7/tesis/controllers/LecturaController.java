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
        public ProgresoDTO guardarInformeAvance(@RequestParam("archivo") MultipartFile archivo) {
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

                return procesarPDFSubido(archivo);

        } catch (IOException e) {
                throw new RuntimeException("Error al guardar el archivo", e);
        }
        }

        @PostMapping("/subir-pdf")
        @ResponseBody
        public ProgresoDTO procesarPDFSubido(@RequestParam("archivo") MultipartFile archivo) {
                if (archivo.isEmpty() || !archivo.getOriginalFilename().endsWith(".pdf")) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El archivo debe ser un PDF válido.");
                }

                List<MateriaDTO> materias = lecturaService.obtenerMateriasDesdeArchivo(archivo);

                if (materias == null || materias.isEmpty()) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                                "El archivo no corresponde a un informe de avance válido.");
                }

                List<MateriaDTO> cursosElectivaBasicas = lecturaService.convertirTextoElectivasATabla(
                                lecturaService.extraerTextoElectivaBasicasBruto(archivo));

                List<MateriaDTO> cursosEnfasis = lecturaService.convertirTextoElectivasATabla(
                                lecturaService.extraerTextoEnfasisBruto(archivo));

                List<MateriaDTO> cursosSeguridad = lecturaService.convertirTextoElectivasATabla(
                                lecturaService.extraerTextoDesarrolloYSeguridadBruto(archivo));

                List<MateriaDTO> cursosComplementariaLenguas = lecturaService.convertirTextoElectivasATabla(
                                lecturaService.extraerTextoComplementariaLenguasBruto(archivo));

                List<MateriaDTO> cursosComplementariaInfo = lecturaService.convertirTextoElectivasATabla(
                                lecturaService.extraerTextoComplementariaInformacionBruto(archivo));

                List<MateriaDTO> tablaElectivas = lecturaService.convertirTextoElectivasATabla(
                                lecturaService.extraerTextoElectivasBruto(archivo));

                List<MateriaDTO> cursosIA = lecturaService.convertirTextoElectivasATabla(
                                lecturaService.extraerTextoInteligenciaArtificialBruto(archivo));

                List<MateriaDTO> tablaDesarrolloComputacion = lecturaService.convertirTextoElectivasATabla(
                                lecturaService.extraerTextoDesarrolloSeguridadAComputacionBruto(archivo));

                List<MateriaDTO> tablaDesarrolloGestion = lecturaService.convertirTextoElectivasATabla(
                                lecturaService.extraerTextoDesarrolloYGestionBruto(archivo));

                List<MateriaDTO> tablaComputacionVisual = lecturaService.convertirTextoElectivasATabla(
                                lecturaService.extraerTextoComputacionVisualBruto(archivo));

                List<MateriaDTO> tablaCVtoIA = lecturaService.convertirTextoElectivasATabla(
                                lecturaService.extraerTextoComputacionVisualAInteligenciaArtificialBruto(archivo));

                List<MateriaDTO> tablaSIGtoIA = lecturaService.convertirTextoElectivasATabla(
                                lecturaService.extraerTextoSistemasGestionAInteligenciaArtificialBruto(archivo));

                List<String> lineasRequisitosGrado = lecturaService.extraerLineasRequisitosGrado(archivo);

                Progreso progreso = new Progreso();
                progreso.setMaterias(materias);
                progreso.setCursosElectivas(tablaElectivas);
                progreso.setCursosEnfasis(cursosEnfasis);
                progreso.setCursosComplementariaLenguas(cursosComplementariaLenguas);
                progreso.setCursosComplementariaInformacion(cursosComplementariaInfo);
                progreso.setCursosIA(cursosIA);
                progreso.setCursosDesarrolloComputacion(tablaDesarrolloComputacion);
                progreso.setCursosDesarrolloGestion(tablaDesarrolloGestion);
                progreso.setCursosComputacionVisual(tablaComputacionVisual);
                progreso.setCursosCVtoIA(tablaCVtoIA);
                progreso.setCursosSIGtoIA(tablaSIGtoIA);
                progreso.setCursosElectivaBasicas(cursosElectivaBasicas);
                progreso.setCursosSeguridad(cursosSeguridad);
                progreso.setLineasRequisitosGrado(lineasRequisitosGrado);
                progreso.setMateriasFaltantes();

                ProgresoDTO progresoDTO = new ProgresoDTO(progreso);

                return progresoDTO;
        }
        

}
