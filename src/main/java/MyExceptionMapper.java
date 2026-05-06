import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.io.PrintWriter;
import java.io.StringWriter;

@Provider
public class MyExceptionMapper implements ExceptionMapper<Exception> {
    @Override
    public Response toResponse(Exception ex) {
        if (ex instanceof WebApplicationException) {
            WebApplicationException webEx = (WebApplicationException) ex;
            int status = webEx.getResponse().getStatus();
            String message = status == 400 ? "JSON invalido o datos de peticion incorrectos" : ex.getMessage();
            return Response.status(status)
                    .entity(message)
                    .type("text/plain")
                    .build();
        }

        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        return Response.status(500)
                .entity(sw.toString())
                .type("text/plain")
                .build();
    }
}
