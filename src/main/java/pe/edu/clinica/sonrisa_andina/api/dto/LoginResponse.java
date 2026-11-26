package pe.edu.clinica.sonrisa_andina.api.dto;

public record LoginResponse(
        String token,
        String tipo,
        long expiraEnSegundos,
        String username,
        String rol,
        String nombre) {
}
