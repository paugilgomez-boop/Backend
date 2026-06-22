import io.swagger.jaxrs.config.BeanConfig;
import java.io.File;
import java.io.IOException;
import java.net.BindException;
import java.net.URI;
import org.apache.log4j.Logger;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.StaticHttpHandler;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import services.CorsFilter;
import services.FaqAssistantClient;
import services.ForumService;
import services.GameService;
import services.TeamService;
import services.UnityGameService;

public class Main {
  private static final String BIND_HOST = config("server.bindHost", "SERVER_BIND_HOST", "0.0.0.0");
  private static final String BIND_PORT = config("server.port", "SERVER_PORT", "8080");
  public static final String PUBLIC_HOST =
      config("server.publicHost", "SERVER_PUBLIC_HOST", "dsa3.upc.edu");
  public static final String BASE_URI =
      "http://" + BIND_HOST + ":" + BIND_PORT + "/dsaApp/";

  final static Logger logger = Logger.getLogger(Main.class);

  private static BindException findBindException(Throwable error) {
    Throwable current = error;
    while (current != null) {
      if (current instanceof BindException) {
        return (BindException) current;
      }
      current = current.getCause();
    }
    return null;
  }

  private static String config(String systemProperty, String envVar, String defaultValue) {
    String value = System.getProperty(systemProperty);
    if (value == null || value.isEmpty()) {
      value = System.getenv(envVar);
    }
    if (value == null || value.isEmpty()) {
      return defaultValue;
    }
    return value;
  }

  public static HttpServer startServer() {
    final ResourceConfig rc =
        new ResourceConfig()
            .register(GameService.class)
            .register(TeamService.class)
            .register(ForumService.class)
            .register(UnityGameService.class)
            .register(CorsFilter.class)
            .register(MyExceptionMapper.class)
            .register(io.swagger.jaxrs.listing.ApiListingResource.class)
            .register(io.swagger.jaxrs.listing.SwaggerSerializers.class);

    BeanConfig beanConfig = new BeanConfig();
    beanConfig.setHost(PUBLIC_HOST);
    beanConfig.setBasePath("/dsaApp");
    beanConfig.setResourcePackage("services");
    beanConfig.setScan(true);

    return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc, false);
  }

  public static void main(String[] args) throws IOException {

    logger.info("Binding REST API to " + BASE_URI);

    final HttpServer server = startServer();

    File publicFolder = new File("public");
    logger.info("Configuring static handler for: " + publicFolder.getAbsolutePath());

    if (!publicFolder.exists()) {
      logger.error("WARNING: 'public' folder NOT FOUND at " + publicFolder.getAbsolutePath());
    }

    StaticHttpHandler staticHttpHandler = new StaticHttpHandler("public");
    server.getServerConfiguration().addHttpHandler(staticHttpHandler, "/");

    try {
      server.start();
    } catch (Exception e) {
      BindException bindError = findBindException(e);
      if (bindError != null) {
        logger.error(
            "No se pudo enlazar " + BIND_HOST + ":" + BIND_PORT + " -> " + bindError.getMessage());
        logger.error("Comprueba si el puerto esta ocupado: ss -tlnp | grep " + BIND_PORT);
        logger.error("Si hay otro java, paralo: kill <PID>");
        logger.error("O arranca en otro puerto: SERVER_PORT=8081 ./start_server.sh");
      }
      if (e instanceof IOException) {
        throw (IOException) e;
      }
      if (e instanceof RuntimeException) {
        throw (RuntimeException) e;
      }
      throw new RuntimeException(e);
    }

    logger.info("REST API listening on " + BASE_URI);
    logger.info("Public host (Swagger/clients): " + PUBLIC_HOST);
    logger.info("Web: http://" + PUBLIC_HOST + ":" + BIND_PORT + "/login.html");
    logger.info("API: http://" + PUBLIC_HOST + ":" + BIND_PORT + "/dsaApp/game");

    FaqAssistantClient faqClient = new FaqAssistantClient();
    logger.info("LLM config url=" + faqClient.getLlmUrl() + " model=" + faqClient.getLlmModel());
    try {
        String ping = faqClient.testConnection();
        logger.info("LLM OK: " + ping);
    } catch (IOException e) {
        logger.error("LLM NO disponible desde Java: " + e.getMessage());
        logger.error("Comprueba en el servidor: echo $LLM_URL $LLM_MODEL && test -n \"$LLM_API_KEY\"");
    }

    System.out.println("Press enter to stop the server...");
    System.in.read();
    server.stop();
  }
}
