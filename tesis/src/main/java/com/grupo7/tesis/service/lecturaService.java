package com.grupo7.tesis.service;

import com.grupo7.tesis.model.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class lecturaService {

    @Autowired
    private ProgresoService progresoService;

    public List<Materia> obtenerMateriasDesdeArchivo(MultipartFile archivo) {
        List<Materia> materias = new ArrayList<>();
        String titulo = "Historial de Cursos";

        try (PDDocument documento = PDDocument.load(archivo.getInputStream())) {
            PDFTextStripper lector = new PDFTextStripper();
            String textoCompleto = lector.getText(documento);
            int indice = textoCompleto.indexOf(titulo);

            if (indice != -1) {
                String textoDeseado = textoCompleto.substring(indice + titulo.length()).trim();
                String[] lineas = textoDeseado.split("\n");

                Pattern patronFinal = Pattern.compile("(.+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)$");

                for (String linea : lineas) {
                    if (linea.trim().isEmpty() || linea.contains("Ciclo Lectivo")) continue;

                    Matcher matcher = patronFinal.matcher(linea);
                    if (matcher.find()) {
                        //String posibleFinal = matcher.group(0);
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

                            if (!calif.matches("[0-9]+(\\.[0-9]{1,2})?|[A-ZÑ]")) {
                                tituloCurso += " " + calif;
                                calif = "SIN CALIFICACIÓN";
                            }

                            materias.add(new Materia(ciclo, materiaCod, nCat, cursoCod, tituloCurso.trim(), calif, cred, tipo));
                        }
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return materias;
    }

    public Progreso obtenerResumenAcademico(List<Materia> materias) {
        return progresoService.obtenerResumenAcademico(materias);
    }

    public String extraerTextoElectivaBasicasBruto(MultipartFile archivo) {
        try (PDDocument documento = PDDocument.load(archivo.getInputStream())) {
            PDFTextStripper lector = new PDFTextStripper();
            String texto = lector.getText(documento);

            String inicioClave = "Electiva de Cs. Básicas";
            String finClave = "Fundamentos de Ciencias de la Computación";

            int inicio = texto.indexOf(inicioClave);
            int fin = texto.indexOf(finClave);

            if (inicio != -1 && fin != -1 && fin > inicio) {
                String bloque = texto.substring(inicio, fin).trim();
                String[] lineas = bloque.split("\n");

                StringBuilder resultado = new StringBuilder();
                boolean tablaComenzada = false;

                for (String linea : lineas) {
                    String l = linea.trim();

                    if (l.equalsIgnoreCase("Electiva de Cs. Básicas") && resultado.length() == 0) {
                        resultado.append("Electiva de Cs. Básicas\n");
                    }

                    if (l.startsWith("Ciclo Lectivo")) {
                        resultado.append(l).append("\n");
                        tablaComenzada = true;
                        continue;
                    }

                    if (tablaComenzada) {
                        if (l.isEmpty() || l.toLowerCase().contains("ajuste") || l.toLowerCase().contains("entered by")) break;
                        resultado.append(l).append("\n");
                    }
                }

                return resultado.toString().trim();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public String extraerTextoEnfasisBruto(MultipartFile archivo) {
        try (PDDocument documento = PDDocument.load(archivo.getInputStream())) {
            PDFTextStripper lector = new PDFTextStripper();
            String texto = lector.getText(documento);

            String inicioClave = "Énfasis y Complementarias";
            String finClave = "Complementaria Lenguas";

            int inicio = texto.indexOf(inicioClave);
            int fin = texto.indexOf(finClave);

            if (inicio != -1 && fin != -1 && fin > inicio) {
                String bloque = texto.substring(inicio, fin).trim();
                String[] lineas = bloque.split("\n");

                StringBuilder resultado = new StringBuilder();
                boolean tablaComenzada = false;

                for (String linea : lineas) {
                    String l = linea.trim();

                    if (l.equalsIgnoreCase("Énfasis y Complementarias") && resultado.length() == 0) {
                        resultado.append("Énfasis y Complementarias\n");
                    }

                    if (l.startsWith("Ciclo Lectivo")) {
                        resultado.append(l).append("\n");
                        tablaComenzada = true;
                        continue;
                    }

                    if (tablaComenzada) {
                        if (l.isEmpty() || l.toLowerCase().contains("ajuste") || l.toLowerCase().contains("entered by")) break;
                        resultado.append(l).append("\n");
                    }
                }

                return resultado.toString().trim();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public String extraerTextoDesarrolloYSeguridadBruto(MultipartFile archivo) {
        try (PDDocument documento = PDDocument.load(archivo.getInputStream())) {
            PDFTextStripper lector = new PDFTextStripper();
            String texto = lector.getText(documento);

            String inicioClave = "Desarrollo avanzado de SW y Seguridad digital";
            String finClave = "Inteligencia artificial y ciencia de datos";

            int inicio = texto.indexOf(inicioClave);
            int fin = texto.indexOf(finClave);

            if (inicio != -1 && fin != -1 && fin > inicio) {
                String bloque = texto.substring(inicio, fin).trim();
                String[] lineas = bloque.split("\n");

                StringBuilder resultado = new StringBuilder();
                boolean tablaComenzada = false;

                for (String linea : lineas) {
                    String l = linea.trim();

                    if (l.equalsIgnoreCase(inicioClave) && resultado.length() == 0) {
                        resultado.append("Desarrollo avanzado de SW y Seguridad digital\n");
                    }

                    if (l.startsWith("Ciclo Lectivo")) {
                        resultado.append(l).append("\n");
                        tablaComenzada = true;
                        continue;
                    }

                    if (tablaComenzada) {
                        if (l.isEmpty() || l.toLowerCase().contains("ajuste") || l.toLowerCase().contains("entered by")) break;
                        resultado.append(l).append("\n");
                    }
                }

                return resultado.toString().trim();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public String extraerTextoComplementariaLenguasBruto(MultipartFile archivo) {
        try (PDDocument documento = PDDocument.load(archivo.getInputStream())) {
            PDFTextStripper lector = new PDFTextStripper();
            String texto = lector.getText(documento);

            String inicioClave = "Complementaria Lenguas";
            String finClave = "Electivas Universidad";

            int inicio = texto.indexOf(inicioClave);
            int fin = texto.indexOf(finClave);

            if (inicio != -1 && fin != -1 && fin > inicio) {
                String bloque = texto.substring(inicio, fin).trim();
                String[] lineas = bloque.split("\n");

                StringBuilder resultado = new StringBuilder();
                boolean tablaComenzada = false;

                for (String linea : lineas) {
                    String l = linea.trim();

                    if (l.equalsIgnoreCase("Complementaria Lenguas") && resultado.length() == 0) {
                        resultado.append("Complementaria Lenguas\n");
                    }

                    if (l.startsWith("Ciclo Lectivo")) {
                        resultado.append(l).append("\n");
                        tablaComenzada = true;
                        continue;
                    }

                    if (tablaComenzada) {
                        if (l.isEmpty() || l.toLowerCase().contains("ajuste") || l.toLowerCase().contains("entered by")) break;
                        resultado.append(l).append("\n");
                    }
                }

                return resultado.toString().trim();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public String extraerTextoComplementariaInformacionBruto(MultipartFile archivo) {
        try (PDDocument documento = PDDocument.load(archivo.getInputStream())) {
            PDFTextStripper lector = new PDFTextStripper();
            String texto = lector.getText(documento);

            String inicioClave = "Complementaria Información";
            String finClave = "Complementaria Lenguas";

            int inicio = texto.indexOf(inicioClave);
            int fin = texto.indexOf(finClave);

            if (inicio != -1 && fin != -1 && fin > inicio) {
                String bloque = texto.substring(inicio, fin).trim();
                String[] lineas = bloque.split("\n");

                StringBuilder resultado = new StringBuilder();
                boolean tablaComenzada = false;

                for (String linea : lineas) {
                    String l = linea.trim();

                    if (l.equalsIgnoreCase("Complementaria Información") && resultado.length() == 0) {
                        resultado.append("Complementaria Información\n");
                    }

                    if (l.startsWith("Ciclo Lectivo")) {
                        resultado.append(l).append("\n");
                        tablaComenzada = true;
                        continue;
                    }

                    if (tablaComenzada) {
                        if (l.isEmpty() || l.toLowerCase().contains("ajuste") || l.toLowerCase().contains("entered by")) break;
                        resultado.append(l).append("\n");
                    }
                }

                return resultado.toString().trim();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public String extraerTextoElectivasBruto(MultipartFile archivo) {
        try (PDDocument documento = PDDocument.load(archivo.getInputStream())) {
            PDFTextStripper lector = new PDFTextStripper();
            String texto = lector.getText(documento);

            String inicioClave = "Electivas Universidad";
            String finClave = "Requisitos de grado";

            int inicio = texto.indexOf(inicioClave);
            int fin = texto.indexOf(finClave);

            if (inicio != -1 && fin != -1 && fin > inicio) {
                String bloque = texto.substring(inicio, fin).trim();
                String[] lineas = bloque.split("\n");

                StringBuilder resultado = new StringBuilder();
                boolean incluir = false;

                for (String linea : lineas) {
                    String l = linea.trim();

                    if (l.equalsIgnoreCase("Electivas Universidad") && resultado.length() == 0) {
                        resultado.append("Electivas Universidad\n");
                    }

                    if (l.startsWith("Ciclo Lectivo")) {
                        resultado.append(l).append("\n");
                        incluir = true;
                        continue;
                    }

                    if (incluir) {
                        if (l.isEmpty() || l.toLowerCase().contains("ajuste") || l.toLowerCase().contains("entered by")) break;
                        resultado.append(l).append("\n");
                    }
                }

                return resultado.toString().trim();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public String extraerTextoInteligenciaArtificialBruto(MultipartFile archivo) {
        try (PDDocument documento = PDDocument.load(archivo.getInputStream())) {
            PDFTextStripper lector = new PDFTextStripper();
            String texto = lector.getText(documento);

            String inicioClave = "Inteligencia artificial y ciencia de datos";
            String finClave = "Práctica profesional Sistemas";

            int inicio = texto.indexOf(inicioClave);
            int fin = texto.indexOf(finClave);

            if (inicio != -1 && fin != -1 && fin > inicio) {
                String bloque = texto.substring(inicio, fin).trim();
                String[] lineas = bloque.split("\n");

                StringBuilder resultado = new StringBuilder();
                boolean tablaComenzada = false;

                for (String linea : lineas) {
                    String l = linea.trim();

                    if (l.equalsIgnoreCase(inicioClave) && resultado.length() == 0) {
                        resultado.append("Inteligencia artificial y ciencia de datos\n");
                    }

                    if (l.startsWith("Ciclo Lectivo")) {
                        resultado.append(l).append("\n");
                        tablaComenzada = true;
                        continue;
                    }

                    if (tablaComenzada) {
                        if (l.isEmpty() || l.toLowerCase().contains("ajuste") || l.toLowerCase().contains("entered by")) break;
                        resultado.append(l).append("\n");
                    }
                }

                return resultado.toString().trim();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<Materia> convertirTextoElectivasATabla(String texto) {
        List<Materia> lista = new ArrayList<>();

        if (texto == null || texto.isEmpty()) return lista;

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

                    lista.add(new Materia(ciclo, materiaCod, nCat, cursoCod, titulo, calif, cred, tipo));
                }
            }
        }

        return lista;
    }

}
