package pe.edu.clinica.sonrisa_andina.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import pe.edu.clinica.sonrisa_andina.domain.Cita;
import pe.edu.clinica.sonrisa_andina.domain.EstadoCita;
import pe.edu.clinica.sonrisa_andina.repository.CitaRepository;
import pe.edu.clinica.sonrisa_andina.repository.projection.ConteoPorEstado;
import pe.edu.clinica.sonrisa_andina.repository.projection.ConteoPorEtiqueta;

/**
 * Genera los indicadores y reportes del sistema (RF-08).
 */
@Service
@RequiredArgsConstructor
public class ReporteService {

    private final CitaRepository citaRepository;

    /** Resumen de un periodo para la pantalla de reportes. */
    public record Resumen(
            LocalDate desde,
            LocalDate hasta,
            long total,
            long programadas,
            long confirmadas,
            long canceladas,
            long reprogramadas,
            long atendidas,
            long ausentes,
            double tasaInasistencia,
            double tasaCancelacion,
            List<ConteoPorEstado> porEstado,
            List<ConteoPorEtiqueta> porServicio,
            List<ConteoPorEtiqueta> porOdontologo) {
    }

    public Resumen resumen(LocalDate desde, LocalDate hasta) {
        LocalDateTime inicio = desde.atStartOfDay();
        LocalDateTime fin = hasta.plusDays(1).atStartOfDay();

        long total = citaRepository.countByInicioBetween(inicio, fin);
        long programadas = citaRepository.countByEstadoAndInicioBetween(EstadoCita.PROGRAMADA, inicio, fin);
        long confirmadas = citaRepository.countByEstadoAndInicioBetween(EstadoCita.CONFIRMADA, inicio, fin);
        long canceladas = citaRepository.countByEstadoAndInicioBetween(EstadoCita.CANCELADA, inicio, fin);
        long reprogramadas = citaRepository.countByEstadoAndInicioBetween(EstadoCita.REPROGRAMADA, inicio, fin);
        long atendidas = citaRepository.countByEstadoAndInicioBetween(EstadoCita.ATENDIDA, inicio, fin);
        long ausentes = citaRepository.countByEstadoAndInicioBetween(EstadoCita.AUSENTE, inicio, fin);

        long baseAsistencia = atendidas + ausentes;
        double tasaInasistencia = baseAsistencia == 0 ? 0 : (ausentes * 100.0) / baseAsistencia;
        double tasaCancelacion = total == 0 ? 0 : (canceladas * 100.0) / total;

        return new Resumen(desde, hasta, total, programadas, confirmadas, canceladas, reprogramadas,
                atendidas, ausentes, redondear(tasaInasistencia), redondear(tasaCancelacion),
                citaRepository.contarPorEstado(inicio, fin),
                citaRepository.contarPorServicio(inicio, fin),
                citaRepository.contarPorOdontologo(inicio, fin));
    }

    public Resumen resumenDelMes() {
        LocalDate hoy = LocalDate.now();
        return resumen(hoy.withDayOfMonth(1), hoy.withDayOfMonth(hoy.lengthOfMonth()));
    }

    /** Tasa de inasistencia de los ultimos 30 dias (KPI del panel). */
    public double tasaInasistenciaReciente() {
        LocalDate hasta = LocalDate.now();
        Resumen resumen = resumen(hasta.minusDays(30), hasta);
        return resumen.tasaInasistencia();
    }

    public long citasDeHoy() {
        LocalDate hoy = LocalDate.now();
        return citaRepository.countByInicioBetween(hoy.atStartOfDay(), hoy.plusDays(1).atStartOfDay());
    }

    public long citasAtendidasHoy() {
        LocalDate hoy = LocalDate.now();
        return citaRepository.countByEstadoAndInicioBetween(
                EstadoCita.ATENDIDA, hoy.atStartOfDay(), hoy.plusDays(1).atStartOfDay());
    }

    public long citasPendientesHoy() {
        LocalDate hoy = LocalDate.now();
        return citaRepository.countByEstadoInAndInicioBetween(
                List.of(EstadoCita.PROGRAMADA, EstadoCita.CONFIRMADA),
                hoy.atStartOfDay(), hoy.plusDays(1).atStartOfDay());
    }

    public long citasCanceladasHoy() {
        LocalDate hoy = LocalDate.now();
        return citaRepository.countByEstadoAndInicioBetween(
                EstadoCita.CANCELADA, hoy.atStartOfDay(), hoy.plusDays(1).atStartOfDay());
    }

    public long citasProximasSieteDias() {
        LocalDate hoy = LocalDate.now();
        return citaRepository.countByInicioBetween(
                LocalDateTime.now(), hoy.plusDays(7).atTime(23, 59));
    }

    public List<Cita> agendaDeHoy() {
        LocalDate hoy = LocalDate.now();
        return citaRepository.findByInicioBetweenOrderByInicioAsc(
                hoy.atStartOfDay(), hoy.plusDays(1).atStartOfDay());
    }

    private double redondear(double valor) {
        return Math.round(valor * 10.0) / 10.0;
    }
}
