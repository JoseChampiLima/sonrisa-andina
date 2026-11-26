package pe.edu.clinica.sonrisa_andina.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import pe.edu.clinica.sonrisa_andina.domain.Cita;
import pe.edu.clinica.sonrisa_andina.domain.EstadoCita;
import pe.edu.clinica.sonrisa_andina.domain.Odontologo;
import pe.edu.clinica.sonrisa_andina.domain.Paciente;
import pe.edu.clinica.sonrisa_andina.domain.Servicio;
import pe.edu.clinica.sonrisa_andina.exception.ReglaNegocioException;
import pe.edu.clinica.sonrisa_andina.repository.CitaRepository;
import pe.edu.clinica.sonrisa_andina.repository.OdontologoRepository;
import pe.edu.clinica.sonrisa_andina.repository.PacienteRepository;
import pe.edu.clinica.sonrisa_andina.repository.ServicioRepository;

/**
 * Servicio central del proceso de reserva y gestion de citas. Concentra las
 * reglas de negocio y coordina la agenda con los recordatorios.
 */
@Service
@RequiredArgsConstructor
public class CitaService {

    private static final List<EstadoCita> ESTADOS_ACTIVOS =
            List.of(EstadoCita.PROGRAMADA, EstadoCita.CONFIRMADA);

    private final CitaRepository citaRepository;
    private final PacienteRepository pacienteRepository;
    private final OdontologoRepository odontologoRepository;
    private final ServicioRepository servicioRepository;
    private final DisponibilidadService disponibilidadService;
    private final RecordatorioService recordatorioService;

    // ------------------------------------------------------------------
    // Reserva
    // ------------------------------------------------------------------

    @Transactional
    public Cita reservar(Long idPaciente, Long idOdontologo, Long idServicio,
                         LocalDateTime inicio, String motivo) {
        Paciente paciente = paciente(idPaciente);
        Odontologo odontologo = odontologo(idOdontologo);
        Servicio servicio = servicio(idServicio);

        disponibilidadService.validarReserva(odontologo, paciente, servicio, inicio, null);

        Cita cita = new Cita(paciente, odontologo, servicio, inicio, motivo);
        try {
            citaRepository.saveAndFlush(cita);
        } catch (DataIntegrityViolationException e) {
            throw new ReglaNegocioException("Ese horario acaba de ser tomado por otra reserva. "
                    + "Actualice la disponibilidad y elija otro horario.");
        }

        recordatorioService.programarRecordatorio(cita);
        return cita;
    }

    // ------------------------------------------------------------------
    // Cambios de estado
    // ------------------------------------------------------------------

    @Transactional
    public Cita reprogramar(Long idCita, LocalDateTime nuevoInicio) {
        Cita original = obtener(idCita);
        if (!original.sePuedeReprogramar()) {
            throw new ReglaNegocioException("La cita no se puede reprogramar porque esta "
                    + original.getEstado().getEtiqueta().toLowerCase() + ".");
        }

        disponibilidadService.validarReserva(original.getOdontologo(), original.getPaciente(),
                original.getServicio(), nuevoInicio, original.getId());

        recordatorioService.cancelarPendientes(original);
        original.marcarReprogramada();
        citaRepository.save(original);

        Cita nueva = new Cita(original.getPaciente(), original.getOdontologo(),
                original.getServicio(), nuevoInicio, original.getMotivo());
        nueva.setCitaOrigen(original);
        try {
            citaRepository.saveAndFlush(nueva);
        } catch (DataIntegrityViolationException e) {
            throw new ReglaNegocioException("Ese horario acaba de ser tomado por otra reserva. "
                    + "Elija otro horario.");
        }

        recordatorioService.programarRecordatorio(nueva);
        return nueva;
    }

    @Transactional
    public Cita confirmar(Long idCita) {
        Cita cita = obtener(idCita);
        cita.confirmar();
        return citaRepository.save(cita);
    }

    @Transactional
    public Cita cancelar(Long idCita, String motivo) {
        Cita cita = obtener(idCita);
        cita.cancelar();
        if (motivo != null && !motivo.isBlank()) {
            cita.setMotivo(recortar(motivo, 200));
        }
        Cita guardada = citaRepository.save(cita);
        recordatorioService.cancelarPendientes(guardada);
        return guardada;
    }

