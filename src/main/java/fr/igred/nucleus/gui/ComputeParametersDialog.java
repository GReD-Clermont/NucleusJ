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

import javax.swing.BorderFactory;
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
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.concurrent.ExecutionException;


public class ComputeParametersDialog extends JFrame implements ItemListener {
	private static final long serialVersionUID = 7134074339815211476L;
	
	private static final JButton jButtonWorkDirectory = new JButton("Seg Data folder");
	
	private final transient IDialogListener dialogListener;
	
	private final JTextField jTextFieldWorkDirectory = new JTextField();
	private final JTextField jTextFieldRawData       = new JTextField();
	private final JTextPane  readUnit                = new JTextPane();
	private final JLabel     jLabelUnit              = new JLabel();
	private final JLabel     jLabelXCalibration      = new JLabel();
	private final JLabel     jLabelYCalibration      = new JLabel();
	private final JLabel     jLabelZCalibration      = new JLabel();
	private final JTextPane  readXCalibration        = new JTextPane();
	private final JTextPane  readYCalibration        = new JTextPane();
	private final JTextPane  readZCalibration        = new JTextPane();
	private final JCheckBox  addCalibrationBox       = new JCheckBox();
	
	private final JPanel calibration;
	
	private final JRadioButton   omeroYesButton     = new JRadioButton("Yes");
	private final JRadioButton   omeroNoButton      = new JRadioButton("No");
	private final JPanel         localModeLayout    = new JPanel();
	private final OMEROPanel     omeroModeLayout    = new OMEROPanel();
	
	private final Container container;
	
