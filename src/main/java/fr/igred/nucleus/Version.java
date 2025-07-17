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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Properties;


public final class Version {
	/** Logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	/** Default constructor: private to prevent instantiation */
	private Version() {
		// DO NOTHING
	}
	
	
	public static String get() {
		
		Properties properties = new Properties();
		String     version    = "undefined";
		try {
			properties.load(Version.class.getClassLoader().getResourceAsStream("nucleusj.properties"));
			version = properties.getProperty("version");
		} catch (IOException e) {
			LOGGER.error("Could not retrieve NucleusJ version.", e);
		}
		
		return version;
	}
	
}
