package pe.edu.clinica.sonrisa_andina.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import pe.edu.clinica.sonrisa_andina.domain.CanalNotificacion;
import pe.edu.clinica.sonrisa_andina.domain.Cita;
import pe.edu.clinica.sonrisa_andina.domain.EstadoNotificacion;
import pe.edu.clinica.sonrisa_andina.domain.Notificacion;
import pe.edu.clinica.sonrisa_andina.repository.NotificacionRepository;
import pe.edu.clinica.sonrisa_andina.service.notificacion.Notificador;

/**
 * Programa y envia los recordatorios de las citas. El envio se delega al
 * Notificador correspondiente al canal, aplicando polimorfismo.
 */
@Service
@RequiredArgsConstructor
public class RecordatorioService {

    private static final Logger log = LoggerFactory.getLogger(RecordatorioService.class);
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy", new Locale("es", "PE"));
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final NotificacionRepository notificacionRepository;
    private final List<Notificador> notificadores;

    @Value("${clinica.recordatorios.horas-anticipacion:24}")
    private int horasAnticipacion;

    // ------------------------------------------------------------------
    // Programacion
    // ------------------------------------------------------------------

    /** Crea (o reemplaza) el recordatorio pendiente de la cita. */
    @Transactional
    public void programarRecordatorio(Cita cita) {
        if (cita == null || cita.getId() == null) {
            return;
        }
        notificacionRepository.deleteByCitaIdAndEstado(cita.getId(), EstadoNotificacion.PENDIENTE);

        String correo = cita.getPaciente().getCorreo();
        boolean porCorreo = correo != null && correo.contains("@");
        CanalNotificacion canal = porCorreo ? CanalNotificacion.CORREO : CanalNotificacion.WHATSAPP;
        String destinatario = porCorreo ? correo : cita.getPaciente().getTelefono();

        LocalDateTime programada = cita.getInicio().minusHours(horasAnticipacion);
        if (programada.isBefore(LocalDateTime.now())) {
            // La cita es en menos de 24 h: se envia la confirmacion de inmediato.
            programada = LocalDateTime.now().plusMinutes(1);
        }

        Notificacion notificacion = new Notificacion(cita, canal, destinatario,
                construirMensaje(cita), programada);
        notificacionRepository.save(notificacion);
        log.info("Recordatorio programado para la cita #{} a las {} via {}",
                cita.getId(), notificacion.getProgramadaPara(), canal);
    }

    /** Cancela los recordatorios pendientes de una cita (cancelacion/reprogramacion). */
    @Transactional
    public void cancelarPendientes(Cita cita) {
        if (cita != null && cita.getId() != null) {
            notificacionRepository.deleteByCitaIdAndEstado(cita.getId(), EstadoNotificacion.PENDIENTE);
        }
    }

    /** Envia de inmediato un recordatorio concreto (accion manual desde la bandeja). */
    @Transactional
    public void enviarAhora(Long idNotificacion) {
        Notificacion notificacion = notificacionRepository.findById(idNotificacion)
                .orElseThrow(() -> new pe.edu.clinica.sonrisa_andina.exception.ReglaNegocioException(
                        "La notificacion solicitada no existe."));
        despachar(notificacion);
    }

    // ------------------------------------------------------------------
    // Envio automatico
    // ------------------------------------------------------------------

    /** Revisa periodicamente la bandeja y envia los recordatorios vencidos. */
    @Scheduled(fixedDelayString = "${clinica.recordatorios.intervalo-ms:60000}", initialDelay = 20000)
    @Transactional
    public void procesarPendientes() {
        List<Notificacion> pendientes = notificacionRepository
                .findByEstadoAndProgramadaParaLessThanEqualOrderByProgramadaParaAsc(
                        EstadoNotificacion.PENDIENTE, LocalDateTime.now());
        for (Notificacion notificacion : pendientes) {
            despachar(notificacion);
        }
    }

    private void despachar(Notificacion notificacion) {
        Notificador notificador = notificadores.stream()
                .filter(n -> n.canal() == notificacion.getCanal())
                .findFirst()
                .orElse(null);

        if (notificador == null) {
            notificacion.setEstado(EstadoNotificacion.FALLIDA);
            notificacionRepository.save(notificacion);
            return;
        }

        try {
            notificador.enviar(notificacion);
            notificacion.setEstado(EstadoNotificacion.ENVIADA);
            notificacion.setFechaEnvio(LocalDateTime.now());
        } catch (Exception e) {
            notificacion.setEstado(EstadoNotificacion.FALLIDA);
            log.warn("No se pudo enviar el recordatorio #{}: {}", notificacion.getId(), e.getMessage());
        }
        notificacionRepository.save(notificacion);
    }

    // ------------------------------------------------------------------
    // Consultas y texto
    // ------------------------------------------------------------------

    public List<Notificacion> recientes() {
        return notificacionRepository.findTop50ByOrderByProgramadaParaDesc();
    }

    public List<Notificacion> pendientes() {
        return notificacionRepository.findByEstadoOrderByProgramadaParaAsc(EstadoNotificacion.PENDIENTE);
    }

    public long contarPendientes() {
        return notificacionRepository.countByEstado(EstadoNotificacion.PENDIENTE);
    }

    public List<Notificacion> deCita(Long idCita) {
        return notificacionRepository.findByCitaId(idCita);
    }

    /** Reintenta un recordatorio fallido. */
    @Transactional
    public void reintentar(Long idNotificacion) {
        Notificacion notificacion = notificacionRepository.findById(idNotificacion)
                .orElseThrow(() -> new pe.edu.clinica.sonrisa_andina.exception.ReglaNegocioException(
                        "La notificacion solicitada no existe."));
        notificacion.setEstado(EstadoNotificacion.PENDIENTE);
        notificacionRepository.save(notificacion);
    }

    private String construirMensaje(Cita cita) {
        String nombre = cita.getPaciente().getNombres();
        return "Hola " + nombre + ", le recordamos su cita en Clinica Dental Sonrisa Andina.\n"
                + "Servicio: " + cita.getServicio().getNombre() + "\n"
                + "Odontologo: " + cita.getOdontologo().getNombreCompleto() + "\n"
                + "Fecha: " + cita.getInicio().format(FECHA) + " a las " + cita.getInicio().format(HORA) + "\n"
                + "Direccion: Jesus Maria, Lima\n"
                + "Responda SI para confirmar su asistencia. Si no puede asistir, cancele su cita "
                + "para liberar el horario.";
    }
}
