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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Vector;


import com.client.Client;
import com.client.ClientFS.FSReturnVals;



public class Master {
	private static int port = 9999;
	Hashtable<String, Vector<String>> filesToChunks;
	private static long counter; 
	private static String sourcePath;	
	
	public static void main(String[] args){
		new Master();
	}
	
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
		// ON SETUP, SET COUNTER TO MAX CHUNK VALUE + 1
		counter = 0;
		// Set up source folder so that the file directory is independent of other preexisting folders 
		sourcePath = System.getProperty("user.dir");
		sourcePath += "\\source";
		if (!Files.exists(Paths.get(sourcePath)) || !Files.isDirectory(Paths.get(sourcePath))) {			
			try {
				Files.createDirectories(Paths.get(sourcePath));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		// SINGLE CLIENT CONNECTION - MUST BE MODIFIED LATER FOR MULTITHREADING
		ServerSocket commChannel = null;
		try{
			commChannel = new ServerSocket(port);
			Socket clientConn = commChannel.accept();
			DataOutputStream dos = new DataOutputStream(clientConn.getOutputStream());
			DataInputStream din = new DataInputStream(clientConn.getInputStream());
			while(true){
				int CMD = din.readInt();
//				System.out.println(CMD);
				switch (CMD){
				case CreateDirCMD:
					dos.writeInt(CreateDir(din.readUTF(), din.readUTF()));
					dos.flush();
					break;
				case DeleteDirCMD:
					dos.writeInt(DeleteDir(din.readUTF()));
					dos.flush();
					break;
				case RenameDirCMD:
					dos.writeInt(RenameDir(din.readUTF(), din.readUTF()));
					dos.flush();
					break;
				case ListDirCMD:
					String target = din.readUTF();
					if(!DirExists(sourcePath + target)){
						dos.writeInt(-1);
					}
					ArrayList<String> files = ListDir(target);
					// send -1 to client if null
					if(files == null) 
						dos.writeInt(-1);
					else {
						dos.writeInt(files.size());
						for(int i = 0; i < files.size(); i++){
							dos.writeUTF(files.get(i));
						}
					}
					dos.flush();
					break;
				default:
					System.out.println("Error in Master, specified CMD "+CMD+" is not recognized.");
					break;	
				}
			}
		} catch (IOException e) {
			System.out.println("Client connection closed");
//			e.printStackTrace();
		} finally {
			try {
				commChannel.close();
			} catch (IOException e) {
				System.out.println("Error closing server socket");
				e.printStackTrace();
			}
		}
		
//		
//		// listen for incoming client connections
//		try {
//			commChannel = new ServerSocket(port);
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
	 * Creates the specified dirname in the src directory 
	 * Returns SrcDirNotExistent if the src directory does not exist - 2
	 * Returns DirExists if directory already exists - 0
	 * Returns Success if creation succeeds - 11
	 * 
	 * Example usage: CreateDir("/", "Shahram"), CreateDir("/Shahram/",
	 * "CSCI485"), CreateDir("/Shahram/CSCI485/", "Lecture1")
	 */
	public int CreateDir(String src, String dirname) throws IOException {
		if(!DirExists(sourcePath + src)) return 2;
		if(DirExists(sourcePath + src + dirname)) return 0;
	
		//create the directory, return 11 - Success
		Files.createDirectories(Paths.get(sourcePath + src + dirname));	 
		return 11;
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
	public int DeleteDir(String dirname) {
		if(!DirExists(sourcePath + dirname)) return 2;
		
		// check if it's empty
		File dir = new File(sourcePath + dirname);
		String[] files = dir.list();
		if(files.length != 0) return 1;
				 
		dir.delete();
		return 3;
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
	public int RenameDir(String src, String NewName) {
		if(!DirExists(sourcePath + src)) return 2;
		if(DirExists(sourcePath + src + NewName)) return 3;

		//rename directory
		File NewDir= new File(sourcePath + NewName);
		File OldDir = new File(sourcePath + src);
		OldDir.renameTo(NewDir);		

		return 11;
	}

	/**
	 * Lists the content of the target directory 
	 * Returns a String array of the names of contents
	 * Returns null if the target directory is empty or directory doesn't exist
	 * 
	 * Example usage: ListDir("/Shahram/CSCI485")
	 */
	public ArrayList<String> ListDir(String tgt) {
		//get the target folder
		File Directory = new File("source/" + tgt);
		String [] contents = Directory.list();
		ArrayList<String> concat = new ArrayList<String>();
		for(int a = 0; a < contents.length; a++){
			if(Files.isDirectory(Paths.get("source/" + tgt + "/" + contents[a]))){
				concat.addAll(ListDir(tgt + "/" + contents[a]));
			}
			concat.add(tgt + "/" + contents[a]);
		}
		return concat;
	}
	
	public boolean DirExists(String name){
		return Files.exists(Paths.get(name)) && Files.isDirectory(Paths.get(name)); 	
	}
	
	//CLEANUP SOURCE DIRECTORY BEFORE UNIT TESTS
	public static boolean UnitTestCLeanUp(File directory) {
	    if(directory.exists()){
	        File[] files = directory.listFiles();
	        if(null!=files){
	            for(int i=0; i<files.length; i++) {
	                if(files[i].isDirectory()) {
	                	UnitTestCLeanUp(files[i]);
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
