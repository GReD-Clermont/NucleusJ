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

import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServiceException;
import ij.IJ;
import ij.Prefs;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.concurrent.ExecutionException;


/**
 * Class to construct graphical interface for the chromocenter analysis pipeline in batch
 *
 * @author pouletaxel
 */
public class ChromocenterAnalysisDialog extends JFrame implements ItemListener {
	private static final long         serialVersionUID        = 896147828956284745L;
	private final        JTextField   jTextFieldWorkDirectory = new JTextField();
	private final        JTextField   jTextFieldRawData       = new JTextField();
	private final        JRadioButton jRadioButtonRhfV        = new JRadioButton("VolumeRHF");
	private final        JRadioButton jRadioButtonRhfI        = new JRadioButton("IntensityRHF");
	private final        JRadioButton jRadioButtonRhfIV       = new JRadioButton("VolumeRHF and IntensityRHF");
	private final        JRadioButton jRadioButtonNucCc       = new JRadioButton("Nucleus and chromocenter");
	private final        JRadioButton jRadioButtonCc          = new JRadioButton("Chromocenter");
	private final        JRadioButton jRadioButtonNuc         = new JRadioButton("Nucleus");
	private final        JLabel       jLabelUnit              = new JLabel();
	private final        JLabel       jLabelXCalibration      = new JLabel();
	private final        JLabel       jLabelYCalibration      = new JLabel();
	private final        JLabel       jLabelZCalibration      = new JLabel();
	private final        JTextPane    readUnit                = new JTextPane();
	private final        JTextPane    readXCalibration        = new JTextPane();
	private final        JTextPane    readYCalibration        = new JTextPane();
	private final        JTextPane    readZCalibration        = new JTextPane();
	private final        JCheckBox    addCalibrationBox       = new JCheckBox();
	private final        JPanel       calibration;
	
	private final transient IDialogListener dialogListener;
	
	private final JRadioButton omeroYesButton  = new JRadioButton("Yes");
	private final JRadioButton omeroNoButton   = new JRadioButton("No");
	private final OMEROPanel   omeroModeLayout;
	private final JPanel       localModeLayout = new JPanel();
	
	private final Container container;
	
