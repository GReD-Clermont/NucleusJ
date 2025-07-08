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
	private static final long serialVersionUID = -1L;
	
	public JProgressBar bar;
	
	
	public Progress() {
	}
	
	
	public Progress(String title, int nb) {
		this.setSize(400, 80);
		this.setTitle("*** " + title + " ***");
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setLocationRelativeTo(null);
		this.bar = new JProgressBar();
		this.bar.setMaximum(nb);
		this.bar.setMinimum(0);
		this.bar.setStringPainted(true);
		this.getContentPane().add(this.bar, BorderLayout.CENTER);
		this.setVisible(true);
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new Progress("NODeJ process", 22);
	}
	
}