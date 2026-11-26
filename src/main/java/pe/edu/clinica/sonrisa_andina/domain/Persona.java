package pe.edu.clinica.sonrisa_andina.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Superclase del dominio que representa a toda persona registrada en la
 * clinica (pacientes y odontologos). Aplica ABSTRACCION y HERENCIA: define
 * los datos comunes y obliga a las subclases a describirse.
 */
@Entity
@Table(name = "persona")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor
public abstract class Persona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_persona")
    private Long id;

    @Column(name = "dni", nullable = false, unique = true, length = 8)
    private String dni;

    @Column(name = "nombres", nullable = false, length = 80)
    private String nombres;

    @Column(name = "apellidos", nullable = false, length = 80)
    private String apellidos;

    @Column(name = "telefono", nullable = false, length = 15)
    private String telefono;

    public String getNombreCompleto() {
        return nombres + " " + apellidos;
    }

    /** Comportamiento polimorfico: cada subtipo se describe a su manera. */
    public abstract String descripcion();
}
