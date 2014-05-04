package ffcanoe.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * contient les classements par course/phase/dossard
 * rem : ne contient pas les points par runs et il s'agit des classements en fin de manche
 * 
 * @author remi
 *
 */
@SuppressWarnings("serial")
@Entity
@Table(name="Resultat_Manche")
public class Manche implements Serializable{
	
	/**
	 * course
	 */
	@Id
	@ManyToOne
	@JoinColumn(name="Code_evenement")
	private Course course;
	
	@Id
	@Column(name="Code_Manche")
	private int typeManche;

	/**
	 * dossard
	 */
	@Id
	@ManyToOne
	@JoinColumns( {@JoinColumn(name="Code_evenement"), @JoinColumn(name="Code_coureur")} )
	private Dossard coureur;
	
	/**
	 * ordre de depart ???
	 */
	private Integer rang;

	/**
	 * classement ???
	 */
	@Transient
	private Integer classement;

//	@ElementCollection
//	@CollectionTable(name="Resultat_Manche_Run", 
//			joinColumns={@JoinColumn(name="Code_evenement"), 
//						 @JoinColumn(name="Code_coureur"), 
//						 @JoinColumn(name="Code_manche")})
//	@Column(name="Total_freestyle")
//	List<Integer> runs;
	
//	@OneToMany(targetEntity=Run.class, )
//	@MapKey
//	Map<Integer, Integer> runs	;

//	@Transient
//	private List<Integer> runs = new ArrayList<Integer>();

	@Transient
	private List<PointsRun> runs = new ArrayList<PointsRun>();

	@Transient
	private Integer totalManche;

	public Integer getRang() {
		return rang;
	}

	public void setRang(Integer rang) {
		this.rang = rang;
	}

	public Course getCourse() {
		return course;
	}

	public void setCourse(Course evenement) {
		this.course = evenement;
	}

	public Dossard getCoureur() {
		return coureur;
	}

	public void setCoureur(Dossard coureur) {
		this.coureur = coureur;
	}

	public Integer getTotalManche() {
		return totalManche;
	}

	public void setTotalManche(Integer totalManche) {
		this.totalManche = totalManche;
	}

	public List<PointsRun> getRuns() {
		return runs;
	}

	public Integer getClassement() {
		return classement;
	}

	public void setClassement(Integer classement) {
		this.classement = classement;
	}
	/**
	 * complete la liste des runs avec des valeurs null si besoin
	 */
	public void fillRuns(int nbRun) {
		for (int i = runs.size(); i<nbRun; i++) {
			runs.add(new PointsRun(i+1));
		}
	}

	public int getTypeManche() {
		return typeManche;
	}

	public void setTypeManche(int type) {
		this.typeManche = type;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((coureur == null) ? 0 : coureur.hashCode());
		result = prime * result + ((course == null) ? 0 : course.hashCode());
		result = prime * result + typeManche;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Manche other = (Manche) obj;
		if (coureur == null) {
			if (other.coureur != null)
				return false;
		} else if (!coureur.equals(other.coureur))
			return false;
		if (course == null) {
			if (other.course != null)
				return false;
		} else if (!course.equals(other.course))
			return false;
		if (typeManche != other.typeManche)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Manche [course=" + course + ", type=" + typeManche + ", coureur="
				+ coureur + ", rang=" + rang + ", classement=" + classement
				+ ", runs=" + runs + ", totalManche=" + totalManche + "]";
	}

}
