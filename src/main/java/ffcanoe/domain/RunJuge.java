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
@Table(name="Resultat_manche_run_juges")
public class RunJuge implements Serializable{
	
	
	/*
	 * 	Code_evenement	integer	not null	,
		Code_coureur	char(50)	not null	,
		Code_manche	integer	not null	,
		Code_run	integer	not null	,
		Code_juge	integer	not null	,
		Entry_move	integer		,
		Fluidite	integer		,
		Bonus_spectacle	integer		,
		Reserve_1	char(30)		,
		Reserve_2	char(30)		,
		Total_run_juge	integer		, 
		Validation integer
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
	
//	@Id
//	@ManyToOne
//	@JoinColumns(
//			{@JoinColumn(name="Code_manche"),
//			 @JoinColumn(name="Code_evenement")} )
//	private Phase phase;

	@Id
	@Column(name="Code_Manche")
	private int typeManche;

	@Id
	@Column(name="Code_run")
	private Integer run;
	
	@Id
	@Column(name="Code_juge")
	private Integer juge;

	@Column(name="Total_run_juge")
	private Integer pointsJuge;

	@Column(name="Validation")
	private Integer validation;


	public Integer getJuge() {
		return juge;
	}

	public void setJuge(Integer juge) {
		this.juge = juge;
	}

	public Integer getPointsJuge() {
		return pointsJuge;
	}

	public void setPointsJuge(Integer pointsJuge) {
		this.pointsJuge = pointsJuge;
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

	public Integer getRun() {
		return run;
	}

	public void setRun(Integer code_run) {
		run = code_run;
	}
	
	public boolean isValidate() {
		return (validation != null) && (validation == 1);
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
		result = prime * result + ((juge == null) ? 0 : juge.hashCode());
		result = prime * result
				+ ((pointsJuge == null) ? 0 : pointsJuge.hashCode());
		result = prime * result + ((run == null) ? 0 : run.hashCode());
		result = prime * result + typeManche;
		result = prime * result
				+ ((validation == null) ? 0 : validation.hashCode());
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
		RunJuge other = (RunJuge) obj;
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
		if (juge == null) {
			if (other.juge != null)
				return false;
		} else if (!juge.equals(other.juge))
			return false;
		if (pointsJuge == null) {
			if (other.pointsJuge != null)
				return false;
		} else if (!pointsJuge.equals(other.pointsJuge))
			return false;
		if (run == null) {
			if (other.run != null)
				return false;
		} else if (!run.equals(other.run))
			return false;
		if (typeManche != other.typeManche)
			return false;
		if (validation == null) {
			if (other.validation != null)
				return false;
		} else if (!validation.equals(other.validation))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "RunJuge [course=" + course + ", coureur=" + coureur + ", type="
				+ typeManche + ", run=" + run + ", juge=" + juge + ", pointsJuge="
				+ pointsJuge + ", validation=" + validation + "]";
	}

}
