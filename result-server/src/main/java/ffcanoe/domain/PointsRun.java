package ffcanoe.domain;

import java.io.Serializable;

@SuppressWarnings("serial")
public class PointsRun implements Serializable{
	
	/**
	 * numero du run
	 */
	private Integer run;
	
	/**
	 * points obtenus
	 */
	private Integer points;
	
	/**
	 * 
	 * valide par tous les juges
	 */
	private boolean valid;

	public Integer getPoints() {
		return points;
	}

	public void setPoints(Integer points) {
		this.points = points;
	}

	public Integer getRun() {
		return run;
	}

	public void setRun(Integer run) {
		this.run = run;
	}

	public PointsRun(Integer run, Integer points) {
		super();
		this.run = run;
		this.points = points;
	}

	public PointsRun(Integer run) {
		super();
		this.run = run;
	}

	public PointsRun() {
		super();
	}

	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}

}
