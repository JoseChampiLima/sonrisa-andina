package pe.edu.clinica.sonrisa_andina.api;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import pe.edu.clinica.sonrisa_andina.api.dto.LoginRequest;
import pe.edu.clinica.sonrisa_andina.api.dto.LoginResponse;
import pe.edu.clinica.sonrisa_andina.api.dto.RegistroRequest;
import pe.edu.clinica.sonrisa_andina.domain.Paciente;
import pe.edu.clinica.sonrisa_andina.domain.Usuario;
import pe.edu.clinica.sonrisa_andina.exception.ReglaNegocioException;
import pe.edu.clinica.sonrisa_andina.security.JwtService;
import pe.edu.clinica.sonrisa_andina.service.UsuarioService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthApiController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioService usuarioService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));
            Usuario usuario = usuarioService.obtenerPorUsername(authentication.getName());
            String token = jwtService.generarToken(usuario);
            return ResponseEntity.ok(new LoginResponse(token, "Bearer", 8 * 3600,
                    usuario.getUsername(), usuario.getRol().name(), usuario.getNombreVisible()));
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Usuario o contrasena incorrectos."));
        }
    }

    @PostMapping("/registro")
    public ResponseEntity<?> registro(@Valid @RequestBody RegistroRequest request) {
        try {
            Paciente paciente = usuarioService.registrarPaciente(request.dni(), request.nombres(),
                    request.apellidos(), request.telefono(), request.correo(), request.password());
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "mensaje", "Paciente registrado correctamente.",
                    "idPaciente", paciente.getId(),
                    "username", paciente.getCorreo() != null ? paciente.getCorreo() : paciente.getDni()));
        } catch (ReglaNegocioException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }
}
