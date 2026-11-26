package pe.edu.clinica.sonrisa_andina.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import pe.edu.clinica.sonrisa_andina.exception.ReglaNegocioException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad central del sistema. Aplica COMPOSICION (una cita esta compuesta
 * por un paciente, un odontologo y un servicio) y ENCAPSULAMIENTO, porque
 * solo se cambia de estado mediante metodos de negocio validados.
 */
@Entity
@Table(name = "cita",
        uniqueConstraints = @UniqueConstraint(name = "uq_cita_odontologo_inicio",
                columnNames = {"id_odontologo", "inicio"}))
@Getter
@Setter
@NoArgsConstructor
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cita")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_paciente", nullable = false)
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_odontologo", nullable = false)
    private Odontologo odontologo;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_servicio", nullable = false)
    private Servicio servicio;

    @Column(name = "inicio", nullable = false)
    private LocalDateTime inicio;

    @Column(name = "fin", nullable = false)
    private LocalDateTime fin;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoCita estado = EstadoCita.PROGRAMADA;

    @Column(name = "motivo", length = 200)
    private String motivo;

    /** Cita anterior cuando el registro proviene de una reprogramacion. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cita_origen")
    private Cita citaOrigen;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn;

    public Cita(Paciente paciente, Odontologo odontologo, Servicio servicio,
                LocalDateTime inicio, String motivo) {
        this.paciente = paciente;
        this.odontologo = odontologo;
        this.servicio = servicio;
        this.inicio = inicio;
        this.fin = servicio.calcularFin(inicio);
        this.motivo = motivo;
        this.estado = EstadoCita.PROGRAMADA;
    }

    // ------------------------------------------------------------------
    // Metodos de negocio (encapsulamiento)
    // ------------------------------------------------------------------

    public void confirmar() {
        if (estado != EstadoCita.PROGRAMADA) {
            throw new ReglaNegocioException(
                    "Solo se puede confirmar una cita en estado PROGRAMADA. Estado actual: " + estado.getEtiqueta());
        }
        this.estado = EstadoCita.CONFIRMADA;
    }

    public void cancelar() {
        if (estado.esFinal() || estado == EstadoCita.CANCELADA || estado == EstadoCita.REPROGRAMADA) {
            throw new ReglaNegocioException(
                    "La cita ya no se puede cancelar. Estado actual: " + estado.getEtiqueta());
        }
        this.estado = EstadoCita.CANCELADA;
    }

    public void marcarAtendida() {
        if (estado != EstadoCita.PROGRAMADA && estado != EstadoCita.CONFIRMADA) {
            throw new ReglaNegocioException(
                    "Solo una cita programada o confirmada puede marcarse como atendida.");
        }
        this.estado = EstadoCita.ATENDIDA;
    }

    public void marcarAusente() {
        if (estado != EstadoCita.PROGRAMADA && estado != EstadoCita.CONFIRMADA) {
            throw new ReglaNegocioException(
                    "Solo una cita programada o confirmada puede marcarse como ausente.");
        }
        this.estado = EstadoCita.AUSENTE;
    }

    /** Marca esta cita como reprogramada; la nueva se registra aparte. */
    public void marcarReprogramada() {
        if (estado.esFinal() || estado == EstadoCita.CANCELADA) {
            throw new ReglaNegocioException(
                    "No se puede reprogramar una cita " + estado.getEtiqueta().toLowerCase());
        }
        this.estado = EstadoCita.REPROGRAMADA;
    }

    public boolean estaActiva() {
        return estado == EstadoCita.PROGRAMADA || estado == EstadoCita.CONFIRMADA;
    }

    public boolean sePuedeReprogramar() {
        return estaActiva();
    }

    public LocalDate getFecha() {
        return inicio.toLocalDate();
    }

    public LocalTime getHora() {
        return inicio.toLocalTime();
    }

    public Integer getDuracionMin() {
        return servicio.getDuracionMin();
    }

    @PrePersist
    void prePersist() {
        LocalDateTime ahora = LocalDateTime.now();
        if (creadoEn == null) {
            creadoEn = ahora;
        }
        actualizadoEn = ahora;
    }

    @PreUpdate
    void preUpdate() {
        actualizadoEn = LocalDateTime.now();
    }
}
