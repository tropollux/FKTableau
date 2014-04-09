package ffcanoe.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;

import com.google.inject.Singleton;

import ffcanoe.domain.Course;
import ffcanoe.domain.Dossard;
import ffcanoe.domain.Manche;
import ffcanoe.domain.Phase;
import ffcanoe.domain.PointsRun;
import ffcanoe.domain.Run;

@Singleton
public class Classement {
	private static final String COURSE_ID_BAT = "courseId.bat";
	private final static Charset ENCODING = StandardCharsets.ISO_8859_1;
	private static SessionFactory sessionFactory = null;
	private static ServiceRegistry serviceRegistry = null;

	/**
	 * CRC du dernier fichier injecte
	 */
	private static long lastChecksum;
	
	public Classement() {
		Configuration configuration = new Configuration();
		configuration.configure();
		
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
			Query query = session.createQuery("select d from Dossard d where d.course.id = :courseId order by d.dossard");
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
	 * renvoie la liste de depart avec le classement provisoire
	 * 
	 * TODO : voir comment on fait apres les qualifs
	 * 
	 * Reponse : il faut aller choper dans la niveau en dessous le rang du bonhomme !!!
	 * ex : pour les quarts, il faut regarder dans la table resultat_manche le classement des 32 !!
	 * 
	 * @param course
	 * @param phase
	 * @return
	 */
	public List<Manche> listeDepart(int course, int phase) {
		
		// on commence par calculer le classement provisoire 
		List<Manche> provisoire = classementProvisoire(course, phase);
		List<Manche> departs = new ArrayList<>();
		
		Session session = null;
		try {
			session = sessionFactory.openSession();

			// on cherche le detail de la phase pour connaitre le type de calcul !! 
			Query queryPhase = session.createQuery("select p from Phase p " +
					"where p.course.id = :course and p.typeManche = :phase ");
			queryPhase.setParameter("course", course);
			queryPhase.setParameter("phase", phase);
			Phase lPhase = (Phase) queryPhase.uniqueResult();
			
			List<Dossard> dossards = null;
			if (lPhase.getTypeManche() == 32) {
				// on commence par les dossards .. pour les qualifs ... apres on verra
				Query queryDossard = session.createQuery("select d from Dossard d " +
						"where d.course.id = :course " +
						"order by d.dossard");
				queryDossard.setParameter("course", course);
				dossards = queryDossard.list();
			} else {
				// faut trouver les resultats de la manche d'avant
				// par exemple en commencant par trouver la  manche d'avant
				SQLQuery queryPreviousManche = 
						session.createSQLQuery("select min(d.Code_manche) from Resultat_Manche d " +
								"where d.Code_evenement = :course " +
								"and d.Code_manche > :phase");
				queryPreviousManche.setParameter("course", course);
				queryPreviousManche.setParameter("phase", phase);
				List list = queryPreviousManche.list();
				if (list.size() == 0) {
					System.out.println("ba on est bien embete : pas de qualif validée");
				} else {
					int mancheDavant= (int) list.get(0);
					// sinon faut prendre les dossards de la manche d'avant
//					Query query = session.createQuery("select m from Manche m " +
//							"where m.course.id = :course " +
//							"and m.phase.typeManche = :phase " +
//							"and m.rang != null " +
//							"and m.rang <= m.phase.nbQualifies " +
//							"order by m.rang desc");
//					query.setParameter("course", course);
//					query.setParameter("phase", mancheDavant);
//					List<Manche> qualifies = query.list();
//					dossards = new ArrayList<Dossard>();
//					for (Manche depart : qualifies) {
//						System.out.println("depart :" + depart.getCoureur());
//						dossards.add(depart.getCoureur());
//					}
					// tentative avec le classement de la manche d'avant
					List<Manche> definitif = classementProvisoire(course, mancheDavant);
					dossards = new ArrayList<Dossard>();
					for (Manche manche : definitif) {
						if (manche.getClassement() <= manche.getPhase().getNbQualifies()) {
							dossards.add(manche.getCoureur());
						} else {
							break;
						}
					}
					// reste plus qu'a l'inverser
					Collections.reverse(dossards);

				}
					
			}
			// puis on se fait une petite jointure a la main dans l'ordre des departs
			boolean found;
			for (Dossard dossard : dossards) {
				found = false;
				for (Manche manche : provisoire) {
					if (manche.getCoureur().getCodeCoureur().equalsIgnoreCase(dossard.getCodeCoureur())) {
						departs.add(manche);
						found=true;
						break;
					}
				}
				if (! found) {
					// il n'existe pas pour le moment, donc faut le creer
					Manche manche = new Manche();
					manche.setCoureur(dossard);
					manche.setCourse(lPhase.getCourse());
					manche.setPhase(lPhase);
					for (int i=0; i< lPhase.getNbRun(); i++) {
						PointsRun runVide = new PointsRun(i+1);
						manche.getRuns().add(runVide);
					}
					departs.add(manche);
				}
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (session != null) {
				session.close();
			}
		}
		return departs;
	}

	/**
	 * renvoie le classement pour une course dans une phase donnee avec le calcul des points !!!
	 * @param course
	 * @param phase
	 * @return
	 */
	public List<Manche> classementProvisoire(int course, int phase) {
		Session session = null;
		List<Manche> resultats = null;
		List<Manche> resultatsTries = null;

		try {
			session = sessionFactory.openSession();
			Query query = session.createQuery("select m from Manche m " +
						"where m.course.id = :course and m.phase.typeManche = :phase ");
			query.setParameter("course", course);
			query.setParameter("phase", phase);
			resultats = query.list();

			// on cherche le detail de la phase pour connaitre le type de calcul !! 
			Query queryPhase = session.createQuery("select p from Phase p " +
					"where p.course.id = :course and p.typeManche = :phase ");
			queryPhase.setParameter("course", course);
			queryPhase.setParameter("phase", phase);
			Phase lPhase = (Phase) queryPhase.uniqueResult();
			
			// on ramene tous les runs
			List<Run> runs = null;
			Query queryRun = session.createQuery("select r from Run r " +
					"where r.course.id = :course and r.phase.typeManche = :phase " +
					"order by r.run");
			queryRun.setParameter("course", course);
			queryRun.setParameter("phase", phase);
			runs = queryRun.list();
			
			for (Run run : runs) {
				// mis à jour de la manche pour le bon dossard
				for (Manche resultat : resultats) {
					if (resultat.getCoureur().equals(run.getCoureur()) ) {
						resultat.getRuns().add(new PointsRun(run.getRun(), run.getPoints()));
						if (lPhase.getTypeResultat() == 0) {
							// faut faire la somme
							if (resultat.getTotalManche() != null) {
								resultat.setTotalManche(resultat.getTotalManche() + run.getPoints());
							} else {
								resultat.setTotalManche(run.getPoints());
							}
						} else {
							// on prend le meilleur
							if (resultat.getTotalManche() == null || resultat.getTotalManche() < run.getPoints() ) {
								resultat.setTotalManche(run.getPoints()); 
							}
						}
						
					}
				}
			}
			// un petit tri pour finir et le tour est joue
			Collections.sort(resultats, new Comparator<Manche>() {

				@Override
				public int compare(Manche m1, Manche m2) {
					if ((m1 == null) && (m2 == null)) {
						return 0;
					}
					if ((m1 == null) || (m1.getTotalManche() == null)){
						return -1;
					}
					if ((m2 == null) || (m2.getTotalManche() == null) ){
						return 1;
					}
					return m2.getTotalManche().compareTo(m1.getTotalManche());
				}
				
			});
			
			int place=1;
			int nbReel=1;
			int lastTotalManche=0; // pour les execos
			// et ensuite attribut les places 
			for (Manche manche : resultats) {
				if (manche.getTotalManche() != null && manche.getTotalManche() > 0) {
					if (manche.getTotalManche() == lastTotalManche) {
						// execo
						manche.setClassement(place);
					} else {
						manche.setClassement(nbReel);
						place=nbReel;
						lastTotalManche = manche.getTotalManche();
					}
					nbReel++;
				}
				// on en profite aussi pour ajouter les runs vides manquants
				manche.fillRuns();
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (session != null) {
				session.close();
			}
		}
		return resultats;
	}

	
	/**
	 * renvoie les resultats d'un dossard pour une course dans une phase donnee
	 * 
	 * @param course
	 * @param phase
	 * @return
	 */
	public Manche detailClassement(int course, int phase, String dossard) {
		Session session = null;
		Manche resultat = null;
		try {
			session = sessionFactory.openSession();
			Query query = session.createQuery("select m from Manche m " +
						"where m.course.id = :course and m.phase.typeManche = :phase " +
						"and m.coureur.dossard = :dossard" );
			query.setParameter("course", course);
			query.setParameter("phase",  phase);
			query.setParameter("dossard", dossard);
			resultat = (Manche) query.uniqueResult();
			
			System.out.println("step1 " + resultat);

			List<Run> points = null;
			Query queryRun = session.createQuery("select r from Run r " +
					"where r.course.id = :course and r.phase.typeManche = :phase " +
					"and r.coureur.dossard = :dossard " +
					"order by r.run");
			queryRun.setParameter("course", course);
			queryRun.setParameter("phase", phase);
			queryRun.setParameter("dossard", dossard);
			points = queryRun.list();

			// suivant le format et l'étape de la course, on ramene 1 2 3 run avec le total (somme ou meilleur)
			int totalPoint = 0;
			List<PointsRun> pointRuns = resultat.getRuns();;
			if (points != null && points.size()>1) {
				
				Query query2 = session.createQuery("select p from Phase p " +
						"where p.course.id = :course and p.typeManche = :phase ");
				query2.setParameter("course", course);
				query2.setParameter("phase", phase);
				Phase lPhase = (Phase) query2.uniqueResult();
				if (lPhase != null) {
					if (lPhase.getTypeResultat() == 0) {
						// on fait la somme
						int i=1;
						for (Run run : points) {
							totalPoint += run.getPoints();
							pointRuns.add(new PointsRun(i++, run.getPoints()));
						}
					} else {
						// on garde le meilleur
						int i=1;
						for (Run run : points) {
							if (run.getPoints() > totalPoint) totalPoint = run.getPoints();
							pointRuns.add(new PointsRun(i++, run.getPoints()));
						}
					}
				}
				
				resultat.setTotalManche(totalPoint);
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (session != null) {
				session.close();
			}
		}
		return resultat;
	}

	/**
	 * renvoie les points par run .. faudrait plutot renvoyer R1, R2 R3 et le total manche en fonction de la phase
	 * 
	 * @param manche
	 * @param typeManche
	 * @param run
	 * @param nDossard
	 * @return
	 */
	public List<Run> points(int course, int phase, String dossard) {
		Session session = null;
		List<Run> points = null;
		try {
			session = sessionFactory.openSession();
			Query query = session.createQuery("select r from Run r " +
						"where r.course.id = :course and r.phase.typeManche = :phase " +
						"and r.coureur.dossard = :dossard " +
						"order by r.run");
			query.setParameter("course", course);
			query.setParameter("phase", phase);
			query.setParameter("dossard", dossard);
			points = query.list();
			
			// suivant le format et l'étape de la course, on ramene 1 2 3 run avec le total (somme ou meilleur)
			if (points != null && points.size()>1) {
				Query query2 = session.createQuery("select p from Phase p " +
						"where p.course.id = :course and p.typeManche = :phase ");
				query2.setParameter("course", course);
				query2.setParameter("phase", phase);
				Phase lPhase = (Phase) query2.uniqueResult();
				if (lPhase != null) {
					int totalPoint = 0;
					if (lPhase.getTypeResultat() == 0) {
						// on fait la somme
						for (Run run : points) {
							totalPoint += run.getPoints();
						}
					} else {
						// on garde le meilleur
						for (Run run : points) {
							if (run.getPoints() > totalPoint) totalPoint = run.getPoints();
						}
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
		
		return points;
	}

	public List<Phase> getPhases(Integer course) {
		Session session = null;
		List<Phase> phases = null;
		try {
			session = sessionFactory.openSession();
			Query query = session.createQuery("select p from Phase p " +
					"where p.course.id = :course");
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

	public List<Manche> majResultatDepricated(Manche manche) {
		
		Session session = null;
		List<Run> runs = null;
		try {
			session = sessionFactory.openSession();
			Transaction transaction = session.beginTransaction();

			// d'abord on regarde si la manche existe !!
			Query queryManche = session.createQuery("from Manche m " +
					"where m.course = :course " +
					"and m.phase = :phase " +
					"and m.coureur = :dossard ");
			queryManche.setParameter("course", manche.getCourse());
			queryManche.setParameter("phase", manche.getPhase());
			queryManche.setParameter("dossard", manche.getCoureur());
			
			List<Manche> manches = queryManche.list();
			if (manches.size() == 0) {
				// il faut commencer par créer la manche
				SQLQuery insertMancheQuery = 
						session.createSQLQuery("insert into Resultat_Manche " +
								"(Code_evenement, Code_coureur, Code_manche, Rang, Total_freestyle) " +
								"values (:code_evenement, :code_coureur, :code_manche, -1, :totalPoints) ");
				insertMancheQuery.setParameter("code_manche", manche.getPhase().getTypeManche());
				insertMancheQuery.setParameter("code_evenement", manche.getCourse().getId());
				insertMancheQuery.setParameter("code_coureur", manche.getCoureur().getCodeCoureur());
				insertMancheQuery.setParameter("totalPoints", 0);
				
				insertMancheQuery.executeUpdate();
			}

			// ensuite on regarde les runs
			Query query = session.createQuery("from Run r " +
						"where r.course = :course " +
						"and r.phase = :phase " +
						"and r.coureur = :dossard " +
						"order by r.run");
			query.setParameter("course", manche.getCourse());
			query.setParameter("phase", manche.getPhase());
			query.setParameter("dossard", manche.getCoureur());
			runs = query.list();

			// on regarde ceux qu on a recu en vérifiant qu'ils existent
			boolean found;
			for (PointsRun pointRun : manche.getRuns()) {
				if (pointRun.getPoints() == null) {
					break;
				}
				
				found=false;
				for (Run run : runs) {
					if (run.getRun() == pointRun.getRun()) {
						found=true;
						break;
					}
				}
				if (found) {
					// on l'update
//						run.setPoints(manche.getRuns().get(i).getPoints());
//						System.out.println("on va persister : \n" + run);
					
					SQLQuery updateRunQuery = 
							session.createSQLQuery("update Resultat_Manche_Run " +
									"set Total_freestyle=:points " +
									"where Code_run=:code_run " +
									"and Code_manche=:code_manche " +
									"and Code_evenement=:code_evenement " +
									"and Code_coureur=:code_coureur");
					updateRunQuery.setParameter("points", pointRun.getPoints());
					updateRunQuery.setParameter("code_run", pointRun.getRun()); 
					updateRunQuery.setParameter("code_manche", manche.getPhase().getTypeManche());
					updateRunQuery.setParameter("code_evenement", manche.getCourse().getId());
					updateRunQuery.setParameter("code_coureur", manche.getCoureur().getCodeCoureur());
					updateRunQuery.executeUpdate();
				} else {
					// on l'insert
					SQLQuery insertRunQuery = 
							session.createSQLQuery("insert into Resultat_Manche_Run " +
									"(Code_evenement, Code_coureur, Code_manche, Code_run, Total_freestyle, Cltc) " +
									"values (:code_evenement, :code_coureur, :code_manche, :code_run, :points, -1) ");
					insertRunQuery.setParameter("code_manche", manche.getPhase().getTypeManche());
					insertRunQuery.setParameter("code_evenement", manche.getCourse().getId());
					insertRunQuery.setParameter("code_coureur", manche.getCoureur().getCodeCoureur());
					insertRunQuery.setParameter("code_run", pointRun.getRun());
					insertRunQuery.setParameter("points", pointRun.getPoints());
					insertRunQuery.executeUpdate();
				}
			}
			transaction.commit();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (session != null) {
				session.close();
			}
		}
		return classementProvisoire(manche.getCourse().getId(), manche.getPhase().getTypeManche());
	}

	public List<Manche> majResultat(Manche manche) {

		int courseId = manche.getCourse().getId();
		String codeCoureur = manche.getCoureur().getCodeCoureur();
		int typeManche = manche.getPhase().getTypeManche();
		
		for (PointsRun pointRun : manche.getRuns()) {
			if (pointRun.getPoints() != null) {
				// eventuellement on pourrait tester une autre valeur (-1)
				majRun(courseId, codeCoureur, typeManche, pointRun.getRun(), pointRun.getPoints());
			}
		}
		return null;
		
	}
	/**
	 * objectif injecter ca : 
	 * 	79|K1H053193|32|1|1190.0|-1|||
	 * 	79|K1H053193|32|2|1320.0|-1|||
	 *  ...
	 * @param manche
	 * @return
	 */
	public Manche majRun(int courseId, String codeCoureur, int typeManche, int nRun, int points) {
		
		Session session = null;
		Manche manche = null;
		boolean maj = false;
		try {
			session = sessionFactory.openSession();
//			Transaction transaction = session.beginTransaction();

			// d'abord on regarde si la manche existe !!
			Query queryManche = session.createQuery("from Manche m " +
					"where m.course.id = :courseId " +
					"and m.phase.typeManche = :typeManche " +
					"and m.coureur.codeCoureur = :codeCoureur ");
			queryManche.setParameter("courseId", courseId);
			queryManche.setParameter("typeManche", typeManche);
			queryManche.setParameter("codeCoureur", codeCoureur);
			manche = (Manche)queryManche.uniqueResult();
			
			if (manche == null) {
				// il faut commencer par creer la manche
				SQLQuery insertMancheQuery = 
						session.createSQLQuery("insert into Resultat_Manche " +
								"(Code_evenement, Code_coureur, Code_manche, Rang, Total_freestyle) " +
								"values (:code_evenement, :code_coureur, :code_manche, -1, :totalPoints) ");
				insertMancheQuery.setParameter("code_manche", typeManche);
				insertMancheQuery.setParameter("code_evenement", courseId);
				insertMancheQuery.setParameter("code_coureur", codeCoureur);
				insertMancheQuery.setParameter("totalPoints", points);
				
				insertMancheQuery.executeUpdate();
				maj = true;

				// et on retourne la chercher !! (en JPA)
				Query queryMancheBis = session.createQuery("from Manche m " +
						"where m.course.id = :courseId " +
						"and m.phase.typeManche = :typeManche " +
						"and m.coureur.codeCoureur = :codeCoureur ");
				queryMancheBis.setParameter("courseId", courseId);
				queryMancheBis.setParameter("typeManche", typeManche);
				queryMancheBis.setParameter("codeCoureur", codeCoureur);
				manche = (Manche)queryMancheBis.uniqueResult();

				System.out.println("import de Manche" + manche);
			}

			// ensuite on regarde si le run existe
			Query queryRun = session.createQuery("from Run r " +
						"where r.course = :course " +
						"and r.phase = :phase " +
						"and r.coureur = :dossard " +
						"and r.run = :run");
			queryRun.setParameter("course", manche.getCourse());
			queryRun.setParameter("phase", manche.getPhase());
			queryRun.setParameter("dossard", manche.getCoureur());
			queryRun.setParameter("run", nRun);
			Run run = (Run)queryRun.uniqueResult();


			if (run != null) {
				if ( (run.getPoints() != null) && !run.getPoints().equals(points)) {
					// on l'update
					maj=true;
					SQLQuery updateRunQuery = 
							session.createSQLQuery("update Resultat_Manche_Run " +
									"set Total_freestyle=:points " +
									"where Code_run=:code_run " +
									"and Code_manche=:code_manche " +
									"and Code_evenement=:code_evenement " +
									"and Code_coureur=:code_coureur");
					updateRunQuery.setParameter("points", points);
					updateRunQuery.setParameter("code_run", nRun); 
					updateRunQuery.setParameter("code_manche", typeManche);
					updateRunQuery.setParameter("code_evenement", courseId);
					updateRunQuery.setParameter("code_coureur", codeCoureur);
					updateRunQuery.executeUpdate();
					System.out.println("Mise a jour du RUN : " + run + "avec les infos" + codeCoureur + " " + nRun + " " + points );
				} else {
					System.out.println("run inchange :" + run );
				}
				
			} else {
				// on l'insert
				maj=true;
				SQLQuery insertRunQuery = 
						session.createSQLQuery("insert into Resultat_Manche_Run " +
								"(Code_evenement, Code_coureur, Code_manche, Code_run, Total_freestyle, Cltc) " +
								"values (:code_evenement, :code_coureur, :code_manche, :code_run, :points, -1) ");
				insertRunQuery.setParameter("code_manche", typeManche);
				insertRunQuery.setParameter("code_evenement", courseId);
				insertRunQuery.setParameter("code_coureur", codeCoureur);
				insertRunQuery.setParameter("code_run", nRun);
				insertRunQuery.setParameter("points", points);
				insertRunQuery.executeUpdate();
				System.out.println("creation du RUN : " + codeCoureur + " " + nRun + " " + points );
			}
//			transaction.commit();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (session != null) {
				session.close();
			}
		} 
		return (maj ? manche : null);
	}

	/**
	 * import le fichier dans la base s il na pas deja ete lu (controle le cheksum)
	 * 
	 * @param fileName
	 * @return le nombre de ligne importees
	 * @throws FileNotFoundException
	 */
	public Manche importFile(String fileName) throws FileNotFoundException {
		File file = new File(fileName);
		long crc32 = 0;
		int nbLigneslus = 0;
		Manche lastManche = null;
		
		if ( ! file.exists() || ! file.canRead()) {
			System.out.println("impossible de lire le fichier " + fileName);
		} else {
			try {
				// on ne fait rien si c'est le meme que celui d'avant
				crc32 = FileUtils.checksumCRC32(file);
				if (crc32 == lastChecksum) {
					System.out.println("fichier deja lu");
					// DEBUG 
					return null;
				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			System.out.println("c'est parti pour l'import du fichier " + fileName);
			BufferedReader bf;
			bf = new BufferedReader(new FileReader(file));
			String newRun;
			try {
				while ((newRun = bf.readLine()) != null) {
					String[] split = newRun.split("\\|");
					// rappel du format attendu
					// 79|K1H164347|32|2|750.0|-1|||
					if (split.length < 5) {
						System.out.println("la ligne est incomplete [" + newRun + "]");
					} else if (split[1].length() == 0 
							|| split[2].length() == 0 
							|| split[3].length() == 0 
							|| split[4].length() == 0) {
						System.out.println("format incorrect [" + newRun + "]");
					} else {
						int courseId = Integer.parseInt(split[0]);
						String codeCoureur = split[1];
						int typeManche = Integer.parseInt(split[2]);
						int nRun = Integer.parseInt(split[3]);
						Double points = Double.parseDouble(split[4]);
						System.out.println("tentative d'import de la ligne " + newRun);
						Manche majManche = majRun(courseId, codeCoureur, typeManche, nRun, points.intValue());
						if (majManche != null) {
							lastManche = majManche;
							nbLigneslus++;
						}
					}
				}
				bf.close();
				lastChecksum = crc32;
				// DEBUG file.renameTo(new File(fileName + ".ok"));
			} catch ( IOException e) {
				e.printStackTrace();
			}
		}
		return lastManche;
	}

	public Manche importFile(int course, int phase, int timeout, String codeCoureur, String fileName) throws IOException  {
		// en JDK7
		Path path = Paths.get(COURSE_ID_BAT);
		List<String> fichierTexte = new ArrayList<>();
		fichierTexte.add("set courseId=" + course);
		fichierTexte.add("set phase=" + phase);
		fichierTexte.add("set timeout=" + timeout * 2000); 
		fichierTexte.add("set codeCoureur=" + codeCoureur); 

		Files.write(path, fichierTexte, ENCODING);

		return importFile(fileName);
	}
}
