import io.swagger.jaxrs.config.BeanConfig;
import org.apache.log4j.Logger;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.StaticHttpHandler;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import services.GameService;

import java.io.IOException;
import java.net.URI;

public class Main {
    public static final String BASE_URI = "http://localhost:8080/dsaApp/";
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
        beanConfig.setResourcePackage(GameService.class.getPackage() == null ? "" : GameService.class.getPackage().getName());
        beanConfig.setTitle("Tower Defence REST API");
        beanConfig.setVersion("1.0.0");
        beanConfig.setDescription("REST API for Game Management");
        beanConfig.setScan(true);

        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
    }

    public static void main(String[] args) throws IOException {
        final HttpServer server = startServer();
        StaticHttpHandler staticHttpHandler = new StaticHttpHandler("./public/");
        server.getServerConfiguration().addHttpHandler(staticHttpHandler, "/");

        logger.info("REST server started at " + BASE_URI);
        System.in.read();
        server.stop();
    }
}
