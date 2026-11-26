package pe.edu.clinica.sonrisa_andina.security;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import pe.edu.clinica.sonrisa_andina.domain.Usuario;

/**
 * Genera y valida los tokens JWT usados por la API REST.
 */
@Service
public class JwtService {

    @Value("${clinica.jwt.secret}")
    private String secret;

    @Value("${clinica.jwt.expiracion-horas:8}")
    private long horasExpiracion;

    public String generarToken(Usuario usuario) {
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + horasExpiracion * 60 * 60 * 1000);
        return Jwts.builder()
                .setSubject(usuario.getUsername())
                .claim("rol", usuario.getRol().name())
                .claim("nombre", usuario.getNombreVisible())
                .setIssuedAt(ahora)
                .setExpiration(expiracion)
                .signWith(clave(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parsear(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(clave())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String username(String token) {
        return parsear(token).getSubject();
    }

    private Key clave() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
