import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
public class Parser {
	
	private Token la; // lookahead token
	private TokenCode sym; // la.kind, used for checking
	private int errorDist; // number of correctly recognized tokens since last error
	private int errors; // number of errors
	
	private final Scanner scanner;
	
	// the map of a terminal to it's First Set
	private final HashMap<String, ArrayList<TokenCode>> firstMap;
	private final HashMap<String, IdentifierExpression> symbolTable;
	private final HashMap<String, IdentifierExpression> defTable;
	public static String output = "";
	
	public Parser(Scanner s) {
		this.scanner = s;
		
		this.firstMap = new HashMap<String, ArrayList<TokenCode>>();
		this.firstMap.put("Declarations",new ArrayList<TokenCode>(Arrays.asList(TokenCode.INTEGER_TYPE)));
		this.firstMap.put("CommandSequence", new ArrayList<TokenCode>(Arrays.asList(TokenCode.IF, TokenCode.WHILE, TokenCode.IDENTIFIER
							,TokenCode.READ, TokenCode.WRITE, TokenCode.SKIP, TokenCode.FOR, TokenCode.DEFINE)));
		this.firstMap.put("Compare", new ArrayList<TokenCode>(Arrays.asList(TokenCode.LESS, TokenCode.GREATER)));
		this.firstMap.put("PlusMinus", new ArrayList<TokenCode>(Arrays.asList(TokenCode.PLUS, TokenCode.MINUS)));
		this.firstMap.put("MulDiv", new ArrayList<TokenCode>(Arrays.asList(TokenCode.MULTIPLY, TokenCode.DIVIDE, TokenCode.KAPPA)));
		this.firstMap.put("Expression", new ArrayList<TokenCode>(Arrays.asList(TokenCode.INTEGER_CONSTANT, TokenCode.IDENTIFIER, TokenCode.LEFT_REGULAR)));
		this.symbolTable = new HashMap<>();
		this.defTable = new HashMap<>();
		
		this.errors = 0;
		this.errorDist = 3;
		this.scan();
		Program program = Program();
		if(sym != TokenCode.EOF) {
			error("Premature end of program", "Constructor");
		}
		
		/*
		for(String key: this.symbolTable.keySet()) {
			System.out.println(key + "=" + this.symbolTable.get(key).expression.value);
		}
		*/
	}
	
	public int getErrors() {
		return this.errors;
	}

//-------------- Auxillary methods ----------------
	
	private void scan() {
		Token t = la;
		la = scanner.next();
		if(la.string != null) {
			if(this.defTable.containsKey(la.string)) {
				IdentifierExpression identExpr = this.defTable.get(la.string);
				int lval = (int)identExpr.expression.left.value;
				String oper = identExpr.expression.operator;
				int rval = (int)identExpr.expression.right.value;
				output += "("+lval+oper+rval+")";
			}
			else {
				output += la.string;
			}
		}
		while(la == null) {
			la = scanner.next();
		}
		
		sym = la.kind;

		errorDist++;
	}
	
	private void check(TokenCode expected) {
		if(sym == expected) {
			if (this.sym == TokenCode.IDENTIFIER && this.la.string.length() > 31) {
                error("Identifier name too long (must be <= 31)", sym.toString());
            }
			scan(); // if it's recognized, go ahead
		}
		else error(expected.toString() + " expected", sym.toString());
	}
	
	private void error(String msg, String func) {
		if(this.errorDist >= 3) {
			System.out.println("line " + la.line + ", col " + la.col + ": " + msg + ", got " + func);
			this.errors++;
		}
		this.errorDist = 0;
	}
	
	private Program Program() {
		// Program -> Definitions LET Declarations IN CommandSequence END
		ArrayList<Definition> definitions = Definitions();
		this.check(TokenCode.LET);
		ArrayList<Declaration> declarations = Declarations();
		this.check(TokenCode.IN);
		CommandSequence cs = CommandSequence();
		this.check(TokenCode.END);
		return new Program(definitions, declarations, cs);
	}
	
	private ArrayList<Definition> Definitions() {
		// Definition -> DefinitionPrim
		if(sym == TokenCode.HASHTAG) {
			ArrayList<Definition> definitions = new ArrayList<>();
			
			while(this.sym == TokenCode.HASHTAG) {
				//DefinitionPrim -> Def DefinitionPrim'
				definitions.add(Def());
			}
			return definitions;
		}
		else {
			return null;
		}
	}
	
	private Definition Def() {
		this.check(TokenCode.HASHTAG);
		this.check(TokenCode.DEFINE);
		String identifier = this.la.string;
		this.check(TokenCode.IDENTIFIER);
		Expression expr = Expr();
		this.check(TokenCode.SEMICOLON);
		if(this.symbolTable.containsKey(identifier)) {}
		else {
			this.symbolTable.put(identifier, new IdentifierExpression(expr));
			this.defTable.put(identifier, new IdentifierExpression(expr));
		}
		return new Definition(identifier, expr);
	}
	
