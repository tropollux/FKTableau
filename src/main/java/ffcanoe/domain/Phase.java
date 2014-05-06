package ffcanoe.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@SuppressWarnings("serial")
@Entity
@Table(name = "Epreuve_Freestyle_Categorie")
public class Phase implements Serializable{

	@Id
	private int id;

	@ManyToOne
	@JoinColumn(name="Id_evenement")
	private Course course;
	
	@Column(name="Code_categorie")
	private String categorie;
	
	@Column(name="Code_manche")
	private Integer typeManche;

	@Column(name="Nb_runs")
	private int nbRun;
	
	@Column(name="Bateau_par_poule")
	private int boatPerPool;

	@Column(name="Nb_qualifies")
	private int nbQualifies;
	
	@Column(name="Type_resultat")
	private int typeResultat;

	private String libelle;
    
	public Course getCourse() {
		return course;
	}
	public void setCourse(Course evenement) {
		this.course = evenement;
	}

	public String getCategorie() {
		return categorie;
	}
	public void setCategorie(String categorie) {
		this.categorie = categorie;
	}
	
	public int getNbRun() {
		return nbRun;
	}
	public void setNbRun(int nbRun) {
		this.nbRun = nbRun;
	}
	
	public int getBoatPerPool() {
		return boatPerPool;
	}
	public void setBoatPerPool(int boatPerPool) {
		this.boatPerPool = boatPerPool;
	}
	
	public int getNbQualifies() {
		return nbQualifies;
	}
	public void setNbQualifies(int nbQualifies) {
		this.nbQualifies = nbQualifies;
	}

	public int getTypeResultat() {
		return typeResultat;
	}
	public void setTypeResultat(int typeResultat) {
		this.typeResultat = typeResultat;
	}

	public String getLibelle() {
		return libelle;
	}
	public void setLibelle(String libelle) {
		this.libelle = libelle;
	}
	public Integer getTypeManche() {
		return typeManche;
	}
	public void setTypeManche(Integer typeManche) {
		this.typeManche = typeManche;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((course == null) ? 0 : course.hashCode());
		result = prime * result
				+ ((typeManche == null) ? 0 : typeManche.hashCode());
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
		Phase other = (Phase) obj;
		if (course == null) {
			if (other.course != null)
				return false;
		} else if (!course.equals(other.course))
			return false;
		if (typeManche == null) {
			if (other.typeManche != null)
				return false;
		} else if (!typeManche.equals(other.typeManche))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "Phase [course=" + course + ", categorie=" + categorie
				+ ", typeManche=" + typeManche + ", nbRun=" + nbRun
				+ ", boatPerPool=" + boatPerPool + ", nbQualifies="
				+ nbQualifies + ", typeResultat=" + typeResultat + ", libelle="
				+ libelle + "]";
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
 

}