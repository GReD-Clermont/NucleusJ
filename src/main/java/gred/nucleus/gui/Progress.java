package gred.nucleus.gui;


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
	
	public JProgressBar bar;
	
	
	public Progress() {
	}
	
	
	public Progress(String title, int nb) {
		super.setSize(400, 80);
		super.setTitle("*** " + title + " ***");
		super.setDefaultCloseOperation(EXIT_ON_CLOSE);
		super.setLocationRelativeTo(null);
		this.bar = new JProgressBar();
		bar.setMaximum(nb);
		bar.setMinimum(0);
		bar.setStringPainted(true);
		super.getContentPane().add(bar, BorderLayout.CENTER);
		super.setVisible(true);
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new Progress("NODeJ process", 22);
	}
	
}