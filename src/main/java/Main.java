import io.swagger.jaxrs.config.BeanConfig;
import org.apache.log4j.Logger;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.StaticHttpHandler;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import services.GameService;

import java.io.IOException;
import java.net.URI;
import java.io.File;

public class Main {
    public static final String BASE_URI = "http://0.0.0.0:8080/dsaApp/";
    final static Logger logger = Logger.getLogger(Main.class);

    public static HttpServer startServer() {
        final ResourceConfig rc = new ResourceConfig()
                .register(GameService.class)
                .register(MyExceptionMapper.class)
                .register(io.swagger.jaxrs.listing.ApiListingResource.class)
                .register(io.swagger.jaxrs.listing.SwaggerSerializers.class);

        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setHost("localhost:8080");
        beanConfig.setBasePath("/dsaApp");
        beanConfig.setResourcePackage("services");
        beanConfig.setScan(true);

        // Creamos el servidor pero NO lo arrancamos aún (start=false)
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc, false);
    }

    public static void main(String[] args) throws IOException {
        final HttpServer server = startServer();

        // Verificamos la ruta de la carpeta public para debug
        File publicFolder = new File("public");
        logger.info("Configuring static handler for: " + publicFolder.getAbsolutePath());
        
        if (!publicFolder.exists()) {
            logger.error("WARNING: 'public' folder NOT FOUND at " + publicFolder.getAbsolutePath());
        }

        // Añadimos el handler de estáticos en la raíz
        StaticHttpHandler staticHttpHandler = new StaticHttpHandler("public");
        server.getServerConfiguration().addHttpHandler(staticHttpHandler, "/");

        // Arrancamos el servidor manualmente
        server.start();

        logger.info("REST server started at " + BASE_URI.replace("0.0.0.0", "localhost"));
        logger.info("Web interface available at http://localhost:8080/login.html");
        
        System.out.println("Press enter to stop the server...");
        System.in.read();
        server.stop();
    }
}
