package com.grupo7.tesis.services;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.grupo7.tesis.dtos.MateriaDTO;
import com.grupo7.tesis.models.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class LecturaService {

    @Autowired
    private ProgresoService progresoService;

    @Autowired
    private InformeAvanceService informeAvanceService;
    
    public InformeAvance guardarInformeAvance(byte[]  archivo, Estudiante estudiante, Pensum pensum) throws IOException {
        //LocalDate fecha = extraerFecha(archivo);
        LocalDate fecha = LocalDate.now();
        InformeAvance informeAvance = new InformeAvance();
        String nombreArchivo = "informeAvance_" + estudiante.getCodigo() + "_" + fecha + ".pdf";

        informeAvance.setNombreArchivo(nombreArchivo);
        informeAvance.setArchivo(archivo);
        informeAvance.setFechaPublicacion(fecha);
        informeAvance.setPensum(pensum);
        informeAvance.setEstudiante(estudiante);

        informeAvanceService.crearInformeAvance(informeAvance);

        /*String textoCompleto = lector.getText(documento);
        
        Pattern pattern = Pattern.compile("expedido el (\\d{1,2}/\\d{1,2}/\\d{4})");
        Matcher matcher = pattern.matcher(textoCompleto);
        
        if (matcher.find()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            return LocalDate.parse(matcher.group(1), formatter);
        }
        return null;*/
        return informeAvance;
    }

    public Progreso obtenerResumenAcademico(List<MateriaDTO> materias, List<MateriaDTO> cursosElectivas,
            List<MateriaDTO> cursosComplementariaLenguas, List<MateriaDTO> cursosComplementariaInformacion,
            List<MateriaDTO> cursosEnfasis, List<MateriaDTO> cursosElectivaBasicas, List<MateriaDTO> cursosSeguridad,
            List<MateriaDTO> cursosIA, List<MateriaDTO> tablaDesarrolloComputacion,
            List<MateriaDTO> tablaDesarrolloGestion,
            List<MateriaDTO> tablaComputacionVisual, List<MateriaDTO> tablaCVtoIA, List<MateriaDTO> tablaSIGtoIA) {
        return progresoService.obtenerResumenAcademico(materias, cursosElectivas, cursosComplementariaLenguas,
                cursosComplementariaInformacion, cursosEnfasis, cursosElectivaBasicas, cursosSeguridad, cursosIA,
                tablaDesarrolloComputacion, tablaDesarrolloGestion, tablaComputacionVisual, tablaCVtoIA, tablaSIGtoIA);
    }

    // Método genérico para extraer texto de PDF desde byte[] 
    private String extraerTexto(byte[] archivo) {
        if (archivo == null) return "";

        try (PDDocument documento = PDDocument.load(archivo)) {
            PDFTextStripper lector = new PDFTextStripper();
            return lector.getText(documento);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    // Método genérico para extraer bloques de texto por claves 
    private String extraerBloque(byte[] archivoBytes, String inicioClave, String finClave) {
        if (archivoBytes == null || archivoBytes.length == 0) {
            return "";
        }

        if (inicioClave == null) {
            System.out.println("⚠ inicioClave es null, no se puede buscar bloque");
            return "";
        }

        String texto = extraerTexto(archivoBytes);
        if (texto == null || texto.isEmpty()) {
            return "";
        }

        System.out.println("Texto extraído del PDF: LENGTH=" + texto.length());

        int inicio = texto.indexOf(inicioClave);
        if (inicio == -1) {
            return "";
        }

        int fin;
        if (finClave == null) {
            fin = texto.length(); // si no hay finClave, tomar hasta el final
        } else {
            fin = texto.indexOf(finClave, inicio);
            if (fin == -1) {
                fin = texto.length(); // si no encuentra finClave, también tomar hasta el final
            }
        }

        if (fin <= inicio) {
            return "";
        }

        String bloque = texto.substring(inicio, fin).trim();
        String[] lineas = bloque.split("\\r?\\n");

        StringBuilder resultado = new StringBuilder();
        boolean tablaComenzada = false;

        for (String linea : lineas) {
            if (linea == null) continue;

            String l = linea.trim();

            if (l.equalsIgnoreCase(inicioClave) && resultado.length() == 0) {
                resultado.append(inicioClave).append("\n");
            }

            if (l.startsWith("Ciclo Lectivo")) {
                resultado.append(l).append("\n");
                tablaComenzada = true;
                continue;
            }

            if (tablaComenzada) {
                if (l.isEmpty() || l.toLowerCase().contains("ajuste") || l.toLowerCase().contains("entered by")) {
                    break;
                }
                resultado.append(l).append("\n");
            }
        }

        return resultado.toString().trim();
    }

    // Todos los métodos de extracción ahora usan el bloque genérico 
    public List<MateriaDTO> obtenerMateriasDesdeArchivo() {
        byte[] archivo = informeAvanceService.obtenerArchivoInformeEstudianteAutenticado();
        String bloque = extraerBloque(archivo, "Historial de Cursos", null); // null indica hasta el final

        if (bloque == null || bloque.isEmpty()) {
            return Collections.emptyList();
        }

        List<MateriaDTO> materias = new ArrayList<>();
        String[] lineas = bloque.split("\\r?\\n");

        Pattern patronFinal = Pattern.compile("(.+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)$");

        for (String linea : lineas) {
            String l = linea.trim();
            if (l.isEmpty() || l.contains("Ciclo Lectivo")) continue;

            Matcher matcher = patronFinal.matcher(l);
            if (matcher.find()) {
                String calif = matcher.group(2).trim();
                String cred = matcher.group(3).trim();
                String tipo = matcher.group(4).trim();

                String[] tokens = matcher.group(1).trim().split("\\s+");

                if (tokens.length >= 4) {
                    String ciclo = tokens[0];
                    String materiaCod = tokens[1];
                    String nCat = tokens[2];
                    String cursoCod = tokens[3];

                    StringBuilder tituloBuilder = new StringBuilder();
                    for (int i = 4; i < tokens.length; i++) {
                        tituloBuilder.append(tokens[i]).append(" ");
                    }
                    String tituloCurso = tituloBuilder.toString().trim();

                    // Manejo de calificación no numérica
                    if (!calif.matches("[0-9]+(\\.[0-9]{1,2})?|[A-ZÑ]")) {
                        tituloCurso += " " + calif;
                        calif = "SIN CALIFICACIÓN";
                    }

                    materias.add(new MateriaDTO(ciclo, materiaCod, nCat, cursoCod, tituloCurso.trim(), calif, cred, tipo));
                }
            }
        }

        return materias;
    }

    public String extraerTextoElectivaBasicas() {
        return extraerBloque(informeAvanceService.obtenerArchivoInformeEstudianteAutenticado(),
                "Electiva de Cs. Básicas", "Fundamentos de Ciencias de la Computación");
    }

    public String extraerTextoEnfasis() {
        return extraerBloque(informeAvanceService.obtenerArchivoInformeEstudianteAutenticado(),
                "Énfasis y Complementarias", "Complementaria Lenguas");
    }

    public String extraerTextoDesarrolloYSeguridad() {
        return extraerBloque(informeAvanceService.obtenerArchivoInformeEstudianteAutenticado(),
                "Desarrollo avanzado de SW y Seguridad digital", "Inteligencia artificial y ciencia de datos");
    }

    public String extraerTextoDesarrolloSeguridadAComputacion() {
        return extraerBloque(informeAvanceService.obtenerArchivoInformeEstudianteAutenticado(),
                "Desarrollo avanzado de SW y Seguridad digital", "Computación Visual");
    }

    public String extraerTextoDesarrolloYGestion() {
        return extraerBloque(informeAvanceService.obtenerArchivoInformeEstudianteAutenticado(),
                "Desarrollo avanzado de SW y Seguridad digital", "Sistemas de Información Y Gestión");
    }

    public String extraerTextoComputacionVisual() {
        return extraerBloque(informeAvanceService.obtenerArchivoInformeEstudianteAutenticado(),
                "Computación Visual", "Sistemas de Información Y Gestión");
    }

    public String extraerTextoComputacionVisualAInteligenciaArtificial() {
        return extraerBloque(informeAvanceService.obtenerArchivoInformeEstudianteAutenticado(),
                "Computación Visual", "Inteligencia artificial y ciencia de datos");
    }

    public String extraerTextoSistemasGestionAInteligenciaArtificial() {
        return extraerBloque(informeAvanceService.obtenerArchivoInformeEstudianteAutenticado(),
                "Sistemas de Información Y Gestión", "Inteligencia artificial y ciencia de datos");
    }

    public String extraerTextoComplementariaLenguas() {
        return extraerBloque(informeAvanceService.obtenerArchivoInformeEstudianteAutenticado(),
                "Complementaria Lenguas", "Electivas Universidad");
    }

    public String extraerTextoComplementariaInformacion() {
        return extraerBloque(informeAvanceService.obtenerArchivoInformeEstudianteAutenticado(),
                "Complementaria Información", "Complementaria Lenguas");
    }

    public String extraerTextoElectivas() {
        return extraerBloque(informeAvanceService.obtenerArchivoInformeEstudianteAutenticado(),
                "Electivas Universidad", "Requisitos de grado");
    }

    public String extraerTextoInteligenciaArtificial() {
        return extraerBloque(informeAvanceService.obtenerArchivoInformeEstudianteAutenticado(),
                "Inteligencia artificial y ciencia de datos", "Práctica profesional Sistemas");
    }
    public List<MateriaDTO> convertirTextoElectivasATabla(String texto) {
        List<MateriaDTO> lista = new ArrayList<>();

        if (texto == null || texto.isEmpty())
            return lista;

        String[] lineas = texto.split("\\n");

        boolean tablaComenzada = false;

        for (String linea : lineas) {
            String l = linea.trim();

            if (l.startsWith("Ciclo Lectivo")) {
                tablaComenzada = true;
                continue;
            }

            if (tablaComenzada && !l.isEmpty()) {
                String[] tokens = l.split("\\s+");

                if (tokens.length >= 8) {
                    String ciclo = tokens[0];
                    String materiaCod = tokens[1];
                    String nCat = tokens[2];
                    String cursoCod = tokens[3];

                    StringBuilder tituloBuilder = new StringBuilder();
                    for (int i = 4; i < tokens.length - 3; i++) {
                        tituloBuilder.append(tokens[i]).append(" ");
                    }

                    String titulo = tituloBuilder.toString().trim();
                    String calif = tokens[tokens.length - 3];
                    String cred = tokens[tokens.length - 2];
                    String tipo = tokens[tokens.length - 1];

                    lista.add(new MateriaDTO(ciclo, materiaCod, nCat, cursoCod, titulo, calif, cred, tipo));
                }
            }
        }

        return lista;
    }

    public List<String> extraerLineasRequisitosGrado() {
        byte[] archivo = informeAvanceService.obtenerArchivoInformeEstudianteAutenticado();
        String bloque = extraerBloque(archivo, "Requisitos de grado", "Historial de Cursos");

        if (bloque == null || bloque.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> lineas = new ArrayList<>();
        String[] todasLineas = bloque.split("\\r?\\n");

        boolean inicioEncontrado = false;
        for (int i = 0; i < todasLineas.length - 1; i++) {
            String lineaActual = todasLineas[i].trim();
            String lineaSiguiente = todasLineas[i + 1].trim();

            if (!inicioEncontrado && lineaActual.equalsIgnoreCase("Requisitos de grado")) {
                if (!lineaSiguiente.isEmpty()) {
                    lineas.add(lineaSiguiente);
                }
                inicioEncontrado = true;
                i++;
                continue;
            }

            if (lineaActual.equalsIgnoreCase("Requisito de Lengua Extranjera B2") ||
                    lineaActual.equalsIgnoreCase("Prueba SABER-PRO")) {

                if (lineaSiguiente.toLowerCase().startsWith("satisfecho") ||
                        lineaSiguiente.toLowerCase().startsWith("no satisfecho")) {
                    lineas.add(lineaActual + " / " + lineaSiguiente);
                    i++;
                } else {
                    lineas.add(lineaActual);
                }
            }
        }

        return lineas;
    }

}
