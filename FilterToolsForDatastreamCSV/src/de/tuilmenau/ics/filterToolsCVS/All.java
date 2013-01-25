/*******************************************************************************
 * Filter Tools for Datastream CSV
 * Copyright 2012 TU Ilmenau.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl-2.0.txt
 ******************************************************************************/
package de.tuilmenau.ics.filterToolsCVS;

public class All
{
	private static final String NODE_PREFIX = "de.tuilmenau.ics.fog.topology.Node.";
	
	
	public static void main(String[] args)
	{
		if(args.length < 1) {
			System.err.println("usage: <filename> <node distribution parameters>");
		}
		
		Filter.main(new String[] { args[0], NODE_PREFIX });
		NodeStatistics.main(new String[] { args[0], NODE_PREFIX });
		
		args[0] = args[0] +NodeStatistics.FILENAME_POSTFIX;
		Distribution.main(args);
	}
}
