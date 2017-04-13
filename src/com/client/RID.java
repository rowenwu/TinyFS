package com.client;

import java.nio.ByteBuffer;

import com.chunkserver.ChunkServer;

public class RID{
	static final int chunkSize = ChunkServer.ChunkSize;
	static final int offsetDigits = Integer.toString(chunkSize).length();
	static final String offsetFormat = "%0"+offsetDigits+"d";
	char chunkServerLabel;
	String chunk;
	int offset;
	int length;
	RID prior;
	RID next;
	boolean valid;
	boolean inLinkedList;
	public RID(String chunkName,int offsetVal){
		startup(chunkName.charAt(0),chunkName.substring(1),offsetVal);
		valid = true;
	}
	public RID(char serverLabel,String chunkName,int offsetVal){
		startup(serverLabel,chunkName,offsetVal);
		valid = true;
	}
	public RID(String chunkName, byte[] b,int offset){
		this(chunkName.charAt(0),chunkName.substring(1),b,offset);
	}
	public RID(char serverLabel,String chunkName, byte[] b,int offset){
		int bIndex = bytesToInt(b,offset);
		int bLength = bytesToInt(b,offset+4);
		startup(serverLabel,chunkName,bIndex);
		length = bLength;
	}
	public RID(String RIDLine){
		String[] halves = RIDLine.split(":");
		char csLabel = halves[0].charAt(0);
		int myOffset = Integer.parseInt(halves[1]);
		String chunkName = halves[0].substring(1);
		startup(csLabel,chunkName,myOffset);
		valid = true;
	}
	private void startup(char serverLabel,String chunkName,int offsetVal){		
		offset = offsetVal;
		chunk = chunkName;
		chunkServerLabel = serverLabel;
		prior = null;
		next = null;
		inLinkedList = false;
	}
	public RID(){
		startup('!',null,-1);
		valid = false;
	}
	public String toString(){
		String offsetStr = String.format(offsetFormat,offset);
		return chunkServerLabel+chunk+":"+offsetStr;
	}
	public byte[] makeTag(){
		ByteBuffer bb = ByteBuffer.allocate(FileHandle.bytesPerIDTag);
		bb.putInt(offset);
		bb.putInt(FileHandle.bytesPerIDTag/2,length);
		return bb.array();
	}
	public static void printTag(byte[] b){
		int bIndex = bytesToInt(b);
		int bLength = bytesToInt(b,4);
		System.out.println("I:"+bIndex);
		System.out.println("L"+bLength);
	}
	public static int bytesToInt(byte[] b){ return bytesToInt(b,0); }
    public static int bytesToInt(byte[] b, int index){
        int v = (b[index] & 0xff) << 24 | (b[index+1] & 0xff) << 16 | (b[index+2] & 0xff) << 8 | (b[index+3] & 0xff);
        return v;
    }	
	public static byte[] intToBytes( final int i ) {
	    ByteBuffer bb = ByteBuffer.allocate(4); 
	    bb.putInt(i); 
	    return bb.array();
	}
	public RID getNext(){ return next;}
	public RID getPrior(){ return prior;}
}
