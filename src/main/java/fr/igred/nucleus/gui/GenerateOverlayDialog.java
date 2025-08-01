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


public class GenerateOverlayDialog extends JFrame implements ActionListener, ItemListener {
	private static final long serialVersionUID = 4963585921321275469L;
	
	private static final String INPUT_CHOOSER  = "inputChooser";
	private static final String INPUT_CHOOSER2 = "inputChooser2";
	
	private final JFileChooser fc = new JFileChooser();
	
	private final transient IDialogListener dialogListener;
	
	private final JRadioButton   omeroYesButton     = new JRadioButton("Yes");
	private final JRadioButton   omeroNoButton      = new JRadioButton("No");
	private final OMEROPanel     omeroModeLayout;
	private final JPanel         localModeLayout    = new JPanel();
	
	private final JTextField dicFileChooser          = new JTextField();
	private final JTextField zProjectionFileChooser  = new JTextField();
	private final Container  container;
	
	private boolean start;
	private boolean useOMERO;
	
	
	public GenerateOverlayDialog(IDialogListener dialogListener) {
		this.dialogListener = dialogListener;
		
		JButton jButtonStart = new JButton("Start");
		jButtonStart.setBackground(new Color(0x2dce98));
		jButtonStart.setForeground(Color.white);
		JButton jButtonQuit = new JButton("Quit");
		jButtonQuit.setBackground(Color.red);
		jButtonQuit.setForeground(Color.white);
		super.setTitle("Generate Overlay - NucleusJ - v" + Version.get());
		super.setMinimumSize(new Dimension(500, 390));
		
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
		
		JLabel jLabelInput = new JLabel("Path to DIC file:");
		localPanel.add(jLabelInput, c);
		c.gridx = 1;
		c.insets = new Insets(0, 0, 0, 20);
		
		localPanel.add(dicFileChooser, c);
		dicFileChooser.setMaximumSize(new Dimension(10000, 20));
		dicFileChooser.setSize(new Dimension(180, 20));
		dicFileChooser.setText("path\\DIC Folder\\");
		
		JButton sourceButton = new JButton("....");
		sourceButton.setSize(new Dimension(20, 18));
		sourceButton.addActionListener(this);
		sourceButton.setName(INPUT_CHOOSER);
		c.insets = new Insets(0, 0, 0, 0);
		c.gridx = 2;
		
		localPanel.add(sourceButton, c);
		
		JLabel jLabelInput2 = new JLabel("Path to Z projection file:");
		c.gridx = 0;
		c.gridy = 3;
		c.insets = new Insets(0, 0, 0, 20);
		localPanel.add(jLabelInput2, c);
		c.gridx = 1;
		c.gridy = 3;
		localPanel.add(zProjectionFileChooser, c);
		zProjectionFileChooser.setMaximumSize(new Dimension(10000, 20));
		zProjectionFileChooser.setSize(new Dimension(180, 20));
		zProjectionFileChooser.setText("path\\Z projection Folder\\");
		
		JButton sourceButton2 = new JButton("...");
		sourceButton2.setSize(new Dimension(20, 20));
		sourceButton2.addActionListener(this);
		sourceButton2.setName(INPUT_CHOOSER2);
		c.insets = new Insets(0, 0, 0, 0);
		c.gridx = 2;
		c.gridy = 3;
		
		localPanel.add(sourceButton2, c);
		
		localPanel.setBorder(padding);
		localModeLayout.add(localPanel);
		container.add(localModeLayout, 1);
		
		// Omero mode layout
		String[] dataTypes = {"Dataset"};
		String label1 = "Z Projection:";
		String label2 = "DIC:";
		omeroModeLayout = new OMEROPanel(dataTypes, label1, label2);
		
		// Start/Quit buttons
		
		Border padding2 = BorderFactory.createEmptyBorder(10, 120, 10, 120);
		
		JPanel startQuitPanel = new JPanel();
		startQuitPanel.setLayout(new GridLayout(1, 2, 30, 10));
		startQuitPanel.add(jButtonStart);
		startQuitPanel.add(jButtonQuit);
		startQuitPanel.setBorder(padding2);
		container.add(startQuitPanel, 2);
		
		
		jButtonQuit.addActionListener(this::quit);
		jButtonStart.addActionListener(this::start);
		super.setVisible(true);
	}
	
	
	public boolean isStart() {
		return start;
	}
	
	
	public String getDICInput() {
		return dicFileChooser.getText();
	}
	
	
	public String getZprojectionInput() {
		return zProjectionFileChooser.getText();
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
	
	
	public String getzProjectionID() {
		return omeroModeLayout.getSourceID(1);
	}
	
	
	public String getDICDataType() {
		return omeroModeLayout.getDataType(0);
	}
	
	
	public String getZprojectionDataType() {
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
		if (((Component) e.getSource()).getName().equals(INPUT_CHOOSER2)) {
			fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		}
		fc.setAcceptAllFileFilterUsed(false);
		
		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			switch (((Component) e.getSource()).getName()) {
				case INPUT_CHOOSER:
					File selectedInput = fc.getSelectedFile();
					dicFileChooser.setText(selectedInput.getPath());
					break;
				case INPUT_CHOOSER2:
					File selectedInput2 = fc.getSelectedFile();
					zProjectionFileChooser.setText(selectedInput2.getPath());
					break;
				default:
					throw new IllegalArgumentException("Unknown action source: " + e.getSource());
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
		}
		
		validate();
		repaint();
	}
	
	
	private void quit(ActionEvent actionEvent) {
		dispose();
	}
	
	
	private void start(ActionEvent actionEvent) {
		start = true;
		dispose();
		try {
			dialogListener.onStart();
		} catch (AccessException | ServiceException | ExecutionException e) {
			IJ.error("Error starting the process", e.getMessage());
		}
	}
	
	
}
