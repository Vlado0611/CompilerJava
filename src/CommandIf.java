
public class CommandIf extends Command{
	
	Expression expression;
	CommandSequence cs;
	CommandEndIf endIf;
	
	public CommandIf(Expression expression, CommandSequence cs, CommandEndIf endIf) {
		this.expression = expression;
		this.cs = cs;
		this.endIf = endIf;
	}
	
}
