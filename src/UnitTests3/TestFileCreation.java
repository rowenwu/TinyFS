package UnitTests3;

import com.master.Master;

public class TestFileCreation {

	// tests file creation and creates 3 chunks in each file, 
	// sets number of chunk records to 3 for the last chunk in each file
	// run UnitTest1 after starting this to set up directory
	public static void main(String[] args) {
		Master mast = new Master();
		
		for(int i = 0; i < 10; i++){
			mast.CreateFile("/Shahram/", "a" + i);
			System.out.println("Created file " + i);
			String handle = null;
			for(int a = 0; a < 3; a++)
				handle = mast.createChunk("/Shahram/a" + i);
			mast.setNumChunkRecords("/Shahram/a" + i, handle, 3);
		}
	}

}
