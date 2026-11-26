package pe.edu.clinica.sonrisa_andina.config;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import pe.edu.clinica.sonrisa_andina.domain.Cita;
import pe.edu.clinica.sonrisa_andina.domain.Especialidad;
import pe.edu.clinica.sonrisa_andina.domain.EstadoCita;
import pe.edu.clinica.sonrisa_andina.domain.HorarioTrabajo;
import pe.edu.clinica.sonrisa_andina.domain.Odontologo;
import pe.edu.clinica.sonrisa_andina.domain.Paciente;
import pe.edu.clinica.sonrisa_andina.domain.Rol;
import pe.edu.clinica.sonrisa_andina.domain.Servicio;
import pe.edu.clinica.sonrisa_andina.domain.Usuario;
import pe.edu.clinica.sonrisa_andina.repository.CitaRepository;
import pe.edu.clinica.sonrisa_andina.repository.EspecialidadRepository;
import pe.edu.clinica.sonrisa_andina.repository.HorarioTrabajoRepository;
import pe.edu.clinica.sonrisa_andina.repository.OdontologoRepository;
import pe.edu.clinica.sonrisa_andina.repository.PacienteRepository;
import pe.edu.clinica.sonrisa_andina.repository.ServicioRepository;
import pe.edu.clinica.sonrisa_andina.repository.UsuarioRepository;
import pe.edu.clinica.sonrisa_andina.service.RecordatorioService;

