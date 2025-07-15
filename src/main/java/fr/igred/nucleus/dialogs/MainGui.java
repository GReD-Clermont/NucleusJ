package fr.igred.nucleus.dialogs;


import fr.igred.nucleus.plugins.Autocrop_;
import fr.igred.nucleus.plugins.ChromocentersAnalysisBatchPlugin_;
import fr.igred.nucleus.plugins.ComputeParametersPlugin_;
import fr.igred.nucleus.plugins.CropFromCoordinates_;
import fr.igred.nucleus.plugins.GenerateOverlay_;
import fr.igred.nucleus.plugins.NODeJ;
import fr.igred.nucleus.plugins.Segmentation_;
import ij.plugin.PlugIn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.lang.invoke.MethodHandles;


public class MainGui extends JFrame {
	/** Logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	
	public MainGui() {
		super.setTitle("NucleusJ 3");
		super.setMinimumSize(new Dimension(300, 400));
		super.setDefaultCloseOperation(EXIT_ON_CLOSE);
		super.setLocationRelativeTo(null);
		
		Container container = super.getContentPane();
		
		LayoutManager mainBoxLayout = new BoxLayout(super.getContentPane(), BoxLayout.PAGE_AXIS);
		container.setLayout(mainBoxLayout);
		
		JPanel localPanel = new JPanel();
		localPanel.setLayout(new GridBagLayout());
		
		JLabel welcomeLabel = new JLabel("Welcome to NJ!");
		welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
		
		localPanel.add(welcomeLabel,
		               new GridBagConstraints(0, 0, 0, 0, 0.0, 0.0,
		                                      GridBagConstraints.FIRST_LINE_START,
		                                      GridBagConstraints.HORIZONTAL,
		                                      new Insets(0, 0, 0, 0), 0, 0));
		
		JButton autocropButton = new JButton("Autocrop");
		localPanel.add(autocropButton,
		               new GridBagConstraints(0, 0, 0, 0, 0.0, 0.0,
		                                      GridBagConstraints.FIRST_LINE_START,
		                                      GridBagConstraints.HORIZONTAL,
		                                      new Insets(40, 0, 0, 0), 120, 0));
		
		JButton segmentationButton = new JButton("Segmentation");
		localPanel.add(segmentationButton,
		               new GridBagConstraints(0, 0, 0, 0, 0.0, 0.0,
		                                      GridBagConstraints.FIRST_LINE_START,
		                                      GridBagConstraints.HORIZONTAL,
		                                      new Insets(80, 0, 0, 0), 85, 0));
		
		JButton cropFromCoordinatesButton = new JButton("Crop From Coordinates");
		localPanel.add(cropFromCoordinatesButton,
		               new GridBagConstraints(0, 0, 0, 0, 0.0, 0.0,
		                                      GridBagConstraints.FIRST_LINE_START,
		                                      GridBagConstraints.HORIZONTAL,
		                                      new Insets(120, 0, 0, 0), 20, 0));
		
		JButton overlayButton = new JButton("Overlay");
		localPanel.add(overlayButton,
		               new GridBagConstraints(0, 0, 0, 0, 0.0, 0.0,
		                                      GridBagConstraints.FIRST_LINE_START,
		                                      GridBagConstraints.HORIZONTAL,
		                                      new Insets(160, 0, 0, 0), 130, 0));
		
		JButton computeParametersButton = new JButton("Compute Parameters Nuc");
		localPanel.add(computeParametersButton,
		               new GridBagConstraints(0, 0, 0, 0, 0.0, 0.0,
		                                      GridBagConstraints.FIRST_LINE_START,
		                                      GridBagConstraints.HORIZONTAL,
		                                      new Insets(200, 0, 0, 0), 35, 0));
		
		JButton nodeJButton = new JButton("NODeJ");
		localPanel.add(nodeJButton,
		               new GridBagConstraints(0, 0, 0, 0, 0.0, 0.0,
		                                      GridBagConstraints.FIRST_LINE_START,
		                                      GridBagConstraints.HORIZONTAL,
		                                      new Insets(240, 0, 0, 0), 140, 0));
		
		JButton computeCcParametersButton = new JButton("Compute Parameters Spots");
		localPanel.add(computeCcParametersButton,
		               new GridBagConstraints(0, 0, 0, 0, 0.0, 0.0,
		                                      GridBagConstraints.FIRST_LINE_START,
		                                      GridBagConstraints.HORIZONTAL,
		                                      new Insets(280, 0, 0, 0), 12, 0));
		
		container.add(localPanel, 0);
		
		autocropButton.addActionListener(e -> {
			PlugIn autocrop = new Autocrop_();
			autocrop.run("");
		});
		
		segmentationButton.addActionListener(e -> {
			PlugIn segmentation = new Segmentation_();
			segmentation.run("");
		});
		
		cropFromCoordinatesButton.addActionListener(e -> {
			PlugIn cropFromCoordinates = new CropFromCoordinates_();
			cropFromCoordinates.run("");
		});
		
		overlayButton.addActionListener(e -> {
			PlugIn overlay = new GenerateOverlay_();
			overlay.run("");
		});
		
		computeParametersButton.addActionListener(e -> {
			PlugIn computeParameters = new ComputeParametersPlugin_();
			computeParameters.run("");
		});
		
		nodeJButton.addActionListener(e -> {
			PlugIn nodej = new NODeJ();
			nodej.run("");
		});
		
		computeCcParametersButton.addActionListener(e -> {
			PlugIn computeCcParameters = new ChromocentersAnalysisBatchPlugin_();
			computeCcParameters.run("");
		});
	}
	
}