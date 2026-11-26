package pe.edu.clinica.sonrisa_andina.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import pe.edu.clinica.sonrisa_andina.domain.Cita;
import pe.edu.clinica.sonrisa_andina.domain.EstadoCita;
import pe.edu.clinica.sonrisa_andina.repository.projection.ConteoPorEstado;
import pe.edu.clinica.sonrisa_andina.repository.projection.ConteoPorEtiqueta;

public interface CitaRepository extends JpaRepository<Cita, Long> {

    // ------------------------------------------------------------------
    // Deteccion de cruces de horario
    // ------------------------------------------------------------------

    /**
     * Devuelve las citas del odontologo que se cruzan con el rango indicado.
     * Se excluyen las citas canceladas o reprogramadas porque ya liberaron
     * su horario. Se usa -1 como "sin exclusion".
     */
    @Query("""
            select c from Cita c
            where c.odontologo.id = :idOdontologo
              and c.id <> :excluirId
              and c.estado not in (pe.edu.clinica.sonrisa_andina.domain.EstadoCita.CANCELADA,
                                   pe.edu.clinica.sonrisa_andina.domain.EstadoCita.REPROGRAMADA)
              and c.inicio < :fin and c.fin > :inicio
            """)
    List<Cita> buscarSolapadas(@Param("idOdontologo") Long idOdontologo,
                               @Param("inicio") LocalDateTime inicio,
                               @Param("fin") LocalDateTime fin,
                               @Param("excluirId") Long excluirId);

    /** Citas del paciente que se cruzan con el rango indicado. */
    @Query("""
            select c from Cita c
            where c.paciente.id = :idPaciente
              and c.id <> :excluirId
              and c.estado not in (pe.edu.clinica.sonrisa_andina.domain.EstadoCita.CANCELADA,
                                   pe.edu.clinica.sonrisa_andina.domain.EstadoCita.REPROGRAMADA,
                                   pe.edu.clinica.sonrisa_andina.domain.EstadoCita.AUSENTE)
              and c.inicio < :fin and c.fin > :inicio
            """)
    List<Cita> buscarSolapadasPaciente(@Param("idPaciente") Long idPaciente,
                                       @Param("inicio") LocalDateTime inicio,
                                       @Param("fin") LocalDateTime fin,
                                       @Param("excluirId") Long excluirId);

    // ------------------------------------------------------------------
    // Consultas de agenda
    // ------------------------------------------------------------------

    List<Cita> findByInicioBetweenOrderByInicioAsc(LocalDateTime desde, LocalDateTime hasta);

    List<Cita> findByInicioBetweenAndEstadoOrderByInicioAsc(
            LocalDateTime desde, LocalDateTime hasta, EstadoCita estado);

    List<Cita> findByInicioBetweenAndOdontologoIdOrderByInicioAsc(
            LocalDateTime desde, LocalDateTime hasta, Long idOdontologo);

    List<Cita> findByInicioBetweenAndOdontologoIdAndEstadoOrderByInicioAsc(
            LocalDateTime desde, LocalDateTime hasta, Long idOdontologo, EstadoCita estado);

    List<Cita> findByOdontologoIdAndInicioBetweenOrderByInicioAsc(
            Long idOdontologo, LocalDateTime desde, LocalDateTime hasta);

    List<Cita> findByPacienteIdOrderByInicioDesc(Long idPaciente);

    List<Cita> findByPacienteIdAndEstadoInOrderByInicioDesc(Long idPaciente, List<EstadoCita> estados);

    List<Cita> findTop8ByInicioGreaterThanEqualAndEstadoInOrderByInicioAsc(
            LocalDateTime desde, List<EstadoCita> estados);

    List<Cita> findTop8ByOdontologoIdAndInicioGreaterThanEqualAndEstadoInOrderByInicioAsc(
            Long idOdontologo, LocalDateTime desde, List<EstadoCita> estados);

    // ------------------------------------------------------------------
    // Conteos y reportes
    // ------------------------------------------------------------------

    long countByEstado(EstadoCita estado);

    long countByEstadoAndInicioBetween(EstadoCita estado, LocalDateTime desde, LocalDateTime hasta);

    long countByInicioBetween(LocalDateTime desde, LocalDateTime hasta);

    long countByEstadoInAndInicioBetween(List<EstadoCita> estados, LocalDateTime desde, LocalDateTime hasta);

    @Query("""
            select c.estado as estado, count(c) as total
            from Cita c
            where c.inicio between :desde and :hasta
            group by c.estado
            """)
    List<ConteoPorEstado> contarPorEstado(@Param("desde") LocalDateTime desde,
                                          @Param("hasta") LocalDateTime hasta);

    @Query("""
            select c.servicio.nombre as etiqueta, count(c) as total
            from Cita c
            where c.inicio between :desde and :hasta
            group by c.servicio.nombre
            order by count(c) desc
            """)
    List<ConteoPorEtiqueta> contarPorServicio(@Param("desde") LocalDateTime desde,
                                              @Param("hasta") LocalDateTime hasta);

    @Query("""
            select concat(c.odontologo.nombres, ' ', c.odontologo.apellidos) as etiqueta,
                   count(c) as total
            from Cita c
            where c.inicio between :desde and :hasta
            group by c.odontologo.id, c.odontologo.nombres, c.odontologo.apellidos
            order by count(c) desc
            """)
    List<ConteoPorEtiqueta> contarPorOdontologo(@Param("desde") LocalDateTime desde,
                                                @Param("hasta") LocalDateTime hasta);
}