/**
 * Carga los datos iniciales del sistema la primera vez que se ejecuta:
 * catalogos, odontologos con sus horarios, cuentas de acceso, pacientes y
 * citas de demostracion. Es idempotente: si ya hay usuarios, no hace nada.
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final EspecialidadRepository especialidadRepository;
    private final ServicioRepository servicioRepository;
    private final OdontologoRepository odontologoRepository;
    private final HorarioTrabajoRepository horarioRepository;
    private final PacienteRepository pacienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final CitaRepository citaRepository;
    private final RecordatorioService recordatorioService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (usuarioRepository.count() > 0) {
            log.info("Datos iniciales ya presentes: no se vuelve a sembrar la base de datos.");
            return;
        }
        log.info("Cargando datos iniciales de Clinica Dental Sonrisa Andina...");

        Map<String, Especialidad> especialidades = cargarEspecialidades();
        Map<String, Servicio> servicios = cargarServicios(especialidades);
        Map<String, Odontologo> odontologos = cargarOdontologos(especialidades);
        cargarHorarios(odontologos);
        Map<String, Paciente> pacientes = cargarPacientes();
        cargarUsuarios(odontologos, pacientes);
        cargarCitas(odontologos, servicios, pacientes);

        log.info("Datos iniciales cargados: {} especialidades, {} servicios, {} odontologos, {} pacientes",
                especialidadRepository.count(), servicioRepository.count(),
                odontologoRepository.count(), pacienteRepository.count());
        log.info("Accesos de demostracion -> admin/admin123 | recepcion/recepcion123 | "
                + "cramos/doctor123 | paciente/paciente123");
    }

    private Map<String, Especialidad> cargarEspecialidades() {
        List<String> nombres = List.of("Odontologia general", "Endodoncia", "Ortodoncia",
                "Odontologia estetica", "Cirugia oral");
        java.util.LinkedHashMap<String, Especialidad> mapa = new java.util.LinkedHashMap<>();
        for (String nombre : nombres) {
            mapa.put(nombre, especialidadRepository.save(new Especialidad(nombre)));
        }
        return mapa;
    }

    private Map<String, Servicio> cargarServicios(Map<String, Especialidad> esp) {
        Map<String, Servicio> mapa = new java.util.LinkedHashMap<>();
        mapa.put("consulta", servicio("Consulta y diagnostico", 30, "50.00",
                esp.get("Odontologia general")));
        mapa.put("limpieza", servicio("Limpieza dental (profilaxis)", 45, "90.00",
                esp.get("Odontologia general")));
        mapa.put("resina", servicio("Restauracion (resina)", 60, "120.00",
                esp.get("Odontologia general")));
        mapa.put("extraccion", servicio("Extraccion simple", 45, "100.00",
                esp.get("Odontologia general"), esp.get("Cirugia oral")));
        mapa.put("endodoncia", servicio("Endodoncia (tratamiento de conductos)", 90, "450.00",
                esp.get("Endodoncia")));
        mapa.put("ortodoncia", servicio("Ortodoncia (control mensual)", 30, "120.00",
                esp.get("Ortodoncia")));
        mapa.put("blanqueamiento", servicio("Blanqueamiento dental", 75, "350.00",
                esp.get("Odontologia estetica")));
        return mapa;
    }

    private Servicio servicio(String nombre, int duracion, String precio, Especialidad... especialidades) {
        Servicio servicio = new Servicio(nombre, duracion, new BigDecimal(precio));
        for (Especialidad especialidad : especialidades) {
            servicio.getEspecialidades().add(especialidad);
        }
        return servicioRepository.save(servicio);
    }

    private Map<String, Odontologo> cargarOdontologos(Map<String, Especialidad> esp) {
        Map<String, Odontologo> mapa = new java.util.LinkedHashMap<>();
        mapa.put("carlos", odontologoRepository.save(new Odontologo("70000001", "Carlos", "Ramos Quispe",
                "900000001", esp.get("Odontologia general"), "10001")));
        mapa.put("lucia", odontologoRepository.save(new Odontologo("70000002", "Lucia", "Torres Vega",
                "900000002", esp.get("Endodoncia"), "10002")));
        mapa.put("andres", odontologoRepository.save(new Odontologo("70000003", "Andres", "Salazar Rojas",
                "900000003", esp.get("Ortodoncia"), "10003")));
        mapa.put("valeria", odontologoRepository.save(new Odontologo("70000004", "Valeria", "Cruz Montalvo",
                "900000004", esp.get("Odontologia estetica"), "10004")));
        mapa.put("miguel", odontologoRepository.save(new Odontologo("70000005", "Miguel", "Paredes Leon",
                "900000005", esp.get("Cirugia oral"), "10005")));
        return mapa;
    }

    private void cargarHorarios(Map<String, Odontologo> odontologos) {
        for (Odontologo odontologo : odontologos.values()) {
            for (DayOfWeek dia = DayOfWeek.MONDAY; dia.getValue() <= DayOfWeek.FRIDAY.getValue(); dia = dia.plus(1)) {
                horarioRepository.save(new HorarioTrabajo(odontologo, dia.getValue(),
                        LocalTime.of(9, 0), LocalTime.of(20, 0)));
            }
            horarioRepository.save(new HorarioTrabajo(odontologo, DayOfWeek.SATURDAY.getValue(),
                    LocalTime.of(9, 0), LocalTime.of(14, 0)));
        }
    }

    private Map<String, Paciente> cargarPacientes() {
        Map<String, Paciente> mapa = new java.util.LinkedHashMap<>();
        mapa.put("maria", pacienteRepository.save(new Paciente("70123456", "Maria", "Quispe Huaman",
                "987654321", "maria.quispe@example.com")));
        mapa.put("jorge", pacienteRepository.save(new Paciente("70456789", "Jorge", "Rios Salas",
                "912345678", "jorge.rios@example.com")));
        mapa.put("ana", pacienteRepository.save(new Paciente("70654321", "Ana", "Beltran Castro",
                "998877665", null)));
        mapa.put("luis", pacienteRepository.save(new Paciente("70987654", "Luis", "Mendoza Rojas",
                "955443322", "luis.mendoza@example.com")));
        return mapa;
    }

    private void cargarUsuarios(Map<String, Odontologo> odontologos, Map<String, Paciente> pacientes) {
        usuarioRepository.save(new Usuario("admin", passwordEncoder.encode("admin123"), Rol.ADMIN, null));
        usuarioRepository.save(new Usuario("recepcion", passwordEncoder.encode("recepcion123"),
                Rol.RECEPCION, null));
        usuarioRepository.save(new Usuario("cramos", passwordEncoder.encode("doctor123"),
                Rol.ODONTOLOGO, odontologos.get("carlos")));
        usuarioRepository.save(new Usuario("paciente", passwordEncoder.encode("paciente123"),
                Rol.PACIENTE, pacientes.get("maria")));
    }

    private void cargarCitas(Map<String, Odontologo> odontologos, Map<String, Servicio> servicios,
                             Map<String, Paciente> pacientes) {
        LocalDate hoy = LocalDate.now();

        // Citas de ayer: alimentan los reportes de inasistencia.
        cita(odontologos.get("carlos"), servicios.get("limpieza"), pacientes.get("maria"),
                hoy.minusDays(1).atTime(9, 0), EstadoCita.ATENDIDA, "Control y profilaxis");
        cita(odontologos.get("lucia"), servicios.get("endodoncia"), pacientes.get("jorge"),
                hoy.minusDays(1).atTime(10, 0), EstadoCita.ATENDIDA, "Dolor en molar inferior");
        cita(odontologos.get("andres"), servicios.get("ortodoncia"), pacientes.get("ana"),
                hoy.minusDays(1).atTime(12, 0), EstadoCita.AUSENTE, "Control mensual de brackets");
        cita(odontologos.get("valeria"), servicios.get("blanqueamiento"), pacientes.get("luis"),
                hoy.minusDays(1).atTime(15, 0), EstadoCita.CANCELADA, "Viaje del paciente");
        cita(odontologos.get("carlos"), servicios.get("consulta"), pacientes.get("jorge"),
                hoy.minusDays(1).atTime(17, 0), EstadoCita.ATENDIDA, "Evaluacion inicial");

        // Agenda de hoy.
        cita(odontologos.get("carlos"), servicios.get("limpieza"), pacientes.get("maria"),
                hoy.atTime(9, 0), EstadoCita.ATENDIDA, "Limpieza semestral");
        cita(odontologos.get("lucia"), servicios.get("endodoncia"), pacientes.get("jorge"),
                hoy.atTime(10, 0), EstadoCita.CONFIRMADA, "Segunda sesion de conductos");
        cita(odontologos.get("andres"), servicios.get("ortodoncia"), pacientes.get("ana"),
                hoy.atTime(11, 30), EstadoCita.AUSENTE, "Ajuste de brackets");
        cita(odontologos.get("valeria"), servicios.get("blanqueamiento"), pacientes.get("luis"),
                hoy.atTime(15, 0), EstadoCita.CONFIRMADA, "Blanqueamiento con luz LED");
        cita(odontologos.get("miguel"), servicios.get("extraccion"), pacientes.get("jorge"),
                hoy.atTime(16, 30), EstadoCita.PROGRAMADA, "Extraccion de tercera molar");

        // Agenda de los proximos dias (genera recordatorios automaticos).
        cita(odontologos.get("carlos"), servicios.get("consulta"), pacientes.get("jorge"),
                hoy.plusDays(1).atTime(9, 0), EstadoCita.PROGRAMADA, "Dolor al masticar");
        cita(odontologos.get("lucia"), servicios.get("endodoncia"), pacientes.get("ana"),
                hoy.plusDays(1).atTime(10, 30), EstadoCita.CONFIRMADA, "Tratamiento de conductos");
        cita(odontologos.get("andres"), servicios.get("ortodoncia"), pacientes.get("maria"),
                hoy.plusDays(1).atTime(12, 0), EstadoCita.PROGRAMADA, "Control de ortodoncia");
        cita(odontologos.get("valeria"), servicios.get("blanqueamiento"), pacientes.get("luis"),
                hoy.plusDays(1).atTime(15, 0), EstadoCita.PROGRAMADA, "Blanqueamiento dental");
        cita(odontologos.get("miguel"), servicios.get("extraccion"), pacientes.get("maria"),
                hoy.plusDays(1).atTime(16, 0), EstadoCita.PROGRAMADA, "Extraccion simple");

        cita(odontologos.get("carlos"), servicios.get("resina"), pacientes.get("ana"),
                hoy.plusDays(2).atTime(9, 30), EstadoCita.PROGRAMADA, "Restauracion en pieza 26");
        cita(odontologos.get("andres"), servicios.get("ortodoncia"), pacientes.get("jorge"),
                hoy.plusDays(2).atTime(11, 0), EstadoCita.PROGRAMADA, "Control mensual");
        cita(odontologos.get("lucia"), servicios.get("endodoncia"), pacientes.get("luis"),
                hoy.plusDays(3).atTime(16, 0), EstadoCita.PROGRAMADA, "Evaluacion de conductos");
        cita(odontologos.get("valeria"), servicios.get("consulta"), pacientes.get("maria"),
                hoy.plusDays(4).atTime(10, 0), EstadoCita.PROGRAMADA, "Evaluacion estetica");
    }

    private void cita(Odontologo odontologo, Servicio servicio, Paciente paciente,
                      LocalDateTime inicio, EstadoCita estado, String motivo) {
        Cita cita = new Cita(paciente, odontologo, servicio, inicio, motivo);
        cita.setEstado(estado);
        citaRepository.saveAndFlush(cita);
        if (cita.getEstado() == EstadoCita.PROGRAMADA || cita.getEstado() == EstadoCita.CONFIRMADA) {
            recordatorioService.programarRecordatorio(cita);
        }
    }
}
