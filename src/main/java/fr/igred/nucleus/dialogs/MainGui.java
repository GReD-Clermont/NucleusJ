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
package fr.igred.nucleus.dialogs;


import fr.igred.nucleus.Version;
import fr.igred.nucleus.plugins.Autocrop_;
import fr.igred.nucleus.plugins.ChromocenterSegmentationBatchPlugin_;
import fr.igred.nucleus.plugins.ChromocentersAnalysisBatchPlugin_;
import fr.igred.nucleus.plugins.ComputeParametersPlugin_;
import fr.igred.nucleus.plugins.CropFromCoordinates_;
import fr.igred.nucleus.plugins.GenerateOverlay_;
import fr.igred.nucleus.plugins.NODeJ;
import fr.igred.nucleus.plugins.Segmentation_;
import ij.plugin.PlugIn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.lang.invoke.MethodHandles;


public class MainGui extends JFrame {
	/** Logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	
	public MainGui() {
		super.setTitle("NucleusJ - v" + Version.get());
		super.setDefaultCloseOperation(EXIT_ON_CLOSE);
		super.setLocationRelativeTo(null);
		
		int gap = 10;
		
		Container container = super.getContentPane();
		
		LayoutManager mainBoxLayout = new FlowLayout();
		container.setLayout(mainBoxLayout);
		
		JPanel localPanel = new JPanel();
		localPanel.setLayout(new GridLayout(0, 1, 0, gap));
		localPanel.setAlignmentX(CENTER_ALIGNMENT);
		localPanel.setAlignmentY(CENTER_ALIGNMENT);
		
		JLabel welcomeLabel = new JLabel("Welcome to NJ!");
		welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
		localPanel.add(welcomeLabel);
		
		JButton autocropButton = new JButton("Autocrop");
		localPanel.add(autocropButton);
		
		JButton segmentationButton = new JButton("Segmentation");
		localPanel.add(segmentationButton);
		
		JButton coordsCropButton = new JButton("Crop From Coordinates");
		localPanel.add(coordsCropButton);
		
		JButton overlayButton = new JButton("Overlay");
		localPanel.add(overlayButton);
		
		JButton computeParamsButton = new JButton("Compute Parameters Nuc");
		localPanel.add(computeParamsButton);
		
		JButton nodeJButton = new JButton("NODeJ");
		localPanel.add(nodeJButton);
		
		JButton ccSegmentButton = new JButton("Chromocenter Segmentation");
		localPanel.add(ccSegmentButton);
		
		JButton computeCCParamsBtn = new JButton("Compute Parameters Spots");
		localPanel.add(computeCCParamsBtn);
		
		container.add(localPanel);
		
		// Action listeners for buttons
		autocropButton.addActionListener(e -> {
			PlugIn autocrop = new Autocrop_();
			autocrop.run("");
		});
		
		segmentationButton.addActionListener(e -> {
			PlugIn segmentation = new Segmentation_();
			segmentation.run("");
		});
		
		coordsCropButton.addActionListener(e -> {
			PlugIn cropFromCoordinates = new CropFromCoordinates_();
			cropFromCoordinates.run("");
		});
		
		overlayButton.addActionListener(e -> {
			PlugIn overlay = new GenerateOverlay_();
			overlay.run("");
		});
		
		computeParamsButton.addActionListener(e -> {
			PlugIn computeParameters = new ComputeParametersPlugin_();
			computeParameters.run("");
		});
		
		nodeJButton.addActionListener(e -> {
			PlugIn nodej = new NODeJ();
			nodej.run("");
		});
		
		ccSegmentButton.addActionListener(e -> {
			PlugIn ccSegmentation = new ChromocenterSegmentationBatchPlugin_();
			ccSegmentation.run("");
		});
		
		computeCCParamsBtn.addActionListener(e -> {
			PlugIn computeCcParameters = new ChromocentersAnalysisBatchPlugin_();
			computeCcParameters.run("");
		});
		
		// Repack GUI to ensure all components are laid out correctly
		super.pack();
		
		// Set the minimum size of the GUI
		int width  = localPanel.getPreferredSize().width + 10 * gap;
		int height = localPanel.getPreferredSize().height + 5 * gap;
		super.setMinimumSize(new Dimension(width, height));
		super.setMaximumSize(super.getMinimumSize());
		super.setPreferredSize(super.getMinimumSize());
		super.setResizable(false);
		
		LOGGER.info("Main GUI initialized");
	}
	
}