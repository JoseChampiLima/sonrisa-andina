package pe.edu.clinica.sonrisa_andina.service.notificacion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import pe.edu.clinica.sonrisa_andina.domain.CanalNotificacion;
import pe.edu.clinica.sonrisa_andina.domain.Notificacion;

/**
 * Recordatorio por WhatsApp. En el prototipo el envio se simula dejando
 * constancia en el log y en la bandeja de notificaciones del sistema.
 * Para produccion basta con reemplazar el cuerpo de enviar() por una
 * llamada a la API oficial de WhatsApp Business (Cloud API).
 */
@Component
public class WhatsAppNotificador implements Notificador {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppNotificador.class);

    @Override
    public CanalNotificacion canal() {
        return CanalNotificacion.WHATSAPP;
    }

    @Override
    public void enviar(Notificacion notificacion) {
        if (notificacion.getDestinatario() == null || notificacion.getDestinatario().isBlank()) {
            throw new IllegalArgumentException("El numero de telefono del paciente esta vacio.");
        }
        log.info("[WHATSAPP SIMULADO] Para: {} | {}",
                notificacion.getDestinatario(), notificacion.getMensaje());
    }
}
