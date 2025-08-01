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

import ij.Prefs;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.Border;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;


/** OMERO panel for user input. */
public class OMEROPanel extends JPanel {
	
	private static final long serialVersionUID = -6868915494470555701L;
	
	private static final Dimension MAX_DIM = new Dimension(10000, 20);
	
	private static final int DEFAULT_PORT = 4064;
	
	private final JTextField     jTextFieldHostname      = new JTextField();
	private final JTextField     jTextFieldPort          = new JTextField();
	private final JTextField     jTextFieldUsername      = new JTextField();
	private final JPasswordField jPasswordField          = new JPasswordField();
	private final JTextField     jTextFieldGroup         = new JTextField();
	private final JTextField     jTextFieldOutputProject = new JTextField();
	
	private final List<JComboBox<String>> jComboBoxDataType;
	private final List<JTextField>        jTextFieldSourceID;
	
	
	public OMEROPanel() {
		this(new String[]{"Project", "Dataset", "Tag", "Image"}, "Source:");
	}
	
	
	public OMEROPanel(String... sourceLabels) {
		this(new String[]{"Project", "Dataset", "Tag", "Image"}, sourceLabels);
	}
	
	
	public OMEROPanel(String[] dataTypes, String... sourceLabels) {
		jComboBoxDataType = new ArrayList<>(sourceLabels.length);
		jTextFieldSourceID = new ArrayList<>(sourceLabels.length);
		List<JLabel> jLabelSource = new ArrayList<>(sourceLabels.length);
		
		for (String label : sourceLabels) {
			jLabelSource.add(new JLabel(label));
			JComboBox<String> comboBox  = new JComboBox<>(dataTypes);
			JTextField        textField = new JTextField();
			jComboBoxDataType.add(comboBox);
			jTextFieldSourceID.add(textField);
			
			comboBox.setMaximumSize(MAX_DIM);
			textField.setMaximumSize(MAX_DIM);
		}
		
		GridBagLayout omeroLayout = new GridBagLayout();
		omeroLayout.columnWeights = new double[]{1, 1, 10};
		super.setLayout(omeroLayout);
		
		addRow(0, "Hostname:", jTextFieldHostname);
		addRow(1, "Port:", jTextFieldPort);
		addRow(2, "Username:", jTextFieldUsername);
		addRow(3, "Password:", jPasswordField);
		addRow(4, "Group ID:", jTextFieldGroup);
		int i;
		for (i = 0; i < jComboBoxDataType.size(); i++) {
			addRow(5 + i, jLabelSource.get(i), jComboBoxDataType.get(i), jTextFieldSourceID.get(i));
		}
		addRow(5 + i, "Output project:", jTextFieldOutputProject);
		
		Border padding = BorderFactory.createEmptyBorder(10, 10, 10, 10);
		super.setBorder(padding);
		initFields();
	}
	
	
	private void addRow(int row, String label, JComponent field) {
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(5, 0, 5, 20);
		c.gridy = row;
		c.gridx = 0;
		add(new JLabel(label), c);
		c.gridx = 1;
		c.gridwidth = 2;
		add(field, c);
		field.setMaximumSize(MAX_DIM);
	}
	
	
	private void addRow(int row, JLabel label, JComboBox<String> comboBox, JTextField textField) {
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(5, 0, 5, 2 * 10);
		c.gridy = row;
		c.gridx = 0;
		add(label, c);
		c.gridx = 1;
		add(comboBox, c);
		c.gridx = 2;
		add(textField, c);
		textField.setMaximumSize(MAX_DIM);
	}
	
	
	private void initFields() {
		String host     = Prefs.get("omero.host", "localhost");
		int    port     = Prefs.getInt("omero.port", DEFAULT_PORT);
		String username = Prefs.get("omero.user", "");
		
		// DEFAULT VALUES :
		jTextFieldHostname.setText(host);
		jTextFieldPort.setText(String.valueOf(port));
		jTextFieldUsername.setText(username);
		
		jPasswordField.setText("");
		jTextFieldGroup.setText("");
	}
	
	
	/**
	 * Returns the hostname entered in the text field.
	 *
	 * @return See above.
	 */
	public String getHostname() {
		return jTextFieldHostname.getText();
	}
	
	
	/**
	 * Returns the port entered in the text field.
	 *
	 * @return See above.
	 */
	public String getPort() {
		return jTextFieldPort.getText();
	}
	
	
	/**
	 * Returns the number of source IDs in the panel.
	 *
	 * @return See above.
	 */
	public int getNumberOfSourceIDs() {
		return jTextFieldSourceID.size();
	}
	
	
	/**
	 * Returns the source ID entered in the text field.
	 *
	 * @return See above.
	 */
	public String getSourceID(int index) {
		return jTextFieldSourceID.get(index).getText();
	}
	
	
	/**
	 * Returns the data type selected in the combo box.
	 *
	 * @return See above.
	 */
	public String getDataType(int index) {
		return (String) jComboBoxDataType.get(index).getSelectedItem();
	}
	
	
	/**
	 * Returns the username entered in the text field.
	 *
	 * @return See above.
	 */
	public String getUsername() {
		return jTextFieldUsername.getText();
	}
	
	
	/**
	 * Returns the password entered in the password field.
	 *
	 * @return See above.
	 */
	public char[] getPassword() {
		return jPasswordField.getPassword();
	}
	
	
	/**
	 * Returns the group ID entered in the text field.
	 *
	 * @return See above.
	 */
	public String getGroup() {
		return jTextFieldGroup.getText();
	}
	
	
	/**
	 * Returns the output project entered in the text field.
	 *
	 * @return See above.
	 */
	public String getOutputProject() {
		return jTextFieldOutputProject.getText();
	}
	
}
