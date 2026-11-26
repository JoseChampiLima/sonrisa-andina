/* =====================================================================
   Sonrisa Andina - comportamiento de la interfaz
   ===================================================================== */
(function () {
    "use strict";

    // ---------------------------------------------------------------
    // Menu lateral en pantallas pequenas
    // ---------------------------------------------------------------
    function initMenu() {
        var boton = document.querySelector("[data-menu-toggle]");
        if (!boton) return;
        boton.addEventListener("click", function () {
            document.body.classList.toggle("nav-abierta");
        });
        document.addEventListener("click", function (evento) {
            if (!document.body.classList.contains("nav-abierta")) return;
            var sidebar = document.querySelector(".sidebar");
            if (sidebar && !sidebar.contains(evento.target) && !boton.contains(evento.target)) {
                document.body.classList.remove("nav-abierta");
            }
        });
    }

    // ---------------------------------------------------------------
    // Confirmacion antes de acciones sensibles
    // ---------------------------------------------------------------
    function initConfirmaciones() {
        document.querySelectorAll("form[data-confirm]").forEach(function (form) {
            form.addEventListener("submit", function (evento) {
                if (!window.confirm(form.dataset.confirm)) {
                    evento.preventDefault();
                }
            });
        });
    }

    // ---------------------------------------------------------------
    // Selector de horarios disponibles (consume /api/disponibilidad)
    // ---------------------------------------------------------------
    function initHorarios() {
        var contenedor = document.getElementById("horarios");
        if (!contenedor) return;

        var lista = document.getElementById("lista-horarios");
        var oculto = document.getElementById("hora");
        var detalle = document.getElementById("horarios-detalle");
        var selectOdontologo = document.querySelector('[name="idOdontologo"]');
        var selectServicio = document.querySelector('[name="idServicio"]');
        var campoFecha = document.querySelector('[name="fecha"]');

        if (!lista || !oculto) return;

        function valorOdontologo() {
            if (contenedor.dataset.odontologo) return contenedor.dataset.odontologo;
            return selectOdontologo ? selectOdontologo.value : "";
        }

        function valorServicio() {
            if (contenedor.dataset.servicio) return contenedor.dataset.servicio;
            return selectServicio ? selectServicio.value : "";
        }

        function mensaje(texto) {
            lista.innerHTML = '<div class="slot-empty">' + texto + "</div>";
        }

        function cargando() {
            lista.innerHTML = '<div class="slot-loading"><span class="spinner"></span>'
                + " Calculando horarios disponibles...</div>";
        }

        function pintar(horarios) {
            lista.innerHTML = "";
            if (!horarios || horarios.length === 0) {
                mensaje("No hay horarios libres para esa fecha. Pruebe otro dia u otro odontologo.");
                return;
            }
            horarios.forEach(function (hora) {
                var boton = document.createElement("button");
                boton.type = "button";
                boton.className = "slot";
                boton.textContent = hora;
                if (oculto.value === hora) {
                    boton.classList.add("selected");
                }
                boton.addEventListener("click", function () {
                    lista.querySelectorAll(".slot").forEach(function (s) {
                        s.classList.remove("selected");
                    });
                    boton.classList.add("selected");
                    oculto.value = hora;
                    if (detalle) {
                        detalle.textContent = "Horario seleccionado: " + hora;
                    }
                });
                lista.appendChild(boton);
            });
        }

        function consultar() {
            var odontologoId = valorOdontologo();
            var servicioId = valorServicio();
            var fecha = campoFecha ? campoFecha.value : "";
            if (!odontologoId || !servicioId || !fecha) {
                mensaje("Seleccione odontologo, servicio y fecha para ver los horarios libres.");
                if (detalle) detalle.textContent = "";
                return;
            }
            cargando();
            var url = contenedor.dataset.url + "?odontologoId=" + encodeURIComponent(odontologoId)
                + "&servicioId=" + encodeURIComponent(servicioId)
                + "&fecha=" + encodeURIComponent(fecha);

            fetch(url, { headers: { "Accept": "application/json" }, credentials: "same-origin" })
                .then(function (respuesta) {
                    if (!respuesta.ok) throw new Error("No se pudo consultar la disponibilidad.");
                    return respuesta.json();
                })
                .then(function (datos) {
                    if (detalle) {
                        detalle.textContent = datos.servicio + " - " + datos.duracionMin
                            + " minutos con " + datos.odontologo + ".";
                    }
                    pintar(datos.horarios);
                })
                .catch(function (error) {
                    mensaje(error.message + " Intente nuevamente.");
                });
        }

        ["change", "input"].forEach(function (evento) {
            if (selectOdontologo) selectOdontologo.addEventListener(evento, consultar);
            if (selectServicio) selectServicio.addEventListener(evento, consultar);
        });
        if (campoFecha) campoFecha.addEventListener("change", consultar);

        consultar();
    }

    // ---------------------------------------------------------------
    // Login: rellenar credenciales de demostracion con un clic
    // ---------------------------------------------------------------
    function initDemoLogin() {
        document.querySelectorAll("[data-demo-user]").forEach(function (boton) {
            boton.addEventListener("click", function () {
                var usuario = document.getElementById("username");
                var clave = document.getElementById("password");
                if (usuario) usuario.value = boton.dataset.demoUser;
                if (clave) clave.value = boton.dataset.demoPass;
                if (usuario) usuario.focus();
            });
        });
    }

    document.addEventListener("DOMContentLoaded", function () {
        initMenu();
        initConfirmaciones();
        initHorarios();
        initDemoLogin();
    });
})();
