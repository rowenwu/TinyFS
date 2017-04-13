package com.client;

import java.nio.ByteBuffer;

import com.client.ClientFS.FSReturnVals;
import com.chunkserver.ChunkServer;
import com.client.FileHandle;
public class ClientRec {
	Client client;
	static final int chunkSize = ChunkServer.ChunkSize;
	public ClientRec(){
        client = new Client();
	}
	

	/**
	 * Appends a record to the open file as specified by ofh Returns BadHandle
	 * if ofh is invalid Returns BadRecID if the specified RID is not null
	 * Returns RecordTooLong if the size of payload exceeds chunksize RID is
	 * null if AppendRecord fails
	 *
	 * Example usage: AppendRecord(FH1, obama, RecID1)
	 */
	public FSReturnVals AppendRecord(FileHandle ofh, byte[] payload, RID RecordID) {
		if ((ofh == null) || ofh.noChunk()){
			return ClientFS.FSReturnVals.BadHandle;
		}
		if ((RecordID.chunk != null) || (RecordID.valid)){
			return ClientFS.FSReturnVals.BadRecID;
		}
		int payloadSize = payload.length;
		if (ofh.freeBytesLastChunk() < payloadSize){
			RecordID = null;
			return ClientFS.FSReturnVals.RecordTooLong;
		}
		String chunkName = ofh.getLastChunkHandle();		
		RecordID.offset = ofh.getBytesUsedLastChunk();;
		RecordID.chunkServerLabel = chunkName.charAt(0);
		RecordID.chunk = chunkName.substring(1);
		RecordID.valid = true;
		RecordID.length = payloadSize;
		boolean writeSuccessful = client.writeChunk(RecordID.chunk, payload,RecordID.offset);
		if (writeSuccessful){
			//System.out.println(RecordID);
			byte[] recordTagData = RecordID.makeTag();
			//RID.printTag(recordTagData);
			int tagOffset = chunkSize - FileHandle.bytesPerIDTag * (ofh.getNumberOfRecordsInChunk() +1);
			//System.out.println("Should start writing byte "+tagOffset);
			boolean tagSuccessful = client.writeChunk(RecordID.chunk, recordTagData,tagOffset);
			if (tagSuccessful){		
				ofh.addRecord(RecordID);
				return ClientFS.FSReturnVals.Success;
			}
		}
		return ClientFS.FSReturnVals.Fail;
	}
	/**
	 * Deletes the specified record by RecordID from the open file specified by
	 * ofh Returns BadHandle if ofh is invalid Returns BadRecID if the specified
	 * RID is not valid Returns RecDoesNotExist if the record specified by
	 * RecordID does not exist.
	 *
	 * Example usage: DeleteRecord(FH1, RecID1)
	 */
	public FSReturnVals DeleteRecord(FileHandle ofh, RID RecordID) {
		//System.out.println("Delete RID "+RecordID);
		if ((ofh == null) || ofh.noChunk()){
			return ClientFS.FSReturnVals.BadHandle;
		}
		if (RecordID == null){
			return ClientFS.FSReturnVals.RecDoesNotExist;
		}
		RID answer;
		if (RecordID.inLinkedList){
			FSReturnVals suc = ofh.deleteLinkedRecord(RecordID);
			//System.out.println(ofh.makeRecordsList());
			return suc;
		}
		//System.out.println("Could not find linked data");
		RID cur = ofh.getChunkFirstRID();
		int iGivenChunk = Integer.parseInt(RecordID.chunk);
		while (cur !=null){
			int iCurChunk = Integer.parseInt(cur.chunk);
			if((iCurChunk==iGivenChunk) && (cur.offset==RecordID.offset)){
				break;
			}
			cur = cur.next;
		}
		if (cur == null){
			return ClientFS.FSReturnVals.RecDoesNotExist;
		}
		return ofh.deleteLinkedRecord(cur);
	}
	/**
	 * Reads the first record of the file specified by ofh into payload Returns
	 * BadHandle if ofh is invalid Returns RecDoesNotExist if the file is empty
	 *
	 * Example usage: ReadFirstRecord(FH1, tinyRec)
	 */
	public FSReturnVals ReadFirstRecord(FileHandle ofh, TinyRec rec){
		if ((ofh == null) || ofh.noChunk()){ return ClientFS.FSReturnVals.BadHandle;}
		RID first = ofh.getFileFirstRID();
		if (first == null){
			return ClientFS.FSReturnVals.RecDoesNotExist;
		}
		return getRecord(ofh,first,rec);
	}

