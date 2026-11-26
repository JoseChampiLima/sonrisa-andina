package pe.edu.clinica.sonrisa_andina.web;

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
import pe.edu.clinica.sonrisa_andina.domain.Servicio;
import pe.edu.clinica.sonrisa_andina.exception.ReglaNegocioException;
import pe.edu.clinica.sonrisa_andina.repository.EspecialidadRepository;
import pe.edu.clinica.sonrisa_andina.service.ServicioService;
import pe.edu.clinica.sonrisa_andina.web.form.ServicioForm;

@Controller
@RequestMapping("/servicios")
@RequiredArgsConstructor
public class ServicioController {

    private final ServicioService servicioService;
    private final EspecialidadRepository especialidadRepository;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("servicios", servicioService.listar());
        return "servicios/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("form", new ServicioForm());
        model.addAttribute("modo", "crear");
        model.addAttribute("especialidades", especialidadRepository.findAllByOrderByNombreAsc());
        return "servicios/formulario";
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute("form") ServicioForm form, BindingResult resultado,
                        Model model, RedirectAttributes redirect) {
        if (resultado.hasErrors()) {
            model.addAttribute("modo", "crear");
            model.addAttribute("especialidades", especialidadRepository.findAllByOrderByNombreAsc());
            return "servicios/formulario";
        }
        try {
            Servicio servicio = servicioService.crear(form);
            redirect.addFlashAttribute("exito", "Servicio \"" + servicio.getNombre() + "\" registrado.");
            return "redirect:/servicios";
        } catch (ReglaNegocioException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("modo", "crear");
            model.addAttribute("especialidades", especialidadRepository.findAllByOrderByNombreAsc());
            return "servicios/formulario";
        }
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes redirect) {
        try {
            Servicio servicio = servicioService.obtener(id);
            model.addAttribute("form", servicioService.aFormulario(servicio));
            model.addAttribute("servicio", servicio);
            model.addAttribute("modo", "editar");
            model.addAttribute("especialidades", especialidadRepository.findAllByOrderByNombreAsc());
            return "servicios/formulario";
        } catch (ReglaNegocioException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
            return "redirect:/servicios";
        }
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Long id, @Valid @ModelAttribute("form") ServicioForm form,
                             BindingResult resultado, Model model, RedirectAttributes redirect) {
        if (resultado.hasErrors()) {
            model.addAttribute("modo", "editar");
            model.addAttribute("servicio", servicioService.obtener(id));
            model.addAttribute("especialidades", especialidadRepository.findAllByOrderByNombreAsc());
            return "servicios/formulario";
        }
        try {
            servicioService.actualizar(id, form);
            redirect.addFlashAttribute("exito", "Servicio actualizado.");
            return "redirect:/servicios";
        } catch (ReglaNegocioException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("modo", "editar");
            model.addAttribute("servicio", servicioService.obtener(id));
            model.addAttribute("especialidades", especialidadRepository.findAllByOrderByNombreAsc());
            return "servicios/formulario";
        }
    }

    @PostMapping("/{id}/estado")
    public String cambiarEstado(@PathVariable Long id, @RequestParam boolean activo,
                                RedirectAttributes redirect) {
        try {
            servicioService.cambiarEstado(id, activo);
            redirect.addFlashAttribute("exito", activo
                    ? "El servicio vuelve a estar disponible para reservas."
                    : "El servicio fue desactivado del catalogo de reservas.");
        } catch (ReglaNegocioException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/servicios";
    }
}
