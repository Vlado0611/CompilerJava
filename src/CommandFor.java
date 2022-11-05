
public class CommandFor extends Command {
	
	ExpressionAssign assign1;
	Expression expr;
	ExpressionAssign assign2;
	CommandSequence cmdSeq;
	
	public CommandFor(ExpressionAssign assign1, Expression expr, ExpressionAssign assign2, CommandSequence cmdSeq) {
		this.assign1 = assign1;
		this.expr = expr;
		this.assign2 = assign2;
		this.cmdSeq = cmdSeq;
	}
	
}
