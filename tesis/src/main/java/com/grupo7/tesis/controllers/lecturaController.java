package com.grupo7.tesis.controllers;

import com.grupo7.tesis.dtos.*;
import com.grupo7.tesis.models.*;
import com.grupo7.tesis.services.lecturaService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
public class lecturaController {

        @Autowired
        private lecturaService lecturaService;

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

                List<MateriaDTO> materias = lecturaService.obtenerMateriasDesdeArchivo(archivo);

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
