package com.master;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Hashtable;
import java.util.Vector;

import com.client.Client;
import com.client.ClientFS.FSReturnVals;


public class Master {
//	private static int port = 9999;
	Hashtable<String, Vector<String>> filesToChunks;
	private static long counter; 
	private static String sourcePath;	
	
	//Commands recognized by the Master
	public static final int CreateDirCMD = 201;
	public static final int DeleteDirCMD = 202;
	public static final int RenameDirCMD = 203;
	public static final int ListDirCMD = 204;
	
	/*
	 * Sets up map of files to chunks 
	 * Starts listening for client and chunkserver connections 
	 */
	public Master(){
		counter = 0;
		// Set up source folder so that the file directory is independent of other preexisting folders 
		sourcePath = System.getProperty("user.dir");
		sourcePath += "\\source\\";
		if (!Files.exists(Paths.get(sourcePath)) || !Files.isDirectory(Paths.get(sourcePath))) {			
			try {
				Files.createDirectories(Paths.get(sourcePath));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		
//		int serverPort = 1111; //Set to 0 to cause ServerSocket to allocate the port 
//		ServerSocket commChannel = null;
//		
//		// listen for incoming client connections
//		try {
//			commChannel = new ServerSocket(serverPort);
//			System.out.println("listening...");
//			while (true) {
//				new ConnectionThread(commChannel.accept()).start();
//			}
//		} catch (IOException e) {
//			System.out.println("Error establishing client connection");
//			e.printStackTrace();
//		} finally {
//			try {
//				commChannel.close();
//			} catch (IOException e) {
//				System.out.println("Error closing server socket");
//				e.printStackTrace();
//			}
//		}
//		
		
	}
	
//	class ConnectionThread extends Thread {
//		private DataOutputStream dos;
//		private DataInputStream din;
//		private Socket socket;
//
//		public ConnectionThread(Socket socket) {
//			this.socket = socket;
//		}
//
//		public void run(){
//			try {
//				dos = new DataOutputStream(socket.getOutputStream());
//				din = new DataInputStream(socket.getInputStream());
//				
//				// continuously receive create, read, or write commands from the client
//				while(true){
//					int CMD = din.readInt();
//					switch (CMD){
//					case CreateDirCMD:
//						
//					}
//				}
//			} catch (IOException e) {
//				System.out.println("Client connection closed");
//			} finally {
//				try {
//					socket.close();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
//		}
//	}
	
	/**
	 * Creates the specified dirname in the src directory Returns 2
	 * SrcDirNotExistent if the src directory does not exist Returns 0
	 * DestDirExists if the specified dirname exists 11
	 *
	 * Example usage: CreateDir("/", "Shahram"), CreateDir("/Shahram/",
	 * "CSCI485"), CreateDir("/Shahram/CSCI485/", "Lecture1")
	 */
	public int CreateDir(String src, String dirname) throws IOException {
		if(!DirExists(src)) return 2;
		if(DirExists(sourcePath + src + dirname)) return 0;
	
		//create the directory, return 11 - Success
		Files.createDirectories(Paths.get(sourcePath + src + dirname));	 
		return 11;
	}

	/**
	 * Deletes the specified dirname in the src directory Returns 2
	 * SrcDirNotExistent if the src directory does not exist Returns 0
	 * DestDirExists if the specified dirname exists Returns 3
	 *
	 * Example usage: DeleteDir("/Shahram/CSCI485/", "Lecture1")
	 */
	public int DeleteDir(String src, String dirname) {
		if(!DirExists(src)) return 2;
		if(!DirExists(sourcePath + src + dirname)) return 0;
		
		//look up which chunkserver is in charge of each chunk in each file in the folder
		//ask each chunkserver to delete the chunks
				 
		//delete files
		if(!DeleteDirectory(new File(sourcePath + src + dirname))) 
			System.out.println("Error deleting directory");
		
		return 3;
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
	
	public boolean DirExists(String name){
		return Files.exists(Paths.get(sourcePath)) && Files.isDirectory(Paths.get(sourcePath)); 	
	}
	
	public static boolean DeleteDirectory(File directory) {
	    if(directory.exists()){
	        File[] files = directory.listFiles();
	        if(null!=files){
	            for(int i=0; i<files.length; i++) {
	                if(files[i].isDirectory()) {
	                    DeleteDirectory(files[i]);
	                }
	                else {
	                    files[i].delete();
	                }
	            }
	        }
	    }
	    return(directory.delete());
	}
}
