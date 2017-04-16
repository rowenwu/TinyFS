package com.client;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Vector;

import com.chunkserver.ChunkServer;
import com.interfaces.ClientInterface;
import com.master.Master;

/**
 * implementation of interfaces at the client side
 * @author Shahram Ghandeharizadeh
 *
 */
public class Client implements ClientInterface {
	//client chunkserver
	private static int csPort = 2222;
	private static String csHostName = "localhost";
	static Socket clientCSConn;
	protected DataOutputStream csDos;
	protected DataInputStream csDin;
	
	//client master connections
	static Socket clientMasterConn;
	private int masterPort = 9999;
	private static String masterHostName = "localhost";
	protected DataInputStream masterDin;
	protected DataOutputStream masterDos;
	
	public Client(){
		if ((clientCSConn != null) && (clientMasterConn != null)) return;
		try {
			clientMasterConn = new Socket(masterHostName, masterPort);
			masterDos = new DataOutputStream(clientMasterConn.getOutputStream());
			masterDin = new DataInputStream(clientMasterConn.getInputStream());
			System.out.println(masterDin.readUTF());
			clientCSConn = new Socket(csHostName, csPort);
			csDos = new DataOutputStream(clientCSConn.getOutputStream());
			csDin = new DataInputStream(clientCSConn.getInputStream());
			
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	
	/**
	 * send the create command
	 * read and return chunk handle
	 */
	public String createChunk() {
		try {
			csDos.writeInt(ChunkServer.CreateChunkCMD);
			csDos.flush();
			String chunkHandle = csDin.readUTF();
			return new String(chunkHandle);
		} catch (IOException e) {
			e.printStackTrace();
		} 
		return null;
	}

	/**
	 * send the write command, chunk handle, payload size, and payload to the server 
	 * read and return the boolean sent back
	 */
	public boolean writeChunk(String ChunkHandle, byte[] payload, int offset) {
		if(offset + payload.length > ChunkServer.ChunkSize) {
			System.out.println("The chunk write should be within the range of the file, invalid chunk write!");
			return false;
		}
		try {
			csDos.writeInt(ChunkServer.WriteChunkCMD);
			csDos.writeUTF(ChunkHandle);
			csDos.writeInt(payload.length);
			csDos.write(payload);
			csDos.writeInt(offset);
			csDos.flush();
			return csDin.readBoolean();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		return false;
	}

	/*
	 * send the read command, chunk handle, offset, and number of bytes to the server
	 * read the response into a byte array
	 */
	public byte[] readChunk(String ChunkHandle, int offset, int NumberOfBytes) {
		if(NumberOfBytes + offset > ChunkServer.ChunkSize) {
			System.out.println("The chunk read should be within the range of the file, invalid chunk read!");
			return null;
		}
		try {
			csDos.writeInt(ChunkServer.ReadChunkCMD);
			csDos.writeUTF(ChunkHandle);
			csDos.writeInt(offset); 
			csDos.writeInt(NumberOfBytes); 
			csDos.flush();
//			System.out.println("Sent read command: " + ChunkHandle + " " + offset + " " + NumberOfBytes); 
			byte[] byteArr = new byte[NumberOfBytes];
			csDin.readFully(byteArr);
			return byteArr;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return null;
	}
	
	// Sends command to master to get list of chunk handles
	// returns array of chunk handle strings
	// returns null if file has no chunk handles or file doesn't exist
	public String[] getChunkHandles(String filePath){
		String[] handles = null;
		try {
			masterDos.writeInt(Master.GetChunkHandlesCMD);
			masterDos.writeUTF(filePath);
			masterDos.flush();
			int numHandles = masterDin.readInt();
			if(numHandles == 0)
				return null;
			handles = new String[numHandles];
			for(int a = 0; a < handles.length; a++) 
				handles[a] = masterDin.readUTF();
		} catch (IOException e) {
			System.out.println("Error getting chunk handles from master, file : " + filePath);
			e.printStackTrace();
		}
		return handles;
	}

	// Sends command to master to get number of chunk records
	// returns -1 if 
	public int getNumChunkRecords(String chunkHandle){
		try {
			masterDos.writeInt(Master.GetNumChunkRecsCMD);
			masterDos.writeUTF(chunkHandle);
			masterDos.flush();
			return masterDin.readInt();
		} catch (IOException e) {
			System.out.println("Error getting chunk handles from master, file : " + chunkHandle);
			e.printStackTrace();
		}
		return -1;
	}

}
