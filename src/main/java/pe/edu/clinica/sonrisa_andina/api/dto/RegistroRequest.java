package pe.edu.clinica.sonrisa_andina.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegistroRequest(
        @NotBlank @Pattern(regexp = "\\d{8}", message = "El DNI debe tener 8 digitos.") String dni,
        @NotBlank String nombres,
        @NotBlank String apellidos,
        @NotBlank @Pattern(regexp = "\\d{6,15}", message = "Telefono invalido.") String telefono,
        @Email(message = "Correo invalido.") String correo,
        @NotBlank @Size(min = 6, message = "La contrasena debe tener al menos 6 caracteres.") String password) {
}
