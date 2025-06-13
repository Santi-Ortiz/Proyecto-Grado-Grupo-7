package com.grupo7.tesis.service;

import com.grupo7.tesis.model.Materia;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class lecturaService {

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
                        String lineaSinFinal = matcher.group(1).trim();
                        String calif = matcher.group(2);
                        String cred = matcher.group(3);
                        String tipo = matcher.group(4);
                        String[] tokens = lineaSinFinal.split("\\s+");
                        if (tokens.length >= 5) {
                            String ciclo = tokens[0];
                            String materiaCod = tokens[1];
                            String nCat = tokens[2];
                            String cursoCod = tokens[3];
                            StringBuilder tituloBuilder = new StringBuilder();
                            for (int i = 4; i < tokens.length; i++) {
                                tituloBuilder.append(tokens[i]).append(" ");
                            }
                            String tituloCurso = tituloBuilder.toString().trim();
                            materias.add(new Materia(ciclo, materiaCod, nCat, cursoCod, tituloCurso, calif, cred, tipo));
                        }
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return materias;
    }

}
