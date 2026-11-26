package pe.edu.clinica.sonrisa_andina.web;

import java.time.LocalDate;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.RequiredArgsConstructor;
import pe.edu.clinica.sonrisa_andina.domain.Cita;
import pe.edu.clinica.sonrisa_andina.domain.EstadoCita;
import pe.edu.clinica.sonrisa_andina.domain.Odontologo;
import pe.edu.clinica.sonrisa_andina.domain.Rol;
import pe.edu.clinica.sonrisa_andina.domain.Usuario;
import pe.edu.clinica.sonrisa_andina.service.CitaService;
import pe.edu.clinica.sonrisa_andina.service.ReporteService;
import pe.edu.clinica.sonrisa_andina.service.UsuarioService;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final CitaService citaService;
    private final ReporteService reporteService;
    private final UsuarioService usuarioService;

    @GetMapping("/")
    public String panel(Authentication authentication, Model model) {
        Usuario usuario = usuarioService.actual(authentication);
        if (usuario != null && usuario.getRol() == Rol.PACIENTE) {
            return "redirect:/mis-citas";
        }

        LocalDate hoy = LocalDate.now();
        Odontologo odontologo = odontologoDe(usuario);
        List<Cita> agendaHoy = citaService.agendaDelDia(hoy, odontologo == null ? null : odontologo.getId());

        long atendidasHoy = agendaHoy.stream().filter(c -> c.getEstado() == EstadoCita.ATENDIDA).count();
        long pendientesHoy = agendaHoy.stream()
                .filter(c -> c.getEstado() == EstadoCita.PROGRAMADA || c.getEstado() == EstadoCita.CONFIRMADA)
                .count();
        long canceladasHoy = agendaHoy.stream().filter(c -> c.getEstado() == EstadoCita.CANCELADA).count();
        long ausentesHoy = agendaHoy.stream().filter(c -> c.getEstado() == EstadoCita.AUSENTE).count();

        model.addAttribute("agendaHoy", agendaHoy);
        model.addAttribute("proximas", citaService.proximasCitas(odontologo));
        model.addAttribute("citasHoy", agendaHoy.size());
        model.addAttribute("atendidasHoy", atendidasHoy);
        model.addAttribute("pendientesHoy", pendientesHoy);
        model.addAttribute("canceladasHoy", canceladasHoy);
        model.addAttribute("ausentesHoy", ausentesHoy);
        model.addAttribute("tasaInasistencia", reporteService.tasaInasistenciaReciente());
        model.addAttribute("pendientesRecordatorio", 0L);
        model.addAttribute("odontologoActual", odontologo);
        return "dashboard";
    }

    private Odontologo odontologoDe(Usuario usuario) {
        if (usuario != null && usuario.getRol() == Rol.ODONTOLOGO
                && usuario.getPersona() instanceof Odontologo odontologo) {
            return odontologo;
        }
        return null;
    }
}
