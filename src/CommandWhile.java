
public class CommandWhile extends Command{
	
	Expression expression;
	CommandSequence cs;
	
	public CommandWhile(Expression expression, CommandSequence cs) {
		this.expression = expression;
		this.cs = cs;
	}
}
