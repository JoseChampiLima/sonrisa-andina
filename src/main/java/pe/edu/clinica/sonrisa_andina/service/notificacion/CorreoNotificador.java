package pe.edu.clinica.sonrisa_andina.service.notificacion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import pe.edu.clinica.sonrisa_andina.domain.CanalNotificacion;
import pe.edu.clinica.sonrisa_andina.domain.Notificacion;

/**
 * Envia recordatorios por correo electronico. Si no existe un servidor SMTP
 * configurado, el envio se simula y queda registrado en el log: el sistema
 * sigue siendo demostrable sin credenciales reales.
 */
@Component
@RequiredArgsConstructor
public class CorreoNotificador implements Notificador {

    private static final Logger log = LoggerFactory.getLogger(CorreoNotificador.class);

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${spring.mail.host:}")
    private String host;

    @Override
    public CanalNotificacion canal() {
        return CanalNotificacion.CORREO;
    }

    @Override
    public void enviar(Notificacion notificacion) throws Exception {
        String destino = notificacion.getDestinatario();
        if (destino == null || !destino.contains("@")) {
            throw new IllegalArgumentException("El destinatario no es un correo valido: " + destino);
        }

        JavaMailSender sender = mailSenderProvider.getIfAvailable();
        if (sender == null || host == null || host.isBlank()) {
            log.info("[CORREO SIMULADO] Para: {} | {}", destino, notificacion.getMensaje());
            return;
        }

        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setTo(destino);
        mensaje.setSubject("Recordatorio de su cita - Clinica Dental Sonrisa Andina");
        mensaje.setText(notificacion.getMensaje());
        sender.send(mensaje);
        log.info("[CORREO ENVIADO] Para: {} | cita #{}", destino, notificacion.getCita().getId());
    }
}