	/**
	 * Reads the last record of the file specified by ofh into payload Returns
	 * BadHandle if ofh is invalid Returns RecDoesNotExist if the file is empty
	 *
	 * Example usage: ReadLastRecord(FH1, tinyRec)
	 */
	public FSReturnVals ReadLastRecord(FileHandle ofh, TinyRec rec){
		if ((ofh == null) || ofh.noChunk()){ return ClientFS.FSReturnVals.BadHandle;}
		RID last = ofh.getFileLastRID();
		if (last == null){
			return ClientFS.FSReturnVals.RecDoesNotExist;
		}
		FSReturnVals result = getRecord(ofh,last,rec);
		return result;
	}
	private FSReturnVals getRecord(FileHandle ofh, RID r, TinyRec rec){
		rec.setRID(r);
		int recordSize = r.length;
		byte[] recordData = client.readChunk(r.chunk, r.offset, recordSize);
		if (recordData == null){
			rec.setPayload(null);
			return ClientFS.FSReturnVals.Fail;
		}
		rec.setPayload(recordData);
		return ClientFS.FSReturnVals.Success;		
	}
	/**
	 * Reads the next record after the specified pivot of the file specified by
	 * ofh into payload Returns BadHandle if ofh is invalid Returns
	 * RecDoesNotExist if the file is empty or pivot is invalid
	 *
	 * Example usage: 1. ReadFirstRecord(FH1, tinyRec1) 2. ReadNextRecord(FH1,
	 * rec1, tinyRec2) 3. ReadNextRecord(FH1, rec2, tinyRec3)
	 */
	public FSReturnVals ReadNextRecord(FileHandle ofh, RID pivot, TinyRec rec){
		if ((ofh == null) || ofh.noChunk()){ return ClientFS.FSReturnVals.BadHandle;}
		RID answer;
		if (pivot.inLinkedList){
			answer = pivot.next;
			if (answer == null){
				rec.setRID(null);
				rec.setPayload(null);
				return ClientFS.FSReturnVals.RecDoesNotExist;
			}
		}else{
			RID cur = ofh.getChunkFirstRID();
			int iPivotChunk = Integer.parseInt(pivot.chunk);
			while (cur !=null){
				int iCurChunk = Integer.parseInt(cur.chunk);
				if((iCurChunk>iPivotChunk) ||((iCurChunk==iPivotChunk) && (cur.offset>pivot.offset))){
					break;
				}
				cur = cur.next;
			}
			if (cur == null){
				rec.setRID(null);
				rec.setPayload(null);
				return ClientFS.FSReturnVals.RecDoesNotExist;
			}
			answer = cur;
		}
		return getRecord(ofh,answer,rec);
	}

	/**
	 * Reads the previous record after the specified pivot of the file specified
	 * by ofh into payload Returns BadHandle if ofh is invalid Returns
	 * RecDoesNotExist if the file is empty or pivot is invalid
	 *
	 * Example usage: 1. ReadLastRecord(FH1, tinyRec1) 2. ReadPrevRecord(FH1,
	 * recn-1, tinyRec2) 3. ReadPrevRecord(FH1, recn-2, tinyRec3)
	 */
	public FSReturnVals ReadPrevRecord(FileHandle ofh, RID pivot, TinyRec rec){
		if ((ofh == null) || ofh.noChunk()){ return ClientFS.FSReturnVals.BadHandle;}
		RID answer;
		if (pivot.inLinkedList){
			answer = pivot.prior;
			if (answer == null){
				rec.setRID(null);
				rec.setPayload(null);
				return ClientFS.FSReturnVals.RecDoesNotExist;
			}
		}else{
			RID cur = ofh.getChunkLastRID();
			int iPivotChunk = Integer.parseInt(pivot.chunk);
			while (cur !=null){
				int iCurChunk = Integer.parseInt(cur.chunk);
				if((iCurChunk<iPivotChunk) ||((iCurChunk==iPivotChunk) && (cur.offset<pivot.offset))){
					break;
				}
				cur = cur.prior;
			}
			if (cur == null){
				rec.setRID(null);
				rec.setPayload(null);
				return ClientFS.FSReturnVals.RecDoesNotExist;
			}
			answer = cur;
		}
		return getRecord(ofh,answer,rec);
	}
}
