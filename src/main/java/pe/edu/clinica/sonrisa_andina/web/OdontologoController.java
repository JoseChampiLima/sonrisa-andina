package pe.edu.clinica.sonrisa_andina.web;

import java.util.List;

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
import pe.edu.clinica.sonrisa_andina.domain.Odontologo;
import pe.edu.clinica.sonrisa_andina.exception.ReglaNegocioException;
import pe.edu.clinica.sonrisa_andina.repository.EspecialidadRepository;
import pe.edu.clinica.sonrisa_andina.service.CitaService;
import pe.edu.clinica.sonrisa_andina.service.OdontologoService;
import pe.edu.clinica.sonrisa_andina.web.form.OdontologoForm;

@Controller
@RequestMapping("/odontologos")
@RequiredArgsConstructor
public class OdontologoController {

    private final OdontologoService odontologoService;
    private final EspecialidadRepository especialidadRepository;
    private final CitaService citaService;

    public record DiaOpcion(int valor, String nombre) {
    }

    private static final List<DiaOpcion> DIAS = List.of(
            new DiaOpcion(1, "Lunes"), new DiaOpcion(2, "Martes"), new DiaOpcion(3, "Miercoles"),
            new DiaOpcion(4, "Jueves"), new DiaOpcion(5, "Viernes"), new DiaOpcion(6, "Sabado"),
            new DiaOpcion(7, "Domingo"));

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("odontologos", odontologoService.listar());
        return "odontologos/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("form", new OdontologoForm());
        model.addAttribute("modo", "crear");
        preparar(model);
        return "odontologos/formulario";
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute("form") OdontologoForm form, BindingResult resultado,
                        Model model, RedirectAttributes redirect) {
        if (resultado.hasErrors()) {
            model.addAttribute("modo", "crear");
            preparar(model);
            return "odontologos/formulario";
        }
        try {
            Odontologo odontologo = odontologoService.crear(form);
            redirect.addFlashAttribute("exito", "Odontologo " + odontologo.getNombreCompleto()
                    + " registrado con su horario de atencion.");
            return "redirect:/odontologos";
        } catch (ReglaNegocioException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("modo", "crear");
            preparar(model);
            return "odontologos/formulario";
        }
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes redirect) {
        try {
            Odontologo odontologo = odontologoService.obtener(id);
            model.addAttribute("form", odontologoService.aFormulario(odontologo));
            model.addAttribute("odontologo", odontologo);
            model.addAttribute("modo", "editar");
            preparar(model);
            return "odontologos/formulario";
        } catch (ReglaNegocioException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
            return "redirect:/odontologos";
        }
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Long id, @Valid @ModelAttribute("form") OdontologoForm form,
                             BindingResult resultado, Model model, RedirectAttributes redirect) {
        try {
            Odontologo odontologo = odontologoService.obtener(id);
            if (resultado.hasErrors()) {
                model.addAttribute("odontologo", odontologo);
                model.addAttribute("modo", "editar");
                preparar(model);
                return "odontologos/formulario";
            }
            odontologoService.actualizar(id, form);
            redirect.addFlashAttribute("exito", "Datos y horario del odontologo actualizados.");
            return "redirect:/odontologos";
        } catch (ReglaNegocioException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("modo", "editar");
            model.addAttribute("odontologo", new Odontologo());
            preparar(model);
            return "odontologos/formulario";
        }
    }

    @PostMapping("/{id}/estado")
    public String cambiarEstado(@PathVariable Long id, @RequestParam boolean activo,
                                RedirectAttributes redirect) {
        try {
            odontologoService.cambiarEstado(id, activo);
            redirect.addFlashAttribute("exito", activo
                    ? "El odontologo vuelve a recibir citas."
                    : "El odontologo fue desactivado; ya no aparece en la agenda.");
        } catch (ReglaNegocioException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/odontologos";
    }

    @GetMapping("/{id}/agenda")
    public String agenda(@PathVariable Long id, Model model) {
        Odontologo odontologo = odontologoService.obtener(id);
        model.addAttribute("odontologo", odontologo);
        model.addAttribute("horarios", odontologoService.horarios(id));
        model.addAttribute("citas", citaService.agendaDelDia(java.time.LocalDate.now(), id));
        return "odontologos/agenda";
    }

    private void preparar(Model model) {
        model.addAttribute("especialidades", especialidadRepository.findAllByOrderByNombreAsc());
        model.addAttribute("diasSemana", DIAS);
    }
}
