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
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

import com.csvreader.CsvReader;

import de.tuilmenau.ics.filterToolsCVS.helper.Averaging;


public class Filter
{
	private static final String DIR_FILTER_FILE_ENDING   = ".csv";
	private static final String DIR_FILTER_FILE_CONTAINS = "EndStatisticWriter";
	
	private static final char SEPARATOR = ';';
	private static final String SEPARATOR_OUT = SEPARATOR +" ";
	
	private static final String NEW_LINE = "\n";
	
	private static final String FILENAME_SUMMARY = "summary.csv";
	private static final String FILENAME_POSTFIX = "_filtered.csv";
	
	private static final String INCLUDE = "-i";
	

	/**
	 * Example parameters:
	 *   "fog\directory\workspace" .AheadOfTime. .DeliveredPackets. .QueueLength.  .PacketsInFlight. .Retransmissions. .RoundTripTime. .DoStepDuration. .DoStepPacketCounter. .vertices .edges .size -i ".Global Routing Service."
	 *   "fog\directory\workspace" de.tuilmenau.ics.fog.topology.Node.  de.tuilmenau.ics.fog.routing.simulated.PartialRoutingService. -i ".FoG Routing Service." -i .requests -i .instances -i .route.
	 */
	public static void main(String[] args)
	{
		if(args.length > 1) {
			String path = args[0];
			File dir = new File(path);
	
			// check directory name
			String[] files = dir.list();
			if(files != null) {
				System.out.println(files.length +" files in directory " +path);
			} else {
				int lastSep = path.lastIndexOf("\\");
				if(lastSep < 0) lastSep = path.lastIndexOf("/");
				
				String filename;
				if(lastSep >= 0) {
					// split filename from directory
					filename = path.substring(lastSep);
					filename = filename.replace("\\", "");
					filename = filename.replace("/", "");
					
					path = path.replaceFirst(filename, "");
				} else {
					// no directory specified
					filename = path;
					path = "";
				}
				
				System.out.println("Specified name seems to be not a directory. Using it as directory '" +path +"' and file '" +filename +"'");
				files = new String[] { filename };
			}

			System.out.println("Separator is '" +SEPARATOR +"'");
			
			// copy filters
			LinkedList<String> filters = new LinkedList<String>();
			LinkedList<String> includes = new LinkedList<String>();
			for(int i=1; i<args.length; i++) {
				if(INCLUDE.equals(args[i])) {
					if(args.length > i +1) {
						i++;
						includes.addLast(args[i]);
					} else {
						System.err.println("Invalid input argument " +args[i] +" since it is the last entry.");
					}
				} else {
					filters.addLast(args[i]);
				}
			}
	
			try {
				for(String file : files) {
					if(file.endsWith(DIR_FILTER_FILE_ENDING) && file.contains(DIR_FILTER_FILE_CONTAINS)) {
						// ignore filter output files
						if(!file.endsWith(FILENAME_POSTFIX) && !file.equalsIgnoreCase(FILENAME_SUMMARY)) {
							filterCSV(path +"\\" +file, filters, includes);
						}
					}
				}
				
				writeAllData(path +"\\" +"summary.csv");
			}
			catch(Exception exc) {
				exc.printStackTrace();
			}
		} else {
			System.err.println("Missing paramters; specifiy directory and filters");
			System.err.println("usage: <directory|filename> filterTerm1 filterTerm2 ... -i includeTerm1 ...");
		}
	}
	
	private static void filterCSV(String filename, LinkedList<String> filters, LinkedList<String> includes) throws IOException
	{
		FileReader fr = new FileReader(filename);
		CsvReader csvIn = new CsvReader(fr, SEPARATOR);
		
		csvIn.setSafetySwitch(false);
		if(csvIn.readHeaders()) {
			csvIn.readRecord();
			System.out.println("'" +filename +"' has " +csvIn.getColumnCount() +" column.");
	
			int usedColumn = 0;
			String[] headers = csvIn.getHeaders();
			StringBuffer newHeader = new StringBuffer();
			StringBuffer newValues = new StringBuffer();

			HashMap<String,String> data = new HashMap<String, String>();
			allData.put(filename, data);
			
			for(String header : headers) {
				boolean matches = false;
				
				// check if a filter matches the entry
				for(String filter : filters) {
					if(header.contains(filter)) {
						matches = true;
						
						// ok, filter matches, but maybe it is on the include list?
						for(String include : includes) {
							if(header.contains(include)) {
								matches = false;
								break;
							}
						}
						
						break;
					}
				}
				
				if(!matches) {
					usedColumn++;
					String value = csvIn.get(header);
					
					newHeader.append(header);
					newHeader.append(SEPARATOR_OUT);
					newValues.append(value);
					newValues.append(SEPARATOR_OUT);
					
					if(data != null) {
						if(!keys.containsKey(header)) {
							keys.put(header, true);
						}
						
						data.put(header, value);
					}
				}
			}
			System.out.println(" -> " +usedColumn +" column remains");
			
			FileWriter fw = new FileWriter(filename +FILENAME_POSTFIX, false);			
			fw.write(newHeader.toString());
			fw.write(NEW_LINE);
			fw.write(newValues.toString());
			fw.close();
		} else {
			System.err.println("Can not read header from '" +filename +"'");
		}
	}
	
	private static void writeAllData(String filename) throws IOException
	{
		FileWriter fw = new FileWriter(filename, false);
		HashMap<String, Averaging> statistic = new HashMap<String, Averaging>();  
		
		//
		// Write header to file
		//
		Object[] headers = keys.keySet().toArray();
		java.util.Arrays.sort(headers);
		
		StringBuffer headersStr = new StringBuffer();
		headersStr.append("file name");
		headersStr.append(SEPARATOR_OUT);
		
		for(Object header : headers) {
			headersStr.append(header.toString());
			headersStr.append(SEPARATOR_OUT);
			
			statistic.put(header.toString(), new Averaging());
		}
		
		fw.write(headersStr.toString());
		fw.write(NEW_LINE);
		
		//
		// Write data to file
		//
		System.out.println("Write statistic to '" +filename +"' with " +headers.length +" column");
		
		Object[] filenames = allData.keySet().toArray();
		java.util.Arrays.sort(filenames);
		
		for(Object file : filenames) {
			HashMap<String, String> data = allData.get(file);
			
			// write filename to file
			StringBuffer dataStr = new StringBuffer();	
			dataStr.append(file);
			dataStr.append(SEPARATOR_OUT);
			
			// write all values to file
			for(Object header : headers) {
				String value = data.get(header);
				
				if(value != null) {
					dataStr.append(value);
				}
				statistic.get(header.toString()).add(value);
				dataStr.append(SEPARATOR_OUT);
			}
			
			fw.write(dataStr.toString());
			fw.write(NEW_LINE);
		}

		// write statistic
		StringBuffer dataStr = new StringBuffer();	
		dataStr.append("Average");
		dataStr.append(SEPARATOR_OUT);

		for(Object header : headers) {
			String value = statistic.get(header).toString();
			
			dataStr.append(value);
			dataStr.append(SEPARATOR_OUT);
		}
		
		fw.write(dataStr.toString());
		fw.write(NEW_LINE);
		
		//
		// Close file
		//
		fw.close();
	}
	
	private static HashMap<String, Boolean> keys = new HashMap<String, Boolean>();
	private static HashMap<String, HashMap<String, String>> allData = new HashMap<String, HashMap<String,String>>();
}
