package pe.edu.clinica.sonrisa_andina.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import pe.edu.clinica.sonrisa_andina.domain.Notificacion;
import pe.edu.clinica.sonrisa_andina.domain.EstadoNotificacion;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    List<Notificacion> findByEstadoAndProgramadaParaLessThanEqualOrderByProgramadaParaAsc(
            EstadoNotificacion estado, java.time.LocalDateTime limite);

    List<Notificacion> findByEstadoOrderByProgramadaParaAsc(EstadoNotificacion estado);

    List<Notificacion> findTop50ByOrderByProgramadaParaDesc();

    List<Notificacion> findByCitaId(Long idCita);

    void deleteByCitaIdAndEstado(Long idCita, EstadoNotificacion estado);

    long countByEstado(EstadoNotificacion estado);
}
