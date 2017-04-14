package UnitTests3;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Vector;

import com.client.Client;
import com.master.Master;

public class TestChunkCreation {
	// create 15 files in the Shahram directory
	// for all the files in the Shahram directory, create chunks, set the number of records for each chunk in the last 10 files
	public static void main(String[] args) {
//		Master mast = new Master();
//		
//		for(int i = 0; i < 10; i++){
//			mast.CreateFile("/Shahram/", "a" + i);
//			System.out.println("Created file " + i);
//			String handle = null;
//			for(int a = 0; a < 3; a++)
//				handle = mast.createChunk("/Shahram/a" + i);
//			mast.setNumChunkRecords("/Shahram/a" + i, handle, 3);
//		}
		
		Client cl = new Client();
		String[] folders = new File("source/Shahram").list();
		for(int i = 0; i < folders.length; i++){
			if(!Files.isDirectory(Paths.get("source/Shahram/" + folders[i]))){
				System.out.println("file " + folders[i]);
				String[] chunks = cl.getChunkHandles("/Shahram/" + folders[i]);
				for(int a = 0; a < chunks.length; a++){
					System.out.println(chunks[a] + " " + cl.getNumChunkRecords(chunks[a]));
				}
				try {
					RandomAccessFile raf = new RandomAccessFile("source/Shahram/" + folders[i], "rws");
					String line;
					while((line = raf.readLine()) != null){
						System.out.println("line : " + line);
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println();
			}
			
		}
	}

}
