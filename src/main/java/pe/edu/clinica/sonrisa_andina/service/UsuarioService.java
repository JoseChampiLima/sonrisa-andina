package pe.edu.clinica.sonrisa_andina.service;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import pe.edu.clinica.sonrisa_andina.domain.Paciente;
import pe.edu.clinica.sonrisa_andina.domain.Rol;
import pe.edu.clinica.sonrisa_andina.domain.Usuario;
import pe.edu.clinica.sonrisa_andina.exception.ReglaNegocioException;
import pe.edu.clinica.sonrisa_andina.repository.PacienteRepository;
import pe.edu.clinica.sonrisa_andina.repository.UsuarioRepository;

/**
 * Puente entre la tabla USUARIO y Spring Security. Ademas centraliza el
 * registro de pacientes con acceso al portal.
 */
@Service
@RequiredArgsConstructor
public class UsuarioService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final PacienteRepository pacienteRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
        if (!usuario.isActivo()) {
            throw new UsernameNotFoundException("La cuenta esta desactivada.");
        }
        return User.withUsername(usuario.getUsername())
                .password(usuario.getPasswordHash())
                .roles(usuario.getRol().name())
                .build();
    }

    /** Usuario autenticado en la peticion actual (o null si es anonimo). */
    public Usuario actual(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }
        return usuarioRepository.findByUsername(authentication.getName()).orElse(null);
    }

    public Usuario obtenerPorUsername(String username) {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ReglaNegocioException("El usuario no existe."));
    }

    /** Registra un paciente y su cuenta de acceso al portal. */
    @Transactional
    public Paciente registrarPaciente(String dni, String nombres, String apellidos, String telefono,
                                      String correo, String password) {
        if (pacienteRepository.existsByDni(dni)) {
            throw new ReglaNegocioException("Ya existe un paciente con el DNI " + dni + ".");
        }
        if (correo != null && !correo.isBlank() && pacienteRepository.existsByCorreo(correo)) {
            throw new ReglaNegocioException("El correo " + correo + " ya esta registrado.");
        }
        Paciente paciente = new Paciente(dni, nombres, apellidos, telefono,
                correo == null || correo.isBlank() ? null : correo.trim().toLowerCase());
        pacienteRepository.save(paciente);

        guardarCuentaPaciente(paciente, paciente.getCorreo() != null ? paciente.getCorreo() : dni, password);
        return paciente;
    }

    public Optional<String> usuarioVinculado(Long pacienteId) {
        return usuarioRepository.findByPersonaId(pacienteId).map(Usuario::getUsername);
    }

    public String usuarioSugerido(Paciente paciente) {
        String correo = paciente.getCorreo();
        return correo != null && correo.length() <= 60 ? correo : paciente.getDni();
    }

    @Transactional
    public void crearAccesoPaciente(Long pacienteId, String password) {
        if (password.isBlank() || password.length() < 8
                || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ReglaNegocioException("La contraseña debe tener al menos 8 caracteres y como máximo 72 bytes.");
        }
        Paciente paciente = pacienteRepository.buscarParaActualizar(pacienteId)
                .orElseThrow(() -> new ReglaNegocioException("El paciente no existe."));
        if (usuarioRepository.existsByPersonaId(pacienteId)) {
            throw new ReglaNegocioException("El paciente ya tiene una cuenta vinculada.");
        }
        guardarCuentaPaciente(paciente, usuarioSugerido(paciente), password);
    }

    private void guardarCuentaPaciente(Paciente paciente, String username, String password) {
        if (usuarioRepository.existsByUsername(username)) {
            throw new ReglaNegocioException("Ya existe una cuenta con el usuario " + username + ".");
        }
        Usuario usuario = new Usuario(username, passwordEncoder.encode(password), Rol.PACIENTE, paciente);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void cambiarPassword(Long idUsuario, String nuevaPassword) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ReglaNegocioException("El usuario no existe."));
        usuario.setPasswordHash(passwordEncoder.encode(nuevaPassword));
        usuarioRepository.save(usuario);
    }
}
