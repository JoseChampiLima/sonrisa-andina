package pe.edu.clinica.sonrisa_andina.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import pe.edu.clinica.sonrisa_andina.domain.Servicio;

public interface ServicioRepository extends JpaRepository<Servicio, Long> {

    List<Servicio> findByActivoTrueOrderByNombreAsc();

    List<Servicio> findAllByOrderByNombreAsc();

    Optional<Servicio> findByNombre(String nombre);

    boolean existsByNombre(String nombre);
}
