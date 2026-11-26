package pe.edu.clinica.sonrisa_andina.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Profesional odontologo. Hereda de Persona y se especializa con su
 * especialidad principal, numero de colegiatura y estado de disponibilidad.
 */
@Entity
@Table(name = "odontologo")
@PrimaryKeyJoinColumn(name = "id_persona")
@Getter
@Setter
@NoArgsConstructor
public class Odontologo extends Persona {

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_especialidad", nullable = false)
    private Especialidad especialidad;

    @Column(name = "nro_colegiatura", nullable = false, unique = true, length = 10)
    private String nroColegiatura;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    public Odontologo(String dni, String nombres, String apellidos, String telefono,
                      Especialidad especialidad, String nroColegiatura) {
        setDni(dni);
        setNombres(nombres);
        setApellidos(apellidos);
        setTelefono(telefono);
        this.especialidad = especialidad;
        this.nroColegiatura = nroColegiatura;
        this.activo = true;
    }

    @Override
    public String descripcion() {
        return "Dr(a). " + getNombreCompleto() + " - " + especialidad.getNombre();
    }
}
