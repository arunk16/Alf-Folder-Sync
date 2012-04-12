package com.ukpn.cdlffe.jobs;

/**
 * Value object carries details about the source and destination folders for the content
 * to be copied. 
 * 
 * @author arun
 *
 */
public class CDLFFEJobVO {
	
	private String sourceFolder;
	
	private String destinationFolder;

	public CDLFFEJobVO(String sourceFolder, String destinationFolder) {
		super();
		this.sourceFolder = sourceFolder;
		this.destinationFolder = destinationFolder;
	}

	public String getSourceFolder() {
		return sourceFolder;
	}

	public void setSourceFolder(String sourceFolder) {
		this.sourceFolder = sourceFolder;
	}

	public String getDestinationFolder() {
		return destinationFolder;
	}

	public void setDestinationFolder(String destinationFolder) {
		this.destinationFolder = destinationFolder;
	}

	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object arg0) {
		boolean result = false;
		if(! (arg0 instanceof CDLFFEJobVO))
			result = false;
		
		CDLFFEJobVO obj = (CDLFFEJobVO)arg0;
		
		if(obj.getSourceFolder() == null || obj.getDestinationFolder() == null)
			result = false;
		if(this.sourceFolder.equals(obj.sourceFolder) && this.destinationFolder.equals(obj.getDestinationFolder()))
			result = true;
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer output = new StringBuffer();
		output.append("Source Folder : " + this.sourceFolder);
		output.append(" -----> ");
		output.append("Destination Folder : " + this.destinationFolder);
		return output.toString();
	}
	
	

}
