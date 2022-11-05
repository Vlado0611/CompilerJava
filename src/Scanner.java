import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;

public class Scanner {
	
	private final char eofCh = '\u0080';
    private final char eol = '\n';
    
    private final HashMap<String, TokenCode> keywords; 	// hashmap for keywords
    private final HashMap<String, TokenCode> tokens; 	// hashmap for tokens 
    private final HashMap<String, TokenCode> dataTypes;	// hashmap for dataTypes
    
    private final String intRegex = "\\d+";
    // d is [0-9]
    private final String hexRegex = "^(0[xX])[a-fA-F0-9]+";
    private final String identifierRegex = "^[a-zA-Z]\\w*";
    // w* is [a-zA-Z_0-9]
    // ^ is the start of the string
    
    private char lookahead; // lookahead token
    private int col;	// current column
    private int line;	// current line
    private final Reader reader; // reader
    
    public Scanner(Reader r) {
    	this.reader = new BufferedReader(r);
    	this.line = 1;
    	this.col = 0;
    	
    	// initialize and fill maps
    	this.tokens = new HashMap<>();
    	fillTokens();
    	this.dataTypes = new HashMap<>();
    	fillDataTypes();
    	this.keywords = new HashMap<>();
    	fillKeywords();
    	
    	// begin reading
    	this.nextCharacter();
    }
    
