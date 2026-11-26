package pe.edu.clinica.sonrisa_andina.web;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
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
import pe.edu.clinica.sonrisa_andina.domain.Cita;
import pe.edu.clinica.sonrisa_andina.domain.EstadoCita;
import pe.edu.clinica.sonrisa_andina.domain.Odontologo;
import pe.edu.clinica.sonrisa_andina.domain.Rol;
import pe.edu.clinica.sonrisa_andina.domain.Usuario;
import pe.edu.clinica.sonrisa_andina.exception.ReglaNegocioException;
import pe.edu.clinica.sonrisa_andina.service.CitaService;
import pe.edu.clinica.sonrisa_andina.service.OdontologoService;
import pe.edu.clinica.sonrisa_andina.service.PacienteService;
import pe.edu.clinica.sonrisa_andina.service.RecordatorioService;
import pe.edu.clinica.sonrisa_andina.service.ServicioService;
import pe.edu.clinica.sonrisa_andina.service.UsuarioService;
import pe.edu.clinica.sonrisa_andina.web.form.CitaForm;
import pe.edu.clinica.sonrisa_andina.web.form.ReprogramarForm;

@Controller
@RequestMapping("/citas")
@RequiredArgsConstructor
public class CitaController {

    private final CitaService citaService;
    private final PacienteService pacienteService;
    private final OdontologoService odontologoService;
    private final ServicioService servicioService;
    private final RecordatorioService recordatorioService;
    private final UsuarioService usuarioService;

    // ------------------------------------------------------------------
    // Listado
    // ------------------------------------------------------------------

    @GetMapping
    public String listar(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                         @RequestParam(required = false) EstadoCita estado,
                         @RequestParam(required = false) Long idOdontologo,
                         Authentication authentication,
                         Model model) {
        LocalDate dia = fecha != null ? fecha : LocalDate.now();
        Long filtro = filtrarOdontologo(authentication, idOdontologo);

        model.addAttribute("citas", citaService.listar(dia, dia, filtro, estado));
        model.addAttribute("fecha", dia);
        model.addAttribute("estado", estado);
        model.addAttribute("idOdontologo", filtro);
        model.addAttribute("estados", EstadoCita.values());
        model.addAttribute("odontologos", odontologoService.listarActivos());
        return "citas/lista";
    }

    // ------------------------------------------------------------------
    // Creacion
    // ------------------------------------------------------------------

    @GetMapping("/nueva")
    public String nueva(@RequestParam(required = false) Long idPaciente, Model model) {
        CitaForm form = new CitaForm();
        form.setFecha(LocalDate.now());
        form.setIdPaciente(idPaciente);
        prepararFormulario(model, form);
        return "citas/formulario";
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute("form") CitaForm form, BindingResult resultado,
                        Model model, RedirectAttributes redirect) {
        if (resultado.hasErrors()) {
            prepararFormulario(model, form);
            return "citas/formulario";
        }
        try {
            Cita cita = citaService.reservar(form.getIdPaciente(), form.getIdOdontologo(),
                    form.getIdServicio(), LocalDateTime.of(form.getFecha(), form.getHora()),
                    form.getMotivo());
            redirect.addFlashAttribute("exito", "Cita #" + cita.getId() + " reservada para el "
                    + form.getFecha() + " a las " + form.getHora() + ".");
            return "redirect:/citas/" + cita.getId();
        } catch (ReglaNegocioException ex) {
            model.addAttribute("error", ex.getMessage());
            prepararFormulario(model, form);
            return "citas/formulario";
        }
    }