	private boolean start;
	private boolean useOMERO;
	
	
	/** Architecture of the graphical windows */
	public ChromocenterAnalysisDialog(IDialogListener dialogListener) {
		final String font = "Albertus";
		container = super.getContentPane();
		
		JLabel      jLabelWorkDirectory  = new JLabel("Work directory and data directory choice:");
		JButton     jButtonWorkDirectory = new JButton("Output Directory");
		JButton     jButtonStart         = new JButton("Start");
		JButton     jButtonQuit          = new JButton("Quit");
		JButton     jButtonRawData       = new JButton("Raw Data");
		ButtonGroup buttonGroupAnalysis  = new ButtonGroup();
		ButtonGroup buttonGroupChoiceRhf = new ButtonGroup();
		JLabel      jLabelAnalysis;
		JLabel      jLabelAnalysis2;
		super.setTitle("Chromocenters Analysis Pipeline (Batch)");
		super.setSize(500, 700);
		super.setLocationRelativeTo(null);
		this.dialogListener = dialogListener;
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.1};
		gridBagLayout.rowHeights = new int[]{17, 200, 124, 7};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0, 0.1};
		gridBagLayout.columnWidths = new int[]{236, 120, 72, 20};
		container.setLayout(gridBagLayout);
		
		String eol = System.lineSeparator();
		
		// Use Omero ?
		ButtonGroup bGroupOmeroMode = new ButtonGroup();
		bGroupOmeroMode.add(omeroYesButton);
		omeroYesButton.addItemListener(this);
		bGroupOmeroMode.add(omeroNoButton);
		omeroNoButton.setSelected(true);
		omeroNoButton.addItemListener(this);
		
		JPanel radioOmeroPanel = new JPanel();
		radioOmeroPanel.setLayout(new BoxLayout(radioOmeroPanel, BoxLayout.LINE_AXIS));
		JLabel jLabelOmero = new JLabel("Select from omero:");
		radioOmeroPanel.add(jLabelOmero);
		radioOmeroPanel.add(omeroYesButton);
		radioOmeroPanel.add(omeroNoButton);
		
		container.add(radioOmeroPanel,
		              new GridBagConstraints(0, 0, 2, 1, 1.0, 0.0,
		                                     GridBagConstraints.FIRST_LINE_START,
		                                     GridBagConstraints.HORIZONTAL,
		                                     new Insets(10, 10, 0, 10), 0, 0));
		
		// Local mode layout
		localModeLayout.setLayout(new BoxLayout(localModeLayout, BoxLayout.PAGE_AXIS));
		JPanel        localPanel  = new JPanel();
		GridBagLayout localLayout = new GridBagLayout();
		localLayout.columnWeights = new double[]{1, 5, 0.5};
		localPanel.setLayout(localLayout);
		
		localPanel.add(jLabelWorkDirectory,
		               new GridBagConstraints(0, 0, 3, 1, 0.0, 0.0,
		                                      GridBagConstraints.FIRST_LINE_START,
		                                      GridBagConstraints.NONE,
		                                      new Insets(10, 10, 0, 10), 0, 0));
		
		JTextPane jTextPane = new JTextPane();
		jTextPane.setText("The Raw Data directory must contain 3 subdirectories:" + eol +
		                  "1. for raw nuclei images, named RawDataNucleus. " + eol +
		                  "2. for segmented nuclei images, named SegmentedDataNucleus." + eol +
		                  "3. for segmented images of chromocenters, named SegmentedDataCc." + eol +
		                  "Please keep the same file name during the image processing.");
		jTextPane.setEditable(false);
		localPanel.add(jTextPane,
		               new GridBagConstraints(0, 1, 3, 1, 1.0, 0.0,
		                                      GridBagConstraints.FIRST_LINE_START,
		                                      GridBagConstraints.HORIZONTAL,
		                                      new Insets(10, 10, 0, 10), 0, 0));
		
		localPanel.add(jButtonRawData,
		               new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
		                                      GridBagConstraints.FIRST_LINE_START,
		                                      GridBagConstraints.NONE,
		                                      new Insets(10, 10, 0, 5), 0, 0));
		jButtonRawData.setPreferredSize(new Dimension(120, 21));
		jButtonRawData.setFont(new Font(font, Font.ITALIC, 10));
		
		localPanel.add(jTextFieldRawData,
		               new GridBagConstraints(1, 2, 2, 1, 1.0, 0.0,
		                                      GridBagConstraints.FIRST_LINE_START,
		                                      GridBagConstraints.HORIZONTAL,
		                                      new Insets(10, 5, 0, 10), 0, 0));
		jTextFieldRawData.setPreferredSize(new Dimension(280, 21));
		jTextFieldRawData.setFont(new Font(font, Font.ITALIC, 10));
		jTextFieldRawData.setName("nucleusj.ccparams.rawdata");
		
		localPanel.add(jButtonWorkDirectory,
		               new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
		                                      GridBagConstraints.FIRST_LINE_START,
		                                      GridBagConstraints.NONE,
		                                      new Insets(10, 10, 0, 5), 0, 0));
		jButtonWorkDirectory.setPreferredSize(new Dimension(120, 21));
		jButtonWorkDirectory.setFont(new Font(font, Font.ITALIC, 10));
		
		localPanel.add(jTextFieldWorkDirectory,
		               new GridBagConstraints(1, 3, 2, 1, 1.0, 0.0,
		                                      GridBagConstraints.FIRST_LINE_START,
		                                      GridBagConstraints.HORIZONTAL,
		                                      new Insets(10, 5, 0, 10), 0, 0));
		jTextFieldWorkDirectory.setPreferredSize(new Dimension(280, 21));
		jTextFieldWorkDirectory.setFont(new Font(font, Font.ITALIC, 10));
		jTextFieldWorkDirectory.setName("nucleusj.ccparams.workdirectory");
		
		calibration = new JPanel();
		calibration.setLayout(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.weightx = 2;
		gc.weighty = 5;
		gc.anchor = GridBagConstraints.FIRST_LINE_START;
		gc.fill = GridBagConstraints.HORIZONTAL;
		JLabel calibrationLabel = new JLabel("Calibration:");
		gc.gridx = 0;
		gc.gridy = 0;
		calibration.add(calibrationLabel, gc);
		gc.gridx = 1;
		gc.gridy = 0;
		addCalibrationBox.setSelected(false);
		addCalibrationBox.addItemListener(this);
		calibration.add(addCalibrationBox, gc);
		
		jLabelAnalysis = new JLabel("Results file of interest:");
		container.add(jLabelAnalysis,
		              new GridBagConstraints(0, 3, 5, 1, 0.0, 0.0,
		                                     GridBagConstraints.LAST_LINE_START,
		                                     GridBagConstraints.NONE,
		                                     new Insets(10, 10, 0, 10), 0, 0));
		
		buttonGroupAnalysis.add(jRadioButtonNucCc);
		buttonGroupAnalysis.add(jRadioButtonCc);
		buttonGroupAnalysis.add(jRadioButtonNuc);
		
		JPanel analysisPanel = new JPanel();
		analysisPanel.setLayout(new BoxLayout(analysisPanel, BoxLayout.LINE_AXIS));
		analysisPanel.add(jRadioButtonNucCc);
		analysisPanel.add(jRadioButtonCc);
		analysisPanel.add(jRadioButtonNuc);
		
		container.add(analysisPanel,
		              new GridBagConstraints(0, 4, 3, 1, 1.0, 0.0,
		                                     GridBagConstraints.FIRST_LINE_START,
		                                     GridBagConstraints.NONE,
		                                     new Insets(10, 10, 0, 10), 0, 0));
		jLabelAnalysis2 = new JLabel("Type of Relative Heterochromatin Fraction:");
		container.add(jLabelAnalysis2,
		              new GridBagConstraints(0, 5, 3, 1, 1.0, 0.0,
		                                     GridBagConstraints.FIRST_LINE_START,
		                                     GridBagConstraints.NONE,
		                                     new Insets(10, 10, 0, 10), 0, 0));
		buttonGroupChoiceRhf.add(jRadioButtonRhfV);
		buttonGroupChoiceRhf.add(jRadioButtonRhfI);
		buttonGroupChoiceRhf.add(jRadioButtonRhfIV);
		
		JPanel rhfPanel = new JPanel();
		rhfPanel.setLayout(new BoxLayout(rhfPanel, BoxLayout.LINE_AXIS));
		rhfPanel.add(jRadioButtonRhfV);
		rhfPanel.add(jRadioButtonRhfI);
		rhfPanel.add(jRadioButtonRhfIV);
		
		container.add(rhfPanel,
		              new GridBagConstraints(0, 6, 5, 1, 1.0, 0.0,
		                                     GridBagConstraints.FIRST_LINE_START,
		                                     GridBagConstraints.HORIZONTAL,
		                                     new Insets(10, 10, 0, 10), 0, 0));
		
		localModeLayout.add(localPanel);
		container.add(localModeLayout,
		              new GridBagConstraints(0, 1, 4, 1, 1.0, 0.0,
		                                     GridBagConstraints.FIRST_LINE_START,
		                                     GridBagConstraints.HORIZONTAL,
		                                     new Insets(10, 10, 10, 10), 0, 0));
		
		// Omero mode layout
		String[] dataTypes = {"Dataset", "Image"};
		String   label1    = "Image Source:";
		String   label2    = "Nucleus segmentation:";
		String   label3    = "Chromocenter segmentation:";
		omeroModeLayout = new OMEROPanel(dataTypes, label1, label2, label3);
		
		// Buttons at the bottom
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.TRAILING));
		buttonPanel.add(jButtonStart);
		buttonPanel.add(jButtonQuit);
		
		container.add(buttonPanel,
		              new GridBagConstraints(0, 7, 4, 1, 1.0, 0.0,
		                                     GridBagConstraints.PAGE_END, GridBagConstraints.NONE,
		                                     new Insets(10, 10, 10, 10), 0, 0));
		
		jButtonQuit.setPreferredSize(new Dimension(120, 21));
		jButtonStart.setPreferredSize(new Dimension(120, 21));
		jButtonWorkDirectory.addActionListener(e -> chooseDirectory(jTextFieldWorkDirectory));
		jButtonRawData.addActionListener(e -> chooseDirectory(jTextFieldRawData));
		jButtonQuit.addActionListener(this::quit);
		jButtonStart.addActionListener(this::start);
		super.setVisible(true);
	}
	
	
	public String getHostname() {
		return omeroModeLayout.getHostname();
	}
	
	
	public String getPort() {
		return omeroModeLayout.getPort();
	}
	
	
	public String getUsername() {
		return omeroModeLayout.getUsername();
	}
	
	
	public char[] getPassword() {
		return omeroModeLayout.getPassword();
	}
	
	
	public String getGroup() {
		return omeroModeLayout.getGroup();
	}
	
	
	public String getOutputProject() {
		return omeroModeLayout.getOutputProject();
	}
	
	
	public String getSourceID() {
		return omeroModeLayout.getSourceID(0);
	}
	
	
	public String getSegID() {
		return omeroModeLayout.getSourceID(1);
	}
	
	
	public String getCcID() {
		return omeroModeLayout.getSourceID(2);
	}
	
	
	public String getDataType() {
		return omeroModeLayout.getDataType(0);
	}
	
	
	public String getDataTypeSeg() {
		return omeroModeLayout.getDataType(1);
	}
	
	
	public String getDataTypeCC() {
		return omeroModeLayout.getDataType(2);
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
	
	
	public boolean isStart() {
		return start;
	}
	
	
	public boolean isNucAndCcAnalysis() {
		return jRadioButtonNucCc.isSelected();
	}
	
	
	public boolean useOMERO() {
		return useOMERO;
	}
	
	
	public boolean isNucAnalysis() {
		return jRadioButtonNuc.isSelected();
	}
	
	
	public boolean isCcAnalysis() {
		return jRadioButtonCc.isSelected();
	}
	
	
	public boolean isRHFVolumeAndIntensity() {
		return jRadioButtonRhfIV.isSelected();
	}
	
	
	public boolean isRhfVolume() {
		return jRadioButtonRhfV.isSelected();
	}
	
	
	public boolean isRhfIntensity() {
		return jRadioButtonRhfI.isSelected();
	}
	
	
	@Override
	public void itemStateChanged(ItemEvent e) {
		// Check if the Omero "Yes" button is selected
		if (e.getSource() == omeroYesButton && e.getStateChange() == ItemEvent.SELECTED) {
			// Show Omero layout and hide local mode layout
			omeroModeLayout.setVisible(true);
			localModeLayout.setVisible(false);
			container.add(omeroModeLayout,
			              new GridBagConstraints(0, 1, 4, 1, 1.0, 0.0,
			                                     GridBagConstraints.FIRST_LINE_START,
			                                     GridBagConstraints.HORIZONTAL,
			                                     new Insets(10, 10, 10, 10), 0, 0));
			useOMERO = true;
		}
		// Check if the Omero "No" button is selected
		else if (e.getSource() == omeroNoButton && e.getStateChange() == ItemEvent.SELECTED) {
			// Show local mode layout and hide Omero layout
			omeroModeLayout.setVisible(false);
			localModeLayout.setVisible(true);
			container.add(localModeLayout,
			              new GridBagConstraints(0, 1, 4, 1, 1.0, 0.0,
			                                     GridBagConstraints.FIRST_LINE_START,
			                                     GridBagConstraints.HORIZONTAL,
			                                     new Insets(10, 10, 10, 10), 0, 0));
			useOMERO = false;
		}
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
		// Refresh the container to reflect the changes
		container.revalidate();
		container.repaint();
	}
	
	
	/**
	 * Closes the dialog.
	 *
	 * @param actionEvent the action event
	 */
	private void quit(ActionEvent actionEvent) {
		dispose();
	}
	
	
	/**
	 * Starts the analysis pipeline.
	 *
	 * @param actionEvent the action event
	 */
	private void start(ActionEvent actionEvent) {
		if (!useOMERO && (jTextFieldWorkDirectory.getText().isEmpty() || jTextFieldRawData.getText().isEmpty())) {
			JOptionPane.showMessageDialog(null,
			                              "You did not choose a work directory or the raw data",
			                              "Error",
			                              JOptionPane.ERROR_MESSAGE);
		} else {
			start = true;
			dispose();
			try {
				dialogListener.onStart();
			} catch (AccessException | ExecutionException | ServiceException e) {
				IJ.error("Error starting the process", e.getMessage());
			}
		}
	}
	
	
	private void chooseDirectory(JTextField textField) {
		String pref = textField.getName();
		if (pref == null || pref.isEmpty()) {
			pref = "nj.ccparams." + Prefs.DIR_IMAGE;
		}
		String previousDir = textField.getText();
		if (previousDir.isEmpty()) {
			previousDir = Prefs.get(pref, previousDir);
		}
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		JFileChooser jFileChooser = new JFileChooser(previousDir);
		jFileChooser.setDialogTitle("Select a directory");
		
		jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int returnValue = jFileChooser.showOpenDialog(getParent());
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			String directory = jFileChooser.getSelectedFile().getAbsolutePath();
			textField.setText(directory);
		}
		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}
	
}