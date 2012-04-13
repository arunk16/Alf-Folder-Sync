package com.ukpn.cdlffe.util;

public class XPathUtils {
	
	public static String getWhiteSpaceReplacedForXPathQuery(String path){
		path = path.replaceAll(" ", Cons.XPATH_SPACE_REPLACEMENT_LITERAL);
		return path;
	}
	
	public static String getWhiteSpaceBackFromXPathQuery(String path){
		path = path.replaceAll(Cons.XPATH_SPACE_REPLACEMENT_LITERAL, " ");
		return path;
	}
	
	public static String generateSourceFolderXPath(String source){
		//String source = "/FFE Cabinet/LIVE - FFE Device Documents/EDF1manuals";
		source = source.replaceAll("/", "/cm:");
		source = Cons.CDL_SOURCE_FOLDER_ALF_PATH+source;
		source = getWhiteSpaceReplacedForXPathQuery(source);		
		return source;
	}
	
	public static String generateDestinationFolderXPath(String value){
		//String source = "/FFE Cabinet/LIVE - FFE Device Documents/EDF1manuals";
		String destinationPath = null;
        value = value.substring(2); //strip D:
        String[] folders = value.split("\\\\");
        destinationPath = Cons.CDL_DESTINATION_FOLDER_ALF_PATH;
        for(int i = 0; i < folders.length; i++){
        	if(!folders[i].isEmpty())
        		destinationPath = destinationPath +"/cm:" +folders[i];
        }       
		return destinationPath;
	}
	
	public static String changeFileNameExtensionToPDF(String fileName){
		String pdfFile = fileName.substring(0, fileName.indexOf("."));
		pdfFile = pdfFile+".pdf";
		return pdfFile;
	}
}
