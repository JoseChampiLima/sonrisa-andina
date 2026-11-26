package pe.edu.clinica.sonrisa_andina.web;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.RequiredArgsConstructor;
import pe.edu.clinica.sonrisa_andina.repository.projection.ConteoPorEtiqueta;
import pe.edu.clinica.sonrisa_andina.service.ReporteService;
import pe.edu.clinica.sonrisa_andina.web.view.Barra;

@Controller
@RequestMapping("/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final ReporteService reporteService;

    @GetMapping
    public String reportes(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            Model model) {

        LocalDate hoy = LocalDate.now();
        LocalDate inicio = desde != null ? desde : hoy.withDayOfMonth(1);
        LocalDate fin = hasta != null ? hasta : hoy.withDayOfMonth(hoy.lengthOfMonth());
        if (fin.isBefore(inicio)) {
            LocalDate temporal = inicio;
            inicio = fin;
            fin = temporal;
        }

        ReporteService.Resumen resumen = reporteService.resumen(inicio, fin);

        model.addAttribute("resumen", resumen);
        model.addAttribute("desde", inicio);
        model.addAttribute("hasta", fin);
        model.addAttribute("barrasServicio", barras(resumen.porServicio()));
        model.addAttribute("barrasOdontologo", barras(resumen.porOdontologo()));
        return "reportes/index";
    }

    private List<Barra> barras(List<ConteoPorEtiqueta> datos) {
        long maximo = datos.stream().mapToLong(ConteoPorEtiqueta::getTotal).max().orElse(0);
        return datos.stream()
                .map(d -> new Barra(d.getEtiqueta(), d.getTotal(),
                        maximo == 0 ? 0 : (int) Math.round(d.getTotal() * 100.0 / maximo)))
                .toList();
    }
}
