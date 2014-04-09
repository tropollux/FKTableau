package resources;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;

@Path("/tableau")
public class TableauRemoteResource extends AbstractResource {

	private static final String REMOTE_URL = System.getenv("REMOTE_URL") + "/tableau";
	
	public TableauRemoteResource() {
		System.out.println("Remote URL " + REMOTE_URL);
	}
	
	@POST
	@Path("/send")
	@Produces("application/json;encoding=utf-8")
	public String send(String jsTableau) {
		ClientResponse response = Client.create().resource(REMOTE_URL + "/send")
									.type("application/json")
									.post(ClientResponse.class, jsTableau);
		if (response.getStatus() != 200) {
			throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
		}
		return response.getEntity(String.class);
	}

	
	@POST
	@Path("/refresh")
	@Produces("application/json;encoding=utf-8")
	public String refresh(String jsTableau) {
		ClientResponse response = Client.create().resource(REMOTE_URL + "/refresh")
									.type("application/json")
									.post(ClientResponse.class, jsTableau);
		if (response.getStatus() != 200) {
			throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
		}
		return response.getEntity(String.class);
	}

	@GET
	@Path("/last")
	@Produces("application/json;encoding=utf-8")
	public String tableau() {
		ClientResponse response = Client.create().resource(REMOTE_URL + "/last")
										.type("application/json")
										.get(ClientResponse.class);
		if (response.getStatus() != 200) {
			throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
		}
		return response.getEntity(String.class);
	}

	@GET
	@Path("/stop")
	public String stopHorloge() {
		ClientResponse response = Client.create().resource(REMOTE_URL + "/stop")
									.type("application/json")
									.get(ClientResponse.class);
		return response.getEntity(String.class);
	}

	@GET
	@Path("/clear")
	public String clear() throws InterruptedException {
		ClientResponse response = Client.create().resource(REMOTE_URL + "/clear")
									.type("application/json")
									.get(ClientResponse.class);
		return response.getEntity(String.class);
	}
}
