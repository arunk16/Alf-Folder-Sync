package com.ukpn.cdlffe.jxl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jxl.Cell;
import jxl.CellType;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

import org.apache.log4j.Logger;

/**
 * Singleton class reads the excel file and understands the source and destination directory structure
 * and populate them in memory for reference from alfresco operations implemented in core package.
 * 
 * @author arun
 *
 */

public class ExcelDealer {
	
	private static Logger log = Logger.getLogger(ExcelDealer.class);
	
	private static ExcelDealer obj;
	
	private HashMap<String,List<String>> cdlFfeDirectoryMap = new  HashMap<String, List<String>>();
	
	private HashMap<String,List<String>> targetTypeMap = new  HashMap<String, List<String>>();
	
	private List<String> cdlDirectories = new ArrayList<String>(); 
	
	private List<String> sourceDir = new ArrayList<String>();
	
	private ExcelDealer(){
		
	}
	
	public static ExcelDealer getInstance() throws Exception{
		if(obj == null){
			obj = new ExcelDealer();
			obj.read(); //populate directory structures in memory
		}
		return obj;
	}
	
	public List<String> getCdlDirectories(){
		return cdlDirectories;
	}
	
	public void read() throws IOException  {
		//populate the map and lists for processing
		Workbook w;
		try {
			InputStream inputFile = this.getClass().getClassLoader().getResourceAsStream("alfresco/IDS-config.xls");
			w = Workbook.getWorkbook(inputFile);
			// Get the first sheet
			Sheet sheet = w.getSheet(0);
			// Loop over and get source directories
			// source-column = 1
				int source_column = 1;
				for (int i = 0; i < sheet.getRows(); i++) {
					Cell cell = sheet.getCell(source_column, i);					
					if (cell.getType() == CellType.LABEL) {
						log.debug("I got a source "
								+ cell.getContents());
					}
					String source = cell.getContents();
					
					//loop and get target directories(CDL)
					// target-dir colum = 2
					int target_column=2;
					
					Cell cell1 = sheet.getCell(target_column, i);
					if (cell1.getType() == CellType.LABEL) {
						log.debug("I got a target "
									+ cell1.getContents());
					}
					String target = cell1.getContents();
					
					//loop and get version type for target
					// version_colum = 3
					int version_column=3;
					
					Cell cell2 = sheet.getCell(version_column, i);
					if (cell2.getType() == CellType.LABEL) {
						log.debug("I got a version "
									+ cell2.getContents());
					}
					String version_type = cell2.getContents();
					if(!source.equalsIgnoreCase("source_dir") && 
							!target.equalsIgnoreCase("target_root_dir")&&
							!version_type.equalsIgnoreCase("version_labels")){
						if(!sourceDir.contains(source))
							sourceDir.add(source);
						
						if(!cdlDirectories.contains(target))
							cdlDirectories.add(target);
						
						if(cdlFfeDirectoryMap.containsKey(source)){
							List<String> list = cdlFfeDirectoryMap.get(source);
							if(!list.contains(target))
							list.add(target);
						} else{
							List<String> list = new ArrayList<String>();
							list.add(target);
							cdlFfeDirectoryMap.put(source,list);
						}
						
						if(targetTypeMap.get(target) != null){							
							List<String> value = targetTypeMap.get(target);
							if(!value.contains(version_type))
								value.add(version_type);
						} else{	
							List<String> value = new ArrayList<String>();
							value.add(version_type);
							targetTypeMap.put(target, value);
						}
						
					}
				}
		} catch (BiffException e) {
			e.printStackTrace();
		}
	}
	
	public HashMap<String, List<String>> getCdlFfeDirectoryMap() {
		return cdlFfeDirectoryMap;
	}

	public void setCdlFfeDirectoryMap(
			HashMap<String, List<String>> cdlFfeDirectoryMap) {
		this.cdlFfeDirectoryMap = cdlFfeDirectoryMap;
	}

	public HashMap<String, List<String>> getTargetTypeMap() {
		return targetTypeMap;
	}

	public void setTargetTypeMap(HashMap<String, List<String>> targetTypeMap) {
		this.targetTypeMap = targetTypeMap;
	}

	public List<String> getSourceDir() {
		return sourceDir;
	}

	public void setSourceDir(List<String> sourceDir) {
		this.sourceDir = sourceDir;
	}

	public void setCdlDirectories(List<String> cdlDirectories) {
		this.cdlDirectories = cdlDirectories;
	}

	public static void main(String []args) throws Exception{
		
		
		ExcelDealer.getInstance().read();
		
	}

}
