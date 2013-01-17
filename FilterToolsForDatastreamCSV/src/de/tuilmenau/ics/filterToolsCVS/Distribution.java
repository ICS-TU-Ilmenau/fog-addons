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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import com.csvreader.CsvReader;


public class Distribution
{
	private static final char SEPARATOR = ';';
	private static final String SEPARATOR_OUT = SEPARATOR +" ";
	
	private static final Locale NUMBER_FORMAT = Locale.GERMAN;
	
	private static final Locale OUTPUT_LANGUAGE = Locale.GERMAN;
	
	private static final String NEW_LINE = "\n";
	

	public static void main(String[] args)
	{
		if(args.length > 1) {
			String filename = args[0];
			LinkedList<String> columns = new LinkedList<String>();
			
			for(int i=1; i<args.length; i++) {
				columns.add(args[i]);
			}
			
			try {
				int[] entries = new int[1];
				HashMap<Double, Integer> data = calculateDistribution(filename, columns, entries);
				writeDistribution(data, columns, entries[0], filename);
			}
			catch(IOException exc) {
				exc.printStackTrace(System.err);
			}
			catch(ParseException exc) {
				System.err.println("Parsing values failed. Current language is " +NUMBER_FORMAT);
				exc.printStackTrace(System.err);
			}
		} else {
			System.err.println("Calculated the distribution of one or more columns");
			System.err.println("usage: <filename> <column1> [column2...]");
		}
	}
	
	private static HashMap<Double, Integer> calculateDistribution(String filename, LinkedList<String> columns, int[] lines) throws IOException, ParseException
	{
		FileReader fr = new FileReader(filename);
		CsvReader csvIn = new CsvReader(fr, SEPARATOR);
		
		csvIn.setSafetySwitch(false);
		if(csvIn.readHeaders()) {
			System.out.println("'" +filename +"' has " +csvIn.getColumnCount() +" column (lang=" +NUMBER_FORMAT +")");
			
			NumberFormat numberParsing = NumberFormat.getInstance(NUMBER_FORMAT);
			HashMap<Double, Integer> data = new HashMap<Double, Integer>();
			lines[0] = 0;
			int valueCounter = 0;
			
			while(csvIn.readRecord()) {
				Double value = 0.0d;
				
				lines[0]++;

				// sum up the values of the mentioned columns
				for(String column : columns) {
					String valueStr = csvIn.get(column);
					
					if(valueStr != null) {
						valueStr = valueStr.trim();
						
						if(!"".equals(valueStr)) {
							valueCounter++;
							
							value += numberParsing.parse(valueStr).doubleValue();
						}
					}
				}
				
				Integer entry = data.get(value);
				if(entry == null) {
					entry = new Integer(1);
				} else {
					entry++;
				}
				data.put(value, entry);
			}
			System.out.println(" -> " +lines[0] +" lines with " +valueCounter +" non-empty values");
			
			return data;
		}
		
		return null;
	}
	
	private static void writeDistribution(HashMap<Double, Integer> distribution, LinkedList<String> columns, int numberEntries, String baseFilename) throws IOException
	{
		//
		// Generate name for distribution
		//
		StringBuffer name = new StringBuffer();
		java.util.Collections.sort(columns);
		
		for(String col : columns) {
			name.append(col);
			name.append("_");
		}
		
		String filename = baseFilename +" " +name.toString() +".csv";
		FileWriter fw = new FileWriter(filename, false);
		
		//
		// Write header to file
		//
		fw.write("value" +SEPARATOR_OUT +"number" +SEPARATOR_OUT +"sum equal or smaller" +SEPARATOR_OUT +"percentage");
		fw.write(NEW_LINE);
		
		//
		// Write data to file
		//
		System.out.println("Write statistic to '" +filename +"'");
		System.out.println("  with " +distribution.size() +" entries");
		System.out.println("  lang " +OUTPUT_LANGUAGE);

		NumberFormat numberFormater = NumberFormat.getInstance(OUTPUT_LANGUAGE);

		List<Double> values = new ArrayList<Double>(distribution.keySet());
		java.util.Collections.sort(values);

		Integer numberSum = 0;
		for(Double value : values) {
			Integer number = distribution.get(value);
			
			numberSum += number;
			
			fw.write(numberFormater.format(value) +SEPARATOR_OUT +numberFormater.format(number) +SEPARATOR_OUT);
			fw.write(numberFormater.format(numberSum) +SEPARATOR_OUT +numberFormater.format((Double)(numberSum.doubleValue() / numberEntries)));
			fw.write(NEW_LINE);
		}

		fw.close();
		
		System.out.println("  sum value = " +numberSum +" (expected=" +numberEntries +")");
		
		if(numberSum != numberEntries) {
			System.err.println("Sum of values and number of entries does not match.");
		}
	}
	
}
