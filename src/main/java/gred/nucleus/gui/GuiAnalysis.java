package gred.nucleus.gui;


import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServiceException;
import gred.nucleus.dialogs.IDialogListener;

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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.concurrent.ExecutionException;


/**
 * GUI for SIP program
 *
 * @author poulet axel
 */
public class GuiAnalysis extends JFrame implements ItemListener, IDialogListener {
	/**  */
	private static final long serialVersionUID = 7560518666194907298L;
	
	private final IDialogListener dialogListener;
	
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
	private final JButton    jbOutputDir = new JButton("Output directory");
	/**  */
	private final JButton    jbInputDir  = new JButton("Raw Nuclei");
	/**  */
	private final JButton    jbInputSeg  = new JButton("Seg. Nuclei");
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
	private final JButton    jbStart     = new JButton("Start");
	private final JButton    jbQuit      = new JButton("Quit");
	
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
	
	
	/**
	 * GUI Architecture
	 */
	public GuiAnalysis(IDialogListener dialogListener) {
		this.dialogListener = dialogListener;
		
		// Global parameter of the JFram and def of the gridBaglayout
		super.setTitle("NODeJ");
		super.setSize(550, 720);
		super.setLocationRelativeTo(null);
		super.setResizable(false);
		super.setLocationByPlatform(true);
		super.setBackground(Color.LIGHT_GRAY);
		super.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		//this._container = getContentPane();
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.1};
		gridBagLayout.rowHeights = new int[]{17, 500, 124, 7};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0, 0.1};
		gridBagLayout.columnWidths = new int[]{270, 120, 72, 20};
		JPanel parameters = new JPanel();
		parameters.setLayout(gridBagLayout);
		container = super.getContentPane();
		BoxLayout mainBoxLayout = new BoxLayout(super.getContentPane(), BoxLayout.Y_AXIS);
		
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
		radioOmeroPanel.setLayout(new BoxLayout(radioOmeroPanel, BoxLayout.X_AXIS));
		JLabel jLabelOmero = new JLabel("Select from omero :");
		radioOmeroPanel.add(jLabelOmero);
		radioOmeroPanel.add(omeroYesButton);
		radioOmeroPanel.add(omeroNoButton);
		radioOmeroPanel.setBorder(BorderFactory.createLineBorder(Color.black, 1));
		container.add(radioOmeroPanel, 0);
		
		// First case of the grid bag layout
		// Local mode layout
		localModeLayout.setLayout(new BoxLayout(localModeLayout, BoxLayout.Y_AXIS));
		
		/**  */
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new GridBagLayout());
		
		JLabel label = new JLabel();
		label.setText("Input and Output directories: ");
		label.setFont(new java.awt.Font("arial", 1, 12));
		mainPanel.add(label,
		              new GridBagConstraints(0, 1, 0, 0, 0.0, 0.0,
		                                     GridBagConstraints.NORTHWEST,
		                                     GridBagConstraints.NONE,
		                                     new Insets(10, 10, 0, 0), 0, 0));
		
		this.jbInputDir.setPreferredSize(new java.awt.Dimension(150, 21));
		this.jbInputDir.setFont(new java.awt.Font("arial", 2, 10));
		mainPanel.add(this.jbInputDir,
		              new GridBagConstraints(0, 1, 0, 0, 0.0, 0.0,
		                                     GridBagConstraints.NORTHWEST,
		                                     GridBagConstraints.NONE,
		                                     new Insets(35, 20, 0, 0), 0, 0));
		
		this.jtfRawData.setPreferredSize(new java.awt.Dimension(280, 21));
		this.jtfRawData.setFont(new java.awt.Font("arial", 2, 10));
		mainPanel.add(this.jtfRawData,
		              new GridBagConstraints(0, 1, 0, 0, 0.0, 0.0,
		                                     GridBagConstraints.NORTHWEST,
		                                     GridBagConstraints.NONE,
		                                     new Insets(35, 190, 0, 0), 0, 0));
		
		this.jbInputSeg.setPreferredSize(new java.awt.Dimension(150, 21));
		this.jbInputSeg.setFont(new java.awt.Font("arial", 2, 10));
		mainPanel.add(this.jbInputSeg,
		              new GridBagConstraints(0, 1, 0, 0, 0.0, 0.0,
		                                     GridBagConstraints.NORTHWEST,
		                                     GridBagConstraints.NONE,
		                                     new Insets(65, 20, 0, 0), 0, 0));
		
		this.jtfRawSeg.setPreferredSize(new java.awt.Dimension(280, 21));
		this.jtfRawSeg.setFont(new java.awt.Font("arial", 2, 10));
		mainPanel.add(this.jtfRawSeg,
		              new GridBagConstraints(0, 1, 0, 0, 0.0, 0.0,
		                                     GridBagConstraints.NORTHWEST,
		                                     GridBagConstraints.NONE,
		                                     new Insets(65, 190, 0, 0), 0, 0));
		
		this.jbOutputDir.setPreferredSize(new java.awt.Dimension(150, 21));
		this.jbOutputDir.setFont(new java.awt.Font("arial", 2, 10));
		mainPanel.add(this.jbOutputDir,
		              new GridBagConstraints(0, 1, 0, 0, 0.0, 0.0,
		                                     GridBagConstraints.NORTHWEST,
		                                     GridBagConstraints.NONE,
		                                     new Insets(95, 20, 0, 0), 0, 0));
		
		this.jtfWorkDir.setPreferredSize(new java.awt.Dimension(280, 21));
		this.jtfWorkDir.setFont(new java.awt.Font("arial", 2, 10));
		mainPanel.add(this.jtfWorkDir,
		              new GridBagConstraints(0, 1, 0, 0, 0.0, 0.0,
		                                     GridBagConstraints.NORTHWEST,
		                                     GridBagConstraints.NONE,
		                                     new Insets(95, 190, 0, 0), 0, 0));
		
		// group of radio button to choose the input type file
		label = new JLabel();
		label.setFont(new java.awt.Font("arial", 1, 12));
		label.setText("Parameters:");
		parameters.add(label,
		               new GridBagConstraints(0, 1, 0, 0, 0.0, 0.0,
		                                      GridBagConstraints.NORTHWEST,
		                                      GridBagConstraints.NONE,
		                                      new Insets(5, 10, 0, 0), 0, 0));
		
		this.jCbIs2D.setFont(new java.awt.Font("arial", 1, 12));
		parameters.add(this.jCbIs2D,
		               new GridBagConstraints(0, 1, 0, 0, 0.0, 0.0,
		                                      GridBagConstraints.NORTHWEST,
		                                      GridBagConstraints.NONE,
		                                      new Insets(25, 20, 0, 0), 0, 0));
		
		label = new JLabel();
		label.setText("Size of the neighborhood:");
		label.setFont(new java.awt.Font("arial", 1, 12));
		parameters.add(label,
		               new GridBagConstraints(0, 1, 0, 0, 0.0, 0.0,
		                                      GridBagConstraints.NORTHWEST,
		                                      GridBagConstraints.NONE,
		                                      new Insets(55, 20, 0, 0), 0, 0));
		
		this.jtfNeigh.setText("3");
		this.jtfNeigh.setPreferredSize(new java.awt.Dimension(60, 21));
		this.jtfNeigh.setFont(new java.awt.Font("arial", 1, 12));
		parameters.add(this.jtfNeigh,
		               new GridBagConstraints(0, 1, 0, 0, 0.0, 0.0,
		                                      GridBagConstraints.NORTHWEST,
		                                      GridBagConstraints.NONE,
		                                      new Insets(52, 245, 0, 0), 0, 0));
		
		label = new JLabel();
		label.setText("Factor for the threshold value:");
		label.setFont(new java.awt.Font("arial", 1, 12));
		parameters.add(label,
		               new GridBagConstraints(0, 1, 0, 0, 0.0, 0.0,
		                                      GridBagConstraints.NORTHWEST,
		                                      GridBagConstraints.NONE,
		                                      new Insets(80, 20, 0, 0), 0, 0));
		
		this.jtfFactor.setText("1.5");
		this.jtfFactor.setPreferredSize(new java.awt.Dimension(60, 21));
		this.jtfFactor.setFont(new java.awt.Font("arial", 1, 12));
		parameters.add(this.jtfFactor,
		               new GridBagConstraints(0, 1, 0, 0, 0.0, 0.0,
		                                      GridBagConstraints.NORTHWEST,
		                                      GridBagConstraints.NONE,
		                                      new Insets(78, 245, 0, 0), 0, 0));
		
		this.jCbIsGauss.setFont(new java.awt.Font("arial", 1, 12));
		parameters.add(this.jCbIsGauss,
		               new GridBagConstraints(0, 1, 0, 0, 0.0, 0.0,
		                                      GridBagConstraints.NORTHWEST,
		                                      GridBagConstraints.NONE,
		                                      new Insets(105, 20, 0, 0), 0, 0));
		
		label = new JLabel();
		label.setText("Gaussian Blur X sigma:");
		label.setFont(new java.awt.Font("arial", 1, 12));
		parameters.add(label,
		               new GridBagConstraints(0, 1, 0, 0, 0.0, 0.0,
		                                      GridBagConstraints.NORTHWEST,
		                                      GridBagConstraints.NONE,
		                                      new Insets(135, 20, 0, 0), 0, 0));
		
		this.jtfGX.setText("1");
		this.jtfGX.setPreferredSize(new java.awt.Dimension(60, 21));
		this.jtfGX.setFont(new java.awt.Font("arial", 1, 12));
		parameters.add(this.jtfGX,
		               new GridBagConstraints(0, 1, 0, 0, 0.0, 0.0,
		                                      GridBagConstraints.NORTHWEST,
		                                      GridBagConstraints.NONE,
		                                      new Insets(132, 195, 0, 0), 0, 0));
		
		label = new JLabel();
		label.setText("Gaussian Blur Y sigma:");
		label.setFont(new java.awt.Font("arial", 1, 12));
		parameters.add(label,
		               new GridBagConstraints(0, 1, 0, 0, 0.0, 0.0,
		                                      GridBagConstraints.NORTHWEST,
		                                      GridBagConstraints.NONE,
		                                      new Insets(160, 20, 0, 0), 0, 0));
		
		this.jtfGY.setText("1");
		this.jtfGY.setPreferredSize(new java.awt.Dimension(60, 21));
		this.jtfGY.setFont(new java.awt.Font("arial", 1, 12));
		parameters.add(this.jtfGY,
		               new GridBagConstraints(0, 1, 0, 0, 0.0, 0.0,
		                                      GridBagConstraints.NORTHWEST,
		                                      GridBagConstraints.NONE,
		                                      new Insets(158, 195, 0, 0), 0, 0));
		
		label = new JLabel();
		label.setText("Gaussian Blur Z sigma:");
		label.setFont(new java.awt.Font("arial", 1, 12));
		parameters.add(label,
		               new GridBagConstraints(0, 1, 0, 0, 0.0, 0.0,
		                                      GridBagConstraints.NORTHWEST,
		                                      GridBagConstraints.NONE,
		                                      new Insets(185, 20, 0, 0), 0, 0));
		
		this.jtfGZ.setText("2");
		this.jtfGZ.setPreferredSize(new java.awt.Dimension(60, 21));
		this.jtfGZ.setFont(new java.awt.Font("arial", 1, 12));
		parameters.add(this.jtfGZ,
		               new GridBagConstraints(0, 1, 0, 0, 0.0, 0.0,
		                                      GridBagConstraints.NORTHWEST,
		                                      GridBagConstraints.NONE,
		                                      new Insets(182, 195, 0, 0), 0, 0));
		
		label = new JLabel();
		label.setText("Connected component filtering parameters: ");
		label.setFont(new java.awt.Font("arial", 1, 12));
		parameters.add(label, new GridBagConstraints(0, 1, 0, 0, 0.0, 0.0,
		                                             GridBagConstraints.NORTHWEST,
		                                             GridBagConstraints.NONE,
		                                             new Insets(215, 10, 0, 0), 0, 0));
		
		this.jCbIsFilter.setFont(new java.awt.Font("arial", 1, 12));
		parameters.add(this.jCbIsFilter,
		               new GridBagConstraints(0, 1, 0, 0, 0.0, 0.0,
		                                      GridBagConstraints.NORTHWEST,
		                                      GridBagConstraints.NONE,
		                                      new Insets(240, 20, 0, 0), 0, 0));
		
		label = new JLabel();
		label.setText("Min volume:");
		label.setFont(new java.awt.Font("arial", 1, 12));
		parameters.add(label,
		               new GridBagConstraints(0, 1, 0, 0, 0.0, 0.0,
		                                      GridBagConstraints.NORTHWEST,
		                                      GridBagConstraints.NONE,
		                                      new Insets(275, 20, 0, 0), 0, 0));
		
		this.jtfMin.setText("0.003");
		this.jtfMin.setPreferredSize(new java.awt.Dimension(60, 21));
		this.jtfMin.setFont(new java.awt.Font("arial", 1, 12));
		parameters.add(this.jtfMin,
		               new GridBagConstraints(0, 1, 0, 0, 0.0, 0.0,
		                                      GridBagConstraints.NORTHWEST,
		                                      GridBagConstraints.NONE,
		                                      new Insets(272, 175, 0, 0), 0, 0));
		
		label = new JLabel();
		label.setText("Max volume:");
		label.setFont(new java.awt.Font("arial", 1, 12));
		parameters.add(label,
		               new GridBagConstraints(0, 1, 0, 0, 0.0, 0.0,
		                                      GridBagConstraints.NORTHWEST,
		                                      GridBagConstraints.NONE,
		                                      new Insets(305, 20, 0, 0), 0, 0));
		
		this.jtfMax.setText("3");
		this.jtfMax.setPreferredSize(new java.awt.Dimension(60, 21));
		this.jtfMax.setFont(new java.awt.Font("arial", 1, 12));
		parameters.add(this.jtfMax,
		               new GridBagConstraints(0, 1, 0, 0, 0.0, 0.0,
		                                      GridBagConstraints.NORTHWEST,
		                                      GridBagConstraints.NONE,
		                                      new Insets(302, 175, 0, 0), 0, 0));
		
		jtfMax.setEnabled(false);
		jtfMin.setEnabled(false);
		
		//////////////////////////////////////
		
		this.jbStart.setPreferredSize(new java.awt.Dimension(120, 21));
		this.jbQuit.setPreferredSize(new java.awt.Dimension(120, 21));
		
		parameters.add(this.jbStart, new GridBagConstraints(0, 1, 0, 0, 0.0, 0.0,
		                                                    GridBagConstraints.NORTHWEST,
		                                                    GridBagConstraints.NONE,
		                                                    new Insets(330, 140, 0, 0), 0, 0));
		parameters.add(this.jbQuit, new GridBagConstraints(1, 1, 0, 0, 0.0, 0.0,
		                                                   GridBagConstraints.NORTHWEST,
		                                                   GridBagConstraints.NONE,
		                                                   new Insets(330, 10, 0, 0), 0, 0));
		
		localModeLayout.add(mainPanel);
		container.add(localModeLayout, 1);
		container.add(parameters, 2);
		
		// Omero mode layout
		omeroModeLayout.setLayout(new BoxLayout(omeroModeLayout, BoxLayout.Y_AXIS));
		
		JPanel        omeroPanel  = new JPanel();
		GridBagLayout omeroLayout = new GridBagLayout();
		omeroLayout.columnWeights = new double[]{0.1, 0.1, 2};
		omeroPanel.setLayout(omeroLayout);
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(5, 0, 5, 20);
		
		c.gridy = 0;
		JLabel jLabelHostname = new JLabel("Hostname :");
		c.gridx = 0;
		c.gridwidth = 1;
		omeroPanel.add(jLabelHostname, c);
		c.gridx = 1;
		c.gridwidth = 2;
		omeroPanel.add(jTextFieldHostname, c);
		jTextFieldHostname.setMaximumSize(new Dimension(10000, 20));
		
		c.gridy = 1;
		JLabel jLabelPort = new JLabel("Port :");
		c.gridx = 0;
		c.gridwidth = 1;
		omeroPanel.add(jLabelPort, c);
		c.gridx = 1;
		c.gridwidth = 2;
		omeroPanel.add(jTextFieldPort, c);
		jTextFieldPort.setMaximumSize(new Dimension(10000, 20));
		
		c.gridy = 2;
		JLabel jLabelUsername = new JLabel("Username :");
		c.gridx = 0;
		c.gridwidth = 1;
		omeroPanel.add(jLabelUsername, c);
		c.gridx = 1;
		c.gridwidth = 2;
		omeroPanel.add(jTextFieldUsername, c);
		jTextFieldUsername.setMaximumSize(new Dimension(10000, 20));
		
		c.gridy = 3;
		JLabel jLabelPassword = new JLabel("Password :");
		c.gridx = 0;
		c.gridwidth = 1;
		omeroPanel.add(jLabelPassword, c);
		c.gridx = 1;
		c.gridwidth = 2;
		omeroPanel.add(jPasswordField, c);
		jPasswordField.setMaximumSize(new Dimension(10000, 20));
		
		c.gridy = 4;
		JLabel jLabelGroup = new JLabel("Group ID :");
		c.gridx = 0;
		c.gridwidth = 1;
		omeroPanel.add(jLabelGroup, c);
		c.gridx = 1;
		c.gridwidth = 2;
		omeroPanel.add(jTextFieldGroup, c);
		jTextFieldGroup.setMaximumSize(new Dimension(10000, 20));
		
		c.gridy = 5;
		JLabel jLabelSource = new JLabel("Raw Nuclei :");
		c.gridx = 0;
		c.gridwidth = 1;
		omeroPanel.add(jLabelSource, c);
		c.gridx = 1;
		omeroPanel.add(jComboBoxDataType, c);
		c.gridx = 2;
		omeroPanel.add(jTextFieldSourceID, c);
		jTextFieldSourceID.setMaximumSize(new Dimension(10000, 20));
		
		c.gridy = 6;
		JLabel jLabelToCrop = new JLabel("Segmented Nuclei  :");
		c.gridx = 0;
		c.gridwidth = 1;
		omeroPanel.add(jLabelToCrop, c);
		c.gridx = 1;
		omeroPanel.add(jComboBoxDataTypeSegmented, c);
		c.gridx = 2;
		omeroPanel.add(segmentedNucleiTextField, c);
		segmentedNucleiTextField.setMaximumSize(new Dimension(20000, 20));
		
		c.gridy = 7;
		JLabel jLabelOutputProject = new JLabel("Output Project :");
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
		this.jCbIs2D.addActionListener(analysis);
		this.jCbIsFilter.addActionListener(analysis);
		
		ActionListener wdListener = new Listener(this, jtfWorkDir, false);
		this.jbOutputDir.addActionListener(wdListener);
		ActionListener rawListener = new Listener(this, this.jtfRawData, false);
		this.jbInputDir.addActionListener(rawListener);
		ActionListener segListener = new Listener(this, this.jtfRawSeg, false);
		this.jbInputSeg.addActionListener(segListener);
		
		ActionListener quitListener = new QuitListener(this);
		this.jbQuit.addActionListener(quitListener);
		ActionListener startListener = new StartListener(this);
		this.jbStart.addActionListener(startListener);
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
	public void main(String[] args) {
		GuiAnalysis gui = new GuiAnalysis(this);
		gui.setLocationRelativeTo(null);
	}
	
	
	/**
	 * /** getter of the workdir path
	 *
	 * @return String workdir path
	 */
	public String getOutputDir() {
		return this.jtfWorkDir.getText();
	}
	
	
	/**
	 * @return String input path
	 */
	public String getInputRaw() {
		return this.jtfRawData.getText();
	}
	
	
	public String getInputSeg() {
		return this.jtfRawSeg.getText();
	}
	
	
	public double getMin() {
		String x = this.jtfMin.getText();
		return Double.parseDouble(x.replaceAll(",", "."));
	}
	
	
	public double getFactor() {
		String x = this.jtfFactor.getText();
		return Double.parseDouble(x.replaceAll(",", "."));
	}
	
	
	public int getNeigh() {
		String x = this.jtfNeigh.getText();
		return Integer.parseInt(x.replaceAll(",", "."));
	}
	
	
	public double getMax() {
		String x = this.jtfMax.getText();
		return Double.parseDouble(x.replaceAll(",", "."));
	}
	
	
	public double getGaussianX() {
		String x = this.jtfGX.getText();
		return Double.parseDouble(x.replaceAll(",", "."));
	}
	
	
	public double getGaussianY() {
		String x = this.jtfGY.getText();
		return Double.parseDouble(x.replaceAll(",", "."));
	}
	
	
	public double getGaussianZ() {
		String x = this.jtfGZ.getText();
		return Double.parseDouble(x.replaceAll(",", "."));
	}
	
	
	public boolean isStart() {
		return this.start;
	}
	
	
	public boolean is2D() {
		return this.jCbIs2D.isSelected();
	}
	
	
	public boolean isGaussian() {
		return this.jCbIsGauss.isSelected();
	}
	
	
	public boolean isFilter() {
		return this.jCbIsFilter.isSelected();
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
	public void OnStart() {
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
				dialogListener.OnStart();
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
		private final GuiAnalysis gui;
		/**  */
		private final JTextField  jtf;
		/**  */
		private final boolean     file;
		
		
		/**
		 * @param gui
		 * @param jtf
		 * @param file
		 */
		Listener(GuiAnalysis gui, JTextField jtf, boolean file) {
			this.gui = gui;
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
				@SuppressWarnings("unused")
				String run = jFileChooser.getSelectedFile().getName();
				String text = jFileChooser.getSelectedFile().getAbsolutePath();
				jtf.setText(text);
			}
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
		
	}
	
}