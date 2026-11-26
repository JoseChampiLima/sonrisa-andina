package pe.edu.clinica.sonrisa_andina.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import pe.edu.clinica.sonrisa_andina.domain.Paciente;

public interface PacienteRepository extends JpaRepository<Paciente, Long> {

    Optional<Paciente> findByDni(String dni);

    boolean existsByDni(String dni);

    boolean existsByCorreo(String correo);

    java.util.List<Paciente> findAllByOrderByApellidosAsc();

    @Query("""
            select p from Paciente p
            where lower(p.nombres) like lower(concat('%', :q, '%'))
               or lower(p.apellidos) like lower(concat('%', :q, '%'))
               or p.dni like concat(:q, '%')
            order by p.apellidos, p.nombres
            """)
    java.util.List<Paciente> buscar(@Param("q") String q);
}
