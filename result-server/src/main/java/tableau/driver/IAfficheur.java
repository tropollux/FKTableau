package tableau.driver;

import java.util.List;

import tableau.domain.Tableau;

public interface IAfficheur {

	public abstract void affiche(Tableau tableau) throws InterruptedException;
	public List<String> getLastTableau();

	void affiche() throws InterruptedException;
	void setInitialScrollDelay(int initialScrollDelay);
	void setTicksPerScroll(int ticksPerScroll);
	void refresh(Tableau tableau);

}