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
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import ffcanoe.dao.CourseDAO;
import ffcanoe.domain.Course;
import ffcanoe.domain.Dossard;
import ffcanoe.domain.Manche;
import ffcanoe.domain.Phase;
import ffcanoe.domain.PointsRun;
import ffcanoe.domain.Run;
import ffcanoe.domain.RunJuge;

@Singleton
public class Classement {
	private static final String COURSE_ID_BAT = "courseId.bat";
	private final static Charset ENCODING = StandardCharsets.ISO_8859_1;
	private static SessionFactory sessionFactory = null;
	private static ServiceRegistry serviceRegistry = null;
	
	@Inject
	private CourseDAO courseDao;

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
		return courseDao.getCourses();
	}

	/**
	 * donne la liste des bateaux pour une course
	 * 
	 * @param courseId
	 * @return
	 */
	public List<Dossard> getDossards(Integer courseId) {
		return courseDao.getDossards(courseId);
	}

	/**
	 * renvoie les infos completes sur un bateau
	 * 
	 * @param dossard
	 * @return
	 */
	public Dossard findDossard(String dossard) {
		return courseDao.findDossard(dossard);
	}

	/**
	 * renvoie la liste de depart avec le classement provisoire
	 * 
	 * Reponse : il faut aller choper dans la niveau en dessous le rang du
	 * bonhomme !!! ex : pour les quarts, il faut regarder dans la table
	 * resultat_manche le classement des 32 !!
	 * 
	 * @param course
	 * @param phase
	 * @return
	 */
	public List<Manche> listeDepart(int phaseId) {

		// on commence par calculer le classement provisoire
		List<Manche> provisoire = classementProvisoire(phaseId);
		List<Manche> departs = new ArrayList<>();

		Session session = sessionFactory.openSession();
		try {
			// on cherche le detail de la phase pour connaitre le type de calcul
			Phase lPhase = courseDao.findPhaseById(phaseId);

			List<Dossard> dossards = null;
			if (lPhase.getTypeManche() == 32) {
				Query queryDossard = session
						.createQuery("select d from Dossard d "
								+ "where d.course = :course "
								+ "order by d.dossard");
				queryDossard.setParameter("course", lPhase.getCourse());
				dossards = queryDossard.list();
			} else {
				// faut trouver les resultats de la manche d'avant
				Phase previousPhase = courseDao.findPreviousPhase(lPhase);
				// tentative avec le classement de la manche d'avant
				List<Manche> definitif = classementProvisoire(previousPhase.getId());
				dossards = new ArrayList<Dossard>();
				for (Manche manche : definitif) {
					// si on n'a pas atteint les quotats on ajoute les nons classés !!!
					if ((manche.getClassement() == null) || (manche.getClassement() <= previousPhase.getNbQualifies())) {
						dossards.add(manche.getCoureur());
					} else {
						break;
					}
				}
				// reste plus qu'a l'inverser
				Collections.reverse(dossards);

			}
			// puis on se fait une petite jointure a la main dans l'ordre des
			// departs
			boolean found;
			for (Dossard dossard : dossards) {
				found = false;
				for (Manche manche : provisoire) {
					if (manche.getCoureur().getCodeCoureur()
							.equalsIgnoreCase(dossard.getCodeCoureur())) {
						departs.add(manche);
						found = true;
						break;
					}
				}
				if (!found) {
					// il n'existe pas pour le moment, donc faut le creer
					Manche manche = new Manche();
					manche.setCoureur(dossard);
					manche.setCourse(lPhase.getCourse());
					manche.setTypeManche(lPhase.getTypeManche());
					for (int i = 0; i < lPhase.getNbRun(); i++) {
						PointsRun runVide = new PointsRun(i + 1);
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
	 * renvoie le classement pour une course dans une phase donnee avec le
	 * calcul des points !!!
	 * 
	 * @param course
	 * @param phase
	 * @return
	 */
	public List<Manche> classementProvisoire(int phaseId) {
		Session session = null;
		List<Manche> resultats = null;

		try {
			Phase lPhase = courseDao.findPhaseById(phaseId);

			session = sessionFactory.openSession();
			Query query = session
					.createQuery("select m from Manche m "
							+ "where m.course = :course and m.typeManche = :typeManche ");
			query.setParameter("course", lPhase.getCourse());
			query.setParameter("typeManche", lPhase.getTypeManche());
			resultats = query.list();


			List<Run> runs = courseDao.getAllValidatedRuns(lPhase);

			for (Run run : runs) {
				// mis à jour de la manche pour le bon dossard
				for (Manche resultat : resultats) {
					if (resultat.getCoureur().equals(run.getCoureur())) {
						resultat.getRuns().add(
								new PointsRun(run.getRun(), run.getPoints(),
										run.isValid()));
						if (run.isValid()) {
							if (lPhase.getTypeResultat() == 0) {
								// faut faire la somme
								if (resultat.getTotalManche() != null) {
									resultat.setTotalManche(resultat
											.getTotalManche() + run.getPoints());
								} else {
									resultat.setTotalManche(run.getPoints());
								}
							} else {
								// on prend le meilleur
								if (resultat.getTotalManche() == null
										|| resultat.getTotalManche() < run
												.getPoints()) {
									resultat.setTotalManche(run.getPoints());
								}
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
					if ((m1 == null) || (m1.getTotalManche() == null)) {
						return -1;
					}
					if ((m2 == null) || (m2.getTotalManche() == null)) {
						return 1;
					}
					return m2.getTotalManche().compareTo(m1.getTotalManche());
				}

			});

			int place = 1;
			int nbReel = 1;
			int lastTotalManche = 0; // pour les execos
			// et ensuite attribut les places
			for (Manche manche : resultats) {
				if (manche.getTotalManche() != null
						&& manche.getTotalManche() > 0) {
					if (manche.getTotalManche() == lastTotalManche) {
						// execo
						manche.setClassement(place);
					} else {
						manche.setClassement(nbReel);
						place = nbReel;
						lastTotalManche = manche.getTotalManche();
					}
					nbReel++;
				}
				// on en profite aussi pour ajouter les runs vides manquants
				manche.fillRuns(lPhase.getNbRun());
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
	 * retourne tous les runs validés d'une phase
	 * 
	 * @param session
	 * @param course
	 * @param phase
	 * @return
	 */
	
	private Manche verifyRunValidation(Manche manche) {
		Session session = sessionFactory.openSession();
		try {
			// on ne cherche meme pas a calculer les runs ... juste les remplir pour savoir s'ils sont valides ou non
			// manche.fillRuns();
			
			List<RunJuge> runJuges;
			Query queryRunJuges = session.createQuery("from RunJuge r "
					+ "where r.course = :course "
					+ "and r.typeManche = :typeManche"
					+ "and r.coureur = :dossard " 
					+ "and (r.validation is null or r.validation != 1) ");
			queryRunJuges.setParameter("course", manche.getCourse());
			queryRunJuges.setParameter("typeManche", manche.getTypeManche());
			queryRunJuges.setParameter("dossard", manche.getCoureur());
			runJuges = queryRunJuges.list();
			List<PointsRun> runs = manche.getRuns();
			for (RunJuge runJuge : runJuges) {
				if (runs != null && runs.size() >= runJuge.getRun()) {
					System.out.println("manche non validée " + manche + "pour le run : " + runJuge);
					runs.get(runJuge.getRun()-1).setValid(false);
				}
			}
		} finally {
			if (session != null) {
				session.close();
			}
		}
		return manche;
	}

	public List<Phase> getPhases(Integer course) {
		return courseDao.getPhases(course);
	}

	/** 
	 * mise à jour d'une manche depuis l'interface
	 * 
	 * @param manche
	 * @return
	 */
	public List<Manche> majResultat(Manche manche) {

		int courseId = manche.getCourse().getId();
		String codeCoureur = manche.getCoureur().getCodeCoureur();

		for (PointsRun pointRun : manche.getRuns()) {
			if (pointRun.getPoints() != null) {
				// eventuellement on pourrait tester une autre valeur (-1)
				majRun(courseId, codeCoureur, manche.getTypeManche(), pointRun.getRun(),
						pointRun.getPoints());
			}
		}
		return null;

	}

	/**
	 * objectif injecter ca : 79|K1H053193|32|1|1190.0|-1|||
	 * 79|K1H053193|32|2|1320.0|-1||| ...
	 * 
	 * @param manche
	 * @return
	 */
	public Manche majRun(int courseId, String codeCoureur, int typeManche,
			int nRun, int points) {

		Session session = null;
		Manche manche = null;
		boolean maj = false;
		try {
			session = sessionFactory.openSession();
			// Transaction transaction = session.beginTransaction();

			manche = courseDao.findManche(courseId, codeCoureur, typeManche);

			if (manche == null) {
				// il faut commencer par creer la manche
				SQLQuery insertMancheQuery = session
						.createSQLQuery("insert into Resultat_Manche "
								+ "(Code_evenement, Code_coureur, Code_manche, Rang, Total_freestyle) "
								+ "values (:code_evenement, :code_coureur, :code_manche, -1, :totalPoints) ");
				insertMancheQuery.setParameter("code_manche", typeManche);
				insertMancheQuery.setParameter("code_evenement", courseId);
				insertMancheQuery.setParameter("code_coureur", codeCoureur);
				insertMancheQuery.setParameter("totalPoints", points);

				insertMancheQuery.executeUpdate();
				maj = true;

				manche = courseDao.findManche(courseId, codeCoureur, typeManche);

				System.out.println("import de Manche" + manche);
			}

			// ensuite on regarde si le run existe
			Query queryRun = session.createQuery("from Run r "
					+ "where r.course = :course " + "and r.typeManche = :typeManche "
					+ "and r.coureur = :dossard " + "and r.run = :run");
			queryRun.setParameter("course", manche.getCourse());
			queryRun.setParameter("typeManche", typeManche);
			queryRun.setParameter("dossard", manche.getCoureur());
			queryRun.setParameter("run", nRun);
			Run run = (Run) queryRun.uniqueResult();

			if (run != null) {
				if ((run.getPoints() != null)
						&& !run.getPoints().equals(points)) {
					// on l'update
					maj = true;
					SQLQuery updateRunQuery = session
							.createSQLQuery("update Resultat_Manche_Run "
									+ "set Total_freestyle=:points "
									+ "where Code_run=:code_run "
									+ "and Code_manche=:code_manche "
									+ "and Code_evenement=:code_evenement "
									+ "and Code_coureur=:code_coureur");
					updateRunQuery.setParameter("points", points);
					updateRunQuery.setParameter("code_run", nRun);
					updateRunQuery.setParameter("code_manche", typeManche);
					updateRunQuery.setParameter("code_evenement", courseId);
					updateRunQuery.setParameter("code_coureur", codeCoureur);
					updateRunQuery.executeUpdate();
					System.out.println("Mise a jour du RUN : " + run
							+ "avec les infos" + codeCoureur + " " + nRun + " "
							+ points);
				} else {
					System.out.println("run inchange :" + run);
				}

			} else {
				// on l'insert
				maj = true;
				SQLQuery insertRunQuery = session
						.createSQLQuery("insert into Resultat_Manche_Run "
								+ "(Code_evenement, Code_coureur, Code_manche, Code_run, Total_freestyle, Cltc) "
								+ "values (:code_evenement, :code_coureur, :code_manche, :code_run, :points, -1) ");
				insertRunQuery.setParameter("code_manche", typeManche);
				insertRunQuery.setParameter("code_evenement", courseId);
				insertRunQuery.setParameter("code_coureur", codeCoureur);
				insertRunQuery.setParameter("code_run", nRun);
				insertRunQuery.setParameter("points", points);
				insertRunQuery.executeUpdate();
				System.out.println("creation du RUN : " + codeCoureur + " "
						+ nRun + " " + points);
			}
			// transaction.commit();

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
	 * import le fichier dans la base s il na pas deja ete lu (controle le
	 * cheksum)
	 * 
	 * @param fileName
	 * @return le nombre de ligne importees
	 * @throws FileNotFoundException
	 */
	public Manche importFile(String fileName) throws FileNotFoundException {
		File file = new File(fileName);
		File fileJuges = new File("juge.data");
		long crc32 = 0;
		int nbLigneslus = 0;
		Manche lastManche = null;
		Manche lastValidatedManche = null;

		if (!file.exists() || !file.canRead() || !fileJuges.exists()
				|| !fileJuges.canRead()) {
			System.out.println("impossible de lire le ou les fichiers "
					+ fileName);
		} else {
			try {
				// on ne fait rien si c'est le meme que celui d'avant
				crc32 = FileUtils.checksumCRC32(file)
						+ FileUtils.checksumCRC32(fileJuges);
				if (crc32 == lastChecksum) {
					System.out.println("fichiers deja lu");
					// DEBUG
					return null;
				}
			} catch (IOException e1) {
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
						System.out.println("la ligne est incomplete [" + newRun
								+ "]");
					} else if (split[1].length() == 0 || split[2].length() == 0
							|| split[3].length() == 0 || split[4].length() == 0) {
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

				bf = new BufferedReader(new FileReader(fileJuges));
				String juge;
				while ((juge = bf.readLine()) != null) {
					Manche majValidatedManche = majJuge(juge);
					if (majValidatedManche != null) {
						lastValidatedManche = majValidatedManche;
					}
				}
				bf.close();
				lastChecksum = crc32;
				// DEBUG file.renameTo(new File(fileName + ".ok"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (lastValidatedManche != null) {
			return verifyRunValidation(lastValidatedManche);
		} else if (lastManche != null)
			return verifyRunValidation(lastManche);

		return null;
	}

	private Manche majJuge(String juge) {
		String[] split = juge.split("\\|");
		// rappel du format attendu
		// 77|K1D223927|32|2|1|50|10|0|||160|1
		/*
		 * Code_evenement integer not null , Code_coureur char(50) not null ,
		 * Code_manche integer not null , Code_run integer not null , Code_juge
		 * integer not null , Entry_move integer , Fluidite integer ,
		 * Bonus_spectacle integer , Reserve_1 char(30) , Reserve_2 char(30) ,
		 * Total_run_juge integer , Validation integer
		 */
		Manche lastValidatedManche = null;

		if (split.length < 11) {
			System.out.println("la ligne est incomplete [" + juge + "]");
		} else if (split[1].length() == 0 || split[2].length() == 0
				|| split[3].length() == 0 || split[4].length() == 0) {
			System.out.println("format incorrect [" + juge + "]");
		} else {
			int courseId = Integer.parseInt(split[0]);
			String codeCoureur = split[1];
			int typeManche = Integer.parseInt(split[2]);
			int nRun = Integer.parseInt(split[3]);
			int nJuge = Integer.parseInt(split[4]);
			Integer entryMove = parseInteger(split[5]);
			Integer fluidite = parseInteger(split[6]);
			Integer bonus = parseInteger(split[7]);
			Integer total = parseInteger(split[10]);

			Integer validation = null;
			if (split.length > 11)
				validation = parseInteger(split[11]);

			Session session = sessionFactory.openSession();
			try {
				Manche manche = courseDao.findManche(courseId, codeCoureur, typeManche);

				if (majJuge(manche, nRun, nJuge, entryMove, fluidite, bonus,
						total, validation)) {
					lastValidatedManche = manche;
				}
				;

			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				if (session != null) {
					session.close();
				}
			}

		}
		return lastValidatedManche;
	}

	private Integer parseInteger(String integerOrNull) {
		if (integerOrNull == null || integerOrNull.length() == 0)
			return null;
		return new Integer(integerOrNull);
	}

	private boolean majJuge(Manche manche, int nRun, int nJuge,
			Integer entryMove, Integer fluidite, Integer bonus, Integer total,
			Integer validation) {
		Session session = null;
		boolean newValidation = false;
		try {
			session = sessionFactory.openSession();

			Query query = session.createQuery("from RunJuge r "
					+ "where r.course = :course " + "and r.typeManche = :typeManche "
					+ "and r.coureur = :dossard " + "and r.run = :run "
					+ "and r.juge = :juge");
			query.setParameter("course", manche.getCourse());
			query.setParameter("typeManche", manche.getTypeManche());
			query.setParameter("dossard", manche.getCoureur());
			query.setParameter("run", nRun);
			query.setParameter("juge", nJuge);
			RunJuge runJuge = (RunJuge) query.uniqueResult();

			if (runJuge != null) {
				if (!runJuge.isValidate() && (validation != null)
						&& (validation == 1)) {
					newValidation = true;
					System.out.println("Validation pour le Juge : " + runJuge);
				}

				// on le valide
				SQLQuery updateJugeQuery = session
						.createSQLQuery("update Resultat_Manche_Run_Juges "
								+ "set Validation =:validation, "
								+ "Entry_move = :entryMove, "
								+ "Fluidite = :fluidite, "
								+ "Bonus_spectacle = :bonus, "
								+ "Total_run_juge = :total "
								+ "where Code_run=:code_run "
								+ "and Code_manche=:code_manche "
								+ "and Code_evenement=:code_evenement "
								+ "and Code_coureur=:code_coureur "
								+ "and Code_juge=:code_juge ");
				updateJugeQuery.setParameter("validation", validation);
				updateJugeQuery.setParameter("entryMove", entryMove);
				updateJugeQuery.setParameter("bonus", bonus);
				updateJugeQuery.setParameter("total", total);
				updateJugeQuery.setParameter("validation", validation);
				updateJugeQuery.setParameter("code_run", nRun);
				updateJugeQuery.setParameter("code_manche", manche.getTypeManche());
				updateJugeQuery.setParameter("code_evenement", manche
						.getCourse().getId());
				updateJugeQuery.setParameter("code_coureur", manche
						.getCoureur().getCodeCoureur());
				updateJugeQuery.setParameter("code_juge", nJuge);
				updateJugeQuery.executeUpdate();

			} else {
				SQLQuery insertQuery = session
						.createSQLQuery("insert into Resultat_Manche_Run_Juges "
								+ "(Code_evenement, Code_coureur, Code_manche, Code_run, Code_juge, Validation, Entry_move, Fluidite, Bonus_spectacle, Total_run_juge) "
								+ "values (:code_evenement, :code_coureur, :code_manche, :code_run, :code_juge, :validation, :entryMove, :fluidite, :bonus, :total) ");
				insertQuery.setParameter("code_manche", manche.getTypeManche());
				insertQuery.setParameter("code_evenement", manche.getCourse()
						.getId());
				insertQuery.setParameter("code_coureur", manche.getCoureur()
						.getCodeCoureur());
				insertQuery.setParameter("code_run", nRun);
				insertQuery.setParameter("code_juge", nJuge);
				insertQuery.setParameter("validation", validation);
				insertQuery.setParameter("entryMove", entryMove);
				insertQuery.setParameter("bonus", bonus);
				insertQuery.setParameter("total", total);
				insertQuery.executeUpdate();
				System.out
						.println("creation du JUGE : " + manche + " " + nJuge);
				if (validation != null && validation == 1) {
					System.out
							.println("Validation directe du run pour le Juge : "
									+ manche + " " + nJuge);
					newValidation = true;
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (session != null) {
				session.close();
			}
		}
		return newValidation;
	}

	/**
	 * demande l'import des données concernant la course, ...
	 * 
	 * @param course
	 * @param phase
	 * @param timeout
	 * @param codeCoureur
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	public Manche importFile(int course, int phase, int timeout,
			String codeCoureur, String fileName) throws IOException {
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
