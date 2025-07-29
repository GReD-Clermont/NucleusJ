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

import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServiceException;
import ij.Prefs;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.concurrent.ExecutionException;


/**
 * Class to construct graphical interface for the chromocenter segmentation pipeline in batch
 *
 * @author Poulet Axel
 */
public class ChromocenterSegmentationPipelineBatchDialog extends JFrame implements ItemListener {
	private static final long serialVersionUID = -1605324416480852307L;
	
	private final transient IDialogListener dialogListener;
	
	private final JTextField jTextFieldWorkDirectory = new JTextField();
	private final JTextField jTextFieldRawData       = new JTextField();
	private final JLabel     jLabelUnit              = new JLabel();
	private final JLabel     jLabelXCalibration      = new JLabel();
	private final JLabel     jLabelYCalibration      = new JLabel();
	private final JLabel     jLabelZCalibration      = new JLabel();
	private final JTextPane  readUnit                = new JTextPane();
	private final JTextPane  readXCalibration        = new JTextPane();
	private final JTextPane  readYCalibration        = new JTextPane();
	private final JTextPane  readZCalibration        = new JTextPane();
	private final JCheckBox  addCalibrationBox       = new JCheckBox();
	private final JPanel     calibration;
	
	
	/** Architecture of the graphical windows */
	public ChromocenterSegmentationPipelineBatchDialog(IDialogListener dialogListener) {
		this.dialogListener = dialogListener;
		Container container;
		JLabel    jLabelWorkDirectory;
		JLabel    jLabelCalibration;
		JButton   jButtonWorkDirectory = new JButton("Output Directory");
		JButton   jButtonStart         = new JButton("Start");
		JButton   jButtonQuit          = new JButton("Quit");
		JButton   jButtonRawData       = new JButton("Raw Data");
		super.setTitle("Chromocenters segmentation pipeline (Batch)");
		super.setSize(500, 500);
		super.setLocationRelativeTo(null);
		
		Font italic = new java.awt.Font("Albertus", Font.ITALIC, 10);
		
		container = super.getContentPane();
		GridBagLayout gridBagLayout = new GridBagLayout();
		// 	gridBagLayout.rowWeights = new double[] {0.0, 0.0, 0.0, 0.0};
		gridBagLayout.rowHeights = new int[]{17, 200, 124, 7, 10};
		// 	gridBagLayout.columnWeights = new double[] {0.0, 20.0, 0.0, 0.1};
		gridBagLayout.columnWidths = new int[]{236, 120, 72, 20};
		container.setLayout(gridBagLayout);
		
		jLabelWorkDirectory = new JLabel();
		container.add(jLabelWorkDirectory,
		              new GridBagConstraints(0, 1, 0, 0, 0.0, 0.0,
		                                     GridBagConstraints.FIRST_LINE_START,
		                                     GridBagConstraints.NONE,
		                                     new Insets(0, 10, 0, 0), 0, 0));
		jLabelWorkDirectory.setText("Work directory and Raw data choice : ");
		
		String eol = System.lineSeparator();
		
		JTextPane jTextPane = new JTextPane();
		jTextPane.setText("The Raw Data directory must contain 2 subdirectories:" + eol +
		                  "1.for raw nuclei images, named RawDataNucleus. " + eol +
		                  "2.for segmented nuclei images, named SegmentedDataNucleus." + eol +
		                  "Please keep the same file name during the image processing.");
		jTextPane.setEditable(false);
		container.add(jTextPane,
		              new GridBagConstraints(0, 1, 0, 0, 0.0, 0.0,
		                                     GridBagConstraints.FIRST_LINE_START,
		                                     GridBagConstraints.NONE,
		                                     new Insets(20, 20, 0, 0), 0, 0));
		
		container.add(jButtonRawData,
		              new GridBagConstraints(0, 1, 0, 0, 0.0, 0.0,
		                                     GridBagConstraints.FIRST_LINE_START,
		                                     GridBagConstraints.NONE,
		                                     new Insets(100, 10, 0, 0), 0, 0));
		jButtonRawData.setPreferredSize(new java.awt.Dimension(120, 21));
		jButtonRawData.setFont(italic);
		
		container.add(jTextFieldRawData,
		              new GridBagConstraints(0, 1, 0, 0, 0.0, 0.0,
		                                     GridBagConstraints.FIRST_LINE_START,
		                                     GridBagConstraints.NONE,
		                                     new Insets(100, 160, 0, 0), 0, 0));
		jTextFieldRawData.setPreferredSize(new java.awt.Dimension(280, 21));
		jTextFieldRawData.setFont(italic);
		jTextFieldRawData.setName("nj.ccseg.rawdata");
		
		container.add(jButtonWorkDirectory,
		              new GridBagConstraints(0, 1, 0, 0, 0.0, 0.0,
		                                     GridBagConstraints.FIRST_LINE_START,
		                                     GridBagConstraints.NONE,
		                                     new Insets(140, 10, 0, 0), 0, 0));
		jButtonWorkDirectory.setPreferredSize(new java.awt.Dimension(120, 21));
		jButtonWorkDirectory.setFont(italic);
		
		container.add(jTextFieldWorkDirectory,
		              new GridBagConstraints(0, 1, 0, 0, 0.0, 0.0,
		                                     GridBagConstraints.FIRST_LINE_START,
		                                     GridBagConstraints.NONE,
		                                     new Insets(140, 160, 0, 0), 0, 0));
		jTextFieldWorkDirectory.setPreferredSize(new java.awt.Dimension(280, 21));
		jTextFieldWorkDirectory.setFont(italic);
		jTextFieldWorkDirectory.setName("nj.ccseg.workdirectory");
		
		jLabelCalibration = new JLabel();
		container.add(jLabelCalibration,
		              new GridBagConstraints(0, 2, 0, 0, 0.0, 0.0,
		                                     GridBagConstraints.FIRST_LINE_START,
		                                     GridBagConstraints.NONE,
		                                     new Insets(20, 10, 0, 0), 0, 0));
		
		calibration = new JPanel();
		calibration.setLayout(new GridBagLayout());
		
		GridBagConstraints gc = new GridBagConstraints();
		gc.weightx = 2;
		gc.weighty = 5;
		gc.anchor = GridBagConstraints.FIRST_LINE_START;
		gc.ipady = GridBagConstraints.FIRST_LINE_START;
		JLabel calibrationLabel = new JLabel("Calibration:");
		gc.gridx = 0;
		gc.gridy = 0;
		calibrationLabel.setAlignmentX(0);
		calibration.add(calibrationLabel);
		
		gc.gridx = 1;
		gc.gridy = 0;
		
		addCalibrationBox.setSelected(false);
		addCalibrationBox.addItemListener(this);
		calibration.add(addCalibrationBox, gc);
		
		container.add(calibration,
		              new GridBagConstraints(0, 2, 2, 0, 0.0, 0.0,
		                                     GridBagConstraints.FIRST_LINE_START,
		                                     GridBagConstraints.NONE,
		                                     new Insets(0, 0, 0, 0), 0, 0));
		
		container.add(jButtonStart,
		              new GridBagConstraints(0, 2, 0, 0, 0.0, 0.0,
		                                     GridBagConstraints.FIRST_LINE_START,
		                                     GridBagConstraints.NONE,
		                                     new Insets(190, 140, 0, 0), 0, 0));
		jButtonStart.setPreferredSize(new java.awt.Dimension(120, 21));
		container.add(jButtonQuit,
		              new GridBagConstraints(0, 2, 0, 0, 0.0, 0.0,
		                                     GridBagConstraints.FIRST_LINE_START,
		                                     GridBagConstraints.NONE,
		                                     new Insets(190, 10, 0, 0), 0, 0));
		jButtonQuit.setPreferredSize(new java.awt.Dimension(120, 21));
		super.setVisible(true);
		
		jButtonWorkDirectory.addActionListener(e -> chooseDirectory(jTextFieldWorkDirectory));
		jButtonRawData.addActionListener(e -> chooseDirectory(jTextFieldRawData));
		jButtonQuit.addActionListener(e -> dispose());
		jButtonStart.addActionListener(e -> start());
	}
	
	
	public double getXCalibration() {
		String xCal = readXCalibration.getText();
		return Double.parseDouble(xCal.replace(",", "."));
	}
	
	
	public double getYCalibration() {
		String yCal = readYCalibration.getText();
		return Double.parseDouble(yCal.replace(",", "."));
	}
	
	
	public double getZCalibration() {
		String zCal = readZCalibration.getText();
		return Double.parseDouble(zCal.replace(",", "."));
	}
	
	
	public boolean getCalibrationStatus() {
		return addCalibrationBox.isSelected();
	}
	
	
	public String getUnit() {
		return readUnit.getText();
	}
	
	
	public String getWorkDirectory() {
		return jTextFieldWorkDirectory.getText();
	}
	
	
	public String getRawDataDirectory() {
		return jTextFieldRawData.getText();
	}
	
	
	public void itemStateChanged(ItemEvent e) {
		if (e.getSource() == addCalibrationBox) {
			if (addCalibrationBox.isSelected()) {
				GridBagConstraints gc = new GridBagConstraints();
				gc.insets = new Insets(0, 0, 5, 0);
				
				jLabelUnit.setText("Unit :");
				gc.gridx = 0;
				gc.gridy = 1;
				calibration.add(jLabelUnit, gc);
				readUnit.setPreferredSize(new Dimension(100, 20));
				readUnit.setText("µm");
				gc.gridx = 1;
				gc.gridy = 1;
				calibration.add(readUnit, gc);
				jLabelUnit.setVisible(true);
				readUnit.setVisible(true);
				
				jLabelXCalibration.setText("X :");
				gc.gridx = 0;
				gc.gridy = 2;
				calibration.add(jLabelXCalibration, gc);
				readXCalibration.setPreferredSize(new Dimension(100, 20));
				readXCalibration.setText("1");
				gc.gridx = 1;
				gc.gridy = 2;
				calibration.add(readXCalibration, gc);
				jLabelXCalibration.setVisible(true);
				readXCalibration.setVisible(true);
				
				jLabelYCalibration.setText("Y :");
				gc.gridx = 0;
				gc.gridy = 3;
				calibration.add(jLabelYCalibration, gc);
				readYCalibration.setPreferredSize(new Dimension(100, 20));
				readYCalibration.setText("1");
				gc.gridx = 1;
				gc.gridy = 3;
				calibration.add(readYCalibration, gc);
				jLabelYCalibration.setVisible(true);
				readYCalibration.setVisible(true);
				
				jLabelZCalibration.setText("Z :");
				gc.gridx = 0;
				gc.gridy = 4;
				calibration.add(jLabelZCalibration, gc);
				readZCalibration.setPreferredSize(new Dimension(100, 20));
				readZCalibration.setText("1");
				gc.gridx = 1;
				gc.gridy = 4;
				calibration.add(readZCalibration, gc);
				jLabelZCalibration.setVisible(true);
				readZCalibration.setVisible(true);
			} else {
				jLabelXCalibration.setVisible(false);
				jLabelYCalibration.setVisible(false);
				jLabelZCalibration.setVisible(false);
				jLabelUnit.setVisible(false);
				
				readXCalibration.setVisible(false);
				readYCalibration.setVisible(false);
				readZCalibration.setVisible(false);
				readUnit.setVisible(false);
			}
			validate();
			repaint();
		}
	}
	
	
	/**
	 * Starts the segmentation process.
	 */
	private void start() {
		try {
			dialogListener.onStart();
		} catch (AccessException | ExecutionException | ServiceException e) {
			JOptionPane.showMessageDialog(this,
			                              "An error occurred while starting the process: " + e.getMessage(),
			                              "Error",
			                              JOptionPane.ERROR_MESSAGE);
		}
	}
	
	

	private void chooseDirectory(JTextField textField) {
		String pref = textField.getName();
		if (pref.isEmpty()) {
			pref = "nj.ccseg." + Prefs.DIR_IMAGE;
		}
		String previousDir = textField.getText();
		if (previousDir.isEmpty()) {
			previousDir = Prefs.get(pref, previousDir);
		}
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		JFileChooser jFileChooser = new JFileChooser(previousDir);
		jFileChooser.setDialogTitle("Select the Work Directory");
		
		jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int returnValue = jFileChooser.showOpenDialog(getParent());
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			String directory = jFileChooser.getSelectedFile().getAbsolutePath();
			textField.setText(directory);
		}
		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}
	
}