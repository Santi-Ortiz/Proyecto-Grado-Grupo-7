package com.grupo7.tesis.services;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grupo7.tesis.models.Materia;
import com.grupo7.tesis.models.RequisitoMateria;
import com.grupo7.tesis.repositories.MateriaRepository;
import com.grupo7.tesis.repositories.RequisitoMateriaRepository;

@Service
public class MateriaService {

    @Autowired
    private MateriaRepository materiaRepository;

    @Autowired
    private RequisitoMateriaRepository requisitoMateriaRepository;

    public List<Materia> obtenerMaterias() {
        return materiaRepository.findAll();
    }

    public Materia crearMateria(Materia materia) {
        return materiaRepository.save(materia);
    }

    public List<Materia> crearMateriasDesdeJson() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        InputStream is = getClass().getClassLoader().getResourceAsStream("plan_estudios_INGSIS.json");
        List<Materia> materias = mapper.readValue(is, new TypeReference<List<Materia>>() {
        });

        for (Materia materia : materias) {
            try {
                String requisitosJson = mapper.writeValueAsString(materia.getRequisitos());
                materia.setRequisitosJson(requisitosJson);
                crearMateria(materia);
                procesarTodosRequisitos();
            } catch (Exception e) {
                materia.setRequisitosJson("[]");
            }
        }

        return materias;
    }

    public Materia obtenerMateriaPorId(Long id) {
        return materiaRepository.findById(id).orElse(null);
    }

    public Materia actualizarMateria(Long id, Materia materia) {
        if (materiaRepository.existsById(id)) {
            materia.setId(id);
            return materiaRepository.save(materia);
        }
        return null;
    }

    public void eliminarMateria(Long id) {
        materiaRepository.deleteById(id);
    }

    public void procesarRequisitosMateria(Materia materia) {
        if (materia.getRequisitos() != null && !materia.getRequisitos().isEmpty()) {
            for (String codigoRequisito : materia.getRequisitos()) {
                try {
                    Optional<Materia> materiaRequisito = materiaRepository.findAll().stream()
                            .filter(m -> m.getCodigo() != null && m.getCodigo().equals(codigoRequisito))
                            .findFirst();
                    
                    if (materiaRequisito.isPresent()) {
                        boolean existeRequisito = requisitoMateriaRepository.findAll().stream()
                                .anyMatch(rm -> rm.getMateria().getId().equals(materia.getId()) && 
                                            rm.getMateriaRequisito().getId().equals(materiaRequisito.get().getId()));
                        
                        if (!existeRequisito) {
                            RequisitoMateria requisito = new RequisitoMateria(materia, materiaRequisito.get());
                            requisitoMateriaRepository.save(requisito);
                            System.out.println("   Requisito agregado: " + materia.getCodigo() + " requiere " + codigoRequisito);
                        }
                    } else {
                        System.out.println("   ⚠️ Materia requisito no encontrada: " + codigoRequisito);
                    }
                } catch (Exception e) {
                    System.out.println("   ❌ Error al procesar requisito " + codigoRequisito + ": " + e.getMessage());
                }
            }
        }
    }

    public void procesarTodosRequisitos() {
        List<Materia> todasLasMaterias = materiaRepository.findAll();
        System.out.println("🔗 Procesando requisitos para " + todasLasMaterias.size() + " materias...");
        
        for (Materia materia : todasLasMaterias) {
            procesarRequisitosMateria(materia);
        }
        
        System.out.println("✅ Procesamiento de requisitos completado");
    }

}
