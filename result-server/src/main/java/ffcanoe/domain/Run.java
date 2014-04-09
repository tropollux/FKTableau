package ffcanoe.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@SuppressWarnings("serial")
@Entity
@Table(name="Resultat_Manche_Run")
public class Run implements Serializable{
	
	
	/*
	 * 	Code_evenement  integer not null        ,
		Code_coureur    char(50)        not null
		Code_manche     integer not null        ,
		Code_run        integer not null        ,
		Total_freestyle double          ,
		Cltc    int_clt         ,
		Reserve_1       integer         ,
		Reserve_2       integer         ,
		Absent  integer
	 */
	
	@Id
	@ManyToOne
	@JoinColumn(name="Code_evenement")
	private Course course;
	
	@Id
	@ManyToOne
	@JoinColumns(
			{@JoinColumn(name="Code_evenement"), @JoinColumn(name="Code_coureur")} )
	private Dossard coureur;
	
	@Id
	@ManyToOne
	@JoinColumns(
			{@JoinColumn(name="Code_manche"),
			 @JoinColumn(name="Code_evenement")} )
	private Phase phase;
	
	@Id
	@Column(name="Code_run")
	private Integer run;
	
	@Column(name="Total_freestyle")
	private Integer points;

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

	public Phase getPhase() {
		return phase;
	}

	public void setPhase(Phase typeManche) {
		this.phase = typeManche;
	}

	public Integer getRun() {
		return run;
	}

	public void setRun(Integer code_run) {
		run = code_run;
	}

	public Integer getPoints() {
		return points;
	}

	public void setPoints(Integer points) {
		this.points = points;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((coureur == null) ? 0 : coureur.hashCode());
		result = prime * result + ((course == null) ? 0 : course.hashCode());
		result = prime * result + ((phase == null) ? 0 : phase.hashCode());
		result = prime * result + run;
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
		Run other = (Run) obj;
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
		if (phase == null) {
			if (other.phase != null)
				return false;
		} else if (!phase.equals(other.phase))
			return false;
		if (run != other.run)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Run [course=" + course + ", coureur=" + coureur + ", phase="
				+ phase + ", run=" + run + ", points=" + points + "]";
	}
	
}
