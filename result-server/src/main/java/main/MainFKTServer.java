package main;

import static com.google.common.base.Objects.firstNonNull;
import static com.sun.jersey.api.core.ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS;
import static com.sun.jersey.api.core.ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS;
import static java.lang.Integer.parseInt;

import java.io.IOException;

import org.codehaus.jackson.jaxrs.JacksonJsonProvider;

import resources.MainResource;
import resources.ResultatResource;
import resources.StaticResource;
import resources.TableauRemoteResource;
import resources.TableauResource;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.sun.jersey.api.container.filter.GZIPContentEncodingFilter;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.simple.container.SimpleServerFactory;

public class MainFKTServer {
  private final Injector injector;

  public MainFKTServer() {
    injector = Guice.createInjector();
  }
  
  public static void main(String[] args) throws IOException {
    int port = parseInt(firstNonNull(System.getenv("PORT"), "8181"));

    MainFKTServer fktServer = new MainFKTServer();
    fktServer.start(port);
   
  }

  public void start(int port) throws IOException {
    System.out.println("Starting server on port: " + port);
    SimpleServerFactory.create("http://localhost:" + port, configuration());
  }


private ResourceConfig configuration() throws IOException {
    String mode = firstNonNull(System.getenv("MODE"), "FULL");
    System.out.println("Server mode : " + mode);

    DefaultResourceConfig config = new DefaultResourceConfig();

    config.getClasses().add(JacksonJsonProvider.class);
    
    config.getSingletons().add(injector.getInstance(MainResource.class));
    config.getSingletons().add(injector.getInstance(StaticResource.class));
    if (! "MASTER".equalsIgnoreCase(mode)) {
    	// en mode FULL ou SALVE (mais jamais sur le master)
    	config.getSingletons().add(injector.getInstance(TableauResource.class));
    }
    if ("MASTER".equalsIgnoreCase(mode)) {
    	// uniquement en mode MASTER
    	config.getSingletons().add(injector.getInstance(TableauRemoteResource.class));
    }
    if (! "SLAVE".equalsIgnoreCase(mode)) {
    	// en mode FULL ou MASTER (mais jamais en SALVE)
    	config.getSingletons().add(injector.getInstance(ResultatResource.class));
    }

    config.getProperties().put(PROPERTY_CONTAINER_REQUEST_FILTERS, GZIPContentEncodingFilter.class);
    config.getProperties().put(PROPERTY_CONTAINER_RESPONSE_FILTERS, GZIPContentEncodingFilter.class);

    return config;
  }

}
