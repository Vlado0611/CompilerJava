import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;

public class TestScanner {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		Token t;
		if(args.length > 0) {
			String source = args[0];
			try {
				Scanner scanner = new Scanner(new InputStreamReader(new FileInputStream(source)));
				do {
					t = scanner.next();
					System.out.println();
					while(t == null) t = scanner.next();
					System.out.print("line " + t.line + " column " + t.col +": ");
					switch(t.kind) {
					case IDENTIFIER: System.out.print(t.string); break;
					case INTEGER_CONSTANT: System.out.print(t.intVal); break;
					default: System.out.print(t.kind);
					}
				}while(t.kind != TokenCode.EOF);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			
		}
		
	}

}
