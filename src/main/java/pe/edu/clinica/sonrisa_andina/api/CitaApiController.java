package pe.edu.clinica.sonrisa_andina.api;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import pe.edu.clinica.sonrisa_andina.api.dto.CitaRequest;
import pe.edu.clinica.sonrisa_andina.api.dto.CitaResponse;
import pe.edu.clinica.sonrisa_andina.api.dto.CancelarRequest;
import pe.edu.clinica.sonrisa_andina.api.dto.ReprogramarRequest;
import pe.edu.clinica.sonrisa_andina.domain.Cita;
import pe.edu.clinica.sonrisa_andina.domain.Paciente;
import pe.edu.clinica.sonrisa_andina.domain.Rol;
import pe.edu.clinica.sonrisa_andina.domain.Usuario;
import pe.edu.clinica.sonrisa_andina.exception.ReglaNegocioException;
import pe.edu.clinica.sonrisa_andina.service.CitaService;
import pe.edu.clinica.sonrisa_andina.service.UsuarioService;

@RestController
@RequestMapping("/api/citas")
@RequiredArgsConstructor
public class CitaApiController {

    private final CitaService citaService;
    private final UsuarioService usuarioService;

    @PostMapping
    public ResponseEntity<CitaResponse> reservar(@Valid @RequestBody CitaRequest request,
                                                 Authentication authentication) {
        Long idPaciente = resolverPaciente(request.idPaciente(), authentication);
        Cita cita = citaService.reservar(idPaciente, request.idOdontologo(), request.idServicio(),
                request.inicio(), request.motivo());
        return ResponseEntity.status(HttpStatus.CREATED).body(CitaResponse.de(cita));
    }

    @GetMapping
    public List<CitaResponse> listar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false) Long odontologoId,
            @RequestParam(required = false) Long pacienteId) {

        if (pacienteId != null) {
            return citaService.citasDePaciente(pacienteId).stream().map(CitaResponse::de).toList();
        }
        LocalDate dia = fecha != null ? fecha : LocalDate.now();
        return citaService.agendaDelDia(dia, odontologoId).stream().map(CitaResponse::de).toList();
    }

    @GetMapping("/{id}")
    public CitaResponse obtener(@PathVariable Long id) {
        return CitaResponse.de(citaService.obtener(id));
    }

    @GetMapping("/mis-citas")
    public List<CitaResponse> misCitas(Authentication authentication) {
        Usuario usuario = usuarioService.actual(authentication);
        if (usuario == null || !(usuario.getPersona() instanceof Paciente paciente)) {
            throw new ReglaNegocioException("La cuenta autenticada no tiene una ficha de paciente.");
        }
        return citaService.citasDePaciente(paciente.getId()).stream().map(CitaResponse::de).toList();
    }

    @PutMapping("/{id}/reprogramar")
    public CitaResponse reprogramar(@PathVariable Long id,
                                    @Valid @RequestBody ReprogramarRequest request,
                                    Authentication authentication) {
        validarPropietario(id, authentication);
        return CitaResponse.de(citaService.reprogramar(id, request.inicio()));
    }

    @PutMapping("/{id}/cancelar")
    public CitaResponse cancelar(@PathVariable Long id,
                                 @RequestBody(required = false) CancelarRequest request,
                                 Authentication authentication) {
        validarPropietario(id, authentication);
        String motivo = request != null ? request.motivo() : null;
        return CitaResponse.de(citaService.cancelar(id, motivo));
    }

    // ------------------------------------------------------------------

    private Long resolverPaciente(Long idSolicitado, Authentication authentication) {
        Usuario usuario = usuarioService.actual(authentication);
        if (usuario != null && usuario.getRol() == Rol.PACIENTE) {
            if (!(usuario.getPersona() instanceof Paciente paciente)) {
                throw new ReglaNegocioException("La cuenta no tiene una ficha de paciente asociada.");
            }
            return paciente.getId();
        }
        if (idSolicitado == null) {
            throw new ReglaNegocioException("Debe indicar el paciente (idPaciente).");
        }
        return idSolicitado;
    }

    private void validarPropietario(Long idCita, Authentication authentication) {
        Usuario usuario = usuarioService.actual(authentication);
        if (usuario == null || usuario.getRol() != Rol.PACIENTE) {
            return;
        }
        Cita cita = citaService.obtener(idCita);
        if (!(usuario.getPersona() instanceof Paciente paciente)
                || !cita.getPaciente().getId().equals(paciente.getId())) {
            throw new ReglaNegocioException("Solo puede modificar sus propias citas.");
        }
    }
}
