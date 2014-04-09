package tableau.driver;

import java.util.ArrayList;
import java.util.List;

import tableau.domain.Ligne;
import tableau.domain.Tableau;

/**
 * gere le tableau 
 * 
 * @TODO 	
 * 		mode simulation sans afficher a l'ecran (je ne sais pas si on avoir les 2 modes en meme)
 * 		sauvegarder l'etat du tableau pour une visualisation vituelle .. done
 *  	logguer les trames envoyées pour un diags  .. done avec le mode debug
 *  
 * @author remi
 *
 */
public class AfficheurFKT extends AfficheurBretagne implements IAfficheur{

	private Tableau tableau;
	private List<String> lastTableau = new ArrayList<String>();

	/* (non-Javadoc)
	 * @see tableau.driver.Afficheur#affiche(tableau.domain.Ligne[])
	 */
	@Override
	public synchronized void affiche(Tableau tableau){
		this.tableau = tableau;
		this.setDebug(tableau.isDebug());
		try {
			affiche();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public synchronized void refresh(Tableau aTableau){
		// le principe consiste a ne recopier que ce qui a pu changer .. mais pas l'etat
		tableau.setDebug(aTableau.isDebug());
		tableau.setFlash(aTableau.isDebug());
		tableau.setScroll(aTableau.isScroll());
		
		// et puis on recopie les lignes
		int i=0;
		List<Ligne> oldLines = tableau.getLignes();
		int nbOldLine = oldLines.size(); 
		List<Ligne> newlines = aTableau.getLignes();
		for (Ligne newLine : newlines) {
			if (i < nbOldLine) {
				Ligne oldLine = oldLines.get(i);
				oldLine.setText(newLine.getText());
				oldLine.setFlash(newLine.isFlash());
				oldLine.setScroll(newLine.isScroll());
				oldLine.setCenter(newLine.isCenter());
				i++;
			} else {
				// ajout des nouvelles lignes
				oldLines.add(newLine);
			}
		}
		// purge de l excedant
		while (oldLines.size() > newlines.size()) {
			oldLines.remove(newlines.size());
		}
		// affiche(); -> sera fait par le prochain affichage
	}

	public synchronized void affiche() throws InterruptedException {

		String ligne;
		List<String> lignesToShow = tableau.getLignesToShow();
		List<String> tempTab = new ArrayList<String>();
		for(int iLigne=0; iLigne<5; iLigne++) {
			ligne = lignesToShow.get(iLigne);
			// on remplace les tirets du 8 par des blancs
			tempTab.add(ligne);
			if (isDebug()) {
				System.out.println("tableau : [" + ligne + "]");
			}
		}
		this.affiche(tempTab);
		lastTableau = tempTab;
	}
	
	public List<String> getLastTableau() {
		return lastTableau;
	}

	public void setInitialScrollDelay(int initialScrollDelay) {
		tableau.setInitialScrollDelay(initialScrollDelay);
	}

	public void setTicksPerScroll(int ticksPerScroll) {
		tableau.setTicksPerScroll(ticksPerScroll);
	}
}