    // ------------------------------------------------------------------
    // Detalle
    // ------------------------------------------------------------------

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model, RedirectAttributes redirect) {
        try {
            Cita cita = citaService.obtener(id);
            model.addAttribute("cita", cita);
            model.addAttribute("notificaciones", recordatorioService.deCita(id));
            return "citas/detalle";
        } catch (ReglaNegocioException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
            return "redirect:/citas";
        }
    }

    // ------------------------------------------------------------------
    // Acciones sobre la cita
    // ------------------------------------------------------------------

    @PostMapping("/{id}/confirmar")
    public String confirmar(@PathVariable Long id, RedirectAttributes redirect) {
        return ejecutar(id, redirect, () -> citaService.confirmar(id), "Cita confirmada correctamente.");
    }

    @PostMapping("/{id}/cancelar")
    public String cancelar(@PathVariable Long id, @RequestParam(required = false) String motivo,
                           RedirectAttributes redirect) {
        return ejecutar(id, redirect, () -> citaService.cancelar(id, motivo),
                "Cita cancelada. El horario quedo liberado para otro paciente.");
    }

    @PostMapping("/{id}/atendida")
    public String atender(@PathVariable Long id, RedirectAttributes redirect) {
        return ejecutar(id, redirect, () -> citaService.marcarAtendida(id), "Cita marcada como atendida.");
    }

    @PostMapping("/{id}/ausente")
    public String ausente(@PathVariable Long id, RedirectAttributes redirect) {
        return ejecutar(id, redirect, () -> citaService.marcarAusente(id), "Cita marcada como ausente.");
    }

    @PostMapping("/{id}/recordatorio")
    public String reenviarRecordatorio(@PathVariable Long id, RedirectAttributes redirect) {
        return ejecutar(id, redirect, () -> {
            Cita cita = citaService.obtener(id);
            recordatorioService.programarRecordatorio(cita);
            return cita;
        }, "Recordatorio programado nuevamente.");
    }

    // ------------------------------------------------------------------
    // Reprogramacion
    // ------------------------------------------------------------------

    @GetMapping("/{id}/reprogramar")
    public String reprogramarFormulario(@PathVariable Long id, Model model, RedirectAttributes redirect) {
        try {
            Cita cita = citaService.obtener(id);
            ReprogramarForm form = new ReprogramarForm();
            form.setFecha(LocalDate.now());
            model.addAttribute("cita", cita);
            model.addAttribute("form", form);
            return "citas/reprogramar";
        } catch (ReglaNegocioException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
            return "redirect:/citas";
        }
    }

    @PostMapping("/{id}/reprogramar")
    public String reprogramar(@PathVariable Long id, @Valid @ModelAttribute("form") ReprogramarForm form,
                              BindingResult resultado, Model model, RedirectAttributes redirect) {
        Cita cita = null;
        try {
            cita = citaService.obtener(id);
        } catch (ReglaNegocioException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
            return "redirect:/citas";
        }
        if (resultado.hasErrors()) {
            model.addAttribute("cita", cita);
            return "citas/reprogramar";
        }
        try {
            LocalDateTime nuevoInicio = LocalDateTime.of(form.getFecha(), form.getHora());
            Cita nueva = citaService.reprogramar(id, nuevoInicio);
            redirect.addFlashAttribute("exito", "La cita se reprogramo. Nueva cita #" + nueva.getId()
                    + " para el " + form.getFecha() + " a las " + form.getHora() + ".");
            return "redirect:/citas/" + nueva.getId();
        } catch (ReglaNegocioException ex) {
            model.addAttribute("cita", cita);
            model.addAttribute("error", ex.getMessage());
            return "citas/reprogramar";
        }
    }

    // ------------------------------------------------------------------
    // Apoyo
    // ------------------------------------------------------------------

    private void prepararFormulario(Model model, CitaForm form) {
        model.addAttribute("form", form);
        model.addAttribute("pacientes", pacienteService.listar());
        model.addAttribute("odontologos", odontologoService.listarActivos());
        model.addAttribute("servicios", servicioService.listarActivos());
    }

    private Long filtrarOdontologo(Authentication authentication, Long solicitado) {
        Usuario usuario = usuarioService.actual(authentication);
        if (usuario != null && usuario.getRol() == Rol.ODONTOLOGO
                && usuario.getPersona() instanceof Odontologo odontologo) {
            return odontologo.getId();
        }
        return solicitado;
    }

    private String ejecutar(Long id, RedirectAttributes redirect,
                            java.util.function.Supplier<?> accion, String mensaje) {
        try {
            accion.get();
            redirect.addFlashAttribute("exito", mensaje);
        } catch (ReglaNegocioException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/citas/" + id;
    }
}
