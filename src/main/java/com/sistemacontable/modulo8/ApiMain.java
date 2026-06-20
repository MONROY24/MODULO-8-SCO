package com.sistemacontable.modulo8;

import com.sistemacontable.modulo8.controlador.AnalisisRestController;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiMain {
    private static final Logger log = LoggerFactory.getLogger(ApiMain.class);

    public static void main(String[] args) {
        // Obtener el puerto desde la variable de entorno de Render, o usar 8080 localmente
        int port = 8080;
        String envPort = System.getenv("PORT");
        if (envPort != null && !envPort.isEmpty()) {
            port = Integer.parseInt(envPort);
        }

        Javalin app = Javalin.create(config -> {
            // Configurar Javalin para servir los archivos estáticos desde src/main/resources/public
            config.staticFiles.add("/public", Location.CLASSPATH);
            
            // Habilitar CORS si es necesario (útil para pruebas)
            config.bundledPlugins.enableCors(cors -> {
                cors.addRule(it -> {
                    it.anyHost();
                });
            });
        }).start(port);

        log.info("Servidor Javalin iniciado en el puerto: {}", port);

        // Inicializar el controlador REST
        AnalisisRestController controller = new AnalisisRestController();

        // Endpoints API
        app.get("/api/empresas", controller::getEmpresas);
        app.get("/api/analisis/{id}", controller::getAnalisis);
        app.get("/api/exportar-pdf/{id}", controller::exportarPdf);

        // Manejo de excepciones globales
        app.exception(Exception.class, (e, ctx) -> {
            log.error("Error no manejado en la API", e);
            ctx.status(500);
            ctx.json(new ErrorResponse(e.getMessage()));
        });
        
        app.exception(IllegalArgumentException.class, (e, ctx) -> {
            log.warn("Petición inválida: {}", e.getMessage());
            ctx.status(400);
            ctx.json(new ErrorResponse(e.getMessage()));
        });
        
        app.exception(IllegalStateException.class, (e, ctx) -> {
            log.warn("Estado inválido: {}", e.getMessage());
            ctx.status(409); // Conflict / Estado inválido
            ctx.json(new ErrorResponse(e.getMessage()));
        });
    }

    // Clase auxiliar para devolver errores en formato JSON
    public static class ErrorResponse {
        public String error;
        public ErrorResponse(String error) {
            this.error = error;
        }
    }
}
