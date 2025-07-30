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
package fr.igred.nucleus;

import fr.igred.nucleus.plugins.ComputeParametersPlugin;
import ij.IJ;


public final class PluginTester {
	
	/** Private constructor to prevent instantiation */
	private PluginTester() {
		// This class should not be instantiated
	}
	
	
	public static void main(String[] args) {
		Class<?> clazz = ComputeParametersPlugin.class;
		String   name  = clazz.getName();
		String url = clazz.getResource("/" +
		                               name.replace('.', '/') +
		                               ".class").toString();
		String pluginsDir = url.substring(5, url.length() - name.length() - 6);
		System.setProperty("plugins.dir", pluginsDir);
		// run the plugin
		IJ.runPlugIn(name, "");
	}
	
}
