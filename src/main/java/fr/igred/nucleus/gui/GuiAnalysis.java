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
import fr.igred.nucleus.dialogs.IDialogListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;


/**
 * GUI for SIP program
 *
 * @author poulet axel
 */
public class GuiAnalysis extends JFrame implements ItemListener, IDialogListener {
	/**  */
	private static final long serialVersionUID = 7560518666194907298L;
	
	private static final Pattern COMMA = Pattern.compile(",", Pattern.LITERAL);
	
	private final transient IDialogListener dialogListener;
	
	private final JRadioButton   omeroYesButton     = new JRadioButton("Yes");
	private final JRadioButton   omeroNoButton      = new JRadioButton("No");
	private final JPanel         localModeLayout    = new JPanel();
	private final JPanel         omeroModeLayout    = new JPanel();
	private final JTextField     jTextFieldHostname = new JTextField();
	private final JTextField     jTextFieldPort     = new JTextField();
	private final JTextField     jTextFieldUsername = new JTextField();
	private final JPasswordField jPasswordField     = new JPasswordField();
	private final JTextField     jTextFieldGroup    = new JTextField();
	
	private final String[] dataTypes = {"Dataset", "Image"};
	
	private final JComboBox<String> jComboBoxDataType          = new JComboBox<>(dataTypes);
	private final JComboBox<String> jComboBoxDataTypeSegmented = new JComboBox<>(dataTypes);
	
	private final JTextField jTextFieldSourceID       = new JTextField();
	private final JTextField segmentedNucleiTextField = new JTextField();
	private final JTextField jTextFieldOutputProject  = new JTextField();
	
	/**  */
	private final JCheckBox  jCbIsGauss  = new JCheckBox("Apply Gaussian filter on raw images ?");
	/**  */
	private final JCheckBox  jCbIs2D     = new JCheckBox("Is it 2D images ?");
	/**  */
	private final JCheckBox  jCbIsFilter = new JCheckBox("Filter connected components?");
	/**  */
	private final JTextField jtfWorkDir  = new JTextField();
	private final JTextField jtfRawData  = new JTextField();
	private final JTextField jtfRawSeg   = new JTextField();
	
	private final JFormattedTextField jtfGX     = new JFormattedTextField(Number.class);
	private final JFormattedTextField jtfGY     = new JFormattedTextField(Number.class);
	private final JFormattedTextField jtfGZ     = new JFormattedTextField(Number.class);
	private final JFormattedTextField jtfMin    = new JFormattedTextField(Number.class);
	private final JFormattedTextField jtfMax    = new JFormattedTextField(Number.class);
	private final JFormattedTextField jtfFactor = new JFormattedTextField(Number.class);
	private final JFormattedTextField jtfNeigh  = new JFormattedTextField(Number.class);
	
	/**  */
	private final Container container;
	
