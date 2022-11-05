
public class ExpressionAssign extends Command {
	
	String identifier;
	Expression expr;
	
	public ExpressionAssign(String identifier, Expression expr) {
		this.identifier = identifier;
		this.expr = expr;
	}
	
}
