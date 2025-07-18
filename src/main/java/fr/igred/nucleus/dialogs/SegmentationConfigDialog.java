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

import fr.igred.nucleus.utils.ConvexHullDetection;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;


public class SegmentationConfigDialog extends JFrame implements ItemListener {
	private final JTextField minVolume           = new JTextField();
	private final JTextField maxVolume           = new JTextField();
	private final JCheckBox  convexHullDetection = new JCheckBox();
	private final JTextField xCalibration        = new JTextField();
	private final JTextField yCalibration        = new JTextField();
	private final JTextField zCalibration        = new JTextField();
	private final JCheckBox  addCalibrationBox   = new JCheckBox();
	private final JButton    buttonOK            = new JButton("Done");
	private final JPanel     volumePane;
	private       JPanel     xCalibrationPanel;
	private       JPanel     yCalibrationPanel;
	private       JPanel     zCalibrationPanel;
	
	
	public SegmentationConfigDialog() {
		super.setTitle("Segmentation - NucleusJ3");
		super.setSize(300, 340);
		super.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		Container     container     = super.getContentPane();
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.rowWeights = new double[]{1.0};
		gridBagLayout.rowHeights = new int[]{300};
		gridBagLayout.columnWeights = new double[]{0.0, 0.3};
		gridBagLayout.columnWidths = new int[]{180, 500};
		
		container.setLayout(gridBagLayout);
		super.getRootPane().setDefaultButton(buttonOK);


        /*/\*\
        -------------------------- Crop Box -----------------------------------
        \*\/*/
		
		
		volumePane = new JPanel();
		volumePane.setLayout(new BoxLayout(volumePane, BoxLayout.PAGE_AXIS));
		volumePane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		volumePane.setAlignmentX(0);
		
		JPanel minVolumePane = new JPanel();
		minVolumePane.setLayout(new BoxLayout(minVolumePane, BoxLayout.LINE_AXIS));
		minVolumePane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		JLabel xBox = new JLabel("Min:");
		minVolumePane.add(xBox);
		minVolumePane.add(Box.createRigidArea(new Dimension(10, 0)));
		minVolume.setText("1");
		minVolume.setMinimumSize(new Dimension(60, 10));
		minVolumePane.add(minVolume);
		
		JPanel maxVolumePane = new JPanel();
		maxVolumePane.setLayout(new BoxLayout(maxVolumePane, BoxLayout.LINE_AXIS));
		maxVolumePane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		JLabel yBox = new JLabel("Max:");
		maxVolumePane.add(yBox);
		maxVolumePane.add(Box.createRigidArea(new Dimension(10, 0)));
		maxVolume.setText("3000000");
		maxVolume.setMinimumSize(new Dimension(60, 10));
		maxVolumePane.add(maxVolume);
		
		JPanel convexHullPane = new JPanel();
		convexHullPane.setLayout(new BoxLayout(convexHullPane, BoxLayout.LINE_AXIS));
		convexHullPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		JLabel zBox = new JLabel("Convex Hull Detection (" + ConvexHullDetection.CONVEX_HULL_ALGORITHM + ":");
		convexHullPane.add(zBox);
		convexHullPane.add(Box.createRigidArea(new Dimension(10, 0)));
		convexHullDetection.setSelected(true);
		convexHullDetection.setMinimumSize(new Dimension(100, 10));
		convexHullDetection.addItemListener(this);
		convexHullPane.add(convexHullDetection);
		
		JLabel volumeLabel = new JLabel("Volume:");
		volumeLabel.setAlignmentX(0);
		volumePane.add(volumeLabel);
		volumePane.add(minVolumePane);
		volumePane.add(maxVolumePane);
		volumePane.add(convexHullPane);
		volumePane.add(Box.createRigidArea(new Dimension(0, 20)));


        /*/\*\
        -------------------------- Calibration -----------------------------------
        \*\/*/
		
		
		JPanel calibrationPanel = new JPanel();
		JLabel calibrationLabel = new JLabel("Calibration:");
		calibrationLabel.setAlignmentX(0);
		calibrationPanel.add(calibrationLabel);
		addCalibrationBox.setSelected(false);
		addCalibrationBox.setMinimumSize(new Dimension(100, 10));
		addCalibrationBox.addItemListener(this);
		calibrationPanel.add(addCalibrationBox);
		volumePane.add(calibrationPanel);


        /*/\*\
        -------------------------- Validation Button -----------------------------------
        \*\/*/
		
		
		buttonOK.setPreferredSize(new java.awt.Dimension(80, 21));
		volumePane.add(Box.createRigidArea(new Dimension(0, 10)));
		volumePane.add(buttonOK);
		
		container.add(volumePane, new GridBagConstraints(0,
		                                                 0,
		                                                 0,
		                                                 0,
		                                                 0.0,
		                                                 0.0,
		                                                 GridBagConstraints.FIRST_LINE_START,
		                                                 GridBagConstraints.NONE,
		                                                 new Insets(0, 0, 0, 0),
		                                                 0,
		                                                 0));
		
		super.setVisible(false);
		
		ActionListener startListener = new StartListener(this);
		buttonOK.addActionListener(startListener);
	}
	
	
	public String getMinVolume() {
		return minVolume.getText();
	}
	
	
	public String getMaxVolume() {
		return maxVolume.getText();
	}
	
	
	public boolean getConvexHullDetection() {
		return convexHullDetection.isSelected();
	}
	
	
	public String getXCalibration() {
		return xCalibration.getText();
	}
	
	
	public String getYCalibration() {
		return yCalibration.getText();
	}
	
	
	public String getZCalibration() {
		return zCalibration.getText();
	}
	
	
	public boolean isCalibrationSelected() {
		return addCalibrationBox.isSelected();
	}
	
	
	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getSource() == convexHullDetection) {
			boolean isConvexHullDetection = convexHullDetection.isSelected();
		} else if (e.getSource() == addCalibrationBox) {
			if (addCalibrationBox.isSelected()) {
				xCalibrationPanel = new JPanel();
				xCalibrationPanel.setLayout(new BoxLayout(xCalibrationPanel, BoxLayout.LINE_AXIS));
				xCalibrationPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
				JLabel xBox2 = new JLabel("X:");
				xCalibrationPanel.add(xBox2);
				xCalibrationPanel.add(Box.createRigidArea(new Dimension(10, 0)));
				xCalibration.setText("1");
				xCalibration.setMinimumSize(new Dimension(60, 10));
				xCalibrationPanel.add(xCalibration);
				
				yCalibrationPanel = new JPanel();
				yCalibrationPanel.setLayout(new BoxLayout(yCalibrationPanel, BoxLayout.LINE_AXIS));
				yCalibrationPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
				JLabel yBox2 = new JLabel("Y:");
				yCalibrationPanel.add(yBox2);
				yCalibrationPanel.add(Box.createRigidArea(new Dimension(10, 0)));
				yCalibration.setText("1");
				yCalibration.setMinimumSize(new Dimension(60, 10));
				yCalibrationPanel.add(yCalibration);
				
				zCalibrationPanel = new JPanel();
				zCalibrationPanel.setLayout(new BoxLayout(zCalibrationPanel, BoxLayout.LINE_AXIS));
				zCalibrationPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
				JLabel zBox2 = new JLabel("Z:");
				zCalibrationPanel.add(zBox2);
				zCalibrationPanel.add(Box.createRigidArea(new Dimension(10, 0)));
				zCalibration.setText("1");
				zCalibration.setMinimumSize(new Dimension(60, 10));
				zCalibrationPanel.add(zCalibration);
				
				volumePane.remove(buttonOK);
				volumePane.add(xCalibrationPanel);
				volumePane.add(yCalibrationPanel);
				volumePane.add(zCalibrationPanel);
				volumePane.add(buttonOK);
			} else {
				try {
					volumePane.remove(buttonOK);
					volumePane.remove(xCalibrationPanel);
					volumePane.remove(yCalibrationPanel);
					volumePane.remove(zCalibrationPanel);
					volumePane.add(buttonOK);
				} catch (NullPointerException nullPointerException) {
					// Do nothing
				}
			}
		}
		validate();
		repaint();
	}
	
	
	private static class StartListener implements ActionListener {
		private final SegmentationConfigDialog segmentationDialog;
		
		
		/** @param segmentationDialog  */
		StartListener(SegmentationConfigDialog segmentationDialog) {
			this.segmentationDialog = segmentationDialog;
		}
		
		
		public void actionPerformed(ActionEvent actionEvent) {
			segmentationDialog.setVisible(false);
		}
		
	}
	
}