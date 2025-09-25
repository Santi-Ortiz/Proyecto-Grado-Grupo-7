package com.grupo7.tesis.services;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
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
    private InformeAvanceService informeAvanceService;
    
    public InformeAvance guardarInformeAvance(byte[]  archivo, Estudiante estudiante, Pensum pensum) throws IOException {
    
        LocalDate fecha = LocalDate.now();
        InformeAvance informeAvance = new InformeAvance();
        String nombreArchivo = "informeAvance_" + estudiante.getCodigo() + "_" + fecha + ".pdf";

        informeAvance.setNombreArchivo(nombreArchivo);
        informeAvance.setArchivo(archivo);
        informeAvance.setFechaPublicacion(fecha);
        informeAvance.setPensum(pensum);
        informeAvance.setEstudiante(estudiante);

        informeAvanceService.crearInformeAvance(informeAvance);

        return informeAvance;
    }

    public List<MateriaDTO> obtenerMateriasDesdeArchivo(MultipartFile archivo) {
        List<MateriaDTO> materias = new ArrayList<>();
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
                    if (linea.trim().isEmpty() || linea.contains("Ciclo Lectivo"))
                        continue;

                    Matcher matcher = patronFinal.matcher(linea);
                    if (matcher.find()) {
                        // String posibleFinal = matcher.group(0);
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

                            materias.add(
                                    new MateriaDTO(ciclo, materiaCod, nCat, cursoCod, tituloCurso.trim(), calif, cred,
                                            tipo));
                        }
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return materias;
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
                        if (l.isEmpty() || l.toLowerCase().contains("ajuste") || l.toLowerCase().contains("entered by"))
                            break;
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
                        if (l.isEmpty() || l.toLowerCase().contains("ajuste") || l.toLowerCase().contains("entered by"))
                            break;
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
                        if (l.isEmpty() || l.toLowerCase().contains("ajuste") || l.toLowerCase().contains("entered by"))
                            break;
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

    public String extraerTextoDesarrolloSeguridadAComputacionBruto(MultipartFile archivo) {
        try (PDDocument documento = PDDocument.load(archivo.getInputStream())) {
            PDFTextStripper lector = new PDFTextStripper();
            String texto = lector.getText(documento);

            String inicioClave = "Desarrollo avanzado de SW y Seguridad digital";
            String finClave = "Computación Visual";

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
                        if (l.isEmpty() || l.toLowerCase().contains("ajuste") || l.toLowerCase().contains("entered by"))
                            break;
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

    public String extraerTextoDesarrolloYGestionBruto(MultipartFile archivo) {
        try (PDDocument documento = PDDocument.load(archivo.getInputStream())) {
            PDFTextStripper lector = new PDFTextStripper();
            String texto = lector.getText(documento);

            String inicioClave = "Desarrollo avanzado de SW y Seguridad digital";
            String finClave = "Sistemas de Información Y Gestión";

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
                        if (l.isEmpty() || l.toLowerCase().contains("ajuste") || l.toLowerCase().contains("entered by"))
                            break;
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

    public String extraerTextoComputacionVisualBruto(MultipartFile archivo) {
        try (PDDocument documento = PDDocument.load(archivo.getInputStream())) {
            PDFTextStripper lector = new PDFTextStripper();
            String texto = lector.getText(documento);

            String inicioClave = "Computación Visual";
            String finClave = "Sistemas de Información Y Gestión";

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
                        resultado.append("Computación Visual\n");
                    }

                    if (l.startsWith("Ciclo Lectivo")) {
                        resultado.append(l).append("\n");
                        tablaComenzada = true;
                        continue;
                    }

                    if (tablaComenzada) {
                        if (l.isEmpty() || l.toLowerCase().contains("ajuste") || l.toLowerCase().contains("entered by"))
                            break;
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

    public String extraerTextoComputacionVisualAInteligenciaArtificialBruto(MultipartFile archivo) {
        try (PDDocument documento = PDDocument.load(archivo.getInputStream())) {
            PDFTextStripper lector = new PDFTextStripper();
            String texto = lector.getText(documento);

            String inicioClave = "Computación Visual";
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
                        resultado.append("Computación Visual\n");
                    }

                    if (l.startsWith("Ciclo Lectivo")) {
                        resultado.append(l).append("\n");
                        tablaComenzada = true;
                        continue;
                    }

                    if (tablaComenzada) {
                        if (l.isEmpty() || l.toLowerCase().contains("ajuste") || l.toLowerCase().contains("entered by"))
                            break;
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

    public String extraerTextoSistemasGestionAInteligenciaArtificialBruto(MultipartFile archivo) {
        try (PDDocument documento = PDDocument.load(archivo.getInputStream())) {
            PDFTextStripper lector = new PDFTextStripper();
            String texto = lector.getText(documento);

            String inicioClave = "Sistemas de Información Y Gestión";
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
                        resultado.append("Sistemas de Información Y Gestión\n");
                    }

                    if (l.startsWith("Ciclo Lectivo")) {
                        resultado.append(l).append("\n");
                        tablaComenzada = true;
                        continue;
                    }

                    if (tablaComenzada) {
                        if (l.isEmpty() || l.toLowerCase().contains("ajuste") || l.toLowerCase().contains("entered by"))
                            break;
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
                        if (l.isEmpty() || l.toLowerCase().contains("ajuste") || l.toLowerCase().contains("entered by"))
                            break;
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
                        if (l.isEmpty() || l.toLowerCase().contains("ajuste") || l.toLowerCase().contains("entered by"))
                            break;
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
                        if (l.isEmpty() || l.toLowerCase().contains("ajuste") || l.toLowerCase().contains("entered by"))
                            break;
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
                        if (l.isEmpty() || l.toLowerCase().contains("ajuste") || l.toLowerCase().contains("entered by"))
                            break;
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

    public List<String> extraerLineasRequisitosGrado(MultipartFile archivo) {
        try (PDDocument documento = PDDocument.load(archivo.getInputStream())) {
            PDFTextStripper lector = new PDFTextStripper();
            String texto = lector.getText(documento);

            String inicioClave = "Requisitos de grado";
            String finClave = "Historial de Cursos";

            int inicio = texto.indexOf(inicioClave);
            int fin = texto.indexOf(finClave);

            if (inicio != -1 && fin != -1 && fin > inicio) {
                String bloque = texto.substring(inicio, fin).trim();

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
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Collections.emptyList();
    }

}
