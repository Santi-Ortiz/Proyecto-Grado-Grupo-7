package com.grupo7.tesis.services;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.grupo7.tesis.dtos.MateriaDTO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class lecturaService {

    // ===== Helpers SOLO para complementarias (usados por extraerTextoComplementariaLenguasBruto) =====
    private static final Pattern FILA_VALIDA =
            Pattern.compile("^(PrimPe|TerPe)\\d{4}\\b.*", Pattern.CASE_INSENSITIVE);

    private static boolean esLineaDescartable(String lower) {
        return lower.contains("se ha incluido esta línea")
                || lower.startsWith("ajuste ")
                || lower.contains("entered by")
                || lower.startsWith("unidades")
                || lower.startsWith("- unidades")
                || lower.startsWith("cursos")      // “Cursos Utilizados/Disponibles/…”
                || lower.startsWith("- cursos")
                || lower.contains("satisfecho")
                || lower.contains("no satisfecho")
                || lower.contains("obligatorias")
                || lower.contains("necesarias")
                || lower.equals("cursos utilizados");
    }

    private static int encontrarFinSeccion(String texto, int desde, String... delimitadores) {
        int fin = texto.length();
        for (String d : delimitadores) {
            int i = texto.indexOf(d, desde + 1);
            if (i != -1 && i < fin) fin = i;
        }
        return fin;
    }

    private static String parseBloqueComplementaria(String bloque, String tituloSeccion) {
        String[] lineas = bloque.split("\n");
        StringBuilder resultado = new StringBuilder();
        boolean tablaComenzada = false;

        for (String linea : lineas) {
            String l = linea.trim();
            if (l.isEmpty()) continue;

            // Título una sola vez
            if (resultado.length() == 0 && l.equalsIgnoreCase(tituloSeccion)) {
                resultado.append(tituloSeccion).append("\n");
                continue;
            }

            // Si aparece un nuevo bloque de "Complementaria ..." diferente, no mezclar
            if (l.toLowerCase().startsWith("complementaria ")
                    && !l.equalsIgnoreCase(tituloSeccion)) {
                break;
            }

            // Encabezado de tabla
            if (l.toLowerCase().startsWith("ciclo lectivo")) {
                resultado.append(l).append("\n");
                tablaComenzada = true;
                continue;
            }

            if (tablaComenzada) {
                String lower = l.toLowerCase();
                if (esLineaDescartable(lower)) continue;

                // Guardamos TODAS las líneas relevantes, incluso si son continuación de una fila.
                // La unión y parseo robusto se hace en convertirTextoElectivasATabla.
                if (FILA_VALIDA.matcher(l).matches() || !l.toLowerCase().startsWith("ciclo lectivo")) {
                    resultado.append(l).append("\n");
                }
            }
        }
        return resultado.toString().trim();
    }
    // ================================================================================================

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
            String finClave = "Electivas Universidad";

            int inicio = texto.indexOf(inicioClave);
            int fin = texto.indexOf(finClave);

            if (inicio != -1 && fin != -1 && fin > inicio) {
                String bloque = texto.substring(inicio, fin).trim();
                String[] lineas = bloque.split("\n");

                StringBuilder resultado = new StringBuilder();
                boolean tablaComenzada = false;

                final Pattern filaValida = Pattern.compile("^(PrimPe|TerPe)\\d{4}\\b.*");

                for (String linea : lineas) {
                    String l = linea.trim();
                    if (l.isEmpty()) continue;

                    if (l.equalsIgnoreCase("Énfasis y Complementarias") && resultado.length() == 0) {
                        resultado.append("Énfasis y Complementarias\n");
                        continue;
                    }

                    if (l.toLowerCase().startsWith("complementaria")) {
                        break;
                    }

                    if (l.toLowerCase().startsWith("ciclo lectivo")) {
                        resultado.append(l).append("\n");
                        tablaComenzada = true;
                        continue;
                    }

                    if (tablaComenzada) {
                        String lower = l.toLowerCase();
                        if (lower.contains("se ha incluido esta línea")
                            || lower.startsWith("ajuste ")
                            || lower.contains("entered by")) {
                            tablaComenzada = false;
                            continue;
                        }
                        if (lower.startsWith("unidades") || lower.startsWith("- unidades")
                            || lower.startsWith("cursos") || lower.startsWith("- cursos")
                            || lower.contains("satisfecho") || lower.contains("no satisfecho")
                            || lower.contains("obligatorias") || lower.contains("necesarias")
                            || lower.equalsIgnoreCase("cursos utilizados")) {
                            continue;
                        }
                        if (filaValida.matcher(l).matches()) {
                            resultado.append(l).append("\n");
                        }
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

    // =============================== FUNCIÓN MODIFICADA CLAVE ====================================
    // AHORA devuelve TODAS las "Complementaria ..." (incluye también "Complementaria Información").
    public String extraerTextoComplementariaLenguasBruto(MultipartFile archivo) {
        StringBuilder out = new StringBuilder();

        try (PDDocument documento = PDDocument.load(archivo.getInputStream())) {
            PDFTextStripper lector = new PDFTextStripper();
            String texto = lector.getText(documento);

            int pos = 0;
            while (true) {
                int inicio = texto.indexOf("Complementaria ", pos);
                if (inicio == -1) break;

                // Título de la complementaria (línea completa)
                int finTitulo = texto.indexOf("\n", inicio);
                String titulo = (finTitulo == -1)
                        ? texto.substring(inicio).trim()
                        : texto.substring(inicio, finTitulo).trim();

                // Fin de bloque: siguiente "Complementaria " o grandes secciones
                int finBloque = encontrarFinSeccion(
                        texto, inicio,
                        "Complementaria ",               // otra complementaria
                        "Práctica profesional",          // secciones mayores que cortan
                        "Electivas Universidad",
                        "Requisitos de grado",
                        "Historial de Cursos"
                );

                String bloqueOriginal = texto.substring(inicio, finBloque).trim();
                String bloqueLimpio = parseBloqueComplementaria(bloqueOriginal, titulo);

                if (!bloqueLimpio.isEmpty() && bloqueLimpio.toLowerCase().contains("ciclo lectivo")) {
                    if (out.length() > 0) out.append("\n"); // separador entre bloques
                    out.append(bloqueLimpio);
                }

                pos = finBloque;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        // Si no encontró nada, mantener compatibilidad devolviendo null
        String res = out.toString().trim();
        return res.isEmpty() ? null : res;
    }
    // =============================================================================================

    public String extraerTextoComplementariaEsteticaBruto(MultipartFile archivo) {
        try (PDDocument documento = PDDocument.load(archivo.getInputStream())) {
            PDFTextStripper lector = new PDFTextStripper();
            String texto = lector.getText(documento);

            String inicioClave = "Complementaria Estética";
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

                    if (l.equalsIgnoreCase("Complementaria Estética") && resultado.length() == 0) {
                        resultado.append("Complementaria Estética\n");
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

    public String extraerTextoComplementariaCianciaPoliticaBruto(MultipartFile archivo) {
        try (PDDocument documento = PDDocument.load(archivo.getInputStream())) {
            PDFTextStripper lector = new PDFTextStripper();
            String texto = lector.getText(documento);

            String inicioClave = "Complementaria Ciencia Politic";
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

                    if (l.equalsIgnoreCase("Complementaria Ciencia Politic") && resultado.length() == 0) {
                        resultado.append("Complementaria Ciencia Politic\n");
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

    // ===================== CORRECCIÓN ROBUSTA PARA FILAS MULTILÍNEA =====================
    public List<MateriaDTO> convertirTextoElectivasATabla(String texto) {
        List<MateriaDTO> lista = new ArrayList<>();
        if (texto == null || texto.isEmpty()) return lista;

        String[] lineas = texto.split("\\r?\\n");
        boolean tablaComenzada = false;

        // Buffer para acumular una fila que puede venir en varias líneas
        StringBuilder filaAcumulada = new StringBuilder();

        // Regex para detectar el final de una fila: "Calif Cred Tipo" al final
        // - calif: número o letra (A, B, ...)
        // - cred: número entero o decimal, con punto o coma
        final Pattern patronFinFila = Pattern.compile(
                "(?:\\s|^)([0-9]+(?:[.,][0-9]{1,2})?|[A-ZÑ])\\s+([0-9]+(?:[.,][0-9]{1,2})?)\\s+(\\S+)\\s*$"
        );

        // Prefijo de inicio de fila (PrimPeXXXX / TerPeXXXX)
        final Pattern patronInicioFila = Pattern.compile("^(PrimPe|TerPe)\\d{4}\\b.*");

        for (String linea : lineas) {
            String l = linea.trim();
            if (l.isEmpty()) continue;

            if (l.toLowerCase().startsWith("ciclo lectivo")) {
                tablaComenzada = true;
                // reiniciar cualquier acumulación previa
                filaAcumulada.setLength(0);
                continue;
            }

            if (!tablaComenzada) continue;

            // Si inicia una nueva fila y había algo acumulado, la cerramos primero
            if (patronInicioFila.matcher(l).matches()) {
                if (filaAcumulada.length() > 0) {
                    parsearYAgregarFilaSiValida(filaAcumulada.toString(), lista);
                    filaAcumulada.setLength(0);
                }
                filaAcumulada.append(l);
            } else {
                // Continuación de la fila actual
                if (filaAcumulada.length() > 0) {
                    filaAcumulada.append(" ").append(l);
                } else {
                    // Texto suelto sin fila en curso -> ignorar
                    continue;
                }
            }

            // Si ya tenemos "Calif Cred Tipo" al final, cerramos y parseamos
            if (patronFinFila.matcher(filaAcumulada.toString()).find()) {
                parsearYAgregarFilaSiValida(filaAcumulada.toString(), lista);
                filaAcumulada.setLength(0);
            }
        }

        // Si al final quedó una fila sin cerrar, intentar parsearla
        if (filaAcumulada.length() > 0) {
            parsearYAgregarFilaSiValida(filaAcumulada.toString(), lista);
        }

        return lista;
    }

    private void parsearYAgregarFilaSiValida(String fila, List<MateriaDTO> lista) {
        if (fila == null) return;
        fila = fila.trim();
        if (fila.isEmpty()) return;

        // Intentar extraer al final "calif cred tipo"
        Pattern fin = Pattern.compile(
                "(.*)\\s+([0-9]+(?:[.,][0-9]{1,2})?|[A-ZÑ])\\s+([0-9]+(?:[.,][0-9]{1,2})?)\\s+(\\S+)\\s*$"
        );
        Matcher m = fin.matcher(fila);

        String parteInicial;
        String calif;
        String cred;
        String tipo;

        if (m.matches()) {
            parteInicial = m.group(1).trim();
            calif = m.group(2).trim();
            cred = m.group(3).trim();
            tipo = m.group(4).trim();
        } else {
            // Fallback a la lógica simple anterior: últimos 3 tokens = calif / cred / tipo
            String[] tokens = fila.split("\\s+");
            if (tokens.length < 8) return; // muy corta para ser válida

            calif = tokens[tokens.length - 3];
            cred = tokens[tokens.length - 2];
            tipo = tokens[tokens.length - 1];

            StringBuilder pi = new StringBuilder();
            for (int i = 0; i < tokens.length - 3; i++) {
                if (i > 0) pi.append(" ");
                pi.append(tokens[i]);
            }
            parteInicial = pi.toString().trim();
        }

        String[] tokensPI = parteInicial.split("\\s+");
        if (tokensPI.length < 4) return;

        String ciclo = tokensPI[0];
        // Validar que el primer token sea PrimPeXXXX o TerPeXXXX
        if (!ciclo.matches("^(PrimPe|TerPe)\\d{4}.*")) {
            return;
        }

        String materiaCod = tokensPI[1];
        String nCat = tokensPI[2];
        String cursoCod = tokensPI[3];

        StringBuilder tituloBuilder = new StringBuilder();
        for (int i = 4; i < tokensPI.length; i++) {
            tituloBuilder.append(tokensPI[i]).append(" ");
        }
        String titulo = tituloBuilder.toString().trim();

        lista.add(new MateriaDTO(ciclo, materiaCod, nCat, cursoCod, titulo, calif, cred, tipo));
    }
    // =============================================================================================

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
