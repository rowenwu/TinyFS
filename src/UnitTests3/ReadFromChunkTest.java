package UnitTests3;
import com.interfaces.*;
import com.client.*;
public class ReadFromChunkTest {
	public static void main(String[] args){
		System.out.println("begin");
//		ClientRec crec = new ClientRec();
//		Client cl = new Client();
		FileHandle fh = new FileHandle();
		System.out.println("Objects");
		fh.openFile("/Shahram/a9");
		System.out.println("Loaded");
		System.out.println(fh.makeRecordsList());
	}
}
