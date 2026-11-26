package pe.edu.clinica.sonrisa_andina.web;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import pe.edu.clinica.sonrisa_andina.domain.Paciente;
import pe.edu.clinica.sonrisa_andina.exception.ReglaNegocioException;
import pe.edu.clinica.sonrisa_andina.service.CitaService;
import pe.edu.clinica.sonrisa_andina.service.PacienteService;
import pe.edu.clinica.sonrisa_andina.service.UsuarioService;
import pe.edu.clinica.sonrisa_andina.web.form.PacienteForm;

@Controller
@RequestMapping("/pacientes")
@RequiredArgsConstructor
public class PacienteController {

    private final PacienteService pacienteService;
    private final CitaService citaService;
    private final UsuarioService usuarioService;

    @GetMapping
    public String listar(@RequestParam(required = false) String q, Model model) {
        model.addAttribute("pacientes", pacienteService.buscar(q));
        model.addAttribute("q", q);
        return "pacientes/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("form", new PacienteForm());
        model.addAttribute("modo", "crear");
        return "pacientes/formulario";
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute("form") PacienteForm form, BindingResult resultado,
                        Model model, RedirectAttributes redirect) {
        if (resultado.hasErrors()) {
            model.addAttribute("modo", "crear");
            return "pacientes/formulario";
        }
        try {
            Paciente paciente = pacienteService.crear(form);
            redirect.addFlashAttribute("exito", "Paciente " + paciente.getNombreCompleto() + " registrado.");
            return "redirect:/pacientes/" + paciente.getId();
        } catch (ReglaNegocioException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("modo", "crear");
            return "pacientes/formulario";
        }
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model, RedirectAttributes redirect) {
        try {
            Paciente paciente = pacienteService.obtener(id);
            model.addAttribute("paciente", paciente);
            model.addAttribute("citas", citaService.historialPaciente(id));
            model.addAttribute("usuarioPortal", usuarioService.usuarioVinculado(id).orElse(null));
            model.addAttribute("usuarioSugerido", usuarioService.usuarioSugerido(paciente));
            return "pacientes/detalle";
        } catch (ReglaNegocioException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
            return "redirect:/pacientes";
        }
    }

    @PostMapping("/{id}/acceso-portal")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION')")
    public String crearAccesoPortal(@PathVariable Long id, @RequestParam String password,
                                    RedirectAttributes redirect) {
        try {
            usuarioService.crearAccesoPaciente(id, password);
            redirect.addFlashAttribute("exito", "Acceso al portal creado.");
        } catch (ReglaNegocioException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        } catch (DataIntegrityViolationException ex) {
            redirect.addFlashAttribute("error", "No se pudo crear el acceso. Verifique si el usuario ya existe.");
        }
        return "redirect:/pacientes/" + id;
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes redirect) {
        try {
            Paciente paciente = pacienteService.obtener(id);
            model.addAttribute("form", pacienteService.aFormulario(paciente));
            model.addAttribute("paciente", paciente);
            model.addAttribute("modo", "editar");
            return "pacientes/formulario";
        } catch (ReglaNegocioException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
            return "redirect:/pacientes";
        }
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Long id, @Valid @ModelAttribute("form") PacienteForm form,
                             BindingResult resultado, Model model, RedirectAttributes redirect) {
        try {
            Paciente paciente = pacienteService.obtener(id);
            if (resultado.hasErrors()) {
                model.addAttribute("paciente", paciente);
                model.addAttribute("modo", "editar");
                return "pacientes/formulario";
            }
            pacienteService.actualizar(id, form);
            redirect.addFlashAttribute("exito", "Datos del paciente actualizados.");
            return "redirect:/pacientes/" + id;
        } catch (ReglaNegocioException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("modo", "editar");
            return "pacientes/formulario";
        }
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            pacienteService.eliminar(id);
            redirect.addFlashAttribute("exito", "Paciente eliminado del registro.");
            return "redirect:/pacientes";
        } catch (ReglaNegocioException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
            return "redirect:/pacientes/" + id;
        }
    }
}
