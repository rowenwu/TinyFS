package com.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ChunkServerPointer {
	public Socket mySocket;
	public DataOutputStream csDos;
	public DataInputStream csDin;
	public boolean isConnected;
	public static final String intToCharValues = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
	public static char intToChar(int i){
		return intToCharValues.charAt(i);
	}
	static final int firstUpperCase = 'A';
	static final int lastUpperCase = 'Z';
	static final int firstLowerCase = 'a';
	static final int lastLowerCase = 'z';
	public static int charToInt(char c){
		int intVal = (int) c;
		if (intVal < firstUpperCase) return -1;
		if (intVal < lastUpperCase+ 1) return intVal-firstUpperCase;
		if ((intVal > lastUpperCase) && (intVal < firstLowerCase)) return -1;
		if (intVal < lastLowerCase + 1 ) return intVal-firstLowerCase+26;
		return -1;
	}

	public ChunkServerPointer(String csHostName,int csPort){
		isConnected = false;
		try{
			mySocket = new Socket(csHostName, csPort);
			csDos = new DataOutputStream(mySocket.getOutputStream());
			csDin = new DataInputStream(mySocket.getInputStream());
			isConnected = true;
		} catch (IOException e) {
			System.out.println("Could not connect to a chunk server at "+csHostName);
			e.printStackTrace();
		} 
	}
}