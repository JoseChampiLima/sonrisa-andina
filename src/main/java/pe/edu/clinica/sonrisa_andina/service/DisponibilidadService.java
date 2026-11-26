package pe.edu.clinica.sonrisa_andina.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import pe.edu.clinica.sonrisa_andina.domain.Cita;
import pe.edu.clinica.sonrisa_andina.domain.HorarioTrabajo;
import pe.edu.clinica.sonrisa_andina.domain.Odontologo;
import pe.edu.clinica.sonrisa_andina.domain.Paciente;
import pe.edu.clinica.sonrisa_andina.domain.Servicio;
import pe.edu.clinica.sonrisa_andina.exception.ReglaNegocioException;
import pe.edu.clinica.sonrisa_andina.repository.CitaRepository;
import pe.edu.clinica.sonrisa_andina.repository.HorarioTrabajoRepository;

/**
 * Calcula la disponibilidad real de un odontologo: parte de su horario
 * semanal, descuenta las citas activas y respeta la duracion del servicio.
 * Es la regla que elimina los cruces de horario (objetivo principal).
 */
@Service
@RequiredArgsConstructor
public class DisponibilidadService {

    private static final Locale LOCALE_PE = new Locale("es", "PE");

    private final HorarioTrabajoRepository horarioRepository;
    private final CitaRepository citaRepository;

    @Value("${clinica.agenda.paso-minutos:15}")
    private int pasoMinutos;

    /** Horarios de inicio ofrecidos para un servicio y fecha determinados. */
    public List<LocalTime> horariosLibres(Odontologo odontologo, Servicio servicio, LocalDate fecha) {
        if (odontologo == null || servicio == null || fecha == null) {
            return List.of();
        }
        if (!odontologo.isActivo() || !servicio.isActivo() || !servicio.puedeRealizarlo(odontologo)) {
            return List.of();
        }

        List<HorarioTrabajo> bloques = horarioRepository
                .findByOdontologoIdAndDiaSemana(odontologo.getId(), fecha.getDayOfWeek().getValue());
        if (bloques.isEmpty()) {
            return List.of();
        }

        List<Cita> ocupadas = citasDelDia(odontologo.getId(), fecha);
        LocalDateTime ahora = LocalDateTime.now();
        List<LocalTime> libres = new ArrayList<>();

        for (HorarioTrabajo bloque : bloques) {
            LocalDateTime cursor = LocalDateTime.of(fecha, bloque.getHoraInicio());
            LocalDateTime limite = LocalDateTime.of(fecha, bloque.getHoraFin());

            while (!cursor.plusMinutes(servicio.getDuracionMin()).isAfter(limite)) {
                LocalDateTime fin = cursor.plusMinutes(servicio.getDuracionMin());
                if (cursor.isAfter(ahora) && estaLibre(ocupadas, cursor, fin)) {
                    libres.add(cursor.toLocalTime());
                }
                cursor = cursor.plusMinutes(pasoMinutos);
            }
        }
        return libres.stream().distinct().sorted().toList();
    }

    /**
     * Valida todas las reglas de agenda antes de reservar o reprogramar.
     *
     * @param idCitaExcluir cita que no debe considerarse ocupada (al reprogramar)
     */
    public void validarReserva(Odontologo odontologo, Paciente paciente, Servicio servicio,
                               LocalDateTime inicio, Long idCitaExcluir) {
        if (inicio == null) {
            throw new ReglaNegocioException("Debe indicar la fecha y hora de la cita.");
        }
        if (inicio.isBefore(LocalDateTime.now())) {
            throw new ReglaNegocioException("No se puede agendar una cita en una fecha u hora pasada.");
        }
        if (odontologo == null || !odontologo.isActivo()) {
            throw new ReglaNegocioException("El odontologo seleccionado no esta disponible para citas.");
        }
        if (servicio == null || !servicio.isActivo()) {
            throw new ReglaNegocioException("El servicio seleccionado no esta activo.");
        }
        if (!servicio.puedeRealizarlo(odontologo)) {
            throw new ReglaNegocioException("El servicio \"" + servicio.getNombre()
                    + "\" no puede ser realizado por " + odontologo.getNombreCompleto()
                    + " (" + odontologo.getEspecialidad().getNombre() + ").");
        }

        LocalDateTime fin = servicio.calcularFin(inicio);
        if (fin.toLocalDate().isAfter(inicio.toLocalDate())) {
            throw new ReglaNegocioException("La cita debe iniciar y terminar el mismo dia.");
        }
        debeEstarEnHorario(odontologo, inicio, fin);

        Long excluir = idCitaExcluir != null ? idCitaExcluir : -1L;

        List<Cita> cruces = citaRepository.buscarSolapadas(odontologo.getId(), inicio, fin, excluir);
        if (!cruces.isEmpty()) {
            Cita c = cruces.get(0);
            throw new ReglaNegocioException("El odontologo ya tiene una cita de "
                    + c.getInicio().toLocalTime() + " a " + c.getFin().toLocalTime()
                    + " con " + c.getPaciente().getNombreCompleto() + ". Elija otro horario.");
        }

        if (paciente != null) {
            List<Cita> crucesPaciente = citaRepository.buscarSolapadasPaciente(
                    paciente.getId(), inicio, fin, excluir);
            if (!crucesPaciente.isEmpty()) {
                Cita c = crucesPaciente.get(0);
                throw new ReglaNegocioException("El paciente ya tiene otra cita a las "
                        + c.getInicio().toLocalTime() + " con "
                        + c.getOdontologo().getNombreCompleto() + ".");
            }
        }
    }

    /** Indica si un rango cae dentro de algun bloque de trabajo del odontologo. */
    public boolean estaEnHorario(Odontologo odontologo, LocalDateTime inicio, LocalDateTime fin) {
        int dia = inicio.getDayOfWeek().getValue();
        return horarioRepository.findByOdontologoIdAndDiaSemana(odontologo.getId(), dia).stream()
                .anyMatch(h -> !inicio.toLocalTime().isBefore(h.getHoraInicio())
                        && !fin.toLocalTime().isAfter(h.getHoraFin()));
    }

    private void debeEstarEnHorario(Odontologo odontologo, LocalDateTime inicio, LocalDateTime fin) {
        if (!estaEnHorario(odontologo, inicio, fin)) {
            String dia = inicio.getDayOfWeek().getDisplayName(TextStyle.FULL, LOCALE_PE);
            throw new ReglaNegocioException("El horario solicitado esta fuera del horario de atencion del "
                    + "odontologo para el " + dia + ". Revise los horarios de trabajo registrados.");
        }
    }

    private List<Cita> citasDelDia(Long idOdontologo, LocalDate fecha) {
        return citaRepository.findByInicioBetweenAndOdontologoIdOrderByInicioAsc(
                        fecha.atStartOfDay(), fecha.plusDays(1).atStartOfDay(), idOdontologo).stream()
                .filter(c -> c.getEstado().ocupaAgenda())
                .toList();
    }

    private boolean estaLibre(List<Cita> ocupadas, LocalDateTime inicio, LocalDateTime fin) {
        return ocupadas.stream().noneMatch(c -> inicio.isBefore(c.getFin()) && fin.isAfter(c.getInicio()));
    }
}
