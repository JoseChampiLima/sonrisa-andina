package pe.edu.clinica.sonrisa_andina.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Paciente de la clinica. Hereda de Persona y agrega los datos necesarios
 * para los recordatorios y la trazabilidad del registro.
 */
@Entity
@Table(name = "paciente")
@PrimaryKeyJoinColumn(name = "id_persona")
@Getter
@Setter
@NoArgsConstructor
public class Paciente extends Persona {

    @Column(name = "correo", unique = true, length = 120)
    private String correo;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    public Paciente(String dni, String nombres, String apellidos, String telefono, String correo) {
        setDni(dni);
        setNombres(nombres);
        setApellidos(apellidos);
        setTelefono(telefono);
        this.correo = correo;
        this.fechaRegistro = LocalDateTime.now();
    }

    @Override
    public String descripcion() {
        return "Paciente " + getNombreCompleto() + " (DNI " + getDni() + ")";
    }
}
