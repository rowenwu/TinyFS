package com.client;
import java.nio.ByteBuffer;

import com.chunkserver.ChunkServer;
import com.client.ClientFS.FSReturnVals;
public class FileHandle {
	private String filePath;
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
	public void readAndApplyRecordsList(String input){
		setupLinkedList();
		String[] lines = input.split("\n");
		for (int i = 0; i < lines.length; i++){
			RID add = new RID(lines[i]);
			addRecord(add);
		}
	}
	public boolean loadChunkRecordTags(){
		System.out.println("Loading "+chunkNumRecords+" records from "+currentChunkHandle);
		int byteCount = bytesPerIDTag * chunkNumRecords;
		int mdOffset = chunkSize - byteCount;
		chunkBytesUsed = 0;
		byte[] metadata = client.readChunk(currentChunkHandle.substring(1), mdOffset, byteCount);
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
			System.out.println("\t"+r);
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
		String newChunk = client.createChunk();
		if (newChunk == null){ return null;}
		//We need to put in code that determines which ChunkServer and which one-letter prefix goes here
		//Right now it is hard-coded to "A"
		currentChunkHandle = "A"+newChunk;
		lastChunkHandle = currentChunkHandle;
		chunkBytesUsed = 0;
		chunkNumRecords = 0;
		chunkFirstRID = null;
		chunkLastRID = null;
		pointsToLast = true;
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
	public void changeChunk(String c){
		currentChunkHandle = c;
		if (c!= null){
			loadNumberOfRecordsInChunk();
			loadChunkRecordTags();
		}
		pointsToLast = (currentChunkHandle == lastChunkHandle);
	}
	public void writeOperation(int offset, int size){ writeOperation(currentChunkHandle,offset,size);}
	public void writeOperation(String chunk, int offset, int size){
		chunkBytesUsed=offset+size;
	}
	public void appendOperation(int size){
		chunkBytesUsed+=size;
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
		}
		return result;
	}
	//Return the last chunk handle in the file
	//Works if last chunk created by FileHandle, doesn't work for loading files
	public String getLastChunkHandle(){
		return lastChunkHandle;
	}
	//Talks to the master to find out how many records are in the current chunk
	public boolean loadNumberOfRecordsInChunk(){
		//lastChunkNumRecords = the number of records in the chunk;
		return true;
	}
	//Delete this method when the above is implemented
	public void setNumberOfRecord(int x){
		chunkNumRecords = x;
	}
	//Identifies and loads record information on the next chunk in the file
	//True if there is another chunk, false if there is not
	public boolean nextChunk(){
		return false;
	}
	//Identifies and loads record information on the previous chunk in the file
	//True if there is another chunk, false if there is not
	public boolean previousChunk(){
		return false;
	}
	//Returns the first chunk handle in the file
	public String getFirstChunkHandle(){
		return currentChunkHandle;
	}
	
	public void setFilePath(String filePath){
		this.filePath = filePath;
	}


}
