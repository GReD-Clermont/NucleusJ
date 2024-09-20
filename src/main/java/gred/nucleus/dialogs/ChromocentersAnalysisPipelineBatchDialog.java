package gred.nucleus.dialogs;

import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServiceException;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.concurrent.ExecutionException;


/**
 * Class to construct graphical interface for the chromocenter analysis pipeline in batch
 *
 * @author pouletaxel
 */
public class ChromocentersAnalysisPipelineBatchDialog extends JFrame implements ItemListener {
	private static final long         serialVersionUID        = 1L;
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
	private              boolean      start                   = false;
	final                IDialogListener               dialogListener;
	private final        JRadioButton      omeroYesButton          = new JRadioButton("Yes");
	private final        JRadioButton      omeroNoButton           = new JRadioButton("No");
	private final        JPanel            omeroModeLayout         = new JPanel();
	private final        JPanel            localModeLayout         = new JPanel();
	private final        JTextField        jTextFieldHostname      = new JTextField();
	private final        JTextField        jTextFieldPort          = new JTextField();
	private final        JTextField        jTextFieldUsername      = new JTextField();
	private final        JPasswordField    jPasswordField          = new JPasswordField();
	private final        JTextField        jTextFieldGroup         = new JTextField();
	private final        String[]          dataTypes               = { "Image", "Dataset" };
	private final        JComboBox<String> jComboBoxDataType       = new JComboBox<>(dataTypes);
	private final        JComboBox<String> jComboBoxDataTypeNuc      = new JComboBox<>(dataTypes);
	private final        JComboBox<String> jComboBoxDataTypeCC      = new JComboBox<>(dataTypes);
	private final        JTextField        jTextFieldSourceID      = new JTextField();
	private final        JTextField jTextFieldNucSegID = new JTextField();
	private final        JTextField        jTextFieldOutputProject = new JTextField();
	private final        JTextField jTextFieldCCsegID = new JTextField();
	private              Container         container;
	private              boolean           useOMERO                = false;
	/** Architecture of the graphical windows */
	public ChromocentersAnalysisPipelineBatchDialog(IDialogListener dialogListener) {
		final String font = "Albertus";
		final String boldFont = "Albertus Extra Bold (W1)";
	    container = getContentPane();
		final JLabel jLabelWorkDirectory = new JLabel("Work directory and data directory choice : ");
		final JButton jButtonWorkDirectory = new JButton("Output Directory");
		final JButton jButtonStart = new JButton("Start");
		final JButton jButtonQuit = new JButton("Quit");
		final JButton jButtonRawData = new JButton("Raw Data");
		final ButtonGroup buttonGroupChoiceAnalysis = new ButtonGroup();
		final ButtonGroup buttonGroupChoiceRhf = new ButtonGroup();
		JLabel jLabelAnalysis;
		JLabel jLabelAnalysis2;
		this.setTitle("Chromocenters Analysis Pipeline (Batch)");
		this.setSize(500, 700);
		this.setLocationRelativeTo(null);
		this.dialogListener = dialogListener;
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.1};
		gridBagLayout.rowHeights = new int[]{17, 200, 124, 7};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0, 0.1};
		gridBagLayout.columnWidths = new int[]{236, 120, 72, 20};
		container.setLayout(gridBagLayout);

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

		container.add(radioOmeroPanel, new GridBagConstraints(0, 0, 2, 1, 1.0, 0.0,
				GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
				new Insets(10, 10, 0, 10), 0, 0));

		// Local mode layout
		localModeLayout.setLayout(new BoxLayout(localModeLayout, BoxLayout.Y_AXIS));
		JPanel localPanel = new JPanel();
		GridBagLayout localLayout = new GridBagLayout();
		localLayout.columnWeights = new double[]{1, 5, 0.5};
		localPanel.setLayout(localLayout);

		localPanel.add(jLabelWorkDirectory,
				new GridBagConstraints(0, 0, 3, 1, 0.0, 0.0,
						GridBagConstraints.NORTHWEST,
						GridBagConstraints.NONE,
						new Insets(10, 10, 0, 10), 0, 0));

		JTextPane jTextPane = new JTextPane();
		jTextPane.setText("The Raw Data directory must contain 3 subdirectories:\n1. for raw nuclei images, named RawDataNucleus. \n2. for segmented nuclei images, named SegmentedDataNucleus.\n3. for segmented images of chromocenters, named SegmentedDataCc.\nPlease keep the same file name during the image processing.");
		jTextPane.setEditable(false);
		localPanel.add(jTextPane,
				new GridBagConstraints(0, 1, 3, 1, 1.0, 0.0,
						GridBagConstraints.NORTHWEST,
						GridBagConstraints.HORIZONTAL,
						new Insets(10, 10, 0, 10), 0, 0));

		localPanel.add(jButtonRawData,
				new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
						GridBagConstraints.NORTHWEST,
						GridBagConstraints.NONE,
						new Insets(10, 10, 0, 5), 0, 0));
		jButtonRawData.setPreferredSize(new Dimension(120, 21));
		jButtonRawData.setFont(new Font(font, Font.ITALIC, 10));

		localPanel.add(jTextFieldRawData,
				new GridBagConstraints(1, 2, 2, 1, 1.0, 0.0,
						GridBagConstraints.NORTHWEST,
						GridBagConstraints.HORIZONTAL,
						new Insets(10, 5, 0, 10), 0, 0));
		jTextFieldRawData.setPreferredSize(new Dimension(280, 21));
		jTextFieldRawData.setFont(new Font(font, Font.ITALIC, 10));

		localPanel.add(jButtonWorkDirectory,
				new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
						GridBagConstraints.NORTHWEST,
						GridBagConstraints.NONE,
						new Insets(10, 10, 0, 5), 0, 0));
		jButtonWorkDirectory.setPreferredSize(new Dimension(120, 21));
		jButtonWorkDirectory.setFont(new Font(font, Font.ITALIC, 10));

		localPanel.add(jTextFieldWorkDirectory,
				new GridBagConstraints(1, 3, 2, 1, 1.0, 0.0,
						GridBagConstraints.NORTHWEST,
						GridBagConstraints.HORIZONTAL,
						new Insets(10, 5, 0, 10), 0, 0));
		jTextFieldWorkDirectory.setPreferredSize(new Dimension(280, 21));
		jTextFieldWorkDirectory.setFont(new Font(font, Font.ITALIC, 10));

		calibration = new JPanel();
		calibration.setLayout(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.weightx = 2;
		gc.weighty = 5;
		gc.anchor = GridBagConstraints.NORTHWEST;
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
/**
		localPanel.add(calibration,
				new GridBagConstraints(0, 4, 3, 1, 1.0, 0.0,
						GridBagConstraints.NORTHWEST,
						GridBagConstraints.HORIZONTAL,
						new Insets(10, 10, 0, 10), 0, 0));
*/
		jLabelAnalysis = new JLabel("Type of Relative Heterochromatin Fraction:");
		container.add(jLabelAnalysis,
				new GridBagConstraints(0, 3, 5, 1, 0.0, 0.0,
						GridBagConstraints.SOUTHWEST,
						GridBagConstraints.NONE,
						new Insets(10, 10, 0, 10), 0, 0));

		//buttonGroupChoiceAnalysis.add(jRadioButtonNucCc);
		//buttonGroupChoiceAnalysis.add(jRadioButtonCc);
		//buttonGroupChoiceAnalysis.add(jRadioButtonNuc);

		JPanel analysisPanel = new JPanel();
		analysisPanel.setLayout(new BoxLayout(analysisPanel, BoxLayout.X_AXIS));
		analysisPanel.add(jRadioButtonNucCc);
		analysisPanel.add(jRadioButtonCc);
		analysisPanel.add(jRadioButtonNuc);

		container.add(analysisPanel,
				new GridBagConstraints(0, 4, 3, 1, 1.0, 0.0,
						GridBagConstraints.NORTHWEST,
						GridBagConstraints.NONE,
						new Insets(10, 10, 0, 10), 0, 0));
		jLabelAnalysis2 = new JLabel("Results file of interest:");
		container.add(jLabelAnalysis2,
				new GridBagConstraints(0, 5, 3, 1, 1.0, 0.0,
						GridBagConstraints.NORTHWEST,
						GridBagConstraints.NONE,
						new Insets(10, 10, 0, 10), 0, 0));
		buttonGroupChoiceRhf.add(jRadioButtonRhfV);
		buttonGroupChoiceRhf.add(jRadioButtonRhfI);
		buttonGroupChoiceRhf.add(jRadioButtonRhfIV);

		JPanel rhfPanel = new JPanel();
		rhfPanel.setLayout(new BoxLayout(rhfPanel, BoxLayout.X_AXIS));
		rhfPanel.add(jRadioButtonRhfV);
		rhfPanel.add(jRadioButtonRhfI);
		rhfPanel.add(jRadioButtonRhfIV);

		container.add(rhfPanel,
				new GridBagConstraints(0, 6, 5, 1, 1.0, 0.0,
						GridBagConstraints.NORTHWEST,
						GridBagConstraints.HORIZONTAL,
						new Insets(10, 10, 0, 10), 0, 0));

		localModeLayout.add(localPanel);
		container.add(localModeLayout, new GridBagConstraints(0, 1, 4, 1, 1.0, 0.0,
				GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
				new Insets(10, 10, 10, 10), 0, 0));


		// Omero mode layout
		omeroModeLayout.setLayout(new BoxLayout(omeroModeLayout, BoxLayout.Y_AXIS));
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		JPanel        omeroPanel  = new JPanel();
		GridBagLayout omeroLayout = new GridBagLayout();
		omeroLayout.columnWeights = new double[]{0.1, 0.1, 2};
		omeroPanel.setLayout(omeroLayout);
		c = new GridBagConstraints();
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
		JLabel jLabelSource = new JLabel("Image Source :");
		c.gridx = 0;
		c.gridwidth = 1;
		omeroPanel.add(jLabelSource, c);
		c.gridx = 1;
		omeroPanel.add(jComboBoxDataType, c);
		c.gridx = 2;
		omeroPanel.add(jTextFieldSourceID, c);
		jTextFieldSourceID.setMaximumSize(new Dimension(10000, 20));

		c.gridy = 6;
		JLabel jLabelToCrop = new JLabel("Nucleus segmentation :");
		c.gridx = 0;
		c.gridwidth = 1;
		omeroPanel.add(jLabelToCrop, c);
		c.gridx = 1;
		omeroPanel.add(jComboBoxDataTypeNuc, c);
		c.gridx = 2;
		omeroPanel.add(jTextFieldNucSegID, c);
		jTextFieldNucSegID.setMaximumSize(new Dimension(20000, 20));

		c.gridy=7;
		JLabel JchannelToCrop = new JLabel("Chromocenter segmentation :");
		c.gridx=0;
		omeroPanel.add(JchannelToCrop,c);
		c.gridx=1;
		omeroPanel.add(jComboBoxDataTypeCC, c);
		c.gridx = 2;
		omeroPanel.add(jTextFieldCCsegID,c);
		jTextFieldCCsegID.setMaximumSize(new Dimension(20, 20));


		c.gridy = 8;
		JLabel jLabelOutputProject = new JLabel("Output Dataset :");
		c.gridx = 0;
		c.gridwidth = 1;
		omeroPanel.add(jLabelOutputProject, c);
		c.gridx = 1;
		c.gridwidth = 2;
		omeroPanel.add(jTextFieldOutputProject, c);
		jTextFieldOutputProject.setMaximumSize(new Dimension(10000, 20));
		container.add(calibration,
				new GridBagConstraints(0, 2, 3, 1, 1.0, 0.0,
						GridBagConstraints.NORTHWEST,
						GridBagConstraints.HORIZONTAL,
						new Insets(10, 10, 0, 10), 0, 0));

		omeroPanel.setBorder(padding);
		omeroModeLayout.add(omeroPanel);
		//container.add(omeroModeLayout, 1);


		// Buttons at the bottom
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.add(jButtonStart);
		buttonPanel.add(jButtonQuit);

		container.add(buttonPanel, new GridBagConstraints(0, 7, 4, 1, 1.0, 0.0,
				GridBagConstraints.SOUTH, GridBagConstraints.NONE,
				new Insets(10, 10, 10, 10), 0, 0));

		jButtonQuit.setPreferredSize(new java.awt.Dimension(120, 21));
		jButtonStart.setPreferredSize(new java.awt.Dimension(120, 21));
		WorkDirectoryListener wdListener = new WorkDirectoryListener();
		jButtonWorkDirectory.addActionListener(wdListener);
		RawDataDirectoryListener ddListener = new RawDataDirectoryListener();
		jButtonRawData.addActionListener(ddListener);
		QuitListener quitListener = new QuitListener(this);
		jButtonQuit.addActionListener(quitListener);
		StartListener startListener = new StartListener(this);
		jButtonStart.addActionListener(startListener);
		this.setVisible(true);


		// DEFAULT VALUES FOR TESTING :
		jTextFieldHostname.setText("omero.igred.fr");
		jTextFieldPort.setText(String.valueOf(4064));

		jTextFieldUsername.setText("");
		jTextFieldGroup.setText("553");
		jPasswordField.setText("");
		jComboBoxDataType.setSelectedIndex(1);
		jComboBoxDataTypeCC.setSelectedIndex(1);
		jComboBoxDataTypeNuc.setSelectedIndex(1);
		jTextFieldSourceID.setText("31510");
		jTextFieldNucSegID.setText("31511");
		jTextFieldCCsegID.setText("31512");
		jTextFieldOutputProject.setText("27229");
	}




	public String getHostname() {
		return jTextFieldHostname.getText();
	}

	public String getPort() {
		return jTextFieldPort.getText();
	}

	public String getUsername() {
		return jTextFieldUsername.getText();
	}

	public String getPassword() {
		return String.valueOf(jPasswordField.getPassword());
	}

	public String getGroup() {
		return jTextFieldGroup.getText();
	}

	public String getOutputProject() {
		return jTextFieldOutputProject.getText();
	}

	public String getSourceID() {
		return jTextFieldSourceID.getText();
	}

	public String getSegID() {
		return jTextFieldNucSegID.getText();
	}

	public String getCcID(){ return jTextFieldCCsegID.getText(); }

	public String getDataType() {
		return (String) jComboBoxDataType.getSelectedItem();
	}

	public String getDataTypeSeg() {
		return (String) jComboBoxDataTypeNuc.getSelectedItem();
	}

	public String getDataTypeCC() {
		return (String) jComboBoxDataTypeCC.getSelectedItem();
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

	public boolean isOmeroEnabled() {
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
			container.add(omeroModeLayout, new GridBagConstraints(0, 1, 4, 1, 1.0, 0.0,
					GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
					new Insets(10, 10, 10, 10), 0, 0));
			useOMERO = true;
		}
		// Check if the Omero "No" button is selected
		else if (e.getSource() == omeroNoButton && e.getStateChange() == ItemEvent.SELECTED) {
			// Show local mode layout and hide Omero layout
			omeroModeLayout.setVisible(false);
			localModeLayout.setVisible(true);
			container.add(localModeLayout, new GridBagConstraints(0, 1, 4, 1, 1.0, 0.0,
					GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
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

				//pack();

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

	public void itemStateChanged2(ItemEvent e) {
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
				
				//pack();
				
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
	 *
	 */
	static class QuitListener implements ActionListener {
		final ChromocentersAnalysisPipelineBatchDialog chromocentersAnalysisPipelineBatchDialog;
		
		
		/** @param chromocentersAnalysisPipelineBatchDialog chromocentersAnalysisPipelineBatchDialog GUI */
		public QuitListener(ChromocentersAnalysisPipelineBatchDialog chromocentersAnalysisPipelineBatchDialog) {
			this.chromocentersAnalysisPipelineBatchDialog = chromocentersAnalysisPipelineBatchDialog;
		}
		
		
		/**
		 *
		 */
		public void actionPerformed(ActionEvent actionEvent) {
			chromocentersAnalysisPipelineBatchDialog.dispose();
		}
		
	}
	
	/** Classes listener to interact with the several elements of the window */
	class StartListener implements ActionListener {
		
		final ChromocentersAnalysisPipelineBatchDialog chromocentersAnalysisPipelineBatchDialog;
		
		
		/** @param chromocentersAnalysisPipelineBatchDialog chromocentersAnalysisPipelineBatchDialog GUI */
		public StartListener(ChromocentersAnalysisPipelineBatchDialog chromocentersAnalysisPipelineBatchDialog) {
			this.chromocentersAnalysisPipelineBatchDialog = chromocentersAnalysisPipelineBatchDialog;
		}
		
		
		/**
		 *
		 */
		public void actionPerformed(ActionEvent actionEvent) {
			if (!useOMERO && (jTextFieldWorkDirectory.getText().isEmpty() || jTextFieldRawData.getText().isEmpty())) {
				JOptionPane.showMessageDialog
						(
								null,
								"You did not choose a work directory or the raw data",
								"Error",
								JOptionPane.ERROR_MESSAGE
						);
			} else {
				start = true;
				chromocentersAnalysisPipelineBatchDialog.dispose();
				try {
					dialogListener.OnStart();
				} catch (AccessException e) {
					throw new RuntimeException(e);
				} catch (ServiceException e) {
					throw new RuntimeException(e);
				} catch (ExecutionException e) {
					throw new RuntimeException(e);
				}
			}
		}
		
	}
	
	/**
	 *
	 */
	class WorkDirectoryListener implements ActionListener {
		/**
		 *
		 */
		public void actionPerformed(ActionEvent actionEvent) {
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			JFileChooser jFileChooser = new JFileChooser();
			jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int returnValue = jFileChooser.showOpenDialog(getParent());
			if (returnValue == JFileChooser.APPROVE_OPTION) {
				@SuppressWarnings("unused")
				String run = jFileChooser.getSelectedFile().getName();
				String workDirectory = jFileChooser.getSelectedFile().getAbsolutePath();
				jTextFieldWorkDirectory.setText(workDirectory);
			}
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
		
	}
	
	
	/**
	 *
	 */
	class RawDataDirectoryListener implements ActionListener {
		/**
		 *
		 */
		public void actionPerformed(ActionEvent actionEvent) {
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			JFileChooser jFileChooser = new JFileChooser();
			jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int returnValue = jFileChooser.showOpenDialog(getParent());
			if (returnValue == JFileChooser.APPROVE_OPTION) {
				@SuppressWarnings("unused")
				String run = jFileChooser.getSelectedFile().getName();
				String rawDataDirectory = jFileChooser.getSelectedFile().getAbsolutePath();
				jTextFieldRawData.setText(rawDataDirectory);
			}
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
		
	}
	
}