package pe.edu.clinica.sonrisa_andina.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import pe.edu.clinica.sonrisa_andina.domain.Odontologo;

public interface OdontologoRepository extends JpaRepository<Odontologo, Long> {

    Optional<Odontologo> findByDni(String dni);

    boolean existsByDni(String dni);

    boolean existsByNroColegiatura(String nroColegiatura);

    List<Odontologo> findByActivoTrueOrderByApellidosAsc();

    List<Odontologo> findAllByOrderByApellidosAsc();

    List<Odontologo> findByEspecialidadIdAndActivoTrue(Long idEspecialidad);
}
