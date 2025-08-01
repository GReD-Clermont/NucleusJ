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
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.concurrent.ExecutionException;


public class CropFromCoordinatesDialog extends JFrame implements ActionListener, ItemListener {
	private static final long serialVersionUID = -1113846613817254789L;
	
	private static final String INPUT_CHOOSER = "inputChooser";
	
	private final transient IDialogListener listener;
	
	private final JTextField   jImageChooser     = new JTextField();
	private final JTextField   jCoordFileChooser = new JTextField();
	private final JFileChooser fc                = new JFileChooser();
	private final JRadioButton omeroYesButton    = new JRadioButton("Yes");
	private final JRadioButton omeroNoButton     = new JRadioButton("No");
	private final OMEROPanel   omeroModeLayout;
	private final JPanel       localModeLayout   = new JPanel();
	
	private final JTextField jTextFieldChannelToCrop = new JTextField();
	private final JTextField jInputFileChooser       = new JTextField();
	
	private final Container container;
	
	private boolean useOMERO;
	
	
	public CropFromCoordinatesDialog(IDialogListener listener) {
		this.listener = listener;
		
		JButton jButtonStart = new JButton("Start");
		jButtonStart.setBackground(new Color(0x2dce98));
		jButtonStart.setForeground(Color.white);
		JButton jButtonQuit = new JButton("Quit");
		jButtonQuit.setBackground(Color.red);
		jButtonQuit.setForeground(Color.white);
		super.setTitle("Crop From Coordinate - NucleusJ - v" + Version.get());
		super.setMinimumSize(new Dimension(500, 410));
		super.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
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
		container.add(radioOmeroPanel, new GridBagConstraints(0, 0, 0, 0, 0.0, 0.0,
		                                                      GridBagConstraints.FIRST_LINE_START,
		                                                      GridBagConstraints.NONE,
		                                                      new Insets(60, 30, 0, 0), 0, 0));
		
		// Local mode layout
		localModeLayout.setLayout(new BoxLayout(localModeLayout, BoxLayout.PAGE_AXIS));
		
		JPanel        localPanel  = new JPanel();
		GridBagLayout localLayout = new GridBagLayout();
		localLayout.columnWeights = new double[]{1, 5, 0.5};
		localPanel.setLayout(localLayout);
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		
		JLabel jLabelInput = new JLabel("Path to coordinate file:");
		localPanel.add(jLabelInput, c);
		c.gridx = 1;
		c.insets = new Insets(0, 0, 0, 20);
		
		localPanel.add(jInputFileChooser, c);
		jInputFileChooser.setMaximumSize(new Dimension(10000, 20));
		jInputFileChooser.setSize(new Dimension(180, 20));
		jInputFileChooser.setText("path\\coordinate file_tab_path\\image");
		
		JButton sourceButton = new JButton("...");
		sourceButton.setSize(new Dimension(20, 20));
		sourceButton.addActionListener(this);
		sourceButton.setName(INPUT_CHOOSER);
		c.insets = new Insets(0, 0, 0, 0);
		c.gridx = 2;
		
		localPanel.add(sourceButton, c);
		
		localPanel.setBorder(padding);
		localModeLayout.add(localPanel);
		container.add(localModeLayout, 1);
		
		// Omero mode layout
		String label1 = "Source:";
		String label2 = "Image to crop:";
		omeroModeLayout = new OMEROPanel(label1, label2);

        /*/\*\
        ------------------------------ Buttons -----------------------------------------
        \*\/*/
		
		// Start/Quit buttons
		
		Border padding2 = BorderFactory.createEmptyBorder(10, 120, 10, 120);
		
		JPanel startQuitPanel = new JPanel();
		startQuitPanel.setLayout(new GridLayout(1, 2, 30, 10));
		startQuitPanel.add(jButtonStart);
		startQuitPanel.add(jButtonQuit);
		startQuitPanel.setBorder(padding2);
		container.add(startQuitPanel, 2);
		
		jButtonQuit.addActionListener(e -> dispose());
		jButtonStart.addActionListener(e -> start());
		super.setVisible(true);
	}
	
	
	public String getLink() {
		return jInputFileChooser.getText();
	}
	
	
	public String getImage() {
		return jImageChooser.getText();
	}
	
	
	public String getCoord() {
		return jCoordFileChooser.getText();
	}
	
	
	public String getInput() {
		return jInputFileChooser.getText();
	}
	
	
	public boolean useOMERO() {
		return useOMERO;
	}
	
	
	public String getHostname() {
		return omeroModeLayout.getHostname();
	}
	
	
	public String getPort() {
		return omeroModeLayout.getPort();
	}
	
	
	public String getSourceID() {
		return omeroModeLayout.getSourceID(0);
	}
	
	
	public String getToCropID() {
		return omeroModeLayout.getSourceID(1);
	}
	
	
	public String getChannelToCrop() {
		return jTextFieldChannelToCrop.getText();
	}
	
	
	public String getDataType() {
		return omeroModeLayout.getDataType(0);
	}
	
	
	public String getDataTypeToCrop() {
		return omeroModeLayout.getDataType(1);
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
	
	
	public void actionPerformed(ActionEvent e) {
		
		if (((Component) e.getSource()).getName().equals(INPUT_CHOOSER)) {
			fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		}
		fc.setAcceptAllFileFilterUsed(false);
		
		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION &&
		    INPUT_CHOOSER.equals(((Component) e.getSource()).getName())) {
			File selectedInput = fc.getSelectedFile();
			jInputFileChooser.setText(selectedInput.getPath());
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
		}
		
		validate();
		repaint();
	}
	
	
	/**
	 * Disposes the dialog and starts the process.
	 */
	private void start() {
		dispose();
		try {
			listener.onStart();
		} catch (AccessException | ServiceException | ExecutionException e) {
			IJ.error("Error starting the process", e.getMessage());
		}
	}
	
	
}
