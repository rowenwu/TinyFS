package com.client;

import java.io.File;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ClientFS extends Client{

	public enum FSReturnVals {
		DirExists, // Returned by CreateDir when directory exists 0
		DirNotEmpty, //Returned when a non-empty directory is deleted 1
		SrcDirNotExistent, // Returned when source directory does not exist 2 
		DestDirExists, // Returned when a destination directory exists 3
		FileExists, // Returned when a file exists 4
		FileDoesNotExist, // Returns when a file does not exist 5
		BadHandle, // Returned when the handle for an open file is not valid 6 
		RecordTooLong, // Returned when a record size is larger than chunk size 7 
		BadRecID, // The specified RID is not valid, used by DeleteRecord 8 
		RecDoesNotExist, // The specified record does not exist, used by DeleteRecord 9
		NotImplemented, // Specific to CSCI 485 and its unit tests 10 
		Success, //Returned when a method succeeds 11
		Fail //Returned when a method fails 12
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
		
		WriteOutput.writeBytes(src);
		WriteOutput.writeBytes(dirname);
		WriteOutput.writeInt(Master.CreateDirCMD);
		WriteOutput.flush();
		 
		int result = Client.ReadIntFromInputStream("Client", ReadInput);
		
		//if src directory does not exist, return SrcDirNotExistent	
		if(result == Master.SrcDirNotExistent){
			return FSReturnVals.SrcDirNotExistent;
		}
		
		//if the specified dirname already exists, return DestDirExists
		if(result == Master.DestDirExists){
			return FSReturnVals.DestDirExists;
		}

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
		
		WriteOutput.writeBytes(src);
		WriteOutput.writeBytes(dirname);
		WriteOutput.writeInt(Master.DeleteDirCMD);
		WriteOutput.flush();
		
		int result = Client.ReadIntFromInputStream("Client", ReadInput);
		
		//if src directory does not exist, return SrcDirNotExistent
		if(result == Master.SrcDirNotExistent){
			return FSReturnVals.SrcDirNotExistent;
		}
		
		//if the dirname exists, return DestDirExists
		if(result == Master.DestDirExists){
			return FSReturnVals.DestDirExists;
		}

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
			
		WriteOutput.writeBytes(src);
		WriteOutput.writeBytes(NewName);
		WriteOutput.writeInt(Master.RenameDirCMD);
		WriteOutput.flush();
		
		int result = Client.ReadIntFromInputStream("Client", ReadInput);
		
		//if src does not exist, return srcdirnotexistent
		if(result == Master.SrcDirNotExistent){
			return FSReturnVals.SrcDirNotExistent;
		}
		
		//if other directory with newname already exists, return destdirexists
		if(result == Master.DestDirExists){
			return FSReturnVals.DestDirExists;
		} 

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
		
		WriteOutput.writeBytes(tgt);
		WriteOutput.writeInt(Master.ListDirCMD);
		WriteOutput.flush();
		
		int result = Client.ReadIntFromInputStream("Client", ReadInput);
		
		//if directory doesn't exist
		if(result == Master.SrcDirNotExistent){
			return FSReturnVals.SrcDirNotExistent;
		}
		
		//if directory is empty
		if(result == Master.SrcDirIsEmpty){
			return null;
		}	
		 
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
