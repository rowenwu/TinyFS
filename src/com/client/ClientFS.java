package com.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;

import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import com.master.Master;

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
	 * Creates the specified dirname in the src directory 
	 * Returns SrcDirNotExistent if the src directory does not exist - 2
	 * Returns DirExists if directory already exists - 0
	 * Returns Success if creation succeeds - 11
	 * 
	 * Example usage: CreateDir("/", "Shahram"), CreateDir("/Shahram/",
	 * "CSCI485"), CreateDir("/Shahram/CSCI485/", "Lecture1")
	 */
	public FSReturnVals CreateDir(String src, String dirname) {	
		try {
			dos.writeInt(Master.CreateDirCMD);
			dos.writeUTF(src);
			dos.writeUTF(dirname);
			dos.flush();
			return FSReturnVals.values()[din.readInt()];
		} catch (IOException e) {
			System.out.println("CreateDir failed, IO Exception");
			e.printStackTrace();
		}
		return FSReturnVals.Fail;
	}

	/**
	 * Deletes the specified dirname in the src directory
	 * Returns SrcDirNotExistent if the src directory does not exist - 2
	 * Returns DirNotEmpty if directory is not empty - 1
	 * Returns Success if deletion succeeds - 11
	 * Returns Fail if deletion fails - 12
	 *
	 * Example usage: DeleteDir("/Shahram/CSCI485/", "Lecture1")
	 */
	public FSReturnVals DeleteDir(String src, String dirname) {
		try {
			dos.writeInt(Master.DeleteDirCMD);
			dos.writeUTF(src + dirname);
			dos.flush();
			return FSReturnVals.values()[din.readInt()];
		} catch (IOException e) {
			System.out.println("DeleteDir failed, IO Exception");
			e.printStackTrace();
		}
		return FSReturnVals.Fail;
	}

	/**
	 * Renames the specified src directory in the specified path to NewName
	 * Returns SrcDirNotExistent if the src directory does not exist - 2
	 * Returns DestDirExists if a directory with NewName exists in the specified path - 3
	 * Returns Success if rename succeeds - 11
	 *
	 * Example usage: RenameDir("/Shahram/CSCI485", "/Shahram/CSCI550") changes
	 * "/Shahram/CSCI485" to "/Shahram/CSCI550"
	 */
	public FSReturnVals RenameDir(String src, String NewName) {
		try {
			dos.writeInt(Master.RenameDirCMD);
			dos.writeUTF(src);
			dos.writeUTF(NewName);
			dos.flush();
			return FSReturnVals.values()[din.readInt()];
		} catch (IOException e) {
			System.out.println("RenameDir failed, IO Exception");
			e.printStackTrace();
		}
		return FSReturnVals.Fail;
	}

	/**
	 * Lists the content of the target directory 
	 * Returns a String array of the names of contents
	 * Returns null if the target directory is empty or directory doesn't exist
	 * 
	 * Example usage: ListDir("/Shahram/CSCI485")
	 */
	public String[] ListDir(String tgt) {
		try {
			dos.writeInt(Master.ListDirCMD);
			dos.writeUTF(tgt);
			dos.flush();
			int numFiles = din.readInt();
			if (numFiles == -1)
				return null;
			String[] dirs = new String[numFiles];
			for(int i = 0; i < dirs.length; i++)
				dirs[i] = din.readUTF();
			Arrays.sort(dirs);
			return dirs;
		} catch (IOException e) {
			System.out.println("ListDir failed, IO Exception");
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Creates the specified filename in the target directory 
	 * Returns SrcDirNotExistent if the target directory does not exist - 2
	 * Returns FileExists if file with name already exists - 4
	 * Returns success if creation succeeds - 11
	 *
	 * Example usage: Createfile("/Shahram/CSCI485/Lecture1/", "Intro.pptx")
	 */
	public FSReturnVals CreateFile(String tgtdir, String filename) {
		return null;
	}

	/**
	 * Deletes the specified filename from the tgtdir 
	 * Returns SrcDirNotExistent if the target directory does not exist - 2
	 * Returns FileDoesNotExist if the specified filename is not-existent - 5
	 * Returns success if deletion succeeds - 11
	 *
	 * Example usage: DeleteFile("/Shahram/CSCI485/Lecture1/", "Intro.pptx")
	 */
	public FSReturnVals DeleteFile(String tgtdir, String filename) {
		return null;
	}

	/**
	 * Opens the file specified by the FilePath and populates the FileHandle
	 * Returns FileDoesNotExist if the specified filename by FilePath is not-existent - 5
	 * Returns Success if successfully opened - 11
	 *
	 * Example usage: OpenFile("/Shahram/CSCI485/Lecture1/Intro.pptx", FH1)
	 */
	public FSReturnVals OpenFile(String FilePath, FileHandle ofh) {
		return null;
	}

	/**
	 * Closes the specified file handle 
	 * Returns BadHandle if ofh is invalid - 6
	 * Returns Success if successfully closed - 11
	 *
	 * Example usage: CloseFile(FH1)
	 */
	public FSReturnVals CloseFile(FileHandle ofh) {
		return null;
	}

}
