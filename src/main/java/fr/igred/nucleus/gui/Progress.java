/*
 * NucleusJ
 * Copyright (C) 2014-2025 iGReD
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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