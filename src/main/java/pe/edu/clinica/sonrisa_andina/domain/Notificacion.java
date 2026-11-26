package pe.edu.clinica.sonrisa_andina.domain;

import java.time.LocalDateTime;

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
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Recordatorio programado para una cita. El envio real lo realiza el
 * servicio de recordatorios usando el Notificador adecuado (polimorfismo).
 */
@Entity
@Table(name = "notificacion")
@Getter
@Setter
@NoArgsConstructor
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_notificacion")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_cita", nullable = false)
    private Cita cita;

    @Enumerated(EnumType.STRING)
    @Column(name = "canal", nullable = false, length = 20)
    private CanalNotificacion canal;

    @Column(name = "destinatario", nullable = false, length = 120)
    private String destinatario;

    @Column(name = "mensaje", nullable = false, length = 500)
    private String mensaje;

    @Column(name = "programada_para", nullable = false)
    private LocalDateTime programadaPara;

    @Column(name = "fecha_envio")
    private LocalDateTime fechaEnvio;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoNotificacion estado = EstadoNotificacion.PENDIENTE;

    public Notificacion(Cita cita, CanalNotificacion canal, String destinatario,
                        String mensaje, LocalDateTime programadaPara) {
        this.cita = cita;
        this.canal = canal;
        this.destinatario = destinatario;
        this.mensaje = mensaje;
        this.programadaPara = programadaPara;
        this.estado = EstadoNotificacion.PENDIENTE;
    }
}
