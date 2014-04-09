package tableau.domain;

import java.util.ArrayList;
import java.util.List;

public class Tableau {
	private List<Ligne> lignes;
	private boolean flash;
	private boolean scroll;
	private int offset;
	private int initialScrollDelay = 2;
	private int scrollDelay = getInitialScrollDelay();
	
	private int ticksPerScroll = 3; // toutes les combiens de fois on scroll !!
	private int ticks;
	private boolean visible=true;
	private boolean debug;

	public Tableau() {
		super();
		lignes = new ArrayList<Ligne>();
	}

	public List<Ligne> getLignes() {
		return lignes;
	}

	public boolean isFlash() {
		return flash;
	}

	public void setFlash(boolean clignote) {
		this.flash = clignote;
	}

	public boolean isScroll() {
		return scroll;
	}

	public void setScroll(boolean defile) {
		this.scroll = defile;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	
	/**
	 * renvoie un tableau de 5 lignes non null dans l'etat ou elles doivent etre et
	 * change l'etat du tableau
	 * 
	 * @return
	 */
	public List<String> getLignesToShow() {

		List<String> lignesVisible = new ArrayList<String>();

		int lOffset=0;
		if (isScroll() && isVisible()) {
			lOffset = getOffset();
			// gestion du nombre de ticks en entre les scrolls
			ticks++;
			if (ticks > ticksPerScroll) {
				ticks = 0;
				if (lOffset >= lignes.size()) {
					lOffset = 0;
					setOffset(0);
					scrollDelay = getInitialScrollDelay() + 1; 
				}
				if (scrollDelay > 0) {
					scrollDelay--;
				} else {
					setOffset(lOffset+1);
				}
			}
		}
		if (isFlash()) {
			boolean lVisible = isVisible();
			setVisible(! isVisible());
			if (! lVisible) {
				lignesVisible.add("");
				lignesVisible.add("");
				lignesVisible.add("");
				lignesVisible.add("");
				lignesVisible.add("");
				return lignesVisible;
			}
		}
		
		// reste visible avec l'offset
		for (int i=lOffset ; i < lOffset+5; i++) {
			if ( i  >= lignes.size() ) {
				lignesVisible.add("");
			} else {
				lignesVisible.add(lignes.get(i).getTextToShow());
			}
		}
		return lignesVisible;
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public int getInitialScrollDelay() {
		return initialScrollDelay;
	}

	public void setInitialScrollDelay(int initialScrollDelay) {
		this.initialScrollDelay = initialScrollDelay;
	}

	public int getTicksPerScroll() {
		return ticksPerScroll;
	}

	public void setTicksPerScroll(int ticksPerScroll) {
		this.ticksPerScroll = ticksPerScroll;
	}
	
}
