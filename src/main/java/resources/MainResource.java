package resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import java.io.File;

@Path("/")
public class MainResource extends AbstractResource {
  @GET
  @Produces("text/html;charset=UTF-8")
  public Response index() {
    File file = file("index.html");
    Response response = ok(templatize(read(file)), file.lastModified());
    return response;
  }

  @Path("hello.html")
  @GET
  @Produces("text/html;charset=UTF-8")
  public Response hello() {
	  File file = file("hello.html");
    return Response.ok("hello world").build();
  }

  @Path("resultat.html")
  @GET
  @Produces("text/html;charset=UTF-8")
  public Response resultat() {
	  File file = file("resultat.html");
  	return ok(templatize(read(file)), file.lastModified());
  }

  @Path("depart.html")
  @GET
  @Produces("text/html;charset=UTF-8")
  public Response depart() {
	  File file = file("depart.html");
  	return ok(templatize(read(file)), file.lastModified());
  }

}
