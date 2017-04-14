package com.master;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;


import com.client.Client;
import com.client.ClientFS.FSReturnVals;


public class Master {
	private static int port = 9999;
	public Hashtable<String, Vector<String>> filesToChunks;
	public static Hashtable<String, Integer> numChunkRecords;
	private static int chunkNum; 
	private static final String sourcePath = "source";	
	
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
		numChunkRecords = new Hashtable<String, Integer>();
		if (!Files.exists(Paths.get(sourcePath)) || !Files.isDirectory(Paths.get(sourcePath))) {	
			chunkNum = 0;
			try {
				Files.createDirectories(Paths.get(sourcePath));
			} catch (IOException e) {
				e.printStackTrace();
			}
			filesToChunks = new Hashtable<String, Vector<String>>();
		} 
		else {
			filesToChunks = mapFilesToChunks("");
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
	
	// recursively enter each directory and each file where chunkhandles are stored
	private Hashtable<String, Vector<String>> mapFilesToChunks(String directory){
		File Directory = new File("source" + directory);
		String [] contents = Directory.list();
		Hashtable<String, Vector<String>> concat = new Hashtable<String, Vector<String>>();
		for(int a = 0; a < contents.length; a++){
			if(Files.isDirectory(Paths.get("source" + directory + "/" + contents[a]))){
				concat.putAll(mapFilesToChunks(directory + "/" + contents[a]));
			} 
			else {
				// read file, where each line contains a chunkhandle and the number of records in the chunk
				RandomAccessFile file;
				Vector<String> chunkHandles = new Vector<String>();
				try {
					file = new RandomAccessFile("source" + directory + "/" + contents[a], "rws");
					String line;
					while((line = file.readLine()) != null){
						StringTokenizer st = new StringTokenizer(line);
						String handle = st.nextToken();
						chunkHandles.add(handle);
						Integer i = new Integer(st.nextToken());
						numChunkRecords.put(handle, i);
						// set max chunk number
						int num = Integer.parseInt(handle.substring(1, handle.length()));
						if(num > chunkNum) chunkNum = num++;
					}
					concat.put(directory + "/" + contents[a], chunkHandles);
					file.close();
				} catch (IOException e) {
					System.out.println("Error reading file: " + directory + "/" + contents[a]);
					e.printStackTrace();
				}
			}
		}
		return concat;
	}
	
	/**
	 * Creates the specified dirname in the src directory 
	 * Returns SrcDirNotExistent if the src directory does not exist - 2
	 * Returns DirExists if directory already exists - 0
	 * Returns Success if creation succeeds - 11
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
	 */
	public ArrayList<String> ListDir(String tgt) {
		//get the target folder
		File Directory = new File("source" + tgt);
		String [] contents = Directory.list();
		ArrayList<String> concat = new ArrayList<String>();
		for(int a = 0; a < contents.length; a++){
			if(Files.isDirectory(Paths.get("source" + tgt + "/" + contents[a]))){
				concat.addAll(ListDir(tgt + "/" + contents[a]));
			}
			concat.add(tgt + "/" + contents[a]);
		}
		return concat;
	}
	
	/**
	 * Creates the specified filename in the target directory 
	 * Returns SrcDirNotExistent if the target directory does not exist - 2
	 * Returns FileExists if file with name already exists - 4
	 * Returns success if creation succeeds - 11
	 */
	public int CreateFile(String tgtdir, String filename){
		if(!DirExists(sourcePath + tgtdir)) return 2;

		File file = new File("source" + tgtdir + filename);

	    try {
			if (file.createNewFile()){
				filesToChunks.put(tgtdir+filename, new Vector<String>());
			    return 11;
			}
		} catch (IOException e) {
			System.out.println("Error creating file");
			e.printStackTrace();
		}
		return 4;
	}
	
	public boolean DirExists(String name){
		return Files.exists(Paths.get(name)) && Files.isDirectory(Paths.get(name)); 	
	}
	
	// change the number of records in the hash table and the persistent file
	public void setNumChunkRecords(String filePath, String chunkHandle, int numRecords){
		RandomAccessFile file;
		try {
			file = new RandomAccessFile("source" + filePath, "rws");
			String line;
			while((line = file.readLine()) != null){
				numChunkRecords.put(filePath, numRecords);
				StringTokenizer st = new StringTokenizer(line);
				if(st.nextToken().equals(chunkHandle)){
					file.seek(file.getFilePointer()-5);
					file.write(formatNumRecs(numRecords).getBytes());
					break;
				}
			}
			file.close();
		} catch (IOException e) {
			System.out.println("failed to change the number of chunks for file, chunk: " + filePath + ", " + chunkHandle);
			e.printStackTrace();
		} 	
	}
	
	public int getNumChunkRecords(String chunkHandle){
		return numChunkRecords.get(chunkHandle);
	}
	
	// logs a chunk in the file with 0000 records
	// returns chunk handle
	public String createChunk(String filePath){
		String chunkHandle = "";
		if(chunkNum%3 == 0) chunkHandle += "A";
		else if(chunkNum%3 == 1) chunkHandle += "B";
		else chunkHandle = "C";
		chunkHandle += formatChunkNum();
		chunkNum++;
		File f = new File("source" + filePath);
	    long fileLength = f.length();
	    RandomAccessFile raf;
		try {
			raf = new RandomAccessFile(f, "rws");
		    raf.seek(fileLength);
		    raf.write(new String(chunkHandle + " 0000\n").getBytes());
		    raf.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//modify filesToChunks hashtable
		Vector<String> chunks = getChunkHandles(filePath);
		chunks.add(chunkHandle);
		filesToChunks.put(filePath, chunks);
		numChunkRecords.put(chunkHandle, 0);
		return chunkHandle;
	}
	
	private static String formatChunkNum(){
		DecimalFormat myFormatter = new DecimalFormat("000000");
		
		return myFormatter.format(chunkNum);
	}
	
	private static String formatNumRecs(int numRecords){
		DecimalFormat myFormatter = new DecimalFormat("0000");
		return myFormatter.format(numRecords);
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

	public Vector<String> getChunkHandles(String file) {
		// TODO Auto-generated method stub
		return filesToChunks.get(file);
	}
	
}
