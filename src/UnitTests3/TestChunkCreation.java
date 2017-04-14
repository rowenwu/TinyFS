package UnitTests3;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Vector;

import com.master.Master;

public class TestChunkCreation {

	// to test master, comment out networking implementation
	// create 15 files in the Shahram directory
	// for all the files in the Shahram directory, create chunks, set the number of records for each chunk in the last 10 files
	public static void main(String[] args) {
		Master mast = new Master();
		for(int a = 0; a < 15; a++){
			mast.CreateFile("/Shahram", "" + a);
		}
		String[] folders = new File("source/Shahram").list();
		for(int a = 0; a < 3; a++)
			for(int i = 0; i < folders.length; i++){
				String handle;
				if(!Files.isDirectory(Paths.get("source/Shahram" + folders[i])))
					handle = mast.createChunk("/Shahram/" + folders[i]);
			}
		for(int i = folders.length - 10; i < folders.length; i++){
			Vector<String> chunks = mast.getChunkHandles("/Shahram/" + folders[i]);
			for(int a = 0; a < chunks.size(); a++){
				mast.setNumChunkRecords("/Shahram/" + folders[i], chunks.get(a), a);
			}
		}
		
		for(int i = 0; i < folders.length; i++){
			if(!Files.isDirectory(Paths.get("source/Shahram" + folders[i]))){
				Vector<String> chunks = mast.getChunkHandles("/Shahram/" + folders[i]);
				for(int a = 0; a < chunks.size(); a++){
					mast.setNumChunkRecords("/Shahram/" + folders[i], chunks.get(a), a);
				}
			}
		}
	}

}
