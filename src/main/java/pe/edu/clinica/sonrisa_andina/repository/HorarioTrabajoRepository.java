package pe.edu.clinica.sonrisa_andina.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import pe.edu.clinica.sonrisa_andina.domain.HorarioTrabajo;

public interface HorarioTrabajoRepository extends JpaRepository<HorarioTrabajo, Long> {

    List<HorarioTrabajo> findByOdontologoIdOrderByDiaSemanaAscHoraInicioAsc(Long idOdontologo);

    List<HorarioTrabajo> findByOdontologoIdAndDiaSemana(Long idOdontologo, Integer diaSemana);

    void deleteByOdontologoId(Long idOdontologo);
}
