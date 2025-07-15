package fr.igred.nucleus.gui;


import javax.swing.JFrame;
import javax.swing.JProgressBar;
import java.awt.BorderLayout;


/**
 * @author axel poulet
 * <p>
 * OppenClasseroom:
 * https://openclassrooms.com/fr/courses/26832-apprenez-a-programmer-en-java/25010-conteneurs-sliders-et-barres-de-progression
 */
public class Progress extends JFrame {
	/**  */
	private static final long serialVersionUID = -1413087289668250165L;
	
	/** The progress bar. */
	private final JProgressBar progressBar = new JProgressBar();
	
	
	public Progress(String title, int nb) {
		super.setSize(400, 80);
		super.setTitle("*** " + title + " ***");
		super.setDefaultCloseOperation(EXIT_ON_CLOSE);
		super.setLocationRelativeTo(null);
		progressBar.setMaximum(nb);
		progressBar.setMinimum(0);
		progressBar.setStringPainted(true);
		super.getContentPane().add(progressBar, BorderLayout.CENTER);
		super.setVisible(true);
	}
	
	
	/**
	 * Sets the value of the progress bar.
	 *
	 * @param value The value to set.
	 */
	public void setValue(int value) {
		progressBar.setValue(value);
	}
	
}