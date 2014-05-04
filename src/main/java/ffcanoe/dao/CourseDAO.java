package ffcanoe.dao;

import java.util.List;
import java.util.Properties;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;

import com.google.inject.Singleton;

import ffcanoe.domain.Course;
import ffcanoe.domain.Dossard;
import ffcanoe.domain.Manche;
import ffcanoe.domain.Phase;
import ffcanoe.domain.Run;
import ffcanoe.domain.RunJuge;

/**
 * DAO général 
 * 
 * @author remi
 *
 */
@Singleton
public class CourseDAO {
	private static SessionFactory sessionFactory = null;
	private static ServiceRegistry serviceRegistry = null;

	public CourseDAO() {
		Configuration configuration = new Configuration().configure();
		Properties properties = configuration.getProperties();
		serviceRegistry = new ServiceRegistryBuilder().applySettings(properties).buildServiceRegistry();
		sessionFactory = configuration.buildSessionFactory(serviceRegistry);
	}

	/**
	 * donne la liste des courses
	 * 
	 * @param courseId
	 * @return
	 */
	public List<Course> getCourses() {
		Session session = null;
		List<Course> courses = null;
		try {
			session = sessionFactory.openSession();
			Query query = session.createQuery("from Course");
			courses = query.list();
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (session != null) {
				session.close();
			}
		}
		return courses;
	}

	/**
	 * donne la liste des bateaux pour une course
	 * 
	 * @param courseId
	 * @return
	 */
	public List<Dossard> getDossards(Integer courseId) {
		Session session = null;
		List<Dossard> dossards = null;
		try {
			session = sessionFactory.openSession();
			Query query = session
					.createQuery("select d from Dossard d where d.course.id = :courseId order by d.dossard");
			query.setParameter("courseId", courseId);
			dossards = query.list();
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (session != null) {
				session.close();
			}
		}
		return dossards;
	}

	/**
	 * renvoie les infos completes sur un bateau
	 * 
	 * @param dossard
	 * @return
	 */
	public Dossard findDossard(String dossard) {
		Session session = null;
		Dossard result = null;
		try {
			session = sessionFactory.openSession();
			Query query = session.createQuery("select d from Dossard d where d.dossard = :dossard");
			query.setParameter("dossard", dossard);
			result = (Dossard) query.uniqueResult();
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (session != null) {
				session.close();
			}
		}
		return result;
	}


	/**
	 * retourne tous les runs validés d'une phase
	 * 
	 * @param session
	 * @param course
	 * @param phase
	 * @return
	 */
	public List<Run> getAllValidatedRuns(int course, int phase) {
		Session session = sessionFactory.openSession();
		List<Run> allRuns = null;
		try {
			// on ramene tous les runs
			Query queryRun = session
					.createQuery("select r from Run r "
							+ "where r.course.id = :course and r.typeManche = :phase "
							+ "order by r.run");
			queryRun.setParameter("course", course);
			queryRun.setParameter("phase", phase);
			allRuns = queryRun.list();

			List<RunJuge> runJuges;
			Query queryJuges = session
					.createQuery("select r from RunJuge r "
							+ "where r.course.id = :course and r.typeManche = :phase "
							+ "and (r.validation is null or r.validation != 1) "
							+ "order by r.run");
			queryJuges.setParameter("course", course);
			queryJuges.setParameter("phase", phase);
			runJuges = queryJuges.list();

			// maintenant faut les retirer de la liste des runs ...
			for (RunJuge runJuge : runJuges) {
				for (Run run : allRuns) {
					if (run.getCoureur().equals(runJuge.getCoureur())
							&& run.getRun() == runJuge.getRun()) {
						System.out.println("run non validé : " + run);
						run.setValid(false);
					}
				}
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (session != null) {
				session.close();
			}
		}
		return allRuns;
	}

	public List<Phase> getPhases(Integer course) {
		Session session = sessionFactory.openSession();
		List<Phase> phases = null;
		try {
			Query query = session.createQuery("select p from Phase p "
					+ "where p.course.id = :course");
			query.setParameter("course", course);
			phases = query.list();
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (session != null) {
				session.close();
			}
		}
		return phases;
	}

	public Phase findPhase(int course, int type) {
		Session session = sessionFactory.openSession();
		Phase phase = null;
		try {
			Query query = session.createQuery("select p from Phase p "
					+ "where p.course.id = :course "
					+ "and p.typeManche = :type");
			query.setParameter("course", course);
			query.setParameter("type", type);
			List list = query.list();
			if (list.size() > 1) {
				System.out.println("Attention plusieurs phases pour une même manche" + course);
			}
			phase = (Phase) list.get(0);
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (session != null) {
				session.close();
			}
		}
		return phase;
	}
	
	public Manche findManche(int courseId, String codeCoureur, int typeManche) {
		Session session = sessionFactory.openSession();
		Manche manche = null;
		try {
			Query queryManche = session.createQuery("from Manche m "
					+ "where m.course.id = :courseId "
					+ "and m.typeManche = :typeManche "
					+ "and m.coureur.codeCoureur = :codeCoureur ");
			queryManche.setParameter("courseId", courseId);
			queryManche.setParameter("typeManche", typeManche);
			queryManche.setParameter("codeCoureur", codeCoureur);
			manche = (Manche) queryManche.uniqueResult();
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (session != null) {
				session.close();
			}
		}
		
		return manche;
	}

}