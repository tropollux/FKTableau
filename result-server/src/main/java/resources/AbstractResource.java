package resources;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

import javax.ws.rs.core.Response;

import webserver.templating.ContentWithVariables;
import webserver.templating.Layout;
import webserver.templating.Template;
import webserver.templating.YamlFrontMatter;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.sun.jersey.api.NotFoundException;

public abstract class AbstractResource {
  private static final long ONE_YEAR = 1000L * 3600 * 24 * 365;

  protected Response ok(Object entity, long modified) {
    return Response.ok(entity).lastModified(new Date(modified)).expires(new Date(modified + ONE_YEAR))
    		.header("Cache-Control", "public")
    		.build();
  }

  protected String templatize(String body) {
    return templatize(body, ImmutableMap.of());
  }

  protected String templatize(String body, Map<?, ?> variables) {
    ContentWithVariables yaml = new YamlFrontMatter().parse(body);

    Map<String, String> yamlVariables = yaml.getVariables();
    String content = yaml.getContent();

    String layout = yamlVariables.get("layout");
    if (layout != null) {
      content = new Layout(read(file(layout))).apply(content);
    }

    return new Template().apply(content, ImmutableMap.builder().putAll(variables).putAll(yamlVariables).build());
  }

  protected String read(File file) {
    try {
      return Files.toString(file, Charsets.UTF_8);
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

  protected File file(String parent, String path) {
    return file(new File(parent, path).getPath());
  }

  protected File file(String path) {
    if (!exists(path)) {
      throw new NotFoundException();
    }
    return new File("web", path);
  }

  protected boolean exists(String path) {
    if (path.endsWith("/")) {
      return false;
    }

    try {
      File root = new File("web");
      File file = new File(root, path);
      if (!file.exists() || !file.getCanonicalPath().startsWith(root.getCanonicalPath())) {
        return false;
      }

      return true;
    } catch (IOException e) {
      return false;
    }
  }
  
  private static TypeAdapter<Number> IntegerTypeAdapter = new TypeAdapter<Number>() {

      @Override
      public void write(JsonWriter out, Number value)
              throws IOException {
          out.value(value);
      }

      @Override
      public Number read(JsonReader in) throws IOException {
          if (in.peek() == JsonToken.NULL) {
              in.nextNull();
              return null;
          }
          try {
              String result = in.nextString();
              if ("".equals(result)) {
                  return null;
              }
              return Integer.parseInt(result);
          } catch (NumberFormatException e) {
              throw new JsonSyntaxException(e);
          }
      }
  };
 
  protected final static Gson gson = new GsonBuilder()
      .registerTypeAdapter(int.class, IntegerTypeAdapter)
      .registerTypeAdapter(Integer.class, IntegerTypeAdapter).create();
}
