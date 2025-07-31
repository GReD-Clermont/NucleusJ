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

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.Border;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;


/** OMERO panel for user input. */
public class OMEROPanel extends JPanel {
	
	private static final long serialVersionUID = -6868915494470555701L;
	
	private static final String[] dataTypes = {"Project", "Dataset", "Tag", "Image"};
	
	private final JTextField     jTextFieldHostname      = new JTextField();
	private final JTextField     jTextFieldPort          = new JTextField();
	private final JTextField     jTextFieldUsername      = new JTextField();
	private final JPasswordField jPasswordField          = new JPasswordField();
	private final JTextField     jTextFieldGroup         = new JTextField();
	private final JTextField     jTextFieldSourceID      = new JTextField();
	private final JTextField     jTextFieldOutputProject = new JTextField();
	
	private final JComboBox<String> jComboBoxDataType = new JComboBox<>(dataTypes);
	
	
	public OMEROPanel() {
		setBoxLayout();
		
		JPanel omeroPanel = new JPanel();
		
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
		
		Border padding = BorderFactory.createEmptyBorder(10, 10, 10, 10);
		omeroPanel.setBorder(padding);
		super.add(omeroPanel);
		
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
	
	
	/**
	 * Sets the layout of the panel to BoxLayout with vertical alignment.
	 */
	private void setBoxLayout() {
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
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
	 * Returns the source ID entered in the text field.
	 *
	 * @return See above.
	 */
	public String getSourceID() {
		return jTextFieldSourceID.getText();
	}
	
	
	/**
	 * Returns the data type selected in the combo box.
	 *
	 * @return See above.
	 */
	public String getDataType() {
		return (String) jComboBoxDataType.getSelectedItem();
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
