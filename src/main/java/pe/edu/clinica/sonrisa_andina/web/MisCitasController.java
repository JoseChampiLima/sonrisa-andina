package pe.edu.clinica.sonrisa_andina.web;

import java.util.List;

import org.hibernate.Hibernate;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lombok.RequiredArgsConstructor;
import pe.edu.clinica.sonrisa_andina.domain.Cita;
import pe.edu.clinica.sonrisa_andina.domain.EstadoCita;
import pe.edu.clinica.sonrisa_andina.domain.Paciente;
import pe.edu.clinica.sonrisa_andina.domain.Rol;
import pe.edu.clinica.sonrisa_andina.domain.Usuario;
import pe.edu.clinica.sonrisa_andina.exception.ReglaNegocioException;
import pe.edu.clinica.sonrisa_andina.service.CitaService;
import pe.edu.clinica.sonrisa_andina.service.UsuarioService;

/**
 * Portal del paciente: consulta y cancelacion de sus propias citas.
 */
@Controller
@RequiredArgsConstructor
public class MisCitasController {

    private final UsuarioService usuarioService;
    private final CitaService citaService;

    @GetMapping("/mis-citas")
    public String misCitas(Authentication authentication, Model model) {
        Usuario usuario = usuarioService.actual(authentication);
        if (usuario == null) {
            return "redirect:/login";
        }
        if (usuario.getRol() != Rol.PACIENTE) {
            return "redirect:/";
        }
        Paciente paciente = pacienteVinculado(usuario);
        if (paciente == null) {
            model.addAttribute("error", "Su cuenta no tiene un perfil de paciente vinculado. Contacte a la clinica.");
            return "mis-citas";
        }
        List<Cita> citas = citaService.citasDePaciente(paciente.getId());
        model.addAttribute("paciente", paciente);
        model.addAttribute("citas", citas);
        model.addAttribute("proximas", citas.stream()
                .filter(c -> c.getEstado() == EstadoCita.PROGRAMADA || c.getEstado() == EstadoCita.CONFIRMADA)
                .toList());
        return "mis-citas";
    }

    @PostMapping("/mis-citas/{id}/cancelar")
    public String cancelar(@PathVariable Long id, Authentication authentication,
                           RedirectAttributes redirect) {
        Usuario usuario = usuarioService.actual(authentication);
        if (usuario == null) {
            return "redirect:/login";
        }
        if (usuario.getRol() != Rol.PACIENTE) {
            return "redirect:/";
        }
        Paciente paciente = pacienteVinculado(usuario);
        if (paciente == null) {
            return "redirect:/mis-citas";
        }
        try {
            Cita cita = citaService.obtener(id);
            if (!cita.getPaciente().getId().equals(paciente.getId())) {
                throw new ReglaNegocioException("No puede cancelar una cita que no le pertenece.");
            }
            citaService.cancelar(id, "Cancelada por el paciente desde el portal");
            redirect.addFlashAttribute("exito", "Su cita fue cancelada y el horario quedo liberado.");
        } catch (ReglaNegocioException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/mis-citas";
    }

    private Paciente pacienteVinculado(Usuario usuario) {
        return Hibernate.unproxy(usuario.getPersona()) instanceof Paciente paciente ? paciente : null;
    }
}
