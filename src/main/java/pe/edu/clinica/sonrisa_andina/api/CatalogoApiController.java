package pe.edu.clinica.sonrisa_andina.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import pe.edu.clinica.sonrisa_andina.api.dto.EspecialidadDto;
import pe.edu.clinica.sonrisa_andina.api.dto.OdontologoDto;
import pe.edu.clinica.sonrisa_andina.api.dto.ServicioDto;
import pe.edu.clinica.sonrisa_andina.repository.EspecialidadRepository;
import pe.edu.clinica.sonrisa_andina.service.OdontologoService;
import pe.edu.clinica.sonrisa_andina.service.ServicioService;

/**
 * Catalogos de solo lectura para la API REST y para el formulario de reserva.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CatalogoApiController {

    private final ServicioService servicioService;
    private final OdontologoService odontologoService;
    private final EspecialidadRepository especialidadRepository;

    @GetMapping("/servicios")
    public List<ServicioDto> servicios(@RequestParam(defaultValue = "true") boolean soloActivos) {
        return (soloActivos ? servicioService.listarActivos() : servicioService.listar())
                .stream().map(ServicioDto::de).toList();
    }

    @GetMapping("/odontologos")
    public List<OdontologoDto> odontologos(@RequestParam(required = false) Long especialidad) {
        return (especialidad == null
                ? odontologoService.listarActivos()
                : odontologoService.listarPorEspecialidad(especialidad))
                .stream().map(OdontologoDto::de).toList();
    }

    @GetMapping("/especialidades")
    public List<EspecialidadDto> especialidades() {
        return especialidadRepository.findAllByOrderByNombreAsc().stream()
                .map(EspecialidadDto::de).toList();
    }
}
