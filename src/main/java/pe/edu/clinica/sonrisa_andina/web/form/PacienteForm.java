package pe.edu.clinica.sonrisa_andina.web.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PacienteForm {

    @NotBlank(message = "El DNI es obligatorio.")
    @Pattern(regexp = "\\d{8}", message = "El DNI debe tener exactamente 8 digitos.")
    private String dni;

    @NotBlank(message = "Los nombres son obligatorios.")
    @Size(max = 80, message = "Los nombres no deben superar 80 caracteres.")
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios.")
    @Size(max = 80, message = "Los apellidos no deben superar 80 caracteres.")
    private String apellidos;

    @NotBlank(message = "El telefono es obligatorio.")
    @Pattern(regexp = "\\d{6,15}", message = "El telefono debe tener entre 6 y 15 digitos.")
    private String telefono;

    @Email(message = "El correo no tiene un formato valido.")
    @Size(max = 120, message = "El correo no debe superar 120 caracteres.")
    private String correo;
}
