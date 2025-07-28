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


public class AutocropConfigDialog extends JFrame implements ItemListener {
	private final JTextField xCropBoxSize                = new JTextField();
	private final JTextField yCropBoxSize                = new JTextField();
	private final JTextField zCropBoxSize                = new JTextField();
	private final JTextField boxNumberFontSize           = new JTextField();
	private final JTextField xCalibration                = new JTextField();
	private final JTextField yCalibration                = new JTextField();
	private final JTextField zCalibration                = new JTextField();
	private final JTextField minVolume                   = new JTextField();
	private final JTextField maxVolume                   = new JTextField();
	private final JTextField thresholdOTSUComputing      = new JTextField();
	private final JTextField channelToComputeThreshold   = new JTextField();
	private final JTextField slicesOTSUComputing = new JTextField();
	private final JTextField boxesSurfacePercent = new JTextField();
	private final JCheckBox  regroupBoxes        = new JCheckBox();
	private final JCheckBox  addCalibrationBox           = new JCheckBox();
	private final JPanel     cropBoxPane;
	private       Boolean    isRegroupBoxesSelected      = true;
	private       JPanel     xCalibrationPanel;
	private       JPanel     yCalibrationPanel;
	private       JPanel     zCalibrationPanel;
	
	
	public AutocropConfigDialog() {
		super.setTitle("Autocrop - NucleusJ3");
		super.setSize(600, 350);
		super.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		Container     container     = super.getContentPane();
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.rowWeights = new double[]{1.0};
		gridBagLayout.rowHeights = new int[]{300};
		gridBagLayout.columnWeights = new double[]{0.0, 0.3};
		gridBagLayout.columnWidths = new int[]{180, 500};
		
		container.setLayout(gridBagLayout);
		JButton buttonOK = new JButton("Done");
		super.getRootPane().setDefaultButton(buttonOK);
		
        /*/\*\
        -------------------------- Crop Box ------------------------------------
        \*\/*/
		
		cropBoxPane = new JPanel();
		cropBoxPane.setLayout(new BoxLayout(cropBoxPane, BoxLayout.PAGE_AXIS));
		cropBoxPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		cropBoxPane.setAlignmentX(0);
		
		JPanel xCropBoxPane = new JPanel();
		xCropBoxPane.setLayout(new BoxLayout(xCropBoxPane, BoxLayout.LINE_AXIS));
		xCropBoxPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		JLabel xBox = new JLabel("X:");
		xCropBoxPane.add(xBox);
		xCropBoxPane.add(Box.createRigidArea(new Dimension(10, 0)));
		xCropBoxSize.setText("40");
		xCropBoxSize.setMinimumSize(new Dimension(60, 10));
		xCropBoxPane.add(xCropBoxSize);
		
		JPanel yCropBoxPane = new JPanel();
		yCropBoxPane.setLayout(new BoxLayout(yCropBoxPane, BoxLayout.LINE_AXIS));
		yCropBoxPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		JLabel yBox = new JLabel("Y:");
		yCropBoxPane.add(yBox);
		yCropBoxPane.add(Box.createRigidArea(new Dimension(10, 0)));
		yCropBoxSize.setText("40");
		yCropBoxSize.setMinimumSize(new Dimension(60, 10));
		yCropBoxPane.add(yCropBoxSize);
		
		JPanel zCropBoxPane = new JPanel();
		zCropBoxPane.setLayout(new BoxLayout(zCropBoxPane, BoxLayout.LINE_AXIS));
		zCropBoxPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		JLabel zBox = new JLabel("Z:");
		zCropBoxPane.add(zBox);
		zCropBoxPane.add(Box.createRigidArea(new Dimension(10, 0)));
		zCropBoxSize.setText("20");
		zCropBoxSize.setMinimumSize(new Dimension(60, 10));
		zCropBoxPane.add(zCropBoxSize);
		
		JPanel numberFontSizePane = new JPanel();
		numberFontSizePane.setLayout(new BoxLayout(numberFontSizePane, BoxLayout.LINE_AXIS));
		numberFontSizePane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		JLabel fontSize = new JLabel("Number font size:");
		numberFontSizePane.add(fontSize);
		numberFontSizePane.add(Box.createRigidArea(new Dimension(10, 0)));
		boxNumberFontSize.setText("30");
		boxNumberFontSize.setMinimumSize(new Dimension(40, 10));
		numberFontSizePane.add(boxNumberFontSize);
		
		JLabel cropBoxLabel = new JLabel("Crop Box Size:");
		cropBoxLabel.setAlignmentX(0);
		cropBoxPane.add(cropBoxLabel);
		cropBoxPane.add(xCropBoxPane);
		cropBoxPane.add(yCropBoxPane);
		cropBoxPane.add(zCropBoxPane);
		cropBoxPane.add(numberFontSizePane);
		cropBoxPane.add(Box.createRigidArea(new Dimension(0, 20)));
		
        /*/\*\
        -------------------------- Calibration ---------------------------------
        \*\/*/
		
		JPanel calibrationPanel = new JPanel();
		JLabel calibrationLabel = new JLabel("Calibration:");
		calibrationLabel.setAlignmentX(0);
		calibrationPanel.add(calibrationLabel);
		addCalibrationBox.setSelected(false);
		addCalibrationBox.setMinimumSize(new Dimension(100, 10));
		addCalibrationBox.addItemListener(this);
		calibrationPanel.add(addCalibrationBox);
		cropBoxPane.add(calibrationPanel);
		
		container.add(cropBoxPane,
		              new GridBagConstraints(0, 0, 0, 0, 0.0, 0.0,
		                                     GridBagConstraints.FIRST_LINE_START,
		                                     GridBagConstraints.NONE,
		                                     new Insets(0, 0, 0, 0), 0, 0));
		
        /*/\*\
        -------------------------- Nucleus Volume ------------------------------
        \*\/*/
		
		JPanel volumeNucleus = new JPanel();
		volumeNucleus.setLayout(new BoxLayout(volumeNucleus, BoxLayout.PAGE_AXIS));
		volumeNucleus.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		volumeNucleus.setAlignmentX(0);
		
		JPanel minVolumeNucleus = new JPanel();
		minVolumeNucleus.setLayout(new BoxLayout(minVolumeNucleus, BoxLayout.LINE_AXIS));
		minVolumeNucleus.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		JLabel min = new JLabel("Min:");
		minVolumeNucleus.add(min);
		minVolumeNucleus.add(Box.createRigidArea(new Dimension(10, 0)));
		minVolume.setText("1");
		minVolume.setMinimumSize(new Dimension(100, 10));
		minVolumeNucleus.add(minVolume);
		
		JPanel maxVolumeNucleus = new JPanel();
		maxVolumeNucleus.setLayout(new BoxLayout(maxVolumeNucleus, BoxLayout.LINE_AXIS));
		maxVolumeNucleus.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		JLabel max = new JLabel("Max:");
		maxVolumeNucleus.add(max);
		maxVolumeNucleus.add(Box.createRigidArea(new Dimension(10, 0)));
		maxVolume.setText("2147483647");
		maxVolume.setMinimumSize(new Dimension(100, 10));
		maxVolumeNucleus.add(maxVolume);
		
		JLabel volumeLabel = new JLabel("Volume Nucleus:");
		volumeLabel.setAlignmentX(0);
		volumeNucleus.add(volumeLabel);
		volumeNucleus.add(minVolumeNucleus);
		volumeNucleus.add(maxVolumeNucleus);
		volumeNucleus.add(Box.createRigidArea(new Dimension(0, 20)));

        /*/\*\
        -------------------------- Other ---------------------------------------
        \*\/*/
		
		JPanel otsuThreshPanel = new JPanel();
		otsuThreshPanel.setLayout(new BoxLayout(otsuThreshPanel, BoxLayout.LINE_AXIS));
		otsuThreshPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		JLabel tOTSUValue = new JLabel("Threshold OTSU computing:");
		otsuThreshPanel.add(tOTSUValue);
		otsuThreshPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		thresholdOTSUComputing.setText("20");
		thresholdOTSUComputing.setMinimumSize(new Dimension(100, 10));
		otsuThreshPanel.add(thresholdOTSUComputing);
		
		JPanel threshChannelPanel = new JPanel();
		threshChannelPanel.setLayout(new BoxLayout(threshChannelPanel, BoxLayout.LINE_AXIS));
		threshChannelPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		JLabel threshChannelsValue = new JLabel("Channels to compute threshold:");
		threshChannelPanel.add(threshChannelsValue);
		threshChannelPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		channelToComputeThreshold.setText("0");
		channelToComputeThreshold.setMinimumSize(new Dimension(100, 10));
		threshChannelPanel.add(channelToComputeThreshold);
		
		JPanel otsuSlicesPanel = new JPanel();
		otsuSlicesPanel.setLayout(new BoxLayout(otsuSlicesPanel, BoxLayout.LINE_AXIS));
		otsuSlicesPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		JLabel slicesOTSUValue = new JLabel("Slices OTSU computing:");
		otsuSlicesPanel.add(slicesOTSUValue);
		otsuSlicesPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		slicesOTSUComputing.setText("0");
		slicesOTSUComputing.setMinimumSize(new Dimension(100, 10));
		otsuSlicesPanel.add(slicesOTSUComputing);
		
		JPanel boxesOnSurfacePanel = new JPanel();
		boxesOnSurfacePanel.setLayout(new BoxLayout(boxesOnSurfacePanel, BoxLayout.LINE_AXIS));
		boxesOnSurfacePanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		JLabel boxesSurfaceValue = new JLabel("Boxes percent surface to filter:");
		boxesOnSurfacePanel.add(boxesSurfaceValue);
		boxesOnSurfacePanel.add(Box.createRigidArea(new Dimension(10, 0)));
		boxesSurfacePercent.setText("50");
		boxesSurfacePercent.setMinimumSize(new Dimension(100, 10));
		boxesOnSurfacePanel.add(boxesSurfacePercent);
		
		JPanel regroupBoxesPanel = new JPanel();
		regroupBoxesPanel.setLayout(new BoxLayout(regroupBoxesPanel, BoxLayout.LINE_AXIS));
		regroupBoxesPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		JLabel regroupBoxesValue = new JLabel("Boxes regrouping:");
		regroupBoxesPanel.add(regroupBoxesValue);
		regroupBoxesPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		regroupBoxes.setSelected(true);
		regroupBoxes.setMinimumSize(new Dimension(100, 10));
		regroupBoxes.addItemListener(this);
		regroupBoxesPanel.add(regroupBoxes);
		
		volumeNucleus.add(otsuThreshPanel);
		volumeNucleus.add(threshChannelPanel);
		volumeNucleus.add(otsuSlicesPanel);
		volumeNucleus.add(boxesOnSurfacePanel);
		volumeNucleus.add(regroupBoxesPanel);
		
        /*/\*\
        -------------------------- Validation Button ---------------------------
        \*\/*/
		
		volumeNucleus.add(Box.createRigidArea(new Dimension(0, 20)));
		buttonOK.setPreferredSize(new java.awt.Dimension(80, 21));
		volumeNucleus.add(buttonOK);
		
		container.add(volumeNucleus,
		              new GridBagConstraints(1, 0, 0, 0, 0.0, 0.0,
		                                     GridBagConstraints.FIRST_LINE_END,
		                                     GridBagConstraints.NONE,
		                                     new Insets(0, 0, 0, 0), 0, 0));
		
		super.setVisible(false);
		
		ActionListener startListener = new StartListener(this);
		buttonOK.addActionListener(startListener);
	}
	
	
	public String getXCropBoxSize() {
		return xCropBoxSize.getText();
	}
	
	
	public String getYCropBoxSize() {
		return yCropBoxSize.getText();
	}
	
	
	public String getZCropBoxSize() {
		return zCropBoxSize.getText();
	}
	
	
	public String getBoxNumberFontSize() {
		return boxNumberFontSize.getText();
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
	
	
	public String getMinVolume() {
		return minVolume.getText();
	}
	
	
	public String getMaxVolume() {
		return maxVolume.getText();
	}
	
	
	public String getThresholdOTSUComputing() {
		return thresholdOTSUComputing.getText();
	}
	
	
	public String getChannelToComputeThreshold() {
		return channelToComputeThreshold.getText();
	}
	
	
	public String getSlicesOTSUComputing() {
		return slicesOTSUComputing.getText();
	}
	
	
	public String getBoxesSurfacePercent() {
		return boxesSurfacePercent.getText();
	}
	
	
	public boolean isRegroupBoxesSelected() {
		return isRegroupBoxesSelected;
	}
	
	
	public boolean isCalibrationSelected() {
		return addCalibrationBox.isSelected();
	}
	
	
	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getSource() == regroupBoxes) {
			isRegroupBoxesSelected = regroupBoxes.isSelected();
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
				
				cropBoxPane.add(xCalibrationPanel);
				cropBoxPane.add(yCalibrationPanel);
				cropBoxPane.add(zCalibrationPanel);
			} else {
				try {
					cropBoxPane.remove(xCalibrationPanel);
					cropBoxPane.remove(yCalibrationPanel);
					cropBoxPane.remove(zCalibrationPanel);
				} catch (NullPointerException nullPointerException) {
					// Do nothing
				}
			}
		}
		validate();
		repaint();
	}
	
	
	private static class StartListener implements ActionListener {
		AutocropConfigDialog autocropDialog;
		
		
		/** @param autocropDialog  */
		StartListener(AutocropConfigDialog autocropDialog) {
			this.autocropDialog = autocropDialog;
		}
		
		
		public void actionPerformed(ActionEvent actionEvent) {
			autocropDialog.setVisible(false);
		}
		
	}
	
}