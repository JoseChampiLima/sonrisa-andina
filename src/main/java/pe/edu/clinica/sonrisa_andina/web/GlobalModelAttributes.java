package pe.edu.clinica.sonrisa_andina.web;

import java.time.LocalDate;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import lombok.RequiredArgsConstructor;
import pe.edu.clinica.sonrisa_andina.domain.Usuario;
import pe.edu.clinica.sonrisa_andina.service.RecordatorioService;
import pe.edu.clinica.sonrisa_andina.service.UsuarioService;

/**
 * Datos comunes a todas las vistas: usuario autenticado, fecha actual y
 * contador de recordatorios pendientes.
 */
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAttributes {

    private final UsuarioService usuarioService;
    private final RecordatorioService recordatorioService;

    @ModelAttribute("usuarioActual")
    public Usuario usuarioActual(Authentication authentication) {
        return usuarioService.actual(authentication);
    }

    @ModelAttribute("hoy")
    public LocalDate hoy() {
        return LocalDate.now();
    }

    @ModelAttribute("pendientesCount")
    public long pendientesCount(Authentication authentication) {
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken
                || !authentication.isAuthenticated()) {
            return 0;
        }
        return recordatorioService.contarPendientes();
    }
}
