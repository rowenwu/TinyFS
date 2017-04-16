package com.chunkserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
//import java.util.Arrays;
import java.util.Vector;

import com.chunkserver.ChunkServer.ChunkServerThread;
import com.client.Client;
import com.interfaces.ChunkServerInterface;

/**
 * implementation of interfaces at the chunkserver side
 * @author Shahram Ghandeharizadeh
 *
 */

public class ChunkServer implements ChunkServerInterface {
	private static String filePath;	
	private static int port = 2222;
	private static ServerSocket ss;
	
	//Used for the file system
	public static long counter;
	
	public static int PayloadSZ = Integer.SIZE/Byte.SIZE;  //Number of bytes in an integer
	public static int CMDlength = Integer.SIZE/Byte.SIZE;  //Number of bytes in an integer  
	
	//Commands recognized by the Server
	public static final int CreateChunkCMD = 101;
	public static final int ReadChunkCMD = 102;
	public static final int WriteChunkCMD = 103;
	
	//Replies provided by the server
	public static final int TRUE = 1;
	public static final int FALSE = 0;
	
	/**
	 * Initialize the chunk server
	 */
	public ChunkServer(){
		// set up chunks folder
		filePath = System.getProperty("user.dir");
		filePath += "\\chunks\\";
		if (!Files.exists(Paths.get(filePath)) || !Files.isDirectory(Paths.get(filePath))) {			
			try {
				Files.createDirectories(Paths.get(filePath));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		counter = new File(filePath).listFiles().length;
		File dir = new File(filePath);
		File[] fs = dir.listFiles();

		if(fs.length == 0){
			counter = 0;
		}else{
			long[] cntrs = new long[fs.length];
			for (int j=0; j < cntrs.length; j++)
				cntrs[j] = Long.valueOf( fs[j].getName() ); 
			
			Arrays.sort(cntrs);
			counter = cntrs[cntrs.length - 1];
		}
		
		// listen for incoming client connections
		try {
			ss = new ServerSocket(port);
			System.out.println("listening...");
			while (true) {
				new ChunkServerThread(ss.accept()).start();
			}
		} catch (IOException e) {
			System.out.println("Error establishing client connection");
			e.printStackTrace();
		} finally {
			try {
				ss.close();
			} catch (IOException e) {
				System.out.println("Error closing server socket");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Each chunk is corresponding to a file.
	 * Return the chunk handle of the last chunk in the file.
	 */
	public String createChunk() {
		counter++;
		return String.valueOf(counter);
	}
	
	/**
	 * Write the byte array to the chunk at the offset
	 * The byte array size should be no greater than 4KB
	 */
	public boolean writeChunk(String ChunkHandle, byte[] payload, int offset) {
		try {
			//If the file corresponding to ChunkHandle does not exist then create it before writing into it
			RandomAccessFile raf = new RandomAccessFile(filePath + ChunkHandle, "rw");
			raf.seek(offset);
			raf.write(payload, 0, payload.length);
			raf.close();
			return true;
		} catch (IOException ex) {
			ex.printStackTrace();
			return false;
		}
	}
	
	/**
	 * read the chunk at the specific offset
	 */
	public byte[] readChunk(String ChunkHandle, int offset, int NumberOfBytes) {
		try {
			//If the file for the chunk does not exist the return null
			boolean exists = (new File(filePath + ChunkHandle)).exists();
			if (exists == false) return null;
			
			//File for the chunk exists then go ahead and read it
			byte[] data = new byte[NumberOfBytes];
			RandomAccessFile raf = new RandomAccessFile(filePath + ChunkHandle, "rw");
			raf.seek(offset);
			raf.read(data, 0, NumberOfBytes);
			raf.close();
			return data;
		} catch (IOException ex){
			ex.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Receive and send data to a single client through input/output streams 
	 */
	class ChunkServerThread extends Thread {
		private DataOutputStream dos;
		private DataInputStream din;
		private Socket socket;

		public ChunkServerThread(Socket socket) {
			this.socket = socket;
		}

		public void run(){
			try {
				dos = new DataOutputStream(socket.getOutputStream());
				din = new DataInputStream(socket.getInputStream());
				
				// continuously receive create, read, or write commands from the client
				while(true){
					int command = din.readInt();
					if(command == CreateChunkCMD) sendChunkHandle();
					else if (command == ReadChunkCMD) sendReadChunk();
					else if (command == WriteChunkCMD) receiveWriteChunk();	
				}
			} catch (IOException e) {
				System.out.println("Client connection closed");
			} finally {
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		private void sendChunkHandle() throws IOException {
			String chunkHandle = createChunk();
			dos.writeUTF(chunkHandle);
			dos.flush();
		}
		
		private void sendReadChunk() throws IOException {
			String chunkHandle = din.readUTF();
//			System.out.println("read chunk handle: " + chunkHandle);
			byte[] chunk = readChunk(chunkHandle, din.readInt(), din.readInt());
			dos.write(chunk);
			dos.flush();
		}
		
		private void receiveWriteChunk() throws IOException {
			String chunkHandle = din.readUTF();
			byte[] payload = new byte[din.readInt()];
			din.readFully(payload);
			int offset = din.readInt();
			dos.writeBoolean(writeChunk(chunkHandle, payload, offset));
			dos.flush();
		}
	}

	public static void main(String args[])
	{
		ChunkServer cs = new ChunkServer();
	}
}
