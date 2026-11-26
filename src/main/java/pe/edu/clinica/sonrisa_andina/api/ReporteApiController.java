package pe.edu.clinica.sonrisa_andina.api;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import pe.edu.clinica.sonrisa_andina.api.dto.InasistenciaResponse;
import pe.edu.clinica.sonrisa_andina.service.ReporteService;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
public class ReporteApiController {

    private final ReporteService reporteService;

    @GetMapping("/inasistencia")
    public InasistenciaResponse inasistencia(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {

        LocalDate fin = hasta != null ? hasta : LocalDate.now();
        LocalDate inicio = desde != null ? desde : fin.minusDays(30);
        ReporteService.Resumen resumen = reporteService.resumen(inicio, fin);
        return new InasistenciaResponse(resumen.desde(), resumen.hasta(), resumen.total(),
                resumen.atendidas(), resumen.ausentes(), resumen.canceladas(),
                resumen.tasaInasistencia(), resumen.tasaCancelacion());
    }
}
