package tableau;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import tableau.domain.Ligne;
import tableau.domain.Tableau;

public class TableauTest {

	@Test
	public void shouldFlash() {
		Tableau tableau = new Tableau();
		Ligne ligne = new Ligne();
		ligne.setFlash(true);
		ligne.setScroll(false);
		ligne.setText("bonjour");
		tableau.getLignes().add(ligne);
		List<String> lignesToShow = tableau.getLignesToShow();
		assertNotNull(lignesToShow);
		assertEquals(lignesToShow.size(), 5);
		assertTrue("bonjour".equalsIgnoreCase(lignesToShow.get(0)));
	}
	
	@Test
	public void shouldScroll() {
		String TEXT="bonjour la compagnie  !";
		Ligne ligne = new Ligne();
		ligne.setFlash(false);
		ligne.setScroll(true);
		ligne.setText(TEXT);
		String textToShow = ligne.getTextToShow();
		assertTrue(TEXT.substring(0).equalsIgnoreCase(textToShow));
		textToShow = ligne.getTextToShow();
		assertTrue(TEXT.substring(1).equalsIgnoreCase(textToShow));
		textToShow = ligne.getTextToShow();
		assertTrue(TEXT.substring(2).equalsIgnoreCase(textToShow));
	}


}
