package pe.edu.clinica.sonrisa_andina.service.notificacion;

import pe.edu.clinica.sonrisa_andina.domain.CanalNotificacion;
import pe.edu.clinica.sonrisa_andina.domain.Notificacion;

/**
 * Contrato comun para todos los canales de recordatorio (POLIMORFISMO).
 * El servicio de recordatorios trabaja contra esta interfaz y no conoce
 * la implementacion concreta: agregar un canal nuevo (por ejemplo SMS)
 * solo requiere crear otra clase que implemente esta interfaz.
 */
public interface Notificador {

    CanalNotificacion canal();

    void enviar(Notificacion notificacion) throws Exception;
}
