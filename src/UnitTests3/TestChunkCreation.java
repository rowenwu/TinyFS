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

	// run after TestFileCreation
	// Start Master before running this
	// tests to see whether the values returned by getChunkHandles and setChunkHandles match the persistent log records
	public static void main(String[] args) {
		Client cl = new Client();
		String[] folders = new File("source/Shahram").list();
		for(int i = 0; i < folders.length; i++){
			if(!Files.isDirectory(Paths.get("source/Shahram/" + folders[i]))){
				System.out.println("file " + folders[i]);
				String[] chunks = cl.getChunkHandles("/Shahram/" + folders[i]);
				// print number of chunk records received from the hashtable
				for(int a = 0; a < chunks.length; a++){
					System.out.println(chunks[a] + " " + cl.getNumChunkRecords(chunks[a]));
				}
				//print number of chunk records as it's stored in the file
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