	private boolean useOMERO;
	private boolean start;
	
	
	/** Default constructor */
	public GuiAnalysis() {
		this(null);
	}
	
	
	/**
	 * GUI Architecture
	 */
	public GuiAnalysis(IDialogListener dialogListener) {
		// Global parameter of the JFram and def of the gridBaglayout
		super.setTitle("NODeJ");
		super.setSize(550, 720);
		super.setLocationRelativeTo(null);
		super.setResizable(false);
		super.setLocationByPlatform(true);
		super.setBackground(Color.LIGHT_GRAY);
		super.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		this.dialogListener = dialogListener != null ? dialogListener : this;
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.1};
		gridBagLayout.rowHeights = new int[]{17, 500, 124, 7};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0, 0.1};
		gridBagLayout.columnWidths = new int[]{270, 120, 72, 20};
		JPanel parameters = new JPanel();
		parameters.setLayout(gridBagLayout);
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
		
		// First case of the grid bag layout
		// Local mode layout
		localModeLayout.setLayout(new BoxLayout(localModeLayout, BoxLayout.PAGE_AXIS));
		
		/**  */
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new GridBagLayout());
		
		JLabel label = new JLabel();
		label.setText("Input and Output directories: ");
		label.setFont(new java.awt.Font("arial", 1, 12));
		mainPanel.add(label,
		              new GridBagConstraints(0, 1, 0, 0, 0.0, 0.0,
		                                     GridBagConstraints.FIRST_LINE_START,
		                                     GridBagConstraints.NONE,
		                                     new Insets(10, 10, 0, 0), 0, 0));
		
		JButton jbInputDir = new JButton("Raw Nuclei");
		jbInputDir.setPreferredSize(new java.awt.Dimension(150, 21));
		jbInputDir.setFont(new java.awt.Font("arial", 2, 10));
		mainPanel.add(jbInputDir,
		              new GridBagConstraints(0, 1, 0, 0, 0.0, 0.0,
		                                     GridBagConstraints.FIRST_LINE_START,
		                                     GridBagConstraints.NONE,
		                                     new Insets(35, 20, 0, 0), 0, 0));
		
		jtfRawData.setPreferredSize(new java.awt.Dimension(280, 21));
		jtfRawData.setFont(new java.awt.Font("arial", 2, 10));
		mainPanel.add(jtfRawData,
		              new GridBagConstraints(0, 1, 0, 0, 0.0, 0.0,
		                                     GridBagConstraints.FIRST_LINE_START,
		                                     GridBagConstraints.NONE,
		                                     new Insets(35, 190, 0, 0), 0, 0));
		
		JButton jbInputSeg = new JButton("Seg. Nuclei");
		jbInputSeg.setPreferredSize(new java.awt.Dimension(150, 21));
		jbInputSeg.setFont(new java.awt.Font("arial", 2, 10));
		mainPanel.add(jbInputSeg,
		              new GridBagConstraints(0, 1, 0, 0, 0.0, 0.0,
		                                     GridBagConstraints.FIRST_LINE_START,
		                                     GridBagConstraints.NONE,
		                                     new Insets(65, 20, 0, 0), 0, 0));
		
		jtfRawSeg.setPreferredSize(new java.awt.Dimension(280, 21));
		jtfRawSeg.setFont(new java.awt.Font("arial", 2, 10));
		mainPanel.add(jtfRawSeg,
		              new GridBagConstraints(0, 1, 0, 0, 0.0, 0.0,
		                                     GridBagConstraints.FIRST_LINE_START,
		                                     GridBagConstraints.NONE,
		                                     new Insets(65, 190, 0, 0), 0, 0));
		
		JButton jbOutputDir = new JButton("Output directory");
		jbOutputDir.setPreferredSize(new java.awt.Dimension(150, 21));
		jbOutputDir.setFont(new java.awt.Font("arial", 2, 10));
		mainPanel.add(jbOutputDir,
		              new GridBagConstraints(0, 1, 0, 0, 0.0, 0.0,
		                                     GridBagConstraints.FIRST_LINE_START,
		                                     GridBagConstraints.NONE,
		                                     new Insets(95, 20, 0, 0), 0, 0));
		
		jtfWorkDir.setPreferredSize(new java.awt.Dimension(280, 21));
		jtfWorkDir.setFont(new java.awt.Font("arial", 2, 10));
		mainPanel.add(jtfWorkDir,
		              new GridBagConstraints(0, 1, 0, 0, 0.0, 0.0,
		                                     GridBagConstraints.FIRST_LINE_START,
		                                     GridBagConstraints.NONE,
		                                     new Insets(95, 190, 0, 0), 0, 0));
		
		// group of radio button to choose the input type file
		label = new JLabel();
		label.setFont(new java.awt.Font("arial", 1, 12));
		label.setText("Parameters:");
		parameters.add(label,
		               new GridBagConstraints(0, 1, 0, 0, 0.0, 0.0,
		                                      GridBagConstraints.FIRST_LINE_START,
		                                      GridBagConstraints.NONE,
		                                      new Insets(5, 10, 0, 0), 0, 0));
		
		jCbIs2D.setFont(new java.awt.Font("arial", 1, 12));
		parameters.add(jCbIs2D,
		               new GridBagConstraints(0, 1, 0, 0, 0.0, 0.0,
		                                      GridBagConstraints.FIRST_LINE_START,
		                                      GridBagConstraints.NONE,
		                                      new Insets(25, 20, 0, 0), 0, 0));
		
		label = new JLabel();
		label.setText("Size of the neighborhood:");
		label.setFont(new java.awt.Font("arial", 1, 12));
		parameters.add(label,
		               new GridBagConstraints(0, 1, 0, 0, 0.0, 0.0,
		                                      GridBagConstraints.FIRST_LINE_START,
		                                      GridBagConstraints.NONE,
		                                      new Insets(55, 20, 0, 0), 0, 0));
		
		jtfNeigh.setText("3");
		jtfNeigh.setPreferredSize(new java.awt.Dimension(60, 21));
		jtfNeigh.setFont(new java.awt.Font("arial", 1, 12));
		parameters.add(jtfNeigh,
		               new GridBagConstraints(0, 1, 0, 0, 0.0, 0.0,
		                                      GridBagConstraints.FIRST_LINE_START,
		                                      GridBagConstraints.NONE,
		                                      new Insets(52, 245, 0, 0), 0, 0));
		
		label = new JLabel();
		label.setText("Factor for the threshold value:");
		label.setFont(new java.awt.Font("arial", 1, 12));
		parameters.add(label,
		               new GridBagConstraints(0, 1, 0, 0, 0.0, 0.0,
		                                      GridBagConstraints.FIRST_LINE_START,
		                                      GridBagConstraints.NONE,
		                                      new Insets(80, 20, 0, 0), 0, 0));
		
		jtfFactor.setText("1.5");
		jtfFactor.setPreferredSize(new java.awt.Dimension(60, 21));
		jtfFactor.setFont(new java.awt.Font("arial", 1, 12));
		parameters.add(jtfFactor,
		               new GridBagConstraints(0, 1, 0, 0, 0.0, 0.0,
		                                      GridBagConstraints.FIRST_LINE_START,
		                                      GridBagConstraints.NONE,
		                                      new Insets(78, 245, 0, 0), 0, 0));
		
		jCbIsGauss.setFont(new java.awt.Font("arial", 1, 12));
		parameters.add(jCbIsGauss,
		               new GridBagConstraints(0, 1, 0, 0, 0.0, 0.0,
		                                      GridBagConstraints.FIRST_LINE_START,
		                                      GridBagConstraints.NONE,
		                                      new Insets(105, 20, 0, 0), 0, 0));
		
		label = new JLabel();
		label.setText("Gaussian Blur X sigma:");
		label.setFont(new java.awt.Font("arial", 1, 12));
		parameters.add(label,
		               new GridBagConstraints(0, 1, 0, 0, 0.0, 0.0,
		                                      GridBagConstraints.FIRST_LINE_START,
		                                      GridBagConstraints.NONE,
		                                      new Insets(135, 20, 0, 0), 0, 0));
		
		jtfGX.setText("1");
		jtfGX.setPreferredSize(new java.awt.Dimension(60, 21));
		jtfGX.setFont(new java.awt.Font("arial", 1, 12));
		parameters.add(jtfGX,
		               new GridBagConstraints(0, 1, 0, 0, 0.0, 0.0,
		                                      GridBagConstraints.FIRST_LINE_START,
		                                      GridBagConstraints.NONE,
		                                      new Insets(132, 195, 0, 0), 0, 0));
		
		label = new JLabel();
		label.setText("Gaussian Blur Y sigma:");
		label.setFont(new java.awt.Font("arial", 1, 12));
		parameters.add(label,
		               new GridBagConstraints(0, 1, 0, 0, 0.0, 0.0,
		                                      GridBagConstraints.FIRST_LINE_START,
		                                      GridBagConstraints.NONE,
		                                      new Insets(160, 20, 0, 0), 0, 0));
		
		jtfGY.setText("1");
		jtfGY.setPreferredSize(new java.awt.Dimension(60, 21));
		jtfGY.setFont(new java.awt.Font("arial", 1, 12));
		parameters.add(jtfGY,
		               new GridBagConstraints(0, 1, 0, 0, 0.0, 0.0,
		                                      GridBagConstraints.FIRST_LINE_START,
		                                      GridBagConstraints.NONE,
		                                      new Insets(158, 195, 0, 0), 0, 0));
		
		label = new JLabel();
		label.setText("Gaussian Blur Z sigma:");
		label.setFont(new java.awt.Font("arial", 1, 12));
		parameters.add(label,
		               new GridBagConstraints(0, 1, 0, 0, 0.0, 0.0,
		                                      GridBagConstraints.FIRST_LINE_START,
		                                      GridBagConstraints.NONE,
		                                      new Insets(185, 20, 0, 0), 0, 0));
		
		jtfGZ.setText("2");
		jtfGZ.setPreferredSize(new java.awt.Dimension(60, 21));
		jtfGZ.setFont(new java.awt.Font("arial", 1, 12));
		parameters.add(jtfGZ,
		               new GridBagConstraints(0, 1, 0, 0, 0.0, 0.0,
		                                      GridBagConstraints.FIRST_LINE_START,
		                                      GridBagConstraints.NONE,
		                                      new Insets(182, 195, 0, 0), 0, 0));
		
		label = new JLabel();
		label.setText("Connected component filtering parameters: ");
		label.setFont(new java.awt.Font("arial", 1, 12));
		parameters.add(label, new GridBagConstraints(0, 1, 0, 0, 0.0, 0.0,
		                                             GridBagConstraints.FIRST_LINE_START,
		                                             GridBagConstraints.NONE,
		                                             new Insets(215, 10, 0, 0), 0, 0));
		
		jCbIsFilter.setFont(new java.awt.Font("arial", 1, 12));
		parameters.add(jCbIsFilter,
		               new GridBagConstraints(0, 1, 0, 0, 0.0, 0.0,
		                                      GridBagConstraints.FIRST_LINE_START,
		                                      GridBagConstraints.NONE,
		                                      new Insets(240, 20, 0, 0), 0, 0));
		
		label = new JLabel();
		label.setText("Min volume:");
		label.setFont(new java.awt.Font("arial", 1, 12));
		parameters.add(label,
		               new GridBagConstraints(0, 1, 0, 0, 0.0, 0.0,
		                                      GridBagConstraints.FIRST_LINE_START,
		                                      GridBagConstraints.NONE,
		                                      new Insets(275, 20, 0, 0), 0, 0));
		
		jtfMin.setText("0.003");
		jtfMin.setPreferredSize(new java.awt.Dimension(60, 21));
		jtfMin.setFont(new java.awt.Font("arial", 1, 12));
		parameters.add(jtfMin,
		               new GridBagConstraints(0, 1, 0, 0, 0.0, 0.0,
		                                      GridBagConstraints.FIRST_LINE_START,
		                                      GridBagConstraints.NONE,
		                                      new Insets(272, 175, 0, 0), 0, 0));
		
		label = new JLabel();
		label.setText("Max volume:");
		label.setFont(new java.awt.Font("arial", 1, 12));
		parameters.add(label,
		               new GridBagConstraints(0, 1, 0, 0, 0.0, 0.0,
		                                      GridBagConstraints.FIRST_LINE_START,
		                                      GridBagConstraints.NONE,
		                                      new Insets(305, 20, 0, 0), 0, 0));
		
		jtfMax.setText("3");
		jtfMax.setPreferredSize(new java.awt.Dimension(60, 21));
		jtfMax.setFont(new java.awt.Font("arial", 1, 12));
		parameters.add(jtfMax,
		               new GridBagConstraints(0, 1, 0, 0, 0.0, 0.0,
		                                      GridBagConstraints.FIRST_LINE_START,
		                                      GridBagConstraints.NONE,
		                                      new Insets(302, 175, 0, 0), 0, 0));
		
		jtfMax.setEnabled(false);
		jtfMin.setEnabled(false);
		
		//////////////////////////////////////
		
		JButton jbStart = new JButton("Start");
		jbStart.setPreferredSize(new java.awt.Dimension(120, 21));
		JButton jbQuit = new JButton("Quit");
		jbQuit.setPreferredSize(new java.awt.Dimension(120, 21));
		
		parameters.add(jbStart, new GridBagConstraints(0, 1, 0, 0, 0.0, 0.0,
		                                               GridBagConstraints.FIRST_LINE_START,
		                                               GridBagConstraints.NONE,
		                                               new Insets(330, 140, 0, 0), 0, 0));
		parameters.add(jbQuit, new GridBagConstraints(1, 1, 0, 0, 0.0, 0.0,
		                                              GridBagConstraints.FIRST_LINE_START,
		                                              GridBagConstraints.NONE,
		                                              new Insets(330, 10, 0, 0), 0, 0));
		
		localModeLayout.add(mainPanel);
		container.add(localModeLayout, 1);
		container.add(parameters, 2);
		
		// Omero mode layout
		omeroModeLayout.setLayout(new BoxLayout(omeroModeLayout, BoxLayout.PAGE_AXIS));
		
		JPanel        omeroPanel  = new JPanel();
		GridBagLayout omeroLayout = new GridBagLayout();
		omeroLayout.columnWeights = new double[]{0.1, 0.1, 2};
		omeroPanel.setLayout(omeroLayout);
		GridBagConstraints c = new GridBagConstraints();
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
		JLabel jLabelSource = new JLabel("Raw Nuclei:");
		c.gridx = 0;
		c.gridwidth = 1;
		omeroPanel.add(jLabelSource, c);
		c.gridx = 1;
		omeroPanel.add(jComboBoxDataType, c);
		c.gridx = 2;
		omeroPanel.add(jTextFieldSourceID, c);
		jTextFieldSourceID.setMaximumSize(new Dimension(10000, 20));
		
		c.gridy = 6;
		JLabel jLabelToCrop = new JLabel("Segmented Nuclei:");
		c.gridx = 0;
		c.gridwidth = 1;
		omeroPanel.add(jLabelToCrop, c);
		c.gridx = 1;
		omeroPanel.add(jComboBoxDataTypeSegmented, c);
		c.gridx = 2;
		omeroPanel.add(segmentedNucleiTextField, c);
		segmentedNucleiTextField.setMaximumSize(new Dimension(20000, 20));
		
		c.gridy = 7;
		JLabel jLabelOutputProject = new JLabel("Output Project:");
		c.gridx = 0;
		c.gridwidth = 1;
		omeroPanel.add(jLabelOutputProject, c);
		c.gridx = 1;
		c.gridwidth = 2;
		omeroPanel.add(jTextFieldOutputProject, c);
		jTextFieldOutputProject.setMaximumSize(new Dimension(10000, 20));
		
		omeroPanel.setBorder(padding);
		omeroModeLayout.add(omeroPanel);
		
		//////////////////////////////////////////////////////////
		
		ActionListener analysis = new RBDeconvListener(this);
		jCbIs2D.addActionListener(analysis);
		jCbIsFilter.addActionListener(analysis);
		
		ActionListener wdListener = new Listener(jtfWorkDir, false);
		jbOutputDir.addActionListener(wdListener);
		ActionListener rawListener = new Listener(jtfRawData, false);
		jbInputDir.addActionListener(rawListener);
		ActionListener segListener = new Listener(jtfRawSeg, false);
		jbInputSeg.addActionListener(segListener);
		
		ActionListener quitListener = new QuitListener(this);
		jbQuit.addActionListener(quitListener);
		ActionListener startListener = new StartListener(this);
		jbStart.addActionListener(startListener);
		super.setVisible(true);
		
		// DEFAULT VALUES FOR TESTING :
		jTextFieldHostname.setText("omero.igred.fr");
		jTextFieldPort.setText("4064");
		
		jTextFieldUsername.setText("");
		jPasswordField.setText("");
		jTextFieldGroup.setText("203");
		
		jTextFieldSourceID.setText("31510");
		segmentedNucleiTextField.setText("31511");
		
		jTextFieldOutputProject.setText("14855");
	}
	
	
	/**
	 * java.trax.gui main2DAnalysis
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		GuiAnalysis gui = new GuiAnalysis();
		gui.setLocationRelativeTo(null);
	}
	
	
	/**
	 * /** getter of the workdir path
	 *
	 * @return String workdir path
	 */
	public String getOutputDir() {
		return jtfWorkDir.getText();
	}
	
	
	/**
	 * @return String input path
	 */
	public String getInputRaw() {
		return jtfRawData.getText();
	}
	
	
	public String getInputSeg() {
		return jtfRawSeg.getText();
	}
	
	
	public double getMin() {
		String x = jtfMin.getText();
		return Double.parseDouble(COMMA.matcher(x).replaceAll("."));
	}
	
	
	public double getFactor() {
		String x = jtfFactor.getText();
		return Double.parseDouble(COMMA.matcher(x).replaceAll("."));
	}
	
	
	public int getNeigh() {
		String x = jtfNeigh.getText();
		return Integer.parseInt(COMMA.matcher(x).replaceAll("."));
	}
	
	
	public double getMax() {
		String x = jtfMax.getText();
		return Double.parseDouble(COMMA.matcher(x).replaceAll("."));
	}
	
	
	public double getGaussianX() {
		String x = jtfGX.getText();
		return Double.parseDouble(COMMA.matcher(x).replaceAll("."));
	}
	
	
	public double getGaussianY() {
		String x = jtfGY.getText();
		return Double.parseDouble(COMMA.matcher(x).replaceAll("."));
	}
	
	
	public double getGaussianZ() {
		String x = jtfGZ.getText();
		return Double.parseDouble(COMMA.matcher(x).replaceAll("."));
	}
	
	
	public boolean isStart() {
		return start;
	}
	
	
	public boolean is2D() {
		return jCbIs2D.isSelected();
	}
	
	
	public boolean isGaussian() {
		return jCbIsGauss.isSelected();
	}
	
	
	public boolean isFilter() {
		return jCbIsFilter.isSelected();
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
	
	
	public String getSegmentedNucleiID() {
		return segmentedNucleiTextField.getText();
	}
	
	
	public String getDataType() {
		return (String) jComboBoxDataType.getSelectedItem();
	}
	
	
	public String getDataTypeSegmented() {
		return (String) jComboBoxDataTypeSegmented.getSelectedItem();
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
		}
		
		validate();
		repaint();
		
	}
	
	
	@Override
	public void onStart() {
		// DO NOTHING
	}
	
	
	/* Listener classes to interact with the several element of the window */
	
	/**
	 * Quit button listener
	 *
	 * @author axel poulet
	 */
	private static class QuitListener implements ActionListener {
		/**  */
		private final GuiAnalysis gui;
		
		
		/**
		 * @param gui
		 */
		QuitListener(GuiAnalysis gui) {
			this.gui = gui;
		}
		
		
		/**
		 * dipose the java.trax.gui and quit the program
		 */
		public void actionPerformed(ActionEvent actionEvent) {
			gui.dispose();
			//System.exit(0);
		}
		
	}
	
	/**
	 * Radio button listener, manage teh access of the different button box etc on function of the parameters choose
	 *
	 * @author axel poulet
	 */
	private class RBDeconvListener implements ActionListener {
		/**  */
		private final GuiAnalysis gui;
		
		
		/**
		 * @param gui
		 */
		RBDeconvListener(GuiAnalysis gui) {
			this.gui = gui;
		}
		
		
		/**
		 * Manages the access of the different java.trax.gui element depending on the chosen parameter
		 */
		public void actionPerformed(ActionEvent actionEvent) {
			if (gui.is2D()) {
				jtfGY.setEnabled(false);
				jtfGZ.setEnabled(false);
			} else if (!gui.is2D()) {
				jtfGY.setEnabled(true);
				jtfGZ.setEnabled(true);
			}
			
			if (gui.isFilter()) {
				jtfMax.setEnabled(true);
				jtfMin.setEnabled(true);
			} else if (!gui.isFilter()) {
				jtfMax.setEnabled(false);
				jtfMin.setEnabled(false);
			}
		}
		
	}
	
	/**
	 * @author axel poulet Listerner for the start button
	 */
	private class StartListener implements ActionListener {
		/**  */
		private final GuiAnalysis gui;
		
		
		/**
		 * @param gui
		 */
		StartListener(GuiAnalysis gui) {
			this.gui = gui;
		}
		
		
		/**
		 * Test all the box, condition etc before to allow the program to run and dispose the java.trax.gui
		 */
		public void actionPerformed(ActionEvent actionEvent) {
			if (useOMERO) {
				start = true;
				gui.dispose();
				
			} else {
				if (jtfWorkDir.getText().isEmpty() ||
				    jtfRawData.getText().isEmpty() ||
				    jtfRawSeg.getText().isEmpty()) {
					JOptionPane.showMessageDialog(null, "You did not choose an input/output directory",
					                              "Error", JOptionPane.ERROR_MESSAGE);
				} else {
					start = true;
					gui.dispose();
				}
			}
			try {
				dialogListener.onStart();
			} catch (AccessException | ServiceException | ExecutionException e) {
				throw new RuntimeException(e);
			}
			
		}
		
	}
	
	/**
	 *
	 */
	private class Listener implements ActionListener {
		/**  */
		private final JTextField jtf;
		/**  */
		private final boolean    file;
		
		
		/**
		 * @param jtf
		 * @param file
		 */
		Listener(JTextField jtf, boolean file) {
			this.jtf = jtf;
			this.file = file;
		}
		
		
		/**  */
		public void actionPerformed(ActionEvent actionEvent) {
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			JFileChooser jFileChooser = new JFileChooser();
			jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			if (file) {
				jFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			}
			int returnValue = jFileChooser.showOpenDialog(getParent());
			if (returnValue == JFileChooser.APPROVE_OPTION) {
				String text = jFileChooser.getSelectedFile().getAbsolutePath();
				jtf.setText(text);
			}
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
		
	}
	
}