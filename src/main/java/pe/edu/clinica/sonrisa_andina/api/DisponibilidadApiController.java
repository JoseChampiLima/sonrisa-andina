package pe.edu.clinica.sonrisa_andina.api;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import pe.edu.clinica.sonrisa_andina.api.dto.DisponibilidadResponse;
import pe.edu.clinica.sonrisa_andina.domain.Odontologo;
import pe.edu.clinica.sonrisa_andina.domain.Servicio;
import pe.edu.clinica.sonrisa_andina.service.DisponibilidadService;
import pe.edu.clinica.sonrisa_andina.service.OdontologoService;
import pe.edu.clinica.sonrisa_andina.service.ServicioService;

/**
 * RF-04: devuelve unicamente los horarios libres del odontologo para el
 * servicio y la fecha indicados.
 */
@RestController
@RequestMapping("/api/disponibilidad")
@RequiredArgsConstructor
public class DisponibilidadApiController {

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final DisponibilidadService disponibilidadService;
    private final OdontologoService odontologoService;
    private final ServicioService servicioService;

    @GetMapping
    public DisponibilidadResponse disponibilidad(
            @RequestParam Long odontologoId,
            @RequestParam Long servicioId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {

        Odontologo odontologo = odontologoService.obtener(odontologoId);
        Servicio servicio = servicioService.obtener(servicioId);
        List<String> horarios = disponibilidadService.horariosLibres(odontologo, servicio, fecha)
                .stream().map(h -> h.format(HORA)).toList();

        return new DisponibilidadResponse(odontologo.getId(), odontologo.getNombreCompleto(),
                servicio.getId(), servicio.getNombre(), servicio.getDuracionMin(), fecha, horarios);
    }
}