    @Transactional
    public Cita marcarAtendida(Long idCita) {
        Cita cita = obtener(idCita);
        cita.marcarAtendida();
        Cita guardada = citaRepository.save(cita);
        recordatorioService.cancelarPendientes(guardada);
        return guardada;
    }

    @Transactional
    public Cita marcarAusente(Long idCita) {
        Cita cita = obtener(idCita);
        cita.marcarAusente();
        Cita guardada = citaRepository.save(cita);
        recordatorioService.cancelarPendientes(guardada);
        return guardada;
    }

    // ------------------------------------------------------------------
    // Consultas
    // ------------------------------------------------------------------

    public Cita obtener(Long id) {
        return citaRepository.findById(id)
                .orElseThrow(() -> new ReglaNegocioException("La cita solicitada no existe."));
    }

    public Optional<Cita> buscar(Long id) {
        return citaRepository.findById(id);
    }

    public List<Cita> listar(LocalDate desde, LocalDate hasta, Long idOdontologo, EstadoCita estado) {
        LocalDateTime inicio = desde.atStartOfDay();
        LocalDateTime fin = hasta.plusDays(1).atStartOfDay();
        if (idOdontologo != null && estado != null) {
            return citaRepository.findByInicioBetweenAndOdontologoIdAndEstadoOrderByInicioAsc(
                    inicio, fin, idOdontologo, estado);
        }
        if (idOdontologo != null) {
            return citaRepository.findByInicioBetweenAndOdontologoIdOrderByInicioAsc(inicio, fin, idOdontologo);
        }
        if (estado != null) {
            return citaRepository.findByInicioBetweenAndEstadoOrderByInicioAsc(inicio, fin, estado);
        }
        return citaRepository.findByInicioBetweenOrderByInicioAsc(inicio, fin);
    }

    public List<Cita> agendaDelDia(LocalDate fecha) {
        return citaRepository.findByInicioBetweenOrderByInicioAsc(
                fecha.atStartOfDay(), fecha.plusDays(1).atStartOfDay());
    }

    public List<Cita> agendaDelDia(LocalDate fecha, Long idOdontologo) {
        if (idOdontologo == null) {
            return agendaDelDia(fecha);
        }
        return citaRepository.findByInicioBetweenAndOdontologoIdOrderByInicioAsc(
                fecha.atStartOfDay(), fecha.plusDays(1).atStartOfDay(), idOdontologo);
    }

    public List<Cita> proximasCitas() {
        return citaRepository.findTop8ByInicioGreaterThanEqualAndEstadoInOrderByInicioAsc(
                LocalDateTime.now(), ESTADOS_ACTIVOS);
    }

    public List<Cita> proximasCitas(Odontologo odontologo) {
        if (odontologo == null) {
            return proximasCitas();
        }
        return citaRepository.findTop8ByOdontologoIdAndInicioGreaterThanEqualAndEstadoInOrderByInicioAsc(
                odontologo.getId(), LocalDateTime.now(), ESTADOS_ACTIVOS);
    }

    public List<Cita> citasDePaciente(Long idPaciente) {
        return citaRepository.findByPacienteIdOrderByInicioDesc(idPaciente);
    }

    public List<Cita> historialPaciente(Long idPaciente) {
        return citaRepository.findByPacienteIdOrderByInicioDesc(idPaciente);
    }

    // ------------------------------------------------------------------
    // Apoyo
    // ------------------------------------------------------------------

    private Paciente paciente(Long id) {
        if (id == null) {
            throw new ReglaNegocioException("Debe seleccionar un paciente.");
        }
        return pacienteRepository.findById(id)
                .orElseThrow(() -> new ReglaNegocioException("El paciente seleccionado no existe."));
    }

    private Odontologo odontologo(Long id) {
        if (id == null) {
            throw new ReglaNegocioException("Debe seleccionar un odontologo.");
        }
        return odontologoRepository.findById(id)
                .orElseThrow(() -> new ReglaNegocioException("El odontologo seleccionado no existe."));
    }

    private Servicio servicio(Long id) {
        if (id == null) {
            throw new ReglaNegocioException("Debe seleccionar un servicio.");
        }
        return servicioRepository.findById(id)
                .orElseThrow(() -> new ReglaNegocioException("El servicio seleccionado no existe."));
    }

    private String recortar(String texto, int max) {
        return texto.length() <= max ? texto : texto.substring(0, max);
    }
}
