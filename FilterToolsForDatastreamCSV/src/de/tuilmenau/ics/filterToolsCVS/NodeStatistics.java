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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.csvreader.CsvReader;


public class NodeStatistics
{

	private static final char SEPARATOR = ';';
	private static final String SEPARATOR_OUT = SEPARATOR +" ";
	
	private static final String NEW_LINE = "\n";
	
	public static final String FILENAME_POSTFIX = "_node_statistics.csv";
	
	private static final String DEFAULT_NODE_PREFIX = "de.tuilmenau.ics.fog.topology.Node.";
	
	
	public static void main(String[] args)
	{
		if(args.length < 1) {
			System.err.println("usage: NodeStatistics <file name> [<prefix of nodes (default: de.tuilmenau.ics.fog.topology.Node)]");
			System.exit(1);
		}
		
		try {
			String prefix;
			if(args.length >= 2) {
				prefix = args[1];
			} else {
				prefix = DEFAULT_NODE_PREFIX;
			}
			
			extractNodeStatistic(args[0], prefix);
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	private static void extractNodeStatistic(String filename, String nodePrefix) throws Exception
	{
		FileReader fr = new FileReader(filename);
		CsvReader csvIn = new CsvReader(fr, SEPARATOR);
		
		csvIn.setSafetySwitch(false);
		if(csvIn.readHeaders()) {
			csvIn.readRecord();
			System.out.println("'" +filename +"' has " +csvIn.getColumnCount() +" column.");
	
			int usedColumn = 0;
			String[] headers = csvIn.getHeaders();

			for(String header : headers) {
				if(header.startsWith(nodePrefix)) {
					usedColumn++;
					String value = csvIn.get(header);
					String postfix = header.replaceFirst(nodePrefix, "");
					
					String[] parts = postfix.split("\\.");
					String node = parts[0];
					String key = postfix.replace(node +".", "");
					
					System.out.println("Node: " +node +" (" +key +"=" +value +")");
					
					HashMap<String, String> nodeData = allData.get(node); 
					if(nodeData == null) {
						nodeData = new HashMap<String, String>();
						nodeData.put("node_name", node);
						allData.put(node, nodeData);
					}
					
					keys.put(key, true);
					nodeData.put(key, value);
				}
			}
			
			System.out.println(" -> " +usedColumn +" column used; " +allData.size() +" nodes found; " +keys.size() +" keys");
			
			FileWriter fw = new FileWriter(filename +FILENAME_POSTFIX, false);

			// sort header entries
			List<String> keyNames = new ArrayList<String>(keys.keySet());
			java.util.Collections.sort(keyNames);
			
			// header
			fw.write("node_name" +SEPARATOR_OUT);
			for(String key : keyNames) {
				fw.write(key +SEPARATOR_OUT);
			}
			fw.write(NEW_LINE);
			
			// sort data entries
			List<String> nodeNames = new ArrayList<String>(allData.keySet());
			java.util.Collections.sort(nodeNames);
			
			// data
			for(String nodeName : nodeNames) {
				HashMap<String, String> nodeData = allData.get(nodeName);
				fw.write(nodeData.get("node_name") +SEPARATOR_OUT);
				for(String key : keyNames) {
					String value = nodeData.get(key);
					
					if(value == null) value = "";
					fw.write(value +SEPARATOR_OUT);
				}
				
				fw.write(NEW_LINE);
			}
			fw.close();
			
			System.out.println("Output data written to " +filename +FILENAME_POSTFIX);
		} else {
			System.err.println("Can not read header from '" +filename +"'");
		}
		
	}

	private static HashMap<String, Boolean> keys = new HashMap<String, Boolean>();
	private static HashMap<String, HashMap<String, String>> allData = new HashMap<String, HashMap<String,String>>();
}