	private boolean start;
	private boolean useOMERO;
	
	
	/** Architecture of the graphical windows */
	public ComputeParametersDialog(IDialogListener dialogListener) {
		this.dialogListener = dialogListener;
		final String font = "Albertus";
		container = super.getContentPane();
		JLabel  jLabelWorkDirectory = new JLabel();
		JLabel  jLabelCalibration   = new JLabel();
		JButton jButtonStart        = new JButton("Start");
		jButtonStart.setBackground(new Color(0x2dce98));
		jButtonStart.setForeground(Color.white);
		JButton jButtonQuit = new JButton("Quit");
		jButtonQuit.setBackground(Color.red);
		jButtonQuit.setForeground(Color.white);
		JButton jButtonRawData = new JButton("Raw Data folder");
		super.setTitle("Compute morphological parameters");
		super.setSize(500, 500);
		super.setLocationRelativeTo(null);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.rowHeights = new int[]{17, 200, 124, 7, 10};
		gridBagLayout.columnWidths = new int[]{236, 120, 72, 20};
		container.setLayout(new BoxLayout(container, BoxLayout.PAGE_AXIS));
		
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
		radioOmeroPanel.setBorder(BorderFactory.createLineBorder(Color.black, 1));
		container.add(radioOmeroPanel, 0);
		
		// local mode layout
		
		localModeLayout.setLayout(new BoxLayout(localModeLayout, BoxLayout.PAGE_AXIS));
		
		JPanel        localPanel  = new JPanel();
		GridBagLayout localLayout = new GridBagLayout();
		localLayout.columnWeights = new double[]{1, 5, 0.5};
		localPanel.setLayout(gridBagLayout);
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		
		localPanel.add(jLabelWorkDirectory,
		               new GridBagConstraints(0, 1, 0, 0, 0.0, 0.0,
		                                      GridBagConstraints.FIRST_LINE_START,
		                                      GridBagConstraints.NONE,
		                                      new Insets(0, 10, 0, 0), 0, 0));
		jLabelWorkDirectory.setText("Work directory and Raw data choice : ");
		JTextPane jTextPane = new JTextPane();
		jTextPane.setText("You must select 2 directories:" + eol +
		                  "1 containing raw nuclei images. " + eol +
		                  "2 containing segmented nuclei images." + eol +
		                  "Images must have same file name.");
		jTextPane.setEditable(false);
		localPanel.add(jTextPane,
		               new GridBagConstraints(0, 1, 0, 0, 0.0, 0.0,
		                                      GridBagConstraints.FIRST_LINE_START,
		                                      GridBagConstraints.NONE,
		                                      new Insets(20, 20, 0, 0), 0, 0));
		localPanel.add(jButtonRawData,
		               new GridBagConstraints(0, 1, 0, 0, 0.0, 0.0,
		                                      GridBagConstraints.FIRST_LINE_START,
		                                      GridBagConstraints.NONE,
		                                      new Insets(100, 10, 0, 0), 0, 0));
		jButtonRawData.setPreferredSize(new java.awt.Dimension(120, 21));
		jButtonRawData.setFont(new java.awt.Font(font, Font.ITALIC, 10));
		localPanel.add(jTextFieldRawData,
		               new GridBagConstraints(0, 1, 0, 0, 0.0, 0.0,
		                                      GridBagConstraints.FIRST_LINE_START,
		                                      GridBagConstraints.NONE,
		                                      new Insets(100, 160, 0, 0), 0, 0));
		jTextFieldRawData.setPreferredSize(new java.awt.Dimension(280, 21));
		jTextFieldRawData.setFont(new java.awt.Font(font, Font.ITALIC, 10));
		jTextFieldRawData.setName("nj.segparams.rawdata");
		
		localPanel.add(jButtonWorkDirectory,
		               new GridBagConstraints(0, 1, 0, 0, 0.0, 0.0,
		                                      GridBagConstraints.FIRST_LINE_START,
		                                      GridBagConstraints.NONE,
		                                      new Insets(140, 10, 0, 0), 0, 0));
		jButtonWorkDirectory.setPreferredSize(new java.awt.Dimension(120, 21));
		jButtonWorkDirectory.setFont(new java.awt.Font(font, Font.ITALIC, 10));
		localPanel.add(jTextFieldWorkDirectory,
		               new GridBagConstraints(0, 1, 0, 0, 0.0, 0.0,
		                                      GridBagConstraints.FIRST_LINE_START,
		                                      GridBagConstraints.NONE,
		                                      new Insets(140, 160, 0, 0), 0, 0));
		jTextFieldWorkDirectory.setPreferredSize(new java.awt.Dimension(280, 21));
		jTextFieldWorkDirectory.setFont(new java.awt.Font(font, Font.ITALIC, 10));
		jTextFieldWorkDirectory.setName("nj.segparams.workdir");
		
		localPanel.add(jLabelCalibration,
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
		addCalibrationBox.setSelected(false);
		addCalibrationBox.addItemListener(this);
		calibration.add(addCalibrationBox, gc);
		localPanel.add(calibration,
		               new GridBagConstraints(0, 2, 2, 0, 0.0, 0.0,
		                                      GridBagConstraints.FIRST_LINE_START,
		                                      GridBagConstraints.NONE,
		                                      new Insets(0, 0, 0, 0), 0, 0));
		
		Border padding2 = BorderFactory.createEmptyBorder(10, 120, 10, 120);
		
		JPanel startExit = new JPanel();
		startExit.setLayout(new GridLayout(1, 2, 30, 10));
		startExit.add(jButtonStart,
		              new GridBagConstraints(0, 2, 0, 0, 0.0, 0.0,
		                                     GridBagConstraints.FIRST_LINE_START,
		                                     GridBagConstraints.NONE,
		                                     new Insets(190, 140, 0, 0), 0, 0));
		startExit.add(jButtonQuit,
		              new GridBagConstraints(0, 2, 0, 0, 0.0, 0.0,
		                                     GridBagConstraints.FIRST_LINE_START,
		                                     GridBagConstraints.NONE,
		                                     new Insets(190, 10, 0, 0), 0, 0));
		startExit.setBorder(padding2);
		container.add(startExit,
		              new GridBagConstraints(0, 3, 0, 0, 0.0, 0.0,
		                                     GridBagConstraints.FIRST_LINE_START,
		                                     GridBagConstraints.NONE,
		                                     new Insets(20, 10, 0, 0), 0, 0));
		jButtonQuit.setPreferredSize(new java.awt.Dimension(120, 21));
		super.setVisible(true);
		jButtonWorkDirectory.addActionListener(e -> chooseDirectory(jTextFieldWorkDirectory));
		jButtonRawData.addActionListener(e -> chooseDirectory(jTextFieldRawData));
		jButtonQuit.addActionListener(this::quit);
		jButtonStart.addActionListener(this::start);
		
		
		Border padding = BorderFactory.createEmptyBorder(10, 10, 10, 10);
		localPanel.setBorder(padding);
		localModeLayout.add(localPanel);
		container.add(localModeLayout, 1);
		
		// Omero mode layout
		omeroModeLayout.setSourceLabel("Raw:");
		omeroModeLayout.setSourceLabel2("Segmented:");
		omeroModeLayout.setSourceLabel3("");
		
		JTextPane jTextPane2 = new JTextPane();
		jTextPane2.setText("You must select 2 Datasets:" + eol +
		                   "1 containing raw nuclei images. " + eol +
		                   "2 containing segmented nuclei images." + eol +
		                   "Images must have same file name and the same calibration.");
		jTextPane2.setEditable(false);
		omeroModeLayout.add(jTextPane2, 0);
		
		super.setVisible(true);
	}
	
	
	public boolean isOmeroEnabled() {
		return useOMERO;
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
	
	
	public String getUsername() {
		return omeroModeLayout.getUsername();
	}
	
	
	public char[] getPassword() {
		return omeroModeLayout.getPassword();
	}
	
	
	public String getGroup() {
		return omeroModeLayout.getGroup();
	}
	
	
	public String getRawDatasetID() {
		return omeroModeLayout.getSourceID();
	}
	
	
	public String getSegDatasetID() {
		return omeroModeLayout.getSourceID2();
	}
	
	
	public String getHostname() {
		return omeroModeLayout.getHostname();
	}
	
	
	public String getPort() {
		return omeroModeLayout.getPort();
	}
	
	
	public void itemStateChanged(ItemEvent e) {
		Object source = e.getSource();
		if (source == omeroNoButton) {
			container.remove(1);
			container.add(localModeLayout, 1);
			useOMERO = false;
		} else if (source == omeroYesButton) {
			container.remove(1);
			container.add(omeroModeLayout, 1);
			useOMERO = true;
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
		}
		validate();
		repaint();
	}
	
	
	private void quit(ActionEvent actionEvent) {
		dispose();
	}
	
	
	private void start(ActionEvent actionEvent) {
		if (useOMERO) {
			try {
				dialogListener.onStart();
			} catch (AccessException | ServiceException | ExecutionException e) {
				IJ.error("Error starting the process", e.getMessage());
			}
			start = true;
			dispose();
		} else {
			if (jTextFieldWorkDirectory.getText().isEmpty() || jTextFieldRawData.getText().isEmpty()) {
				JOptionPane.showMessageDialog(null,
				                              "You did not choose a work directory or the raw data",
				                              "Error",
				                              JOptionPane.ERROR_MESSAGE);
			} else {
				start = true;
				try {
					dialogListener.onStart();
				} catch (AccessException | ServiceException | ExecutionException e) {
					IJ.error("Error starting the process", e.getMessage());
				}
				dispose();
			}
		}
	}
	
	
	private void chooseDirectory(JTextField textField) {
		String pref = textField.getName();
		if (pref == null || pref.isEmpty()) {
			pref = "nj.segparams." + Prefs.DIR_IMAGE;
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