	private ArrayList<Declaration> Declarations(){
		// Declarations -> INTEGER Id_seq Identifier.
		if(sym == TokenCode.INTEGER_TYPE) {
			this.check(TokenCode.INTEGER_TYPE);
			ArrayList<Declaration> decls = new ArrayList<>();
			ArrayList<String> ids = IdSeq();
			
			for(String identifier : ids) {
				if(this.symbolTable.containsKey(identifier)) {
					this.error("Identifier " + identifier + " already declared", "decls");
				}
				else {
				  decls.add(new Declaration(identifier));
				  this.symbolTable.put(identifier, new IdentifierExpression(new Expression("", null, null))); // no expression for now, just declaring
				}
			}
			this.check(TokenCode.DOT);
			return decls;
		}
		else {
			return null;
		}
	}
	
	private ArrayList<String> IdSeq() {
		// Idseq -> Idseq'
		ArrayList<String> idseq = new ArrayList<String>();
		Token t;
		while(sym != TokenCode.DOT) {
			// IdSeq' -> Identifier, IdSeq'
			t = this.la;
			if(t.kind == TokenCode.IDENTIFIER) {
				this.check(TokenCode.IDENTIFIER);
				idseq.add(t.string);
				if(sym == TokenCode.DOT) break;
				this.check(TokenCode.COMMA);
			}
			else {
				this.check(TokenCode.IDENTIFIER);
			}
		}
		return idseq;
	}
		
	private CommandSequence CommandSequence() {
		// CommandSeq -> Cmd_Seq
		ArrayList<Command> cmds = new ArrayList<>();
		while(this.firstMap.get("CommandSequence").contains(sym)) {
			// Cmd_Seq -> Command Cmd_Seq 
			cmds.add(Cmds());
		}
		
		return new CommandSequence(cmds);
	}
	
	private Command Cmds() {
		if(sym == TokenCode.IF) {
			return IfCmd();
		}
		else if(sym == TokenCode.WHILE) {
			return WhileCmd();
		}
		else if(sym == TokenCode.READ) {
			return ReadCmd();
		}
		else if(sym == TokenCode.WRITE) {
			return WriteCmd();
		}
		else if(sym == TokenCode.IDENTIFIER) {
			return AssignExpr();
		}
		else if(sym == TokenCode.SKIP) {
			return SkipCmd();
		}
		else if(sym == TokenCode.FOR) {
			return ForCmd();
		}
		else {
			error("Invalid command", "Cmds");
			return null;
		}
	}
	
	private CommandIf IfCmd() {
		this.check(TokenCode.IF);
		this.check(TokenCode.LEFT_REGULAR);
		Expression expr = Expr();
		this.check(TokenCode.RIGHT_REGULAR);
		this.check(TokenCode.THEN);
		CommandSequence cs = CommandSequence();
		CommandEndIf endIf = EndIf();
		this.check(TokenCode.SEMICOLON);
		return new CommandIf(expr, cs, endIf);
	}
	
	private CommandEndIf EndIf() {
		// EndIf -> ELSE CommandSeq FI | FI
		if(sym == TokenCode.ELSE) {
			this.scan();
			CommandSequence cs = CommandSequence();
			this.check(TokenCode.FI);
			return new CommandEndIf(cs);
		}
		else if(sym == TokenCode.FI) {
			this.scan();
			return new CommandEndIf(null);
		}
		else {
			error("Invalid end of IF command", "EndIf");
			return null;
		}
	}
	
	private CommandWhile WhileCmd() {
		// WhileCmd -> WHILE Expression DO Command_Seq END;
		this.check(TokenCode.WHILE);
		Expression expr = Expr();
		this.check(TokenCode.DO);
		CommandSequence cs = CommandSequence();
		this.check(TokenCode.END);
		this.check(TokenCode.SEMICOLON);
		return new CommandWhile(expr, cs);
	}
	
	private CommandFor ForCmd() {
		// ForCmd -> FOR LPAR Assign SEMICOLON Expression SEMICOLON Assign RPAR Command_Seq END SEMICOLON
		
		this.check(TokenCode.FOR);
		this.check(TokenCode.LEFT_REGULAR);
		ExpressionAssign exprAssign1 = AssignExpr();
		//this.check(TokenCode.SEMICOLON);
		Expression expr = Expr();
		this.check(TokenCode.SEMICOLON);
		ExpressionAssign exprAssign2 = AssignExprFor();
		this.check(TokenCode.RIGHT_REGULAR);
		CommandSequence cmdSeq = CommandSequence();
		this.check(TokenCode.END);
		this.check(TokenCode.SEMICOLON);
		return new CommandFor(exprAssign1, expr, exprAssign2, cmdSeq);
	}
	
	private CommandRead ReadCmd() {
		this.check(TokenCode.READ);
		//this.check(TokenCode.LEFT_REGULAR);
		String identifier = this.la.string;
		this.check(TokenCode.IDENTIFIER);
		//this.check(TokenCode.RIGHT_REGULAR);
		this.check(TokenCode.SEMICOLON);
		return new CommandRead(identifier);
	}
	
