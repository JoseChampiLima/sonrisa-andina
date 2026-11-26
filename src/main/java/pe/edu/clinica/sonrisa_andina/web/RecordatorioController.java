package pe.edu.clinica.sonrisa_andina.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lombok.RequiredArgsConstructor;
import pe.edu.clinica.sonrisa_andina.exception.ReglaNegocioException;
import pe.edu.clinica.sonrisa_andina.service.RecordatorioService;

@Controller
@RequestMapping("/recordatorios")
@RequiredArgsConstructor
public class RecordatorioController {

    private final RecordatorioService recordatorioService;

    @GetMapping
    public String bandeja(Model model) {
        model.addAttribute("notificaciones", recordatorioService.recientes());
        model.addAttribute("pendientes", recordatorioService.pendientes());
        return "recordatorios/lista";
    }

    @PostMapping("/{id}/enviar")
    public String enviar(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            recordatorioService.enviarAhora(id);
            redirect.addFlashAttribute("exito", "Recordatorio enviado.");
        } catch (ReglaNegocioException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/recordatorios";
    }

    @PostMapping("/{id}/reintentar")
    public String reintentar(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            recordatorioService.reintentar(id);
            redirect.addFlashAttribute("exito", "El recordatorio volvio a la cola de envio.");
        } catch (ReglaNegocioException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/recordatorios";
    }

    @PostMapping("/procesar")
    public String procesar(RedirectAttributes redirect) {
        recordatorioService.procesarPendientes();
        redirect.addFlashAttribute("exito", "Se procesaron los recordatorios pendientes.");
        return "redirect:/recordatorios";
    }
}
