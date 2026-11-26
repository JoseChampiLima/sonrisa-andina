package pe.edu.clinica.sonrisa_andina.web;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @RequestMapping(path = "/acceso-denegado", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String accesoDenegado() {
        return "acceso-denegado";
    }
}
