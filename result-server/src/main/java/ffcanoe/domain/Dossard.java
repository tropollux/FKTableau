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
@Table(name="Resultat")
public class Dossard implements Serializable{
	
	/** 
	 * numero de la course
	 */
	@Id
	@ManyToOne
	@JoinColumn(name="Code_evenement")
	private	Course course;
	
	/**
	 * Kxxx
	 */
	@Id
	@Column(name="Code_coureur")
	private String codeCoureur;
	
	/**
	 * le vrai dossard
	 */
	private String dossard;

	/**
	 * nom prenom du competiteur
	 */
	private String bateau;
	
	/**
	 * club du competiteur
	 */
	private String club;
	
	public String getCodeCoureur() {
		return codeCoureur;
	}

	public void setCodeCoureur(String codeCoureur) {
		this.codeCoureur = codeCoureur;
	}

	public String getDossard() {
		return dossard;
	}

	public void setDossard(String dossard) {
		this.dossard = dossard;
	}

	public String getBateau() {
		return bateau;
	}

	public void setBateau(String bateau) {
		this.bateau = bateau;
	}

	public String getClub() {
		return club;
	}

	public void setClub(String club) {
		this.club = club;
	}

	public Course getCourse() {
		return course;
	}

	public void setCourse(Course evenement) {
		this.course = evenement;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((codeCoureur == null) ? 0 : codeCoureur.hashCode());
		result = prime * result + ((course == null) ? 0 : course.hashCode());
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
		Dossard other = (Dossard) obj;
		if (codeCoureur == null) {
			if (other.codeCoureur != null)
				return false;
		} else if (!codeCoureur.equals(other.codeCoureur))
			return false;
		if (course == null) {
			if (other.course != null)
				return false;
		} else if (!course.equals(other.course))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Dossard [course=" + course + ", codeCoureur=" + codeCoureur
				+ ", dossard=" + dossard + ", bateau=" + bateau + ", club="
				+ club + "]";
	}

}
