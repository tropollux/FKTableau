package resources;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import tableau.domain.Tableau;
import tableau.driver.AfficheurFKT;
import tableau.driver.IAfficheur;

@Path("/tableau")
public class TableauResource extends AbstractResource {
	private IAfficheur afficheur = new AfficheurFKT();
	private Horloge horloge = null;

	@POST
	@Path("/send")
	@Produces("application/json;encoding=utf-8")
	public List<String> send(String jsTableau) {
		System.out.println(jsTableau);
		Tableau tableau = gson.fromJson(jsTableau, Tableau.class);

		if (horloge != null) {
			stopHorloge();
		}
		horloge = new Horloge(tableau);
		horloge.start();
		return afficheur.getLastTableau();
	}

	@POST
	@Path("/refresh")
	@Produces("application/json;encoding=utf-8")
	public List<String> refresh(String jsTableau) {
		System.out.println("remote : jsTableau : " + jsTableau);
		// si il n'y a pas de thread actif on en lance un
		if (horloge == null) {
			return send(jsTableau);
		}
		Tableau tableau = gson.fromJson(jsTableau, Tableau.class);
		afficheur.refresh(tableau);
		return new ArrayList<String>();
	}

	@GET
	@Path("/last")
	@Produces("application/json;encoding=utf-8")
	public List<String> tableau() {
		return afficheur.getLastTableau();
	}

	@GET
	@Path("/stop")
	@Produces("application/json;encoding=utf-8")
	public List<String> stopHorloge() {
		if (horloge != null) {
			System.out.println("arret du thread " + horloge.getId());
			horloge.interrupt();
			horloge = null;
		}
		return afficheur.getLastTableau();
	}

	@GET
	@Path("/clear")
	public String clear() throws InterruptedException {
		stopHorloge();
		afficheur.affiche(new Tableau());
		return "";
	}

	private class Horloge extends Thread {
		private final Tableau tableau;

		public Horloge(Tableau tableau) {
			this.tableau = tableau;
		}

		@Override
		public void run() {
			System.out.println("nouveau thread " + getId());
			try {
				afficheur.affiche(getTableau());
				while (true) {
					Thread.sleep(500);
					afficheur.affiche();
				}
			} catch (InterruptedException e) {
				System.out.println("on s'arrete " + this.getId());
			}
		}

		public Tableau getTableau() {
			return tableau;
		}
	}
}
