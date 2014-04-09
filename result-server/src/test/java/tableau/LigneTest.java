package tableau;

import static org.junit.Assert.*;

import org.junit.Test;

import tableau.domain.Ligne;

public class LigneTest {

	@Test
	public void formatText() {
		long xor = 100;
		String result = "" + xor;
		assertEquals(result.length(), 3);
		assertTrue(String.format("%03d", xor).equalsIgnoreCase("100"));
	}

	@Test
	public void formatText2dg() {
		long xor = 10;
		String result = "" + xor;
		assertEquals(result.length(), 2);
		assertTrue(String.format("%03d", xor).equalsIgnoreCase("010"));
	}

	@Test
	public void formatText1dg() {
		long xor = 1;
		String result = "" + xor;
		assertEquals(result.length(), 1);
		assertTrue(String.format("%03d", xor).equalsIgnoreCase("001"));
	}
	
	@Test
	public void shouldFlash() {
		Ligne ligne = new Ligne();
		ligne.setFlash(true);
		ligne.setScroll(false);
		ligne.setText("bonjour");
		String textToShow = ligne.getTextToShow();
		assertTrue("".equalsIgnoreCase(textToShow));
		textToShow = ligne.getTextToShow();
		assertTrue("bonjour".equalsIgnoreCase(textToShow));
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
