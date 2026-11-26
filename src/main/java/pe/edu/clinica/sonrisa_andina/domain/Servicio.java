package pe.edu.clinica.sonrisa_andina.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Servicio odontologico del catalogo. Su duracion define el rango que
 * ocupara la cita en la agenda.
 */
@Entity
@Table(name = "servicio")
@Getter
@Setter
@NoArgsConstructor
public class Servicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_servicio")
    private Long id;

    @Column(name = "nombre", nullable = false, unique = true, length = 80)
    private String nombre;

    @Column(name = "duracion_min", nullable = false)
    private Integer duracionMin;

    @Column(name = "precio", nullable = false, precision = 8, scale = 2)
    private BigDecimal precio = BigDecimal.ZERO;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "servicio_especialidad",
            joinColumns = @JoinColumn(name = "id_servicio"),
            inverseJoinColumns = @JoinColumn(name = "id_especialidad"))
    private Set<Especialidad> especialidades = new HashSet<>();

    public Servicio(String nombre, Integer duracionMin, BigDecimal precio) {
        this.nombre = nombre;
        this.duracionMin = duracionMin;
        this.precio = precio;
        this.activo = true;
    }

    /** Hora de fin de la cita segun la duracion del servicio. */
    public LocalDateTime calcularFin(LocalDateTime inicio) {
        return inicio.plusMinutes(duracionMin);
    }

    /** Un odontologo puede realizar el servicio si su especialidad esta habilitada. */
    public boolean puedeRealizarlo(Odontologo odontologo) {
        if (odontologo == null || odontologo.getEspecialidad() == null) {
            return false;
        }
        Long idEspecialidad = odontologo.getEspecialidad().getId();
        return especialidades.stream().anyMatch(e -> e.getId().equals(idEspecialidad));
    }

    @Override
    public String toString() {
        return nombre;
    }
}
