package tableau.domain;

public class Ligne {
	private String text; 
	private boolean scroll;
	private boolean flash;
	private boolean center;
	private int offset;
	private boolean visible = true;
	
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public boolean isScroll() {
		return scroll;
	}
	public void setScroll(boolean scroll) {
		this.scroll = scroll;
	}
	public boolean isFlash() {
		return flash;
	}
	public void setFlash(boolean clignote) {
		this.flash = clignote;
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
	 * retourne le texte suivant l'etat de la ligne et le modifie
	 * 
	 * @return
	 */
	public String getTextToShow() {
		if (isScroll() && isVisible()) {
			int lOffset = getOffset();
			if (lOffset >= getText().length()) {
				lOffset = 0;
			}
			setOffset(lOffset+1);
			return max20c((getText() + " - " +  getText()).substring(lOffset));
		}
		String textToShow = getText();
		if (isCenter() && textToShow.length() < 20) {
			StringBuffer sb = new StringBuffer();
			int nbSpaceToAdd = (20 - textToShow.length()) / 2 ;
			while (nbSpaceToAdd-- > 0) {
				sb.append('_');
			}
			sb.append(textToShow);
			textToShow = sb.toString();
		}
		
		if (isFlash()) {
			boolean lVisible = isVisible();
			setVisible(! isVisible());
			if (lVisible) {
				return max20c(textToShow);
			} else {
				return "";
			}
		}
		return max20c(textToShow);
	}
	
	private String max20c(String string) {
		return (string.length() > 20 ? string.substring(0, 20) : string); 
	}
	public boolean isCenter() {
		return center;
	}
	public void setCenter(boolean center) {
		this.center = center;
	}
}
