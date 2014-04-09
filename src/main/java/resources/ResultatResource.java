package resources;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import tableau.domain.Ligne;
import ffcanoe.domain.Course;
import ffcanoe.domain.Dossard;
import ffcanoe.domain.Manche;
import ffcanoe.domain.Phase;
import ffcanoe.service.Classement;

@Path("/ffcanoe")
public class ResultatResource extends AbstractResource {
	private Classement classement;

	@Inject
	public ResultatResource(Classement classement) {
		this.classement = classement;
	}

	/**
	 * liste des courses
	 * 
	 * @return
	 */
	@GET
	@Path("/courses")
	@Produces("application/json;encoding=utf-8")
	public List<Course> getCourses() {
		return classement.getCourses();
	}

	/**
	 * liste des phases d'une course
	 * 
	 * @param courseId
	 * @return
	 */
	@GET
	@Path("/phases/{course}")
	@Produces("application/json;encoding=utf-8")
	public List<Phase> getPhases(@PathParam("course") Integer course) {
		System.out.println("getPhases :" + course);
		return classement.getPhases(course);
	}

	
	/**
	 * liste des dossards pour une course donnee
	 * 
	 * @param courseId
	 * @return
	 */
	@GET
	@Path("/dossards/{course}")
	@Produces("application/json;encoding=utf-8")
	public List<Dossard> classement(@PathParam("course") Integer course) {
		return classement.getDossards(course);
	}

	@GET
	@Path("/lst_dossards")
	@Produces("application/json;encoding=utf-8")
	public List<Ligne> liste(@QueryParam("course") Integer course) {
		List<Ligne> lignes= new ArrayList<Ligne>();
		Ligne ligne;
		for (Dossard result : classement.getDossards(course)) {
			ligne = new Ligne();
			ligne.setText(result.getDossard() + "-" + result.getBateau() + " (" + result.getClub() + " )");
			lignes.add(ligne);
		}
		return lignes;
	}
	
	/**
	 * detail d'un dossard
	 * 
	 * @param dossardId
	 * @return
	 */
	@GET
	@Path("/dossard/{dossard}")
	@Produces("application/json;encoding=utf-8")
	public Dossard detail(@PathParam("dossard") String dossard) {
		return classement.findDossard(dossard);
	}
	
	/**
	 * classement d'une course pour une phase donnee (K1DC qualif, K1HJ 1/2)
	 * 
	 * @param courseId
	 * @param phase
	 * @return
	 */
	@GET
	@Path("/classement/{course}")
	@Produces("application/json;encoding=utf-8")
	public List<Manche> getResultats(
				@PathParam("course") int course, 
				@QueryParam("phase") @DefaultValue("32") int phase) {
		return classement.classementProvisoire(course, phase);
	}

	/**
	 * classement d'une course pour une phase donnee (K1DC qualif, K1HJ 1/2)
	 * 
	 * @param courseId
	 * @param phase
	 * @return
	 */
	@GET
	@Path("/resultat/{course}")
	@Produces("application/json;encoding=utf-8")
	public List<Manche> getResultat(
				@PathParam("course") int course, 
				@QueryParam("phase") @DefaultValue("32") int phase) {
		List<Manche> provisoire = classement.classementProvisoire(course, phase);
		System.out.println("resultats provisoires");
		int i=1;
		
		for (Manche manche : provisoire) {
			System.out.println( i++ + ";"
						+ manche.getCoureur().getDossard() + ";" 
						+ manche.getCoureur().getBateau() +";" 
						+ manche.getRuns().get(0).getPoints() +";" 
						+ manche.getRuns().get(1).getPoints() +";" 
						+ manche.getTotalManche() + ";");
		}
		return provisoire;
	}
	
	/**
	 * liste des departs d'une course pour une phase donnee (K1DC qualif, K1HJ 1/2)
	 * 
	 * @param courseId
	 * @param phase
	 * @return
	 */
	@GET
	@Path("/depart/{course}")
	@Produces("application/json;encoding=utf-8")
	public List<Manche> getDeparts(
				@PathParam("course") int course, 
				@QueryParam("phase") @DefaultValue("32") int phase) {
		return classement.listeDepart(course, phase);
	}
	
	/**
	 * Mise a jour des resultats pour une course pour une phase donnee (K1DC qualif, K1HJ 1/2)
	 * 
	 * @param courseId
	 * @param phase
	 * @return
	 */
	@GET
	@Path("/maj")
	@Produces("application/json;encoding=utf-8")
	public List<Manche> majResultat(
				@QueryParam("resultat") String jsResultat) {
		System.out.println(jsResultat);
		Manche manche = gson.fromJson(jsResultat, Manche.class);
		return classement.majResultat(manche);
	}
	
	/**
	 * Mise a jour des resultats pour une course pour une phase donnee (K1DC qualif, K1HJ 1/2)
	 * 
	 * @param courseId
	 * @param phase
	 * @return
	 */
	@GET
	@Path("/import/{course}")
	@Produces("application/json;encoding=utf-8")
	public Manche newResultat(
				@PathParam("course") int course,
				@QueryParam("filename") @DefaultValue("run.data") String fileName,
				@QueryParam("phase") @DefaultValue("32") int phase,
				@QueryParam("codeCoureur") @DefaultValue("") String codeCoureur,
				@QueryParam("timeout") @DefaultValue("5") int timeout) {
		try {
			return classement.importFile(course, phase, timeout, codeCoureur, fileName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
		
	/**
	 * 
	 * detail des points d'un bateau pour une course : le 203 dans la Qualif C1HJ
	 * 
	 * @param courseId
	 * @param phase
	 * @param dossardId
	 * @return
	 */
	@GET
	@Path("/classement/{course}/detail")
	@Produces("application/json;encoding=utf-8")
	public Manche getPoints(
				@PathParam("course") int course, 
				@QueryParam("phase") @DefaultValue("32") int phase,
				@QueryParam("dossard") String dossard) {

		Manche manche = classement.detailClassement(course, phase, dossard);
		System.out.println(manche);
		return manche;
	}
	}
