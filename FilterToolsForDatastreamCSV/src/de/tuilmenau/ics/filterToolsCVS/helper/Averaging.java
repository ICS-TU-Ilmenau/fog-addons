/*******************************************************************************
 * Filter Tools for Datastream CSV
 * Copyright 2012 TU Ilmenau.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl-2.0.txt
 ******************************************************************************/
package de.tuilmenau.ics.filterToolsCVS.helper;
import java.text.NumberFormat;


public class Averaging
{
	public Averaging()
	{
		format = NumberFormat.getInstance();
	}
	
	public void add(String value)
	{
		if(counter >= 0) {
			try {
				if(value != null) {
					sum += format.parse(value).doubleValue();
				}
				// else: tread it as zero
				counter++;
			}
			catch(Exception excDou) {
				// there was one entry, which is not a number
				// -> mark it as invalid
				counter = -1;
			}
		}
	}
	
	private String format(double value)
	{
		long rouded = Math.round(value);
		
		// integer or double value?
		if(value == rouded) {
			return format.format(rouded);
		} else {
			return format.format(value);
		}
	}
	
	public String toString()
	{
		if(counter > 0) {
			Double average = sum / (double)counter;
			return format(average);
		}
		else if(counter == 0) {
			return format(sum);
		}
		else {
			return "-";
		}
	}
	
	private double sum = 0;
	private int counter = 0;
	private NumberFormat format;
}
