/*
 * NucleusJ
 * Copyright (C) 2025 iGReD
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
package fr.igred.nucleus.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.lang.invoke.MethodHandles;


/** Utility class for command-line interface operations. */
public final class CLIUtil {
	/** Logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	private static final char[] EMPTY_CHAR_ARRAY = new char[0];
	
	private static boolean warnedAboutConsole = false;
	
	
	private CLIUtil() {
		// Prevent instantiation
	}
	
	
	/**
	 * Prints a message to the console.
	 *
	 * @param message The message to print.
	 */
	public static void print(String message) {
		if (warnedAboutConsole || System.console() == null) {
			if (!warnedAboutConsole) {
				LOGGER.warn("No console available. Output will not be interactive.");
				warnedAboutConsole = true;
			}
			LOGGER.info(message);
		} else {
			try (PrintWriter console = System.console().writer()) {
				console.println(message);
			}
		}
	}
	
	
	public static char[] readPassword() {
		print("Enter password: ");
		return System.console() != null ? System.console().readPassword() : EMPTY_CHAR_ARRAY;
	}
	
}
