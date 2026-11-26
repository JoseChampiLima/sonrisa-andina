package pe.edu.clinica.sonrisa_andina.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import pe.edu.clinica.sonrisa_andina.domain.Especialidad;

public interface EspecialidadRepository extends JpaRepository<Especialidad, Long> {

    List<Especialidad> findAllByOrderByNombreAsc();

    boolean existsByNombre(String nombre);

    Optional<Especialidad> findByNombre(String nombre);
}
