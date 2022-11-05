
public class Expression extends Command{
	String operator;
	Expression left;
	Expression right;
	Object value;
	
	public Expression(String operator, Expression left, Expression right/*, Object value*/) {
		this.operator = operator;
		this.left = left;
		this.right = right;
		//this.value = value;
	}
	
	public Expression(String operator, Expression left, Expression right, Object value) {
		this.operator = operator;
		this.left = left;
		this.right = right;
		this.value = value;
	}
}
