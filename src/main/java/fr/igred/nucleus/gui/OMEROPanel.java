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

import fr.igred.nucleus.Version;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServiceException;
import ij.IJ;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.concurrent.ExecutionException;


public class OMEROPanel extends JFrame implements ActionListener, ItemListener {
	private static final long serialVersionUID = 1539112593885790535L;
	
	private static final String INPUT_CHOOSER  = "inputChooser";
	private static final String OUTPUT_CHOOSER = "outputChooser";
	private static final String CONFIG_CHOOSER = "configChooser";
	
	private final transient IDialogListener dialogListener;
	
	private final Container    container;
	private final JFileChooser fc                 = new JFileChooser();
	private final JRadioButton omeroYesButton     = new JRadioButton("Yes");
	private final JRadioButton omeroNoButton      = new JRadioButton("No");
	private final JTextField   jInputFileChooser  = new JTextField();
	private final JTextField   jOutputFileChooser = new JTextField();
	private final JPanel       configFilePanel    = new JPanel();
	private final JLabel       defConf            = new JLabel("Default configuration");
	
	private final AutocropConfigDialog autocropConfigFileDialog;
	
	private final JRadioButton   rdoDefault         = new JRadioButton("Default");
	private final JRadioButton   rdoAddConfigFile   = new JRadioButton("From file");
	private final JTextField     jConfigFileChooser = new JTextField();
	private final JRadioButton   rdoAddConfigDialog = new JRadioButton("New");
	private final JButton        jButtonConfig      = new JButton("Config");
	private final JPanel         localModeLayout    = new JPanel();
	private final JPanel         omeroModeLayout    = new JPanel();
	private final JTextField     jTextFieldHostname = new JTextField();
	private final JTextField     jTextFieldPort     = new JTextField();
	private final JTextField     jTextFieldUsername = new JTextField();
	private final JPasswordField jPasswordField     = new JPasswordField();
	private final JTextField     jTextFieldGroup    = new JTextField();
	
	private final String[] dataTypes = {"Project", "Dataset", "Tag", "Image"};
	
	private final String[] thresholdType = {"Otsu",
	                                        "RenyiEntropy",
	                                        "Huang",
	                                        "Intermodes",
	                                        "IsoData",
	                                        "Li",
	                                        "MaxEntropy",
	                                        "Mean",
	                                        "MinError",
	                                        "Minimum",
	                                        "Moments",
	                                        "Percentile",
	                                        "Shanbhag",
	                                        "Triangle",
	                                        "Yen"};
	
	private final JComboBox<String> jComboBoxDataType       = new JComboBox<>(dataTypes);
	private final JComboBox<String> jComboBoxThresholdType  = new JComboBox<>(thresholdType);
	private final JTextField        jTextFieldSourceID      = new JTextField();
	private final JTextField        jTextFieldOutputProject = new JTextField();
	private final JButton           confButton              = new JButton("...");
	
	private final JSpinner jSpinnerThreads;
	
