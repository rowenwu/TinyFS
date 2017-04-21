package com.client;
import java.nio.ByteBuffer;
import java.util.Vector;

import com.chunkserver.ChunkServer;
import com.client.ClientFS.FSReturnVals;
public class FileHandle {
	private String filePath;
	private Vector<String> fileChunks;
	private int chunkIndex;
	static final int chunkSize = ChunkServer.ChunkSize;
	public static final int bytesPerIDTag = 8;
	private int chunkBytesUsed;
	private int chunkNumRecords;
	private String currentChunkHandle;
	private String lastChunkHandle;
	private RID chunkFirstRID;
	private RID chunkLastRID;
	private boolean pointsToLast;
	Client client;
	public FileHandle(){
		client = new Client();
		setupLinkedList();
		currentChunkHandle = null;
		fileChunks = new Vector<String>();
	}
	public boolean openFile(String path){
		if (path == null){return false;}
		filePath = path;
		chunkIndex = 0;
		fileChunks = new Vector<String>();
		//System.out.println(fileChunks.size()+" chunks in file");
		//System.out.println("Ready to read chunk handles from path "+path);
		String[] chunkHandlesReturned = client.getChunkHandles(path);
		if (chunkHandlesReturned == null){
			return false;
		}
		//System.out.println(chunkHandlesReturned.length+" chunks were returned");
		for (int i = 0; i < chunkHandlesReturned.length; i++){
			//System.out.println("Has chunk "+chunkHandlesReturned[i]);
			fileChunks.add(chunkHandlesReturned[i]);
		}
		//fileChunks.copyInto(chunkHandlesReturned);
		
		return changeChunk(fileChunks.get(chunkIndex));
	}
	public boolean noChunk(){
		if (currentChunkHandle == null){ return true;}
		return false;
	}
	private void setupLinkedList(){
		chunkFirstRID = null;
		chunkLastRID = null;
		//fileFirstRID = null;
		//fileLastRID = null;
	}
	public String makeRecordsList(){
		RID cur = chunkFirstRID;
		String ret = "";
		while (cur != null){
			ret = ret + cur.toString() + "\n";
			cur = cur.next;
		}
		return ret.substring(0, ret.length()-1);
	}
	private void delinkLinkedList(){
		RID cur = chunkFirstRID;
		while (cur!=null){
			cur.inLinkedList = false;
			cur = cur.next;
		}
	}
	public boolean loadChunkRecordTags(){
		delinkLinkedList();
		//System.out.println("Loading "+chunkNumRecords+" records from "+currentChunkHandle);
		int byteCount = bytesPerIDTag * chunkNumRecords;
		int mdOffset = chunkSize - byteCount;
		chunkBytesUsed = 0;
		//XXX
		byte[] metadata = client.readChunk(currentChunkHandle, mdOffset, byteCount);
		if (metadata == null){
			return false;
		}
		RID before = null;
		RID r = null;
		for (int i = 0; i < chunkNumRecords; i++){
			r = new RID(currentChunkHandle,metadata,bytesPerIDTag*i);
			r.next = before;
			r.inLinkedList = true;
			if (i > 0){
				before.prior = r;
			}else{
				chunkLastRID = r;
			}
			before = r;
			chunkBytesUsed+=r.length;
			//System.out.println("\t"+r);
		}
		r.prior = null;
		chunkFirstRID = r;
		return true;
		
	}
	private FSReturnVals writeChunkIDTags(){
		ByteBuffer bb = ByteBuffer.allocate(bytesPerIDTag*chunkNumRecords);
		int halfID = bytesPerIDTag / 2;
		RID cur = chunkLastRID;
		int tagOffset = chunkSize - bytesPerIDTag * chunkNumRecords;
		for (int i = 0; i < chunkNumRecords; i++){
			//System.out.println("Write: "+cur);
			bb.putInt(bytesPerIDTag*i,cur.offset);
			bb.putInt(bytesPerIDTag*i+halfID,cur.length);
			cur = cur.prior;
		}
		byte[] idTags = bb.array();
		/**
		for (int i = 0; i * 4 < idTags.length; i++){
			System.out.println("\t"+RID.bytesToInt(idTags,i*4));
		}
		**/
		boolean writeSuccess = client.writeChunk(currentChunkHandle.substring(1), idTags, tagOffset);
		if (writeSuccess){
			return ClientFS.FSReturnVals.Success;
		}
		System.out.println("Write ID tags failed");
		return ClientFS.FSReturnVals.Fail;
	}
	public String newChunk(){
		String newChunk = client.createChunk(filePath);
		if (newChunk == null){ return null;}
		//We need to put in code that determines which ChunkServer and which one-letter prefix goes here
		//Right now it is hard-coded to "A"
		System.out.println("Client gave newChunk " + newChunk);
		currentChunkHandle = newChunk;
		lastChunkHandle = currentChunkHandle;
		chunkBytesUsed = 0;
		chunkNumRecords = 0;
		chunkFirstRID = null;
		chunkLastRID = null;
		pointsToLast = true;
		fileChunks.add(currentChunkHandle);
		System.out.println("We have "+fileChunks.size()+" chunks");
		return currentChunkHandle;
	}
	public int getRecordSize(RID r){
		return r.length;
	}
	public String getCurrentChunkHandle(){ return currentChunkHandle;}
	public int getBytesUsedLastChunk(){
		if (!pointsToLast){
			changeChunk(getLastChunkHandle());			
		}
		return getBytesUsedCurrentChunk();
	}
	public int getBytesUsedCurrentChunk(){ return chunkBytesUsed;}
	public int freeBytesLastChunk(){
		if (!pointsToLast){
			changeChunk(getLastChunkHandle());			
		}
		return freeBytesCurrentChunk();
	}
	public int freeBytesCurrentChunk(){
		return chunkSize - chunkBytesUsed + bytesPerIDTag * chunkNumRecords;
	}
	public boolean changeChunk(String c){
		if (c!= null){
			if (!c.equals(currentChunkHandle)){
				currentChunkHandle = c;
				loadNumberOfRecordsInChunk();
				loadChunkRecordTags();
			}
		}
		pointsToLast = (currentChunkHandle.equals(lastChunkHandle));
		return true;
	}
	public int getNumberOfRecordsInChunk(){
		return chunkNumRecords;
	}
	public RID getChunkFirstRID(){return chunkFirstRID;}
	public RID getChunkLastRID(){return chunkLastRID;}
	//Loads the first chunk in the file
	public boolean loadFirstChunk(){
		String firstHandle = getFirstChunkHandle();
		if (!firstHandle.equals(currentChunkHandle)){
			changeChunk(firstHandle);
		}
		return true;
	}
	public RID getFileFirstRID(){
		loadFirstChunk();
		return chunkFirstRID;
	}
	public boolean loadLastChunk(){
		String lastHandle = getLastChunkHandle();
		if (!lastHandle.equals(currentChunkHandle)){
			changeChunk(lastHandle);
		}
		return true;
	}
	public RID getFileLastRID(){
		loadLastChunk();
		return chunkLastRID;
	}
	public void blankLinkedList(){
		chunkFirstRID = null;
		chunkLastRID = null;
	}
	public void deleteFirstRecord(){
		if (chunkFirstRID != null){
			chunkFirstRID = chunkFirstRID.next;
			chunkFirstRID.prior = null;
		}
	}
	public void deleteLastRecord(){
		if (chunkLastRID != null){
			chunkLastRID = chunkLastRID.prior;
			chunkLastRID.next = null;
		}
	}
	void addRecord(RID r){
		r.inLinkedList = true;
		chunkBytesUsed+=r.length;
		chunkNumRecords+=1;
		if (chunkFirstRID == null){
			chunkFirstRID = r;
			chunkLastRID = r;
			return;
		}
		if (chunkLastRID == chunkFirstRID){
			r.prior = chunkFirstRID;
			chunkFirstRID.next = r;
			chunkLastRID = r;
			return;
		}
		chunkLastRID.next = r;
		r.prior = chunkLastRID;
		chunkLastRID = r;
		client.changeNumChunkRecords(currentChunkHandle,chunkNumRecords);
	}
	FSReturnVals deleteLinkedRecord(RID RecordID){
		RID delPrior = RecordID.prior;
		RID delNext = RecordID.next;
		boolean isFirst = false;
		boolean isLast = false;
		if (RecordID.prior == null){
			isFirst = true;
			System.out.println("Is first");
			if (RecordID.next == null){
				blankLinkedList();
				isLast = true;
			}else{
				deleteFirstRecord();
			}
		}else if(RecordID.next == null){
			deleteLastRecord();
			isLast = true;
		}else{
			RID tPrior = RecordID.prior;
			RID tNext = RecordID.next;
			tNext.prior = tPrior;
			tPrior.next = tNext;
			RecordID = null;
		}
		chunkNumRecords--;
		FSReturnVals result = writeChunkIDTags();
		if (result == ClientFS.FSReturnVals.Fail){
			System.out.println("Delete failed");
			chunkNumRecords++;
			if(isFirst){
				if (chunkFirstRID != null){
					chunkFirstRID.prior = RecordID;
				}
				chunkFirstRID = RecordID;
			}
			if (isLast){
				if (chunkLastRID != null){
					chunkLastRID.next = RecordID;
				}
				chunkLastRID = RecordID;
			}
			if (delPrior != null){
				delPrior.next = RecordID;
			}
			if (delNext != null){
				delNext.prior = RecordID;
			}
		}else if (result == ClientFS.FSReturnVals.Success){
			client.changeNumChunkRecords(currentChunkHandle,chunkNumRecords);
		}
		return result;
	}
	//Return the last chunk handle in the file
	//Works if last chunk created by FileHandle, doesn't work for loading files
	public String getLastChunkHandle(){
		return fileChunks.get(fileChunks.size()-1);
	}
	public void setNumRecords(int x){
		chunkNumRecords = x;
	}
	//Talks to the master to find out how many records are in the current chunk
	public boolean loadNumberOfRecordsInChunk(){
		try{
			chunkNumRecords = client.getNumChunkRecords(currentChunkHandle);
			return true;
		}catch(Exception e){
			return false;
		}
	}
	//Identifies and loads record information on the next chunk in the file
	//True if there is another chunk, false if there is not
	public boolean nextChunk(){
		if (chunkIndex + 1 < fileChunks.size()){
			chunkIndex++;
			changeChunk(fileChunks.get(chunkIndex));
			return true;
		}
		return false;
	}
	//Identifies and loads record information on the previous chunk in the file
	//True if there is another chunk, false if there is not
	public boolean previousChunk(){
		if (chunkIndex - 1 >= 0){
			chunkIndex--;
			changeChunk(fileChunks.get(chunkIndex));
			return true;
		}
		return false;
	}
	//Returns the first chunk handle in the file
	public String getFirstChunkHandle(){
		return fileChunks.get(0);
	}
	
	public void setFilePath(String filePath){
		this.filePath = filePath;
	}
	
	public String getFilePath(){
		return filePath;
	}
}