    private void nextCharacter() {
    	try {
			this.lookahead = (char)this.reader.read();
			this.col++;
			
			if(this.lookahead == this.eol) {
				this.line++;
				this.col = 0;
			}
			else if(this.lookahead == '\uffff') {
				this.lookahead = this.eofCh;
				return;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			this.lookahead = eofCh;
		}	
    }
    
    public Token next() {  	
    	while(this.lookahead <= ' ') {
    		this.nextCharacter(); // skip blanks, tabs, eols
    	}
    	
    	Token t = new Token();
    	t.line = this.line;
    	t.col = this.col;
    	
    	if((this.lookahead >= 'a' && this.lookahead <='z') || (this.lookahead >= 'A' && this.lookahead <= 'Z')) {
    		readName(t);
    	}
    	else if(this.lookahead >= '0' && this.lookahead <= '9') {
    		readNumber(t);
    	}
    	else {
    		String lookaheadString = Character.toString(this.lookahead);
    		switch(this.lookahead) {
    		case '+': 
    		case '-': 
    		case '*':  
    		case '^':
    		case ';':
    		case '(':
    		case ')':
    		case '>':
    		case '<':
    		case ',':
    		case '.':
    		case '=':
    		case ':':
    		case '#':
    			nextCharacter(); // continue scanning
    			t.kind = this.tokens.get(lookaheadString);
    			t.string = lookaheadString;
    			break;
    		
    		case '/':
    			// single line comment, multi-line comment or division
    			this.nextCharacter();
    			if(this.lookahead == '/') { // single line comment
    				this.nextCharacter();
    				while(this.lookahead != this.eol && this.lookahead != this.eofCh) {
    					this.nextCharacter();
    				}
    				this.nextCharacter();
    				t = null; // parser skips it
    			}
    			else if(this.lookahead == '*') { // multiline comment
    				char prev;
    				this.nextCharacter();
    				while(this.lookahead != this.eofCh && this.lookahead != '/') {
    					prev = this.lookahead;
    					this.nextCharacter();
    					if(this.lookahead == '/') {
    						if(prev == '*') {
    							break;
    						}
    						else {
    							this.nextCharacter();
    						}
    						while (this.lookahead == '/') this.nextCharacter();
    					}
    				
    				}
    				
    				if(this.lookahead == this.eofCh) {
    					t.kind = TokenCode.NONE;
    				}
    				else {
    					this.nextCharacter();
    				}
    				t = null;
    				
    			}
    			else { // divison
    				t.kind = TokenCode.DIVIDE;
    				t.string = lookaheadString;
    			}
    			break;
    		
    		case eofCh:
    			t.kind = TokenCode.EOF;
    			break;
    		
    		default:
    				this.nextCharacter();
    				t.kind = TokenCode.NONE;
    				break;
    		}
    	}
    	return t;
    }
    
    private void readName(Token t) {
    	// check if the name is a keyword, data type or identifier
    	t.string = Character.toString(this.lookahead); // we have the first token
    	
    	this.nextCharacter();
    	while(Character.isLetterOrDigit(this.lookahead) || this.lookahead == '_') {
    		t.string += Character.toString(this.lookahead);
    		this.nextCharacter();
    	}
    	
    	// check if it's a keyword
    	if(this.keywords.containsKey(t.string)) {
    		t.kind = this.keywords.get(t.string);
    		return;
    	}
    	
    	// check if it's a data type
    	if(this.dataTypes.containsKey(t.string)) {
    		t.kind = this.dataTypes.get(t.string);
    		return;
    	}
    	
    	// if none of the above, it's an identifier
    	if(t.string.matches(identifierRegex)) {
    		t.kind = TokenCode.IDENTIFIER;
    	}
    	else{// unless it's invalid
    		System.out.println("Scanner: line " + this.line + " column " + this.col + ": Invalid identifier name");
    		t.kind = TokenCode.NONE;
    	}
    }
    
    private void readNumber(Token t) {
    	// found a number constant, read it fully and check its validity
    	t.string = Character.toString(this.lookahead);
    	this.nextCharacter();
    	
    	while(Character.isDigit(this.lookahead) || Character.toString(this.lookahead).equalsIgnoreCase("x") 
    			|| (this.lookahead >= 'a' && this.lookahead <= 'f') || (this.lookahead >= 'A' && this.lookahead <= 'F')) {
    		t.string += Character.toString(this.lookahead);
    		this.nextCharacter();
    	}
    	
    	if(t.string.matches(intRegex) || t.string.matches(hexRegex)) {
    		t.kind = TokenCode.INTEGER_CONSTANT;
    		if(t.string.startsWith("0x") || t.string.startsWith("0X")) {
    			t.intVal = Integer.parseInt(t.string.substring(2), 16);
    		}
    		else {
    			t.intVal = Integer.parseInt(t.string);
    		}
    	}
    	else {
    		System.out.println("Scanner: line " + this.line + " col " + this.col + ": Invalid number constant");
    		t.kind = TokenCode.NONE;
    	}
    }
    
    private void fillKeywords() {
    	this.keywords.put("LET", TokenCode.LET);
    	this.keywords.put("IN", TokenCode.IN);
    	this.keywords.put("END", TokenCode.END);
    	this.keywords.put("IF",TokenCode.IF);
    	this.keywords.put("THEN", TokenCode.THEN);
    	this.keywords.put("FI", TokenCode.FI);
    	this.keywords.put("ELSE", TokenCode.ELSE);
    	this.keywords.put("WHILE", TokenCode.WHILE);
    	this.keywords.put("DO", TokenCode.DO);
    	this.keywords.put("READ", TokenCode.READ);
    	this.keywords.put("WRITE", TokenCode.WRITE);
    	this.keywords.put("SKIP", TokenCode.SKIP);
    	this.keywords.put("FOR", TokenCode.FOR);
    	this.keywords.put("DEFINE", TokenCode.DEFINE);
    }
    
    private void fillTokens() {
    	this.tokens.put("+", TokenCode.PLUS);
    	this.tokens.put("-", TokenCode.MINUS);
    	this.tokens.put("*", TokenCode.MULTIPLY);
    	this.tokens.put("/", TokenCode.DIVIDE);
    	this.tokens.put("^", TokenCode.KAPPA);
    	this.tokens.put("<", TokenCode.LESS);
    	this.tokens.put(">", TokenCode.GREATER);
    	this.tokens.put("=", TokenCode.EQUALS);
    	this.tokens.put(";", TokenCode.SEMICOLON);
    	this.tokens.put(":", TokenCode.COLON);
    	this.tokens.put("(", TokenCode.LEFT_REGULAR);
    	this.tokens.put(")", TokenCode.RIGHT_REGULAR);
    	this.tokens.put(",", TokenCode.COMMA);
    	this.tokens.put(".", TokenCode.DOT);
    	this.tokens.put("#", TokenCode.HASHTAG);
    }
    
    private void fillDataTypes() {
    	this.dataTypes.put("INTEGER", TokenCode.INTEGER_TYPE);
    }
}
