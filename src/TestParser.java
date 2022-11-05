import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

public class TestParser {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		try {
			Parser parser = new Parser(new Scanner(new InputStreamReader(new FileInputStream(args[0]))));
			if(parser.getErrors() == 0) {
				FileWriter fw;
				try {
					fw = new FileWriter("izlaz.txt", true);
					BufferedWriter bw = new BufferedWriter(fw);
					bw.write(Parser.output);
					bw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("Parsing completed successfully");
			}
			else {
				System.out.println("Found " + parser.getErrors() + " errors");
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
