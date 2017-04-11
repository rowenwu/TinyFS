package com.client;

public class ClientFS {

	public enum FSReturnVals {
		DirExists, // Returned by CreateDir when directory exists
		DirNotEmpty, //Returned when a non-empty directory is deleted
		SrcDirNotExistent, // Returned when source directory does not exist
		DestDirExists, // Returned when a destination directory exists
		FileExists, // Returned when a file exists
		FileDoesNotExist, // Returns when a file does not exist
		BadHandle, // Returned when the handle for an open file is not valid
		RecordTooLong, // Returned when a record size is larger than chunk size
		BadRecID, // The specified RID is not valid, used by DeleteRecord
		RecDoesNotExist, // The specified record does not exist, used by DeleteRecord
		NotImplemented, // Specific to CSCI 485 and its unit tests
		Success, //Returned when a method succeeds
		Fail //Returned when a method fails
	}

	/**
	 * Creates the specified dirname in the src directory Returns
	 * SrcDirNotExistent if the src directory does not exist Returns
	 * DestDirExists if the specified dirname exists
	 *
	 * Example usage: CreateDir("/", "Shahram"), CreateDir("/Shahram/",
	 * "CSCI485"), CreateDir("/Shahram/CSCI485/", "Lecture1")
	 */
	public FSReturnVals CreateDir(String src, String dirname) {
		/*
		//if src directory does not exist, return SrcDirNotExistent
		if(ListDir(src)==SrcDirNotExistent){
			return SrcDirNotExistent;
		}
		
		//if the specified dirname already exists, return DestDirExists
		if(ListDir(src+dirname) != SrcDirNotExistent){
			return DestDirExists;
		}
	
		//create the directory
		Files.createDirectories(Paths.get(src+dirname));
		
		//update master namespaces
		 
		//update the log
		 */
		return null;
	}

	/**
	 * Deletes the specified dirname in the src directory Returns
	 * SrcDirNotExistent if the src directory does not exist Returns
	 * DestDirExists if the specified dirname exists
	 *
	 * Example usage: DeleteDir("/Shahram/CSCI485/", "Lecture1")
	 */
	public FSReturnVals DeleteDir(String src, String dirname) {
		/*
		//if src directory does not exist, return SrcDirNotExistent
		if(ListDir(src)==SrcDirNotExistent){
			return SrcDirNotExistent;
		}
		
		//if the dirname exists, return DestDirExists
		String[] DestDirContents= ListDir(src+dirname);
		if(DestDirContents != SrcDirNotExistent){
			//look up which chunkserver is in charge of each chunk in each file in the folder
			//ask each chunkserver to delete the chunks
			 
			//delete all files from the file of namespaces
		 
			//delete directory from the file of namespaces
				 
			//delete files
			File file = null;
			for(int i=0; i<DestDirContents.size(); i++){
				file = new File(DestDirContents[i]);
				file.delete();
			}
		    
		    	//delete directory
		    	file= new File(src+dirname);

			//update the log
			
			return DestDirExists;
		}
		 */
		return null;
	}

	/**
	 * Renames the specified src directory in the specified path to NewName
	 * Returns SrcDirNotExistent if the src directory does not exist Returns
	 * DestDirExists if a directory with NewName exists in the specified path
	 *
	 * Example usage: RenameDir("/Shahram/CSCI485", "/Shahram/CSCI550") changes
	 * "/Shahram/CSCI485" to "/Shahram/CSCI550"
	 */
	public FSReturnVals RenameDir(String src, String NewName) {
		/*		
		//if src does not exist, return srcdirnotexistent
		if(ListDir(src)==SrcDirNotExistent){
			return SrcDirNotExistent;
		}
		
		//if other directory with newname already exists, return destdirexists
		if(!ListDir(NewName)==SrcDirnotExistent){
			return DestDirExists;
		} 

		//update master namespaces
	
		//rename directory
		File NewDir= new File(NewName);
		File OldDir = new File(src);
		OldDir.renameTo(NewDir);
		
		//ask master to update the log

		*/
		return null;
	}

	/**
	 * Lists the content of the target directory Returns SrcDirNotExistent if
	 * the target directory does not exist Returns null if the target directory
	 * is empty
	 *
	 * Example usage: ListDir("/Shahram/CSCI485")
	 */
	public String[] ListDir(String tgt) {
		/*
		//get the target folder
		File Directory = new File(tgt);
		
		//if directory doesn't exist
		if(!Directory.exists()){
			return SrcDirNotExistent;
		}
		
		//if directory is empty
		String[] Contents = Directory.list();
		if(Contents.size()==0){
			return null;
		}	
		
		//if directory contains files/directories
		 return Contents;
		 */
		return null;
	}

	/**
	 * Creates the specified filename in the target directory Returns
	 * SrcDirNotExistent if the target directory does not exist Returns
	 * FileExists if the specified filename exists in the specified directory
	 *
	 * Example usage: Createfile("/Shahram/CSCI485/Lecture1/", "Intro.pptx")
	 */
	public FSReturnVals CreateFile(String tgtdir, String filename) {
		return null;
	}

	/**
	 * Deletes the specified filename from the tgtdir Returns SrcDirNotExistent
	 * if the target directory does not exist Returns FileDoesNotExist if the
	 * specified filename is not-existent
	 *
	 * Example usage: DeleteFile("/Shahram/CSCI485/Lecture1/", "Intro.pptx")
	 */
	public FSReturnVals DeleteFile(String tgtdir, String filename) {
		return null;
	}

	/**
	 * Opens the file specified by the FilePath and populates the FileHandle
	 * Returns FileDoesNotExist if the specified filename by FilePath is
	 * not-existent
	 *
	 * Example usage: OpenFile("/Shahram/CSCI485/Lecture1/Intro.pptx", FH1)
	 */
	public FSReturnVals OpenFile(String FilePath, FileHandle ofh) {
		return null;
	}

	/**
	 * Closes the specified file handle Returns BadHandle if ofh is invalid
	 *
	 * Example usage: CloseFile(FH1)
	 */
	public FSReturnVals CloseFile(FileHandle ofh) {
		return null;
	}

}
