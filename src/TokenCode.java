
public enum TokenCode {
	// Keywords
	NONE,
	EOF,
	LET,
	IN,
	END,
	IF,
	THEN,
	FI,
	ELSE,
	WHILE,
	DO,
	READ,
	WRITE,
	SKIP,
	FOR,
	DEFINE,
	
	// Identifiers and data types
	IDENTIFIER,
	INTEGER_TYPE,
	
	// Operators, braces and special characters
	PLUS,
	MINUS,
	MULTIPLY,
	DIVIDE,
	KAPPA,
	LESS,
	GREATER,
	EQUALS,
	SEMICOLON,
	COLON,
	LEFT_REGULAR,
	RIGHT_REGULAR,
	COMMA,
	DOT,
	HASHTAG,
	
	// Constants
	INTEGER_CONSTANT,
}