	private boolean    useOMERO;
	private ConfigMode configMode;
	
	
	/** Architecture of the graphical windows */
	public OMEROPanel(IDialogListener dialogListener) {
		this.dialogListener = dialogListener;
		
		JButton jButtonStart = new JButton("Start");
		JButton jButtonQuit  = new JButton("Quit");
		super.setTitle("Autocrop - NucleusJ - v" + Version.get());
		super.setMinimumSize(new Dimension(400, 500));
		super.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		super.setLocationRelativeTo(null);
		autocropConfigFileDialog = new AutocropConfigDialog();
		autocropConfigFileDialog.setVisible(false);
		
		container = super.getContentPane();
		LayoutManager mainBoxLayout = new BoxLayout(super.getContentPane(), BoxLayout.PAGE_AXIS);
		container.setLayout(mainBoxLayout);
		
		Border padding = BorderFactory.createEmptyBorder(10, 10, 10, 10);
		
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
		
		// Local mode layout
		localModeLayout.setLayout(new BoxLayout(localModeLayout, BoxLayout.PAGE_AXIS));
		
		JPanel        localPanel  = new JPanel();
		GridBagLayout localLayout = new GridBagLayout();
		localLayout.columnWeights = new double[]{1, 5, 0.5};
		localPanel.setLayout(localLayout);
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		
		JLabel jLabelInput = new JLabel("Input directory:");
		localPanel.add(jLabelInput, c);
		c.gridx = 1;
		c.insets = new Insets(0, 0, 0, 20);
		localPanel.add(jInputFileChooser, c);
		jInputFileChooser.setMaximumSize(new Dimension(10000, 20));
		JButton sourceButton = new JButton("...");
		sourceButton.addActionListener(this);
		sourceButton.setName(INPUT_CHOOSER);
		c.insets = new Insets(0, 0, 0, 0);
		c.gridx = 2;
		localPanel.add(sourceButton, c);
		
		JLabel jLabelOutput = new JLabel("Output directory:");
		c.gridx = 0;
		c.gridy = 1;
		localPanel.add(jLabelOutput, c);
		c.gridx = 1;
		c.insets = new Insets(0, 0, 0, 20);
		localPanel.add(jOutputFileChooser, c);
		jOutputFileChooser.setMaximumSize(new Dimension(10000, 20));
		JButton destButton = new JButton("...");
		destButton.addActionListener(this);
		destButton.setName(OUTPUT_CHOOSER);
		c.insets = new Insets(10, 0, 0, 0);
		c.gridx = 2;
		localPanel.add(destButton, c);
		
		localPanel.setBorder(padding);
		localModeLayout.add(localPanel);
		container.add(localModeLayout, 1);
		
		// Omero mode layout
		omeroModeLayout.setLayout(new BoxLayout(omeroModeLayout, BoxLayout.PAGE_AXIS));
		
		JPanel        omeroPanel  = new JPanel();
		GridBagLayout omeroLayout = new GridBagLayout();
		omeroLayout.columnWeights = new double[]{0.1, 0.1, 2};
		omeroPanel.setLayout(omeroLayout);
		c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(5, 0, 5, 20);
		
		c.gridy = 0;
		JLabel jLabelHostname = new JLabel("Hostname:");
		c.gridx = 0;
		c.gridwidth = 1;
		omeroPanel.add(jLabelHostname, c);
		c.gridx = 1;
		c.gridwidth = 2;
		omeroPanel.add(jTextFieldHostname, c);
		jTextFieldHostname.setMaximumSize(new Dimension(10000, 20));
		
		c.gridy = 1;
		JLabel jLabelPort = new JLabel("Port:");
		c.gridx = 0;
		c.gridwidth = 1;
		omeroPanel.add(jLabelPort, c);
		c.gridx = 1;
		c.gridwidth = 2;
		omeroPanel.add(jTextFieldPort, c);
		jTextFieldPort.setMaximumSize(new Dimension(10000, 20));
		
		c.gridy = 2;
		JLabel jLabelUsername = new JLabel("Username:");
		c.gridx = 0;
		c.gridwidth = 1;
		omeroPanel.add(jLabelUsername, c);
		c.gridx = 1;
		c.gridwidth = 2;
		omeroPanel.add(jTextFieldUsername, c);
		jTextFieldUsername.setMaximumSize(new Dimension(10000, 20));
		
		c.gridy = 3;
		JLabel jLabelPassword = new JLabel("Password:");
		c.gridx = 0;
		c.gridwidth = 1;
		omeroPanel.add(jLabelPassword, c);
		c.gridx = 1;
		c.gridwidth = 2;
		omeroPanel.add(jPasswordField, c);
		jPasswordField.setMaximumSize(new Dimension(10000, 20));
		
		c.gridy = 4;
		JLabel jLabelGroup = new JLabel("Group ID:");
		c.gridx = 0;
		c.gridwidth = 1;
		omeroPanel.add(jLabelGroup, c);
		c.gridx = 1;
		c.gridwidth = 2;
		omeroPanel.add(jTextFieldGroup, c);
		jTextFieldGroup.setMaximumSize(new Dimension(10000, 20));
		
		c.gridy = 5;
		JLabel jLabelSource = new JLabel("Source:");
		c.gridx = 0;
		c.gridwidth = 1;
		omeroPanel.add(jLabelSource, c);
		c.gridx = 1;
		omeroPanel.add(jComboBoxDataType, c);
		c.gridx = 2;
		omeroPanel.add(jTextFieldSourceID, c);
		jTextFieldSourceID.setMaximumSize(new Dimension(10000, 20));
		
		c.gridy = 6;
		JLabel jLabelOutputProject = new JLabel("Output project:");
		c.gridx = 0;
		c.gridwidth = 1;
		omeroPanel.add(jLabelOutputProject, c);
		c.gridx = 1;
		c.gridwidth = 2;
		omeroPanel.add(jTextFieldOutputProject, c);
		jTextFieldOutputProject.setMaximumSize(new Dimension(10000, 20));
		
		omeroPanel.setBorder(padding);
		omeroModeLayout.add(omeroPanel);
		
		// Threshold preferences
		JPanel thresholdPanel = new JPanel();
		thresholdPanel.setLayout(new BoxLayout(thresholdPanel, BoxLayout.LINE_AXIS));
		c.gridy = 7;
		JLabel jLabelThresholdType = new JLabel("Threshold Method:");
		c.gridx = 0;
		c.gridwidth = 1;
		thresholdPanel.add(jLabelThresholdType, c);
		c.gridx = 1;
		thresholdPanel.add(jComboBoxThresholdType, c);
		thresholdPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 100));
		container.add(thresholdPanel, 2);
		
		// Config panel
		JPanel configPanel = new JPanel();
		configPanel.setLayout(new BoxLayout(configPanel, BoxLayout.LINE_AXIS));
		JLabel jLabelConfig = new JLabel("Config file (optional):");
		configPanel.add(jLabelConfig);
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(rdoDefault);
		
		rdoDefault.setSelected(true);
		rdoDefault.addItemListener(this);
		configPanel.add(rdoDefault);
		
		buttonGroup.add(rdoAddConfigDialog);
		rdoAddConfigDialog.addItemListener(this);
		configPanel.add(rdoAddConfigDialog);
		
		buttonGroup.add(rdoAddConfigFile);
		rdoAddConfigFile.addItemListener(this);
		configPanel.add(rdoAddConfigFile);
		configPanel.setBorder(padding);
		container.add(configPanel, 3);
		// Initialize config to default
		container.add(defConf, 4);
		defConf.setBorder(padding);
		configMode = ConfigMode.DEFAULT;
		
		// Thread preferences
		JPanel threadPanel = new JPanel();
		threadPanel.setLayout(new BoxLayout(threadPanel, BoxLayout.LINE_AXIS));
		JLabel jLabelThreads = new JLabel("Number of used threads:");
		threadPanel.add(jLabelThreads);
		int          maxThreads = Runtime.getRuntime().availableProcessors();
		SpinnerModel model      = new SpinnerNumberModel(Math.min(maxThreads, 1), 1, maxThreads, 1);
		jSpinnerThreads = new JSpinner(model);
		threadPanel.add(jSpinnerThreads);
		threadPanel.setBorder(BorderFactory.createEmptyBorder(10, 100, 10, 100));
		container.add(threadPanel, 5);
		
		// Start/Quit buttons
		JPanel startQuitPanel = new JPanel();
		startQuitPanel.setLayout(new BoxLayout(startQuitPanel, BoxLayout.LINE_AXIS));
		startQuitPanel.add(jButtonStart);
		startQuitPanel.add(jButtonQuit);
		startQuitPanel.setBorder(padding);
		container.add(startQuitPanel, 6);
		
		jButtonQuit.addActionListener(e -> quit());
		jButtonStart.addActionListener(e -> start());
		jButtonConfig.addActionListener(e -> autocropConfigFileDialog.setVisible(true));
		
		super.setVisible(true);
		
		// DEFAULT VALUES FOR TESTING :
		jTextFieldHostname.setText("omero.gred-clermont.fr");
		jTextFieldPort.setText("4064");
		
		jTextFieldUsername.setText("");
		jPasswordField.setText("");
		jTextFieldGroup.setText("553");
		
		jComboBoxDataType.setSelectedIndex(3);
		jTextFieldSourceID.setText("");
		jTextFieldOutputProject.setText("");
	}
	
	
	public String getInput() {
		return jInputFileChooser.getText();
	}
	
	
	public String getOutput() {
		return jOutputFileChooser.getText();
	}
	
	
	public boolean isOmeroEnabled() {
		return useOMERO;
	}
	
	
	public String getHostname() {
		return jTextFieldHostname.getText();
	}
	
	
	public String getPort() {
		return jTextFieldPort.getText();
	}
	
	
	public String getSourceID() {
		return jTextFieldSourceID.getText();
	}
	
	
	public String getDataType() {
		return (String) jComboBoxDataType.getSelectedItem();
	}
	
	
	public String getTypeThresholding() {
		return (String) jComboBoxThresholdType.getSelectedItem();
	}
	
	
	public String getUsername() {
		return jTextFieldUsername.getText();
	}
	
	
	public char[] getPassword() {
		return jPasswordField.getPassword();
	}
	
	
	public String getGroup() {
		return jTextFieldGroup.getText();
	}
	
	
	public String getOutputProject() {
		return jTextFieldOutputProject.getText();
	}
	
	
	public String getConfig() {
		return jConfigFileChooser.getText();
	}
	
	
	public ConfigMode getConfigMode() {
		return configMode;
	}
	
	
	public AutocropConfigDialog getAutocropConfigFileDialog() {
		return autocropConfigFileDialog;
	}
	
	
	public int getThreads() {
		return (int) jSpinnerThreads.getValue();
	}
	
	
	public void actionPerformed(ActionEvent e) {
		switch (((JButton) e.getSource()).getName()) {
			case INPUT_CHOOSER:
				fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				break;
			case OUTPUT_CHOOSER:
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				break;
			case CONFIG_CHOOSER:
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				break;
		}
		fc.setAcceptAllFileFilterUsed(false);
		
		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			switch (((JButton) e.getSource()).getName()) {
				case INPUT_CHOOSER:
					File selectedInput = fc.getSelectedFile();
					jInputFileChooser.setText(selectedInput.getPath());
					break;
				case OUTPUT_CHOOSER:
					File selectedOutput = fc.getSelectedFile();
					jOutputFileChooser.setText(selectedOutput.getPath());
					break;
				case CONFIG_CHOOSER:
					File selectedConfig = fc.getSelectedFile();
					jConfigFileChooser.setText(selectedConfig.getPath());
					break;
			}
		}
		fc.setSelectedFile(null);
	}
	
	
	@Override
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
		} else {
			container.remove(3);
			if (autocropConfigFileDialog.isVisible()) {
				autocropConfigFileDialog.setVisible(false);
			}
			
			Border padding = BorderFactory.createEmptyBorder(10, 10, 10, 10);
			if (source == rdoDefault) {
				container.add(defConf, 3);
				defConf.setBorder(padding);
				configMode = ConfigMode.DEFAULT;
			} else if (source == rdoAddConfigDialog) {
				container.add(jButtonConfig, 3);
				jButtonConfig.setBorder(padding);
				configMode = ConfigMode.INPUT;
			} else if (source == rdoAddConfigFile) {
				configFilePanel.setLayout(new BoxLayout(configFilePanel, BoxLayout.LINE_AXIS));
				configFilePanel.add(jConfigFileChooser);
				jConfigFileChooser.setMaximumSize(new Dimension(10000, 20));
				confButton.addActionListener(this);
				confButton.setName(CONFIG_CHOOSER);
				configFilePanel.add(confButton);
				container.add(configFilePanel, 3);
				configFilePanel.setBorder(padding);
				configMode = ConfigMode.FILE;
			}
		}
		
		validate();
		repaint();
	}
	
	
	/** Starts the autocrop process */
	private void start() {
		dispose();
		autocropConfigFileDialog.dispose();
		try {
			dialogListener.onStart();
		} catch (AccessException | ServiceException | ExecutionException e) {
			IJ.error("Error starting the process", e.getMessage());
		}
	}
	
	
	/** Closes the dialogs */
	private void quit() {
		dispose();
		autocropConfigFileDialog.dispose();
	}
	
	
	/** Enum to define the configuration mode */
	public enum ConfigMode {
		DEFAULT,
		FILE,
		INPUT
	}
	
	
}