	private CommandWrite WriteCmd() {
		this.check(TokenCode.WRITE);
		//this.check(TokenCode.LEFT_REGULAR);
		Expression expr = Expr();
		//this.check(TokenCode.RIGHT_REGULAR);
		this.check(TokenCode.SEMICOLON);
		return new CommandWrite(expr);
	}
	
	private Command SkipCmd() {
		this.check(TokenCode.SKIP);
		this.check(TokenCode.SEMICOLON);
		Command cmd = new Command();
		return cmd;
	}
	
	private ExpressionAssign AssignExpr() {
		String identifier = this.la.string;
		this.check(TokenCode.IDENTIFIER);
		this.check(TokenCode.COLON);
		this.check(TokenCode.EQUALS);
		Expression expr = Expr();
		if(this.symbolTable.containsKey(identifier)) {
			this.symbolTable.get(identifier).expression = expr;
		}
		else {
			error("Identifier " + identifier + " is not declared", "AssignExpr");
		}
		this.check(TokenCode.SEMICOLON);
		return new ExpressionAssign(identifier, expr);
	}
	
	private ExpressionAssign AssignExprFor() {
		String identifier = this.la.string;
		this.check(TokenCode.IDENTIFIER);
		this.check(TokenCode.COLON);
		this.check(TokenCode.EQUALS);
		Expression expr = Expr();
		if(this.symbolTable.containsKey(identifier)) {
			this.symbolTable.get(identifier).expression = expr;
		}
		else {
			error("Identifier " + identifier + " is not declared", "AssignExpr");
		}
		return new ExpressionAssign(identifier, expr);
	}
	
	private Expression Expr() {
		// Expr -> Expr2 Expr'
		Expression first = Expr2();
		return ExprPrim(first);
	}
	
	private Expression ExprPrim(Expression first) {
		// Expr' -> Compare Expr2 Expr'
		if(sym == TokenCode.LESS || sym == TokenCode.GREATER) {
			// Compare -> < | > | =
			String comparator = sym == TokenCode.LESS ? "<" : ">";
			this.scan();
			Expression second = Expr2();
			return ExprPrim(new Expression(comparator, first, second));
		}
		else if(sym == TokenCode.EQUALS) {
			this.scan();
			Expression second = Expr2();
			String comparator = "=";
			return ExprPrim(new Expression(comparator, first, second));
		}
		else {
			// eps
			return first;
		}
	}
	
	private Expression Expr2() {
		// Expr2 -> Expr3 Expr2'
		Expression first = Expr3();
		return Expr2Prim(first);
	}
	
	private Expression Expr2Prim(Expression first) {
		// Expr2' -> PlusMinus Expr3 Expr2Prim
		if(sym == TokenCode.PLUS || sym == TokenCode.MINUS) {
			// PlusMinus -> + | -
			String operator = sym == TokenCode.PLUS ? "+" : "-";
			this.scan();
			Expression second = Expr3();
			return Expr2Prim(new Expression(operator, first, second));
		}
		else {
			// eps
			return first;
		}
	}
	
	private Expression Expr3() {
		// Expr3 -> Expr4 Expr3'
		Expression first = Expr4();
		return Expr3Prim(first);
	}
	
	private Expression Expr3Prim(Expression first) {
		// Expr3' -> MulDivKappa Expr4 Expr3'
		
		if(sym == TokenCode.MULTIPLY || sym == TokenCode.DIVIDE || sym == TokenCode.KAPPA) {
			// MulDivMod -> * | / | %
			String operator;
			if(sym == TokenCode.MULTIPLY) {
				operator = "*";
			}
			else if (sym == TokenCode.DIVIDE){
				operator = "/";
			}
			else {
				operator = "^";
			}
			this.scan();
			Expression second = Expr4();
			return Expr3Prim(new Expression(operator, first, second));
		}
		else {
			// eps
			return first;
		}
	}
	
	private Expression Expr4() {
		// Expr4 -> Number | Identifier | (Expression)
		if(sym == TokenCode.INTEGER_CONSTANT) {
			Expression constant = new Expression("", null, null);
			constant.value = this.la.intVal;
			this.scan();
			return constant;
		}
		else if(sym == TokenCode.IDENTIFIER) {
			Expression result = new Expression("", null, null);
			result.value = this.la.string;
			if(!this.symbolTable.containsKey(this.la.string)) {
				error("Identifier " + this.la.string + " is not declared", "Expr4");
			}
			this.scan();
			return result;
		}
		else if(sym == TokenCode.LEFT_REGULAR) {
			this.scan();
			Expression expr = Expr();
			this.check(TokenCode.RIGHT_REGULAR);
			return expr;
		}
		else {
			error("Expression expected", "Expr5");
			this.scan();
			return null;
		}
	}
	
	
	
}
