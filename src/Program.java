import java.util.ArrayList;

public class Program {
	
	ArrayList<Definition> definitions;
	ArrayList<Declaration> declarations;
	CommandSequence cs;
	
	public Program(ArrayList<Definition> definitions,ArrayList<Declaration> declarations, CommandSequence cs) {
		this.definitions = definitions;
		this.declarations = declarations;
		this.cs = cs;
	}
}
