/**
 * 'Oak'
 *
 * This grammar is basically JSON with a few things ironed out
 *
 * * Comments are allowed, same as Java
 *
 * * Keys don't have "quotes"
 *
 * + exprs are supported: "TimeoutInMs: 60*60*1000 "
 *
 */
grammar Oak;

Plus:		'+'; // Keep as first please
Minus:		'-';
Multiply:	'*';
Divide:		'/';
Mod:		'%';
Comp:		'~';
Not:		'!';
Lt:			'<';
Lte:		'<=';
Gt:			'>';
Gte:		'>=';
And:		'&';
Or:			'|';
Land:		'&&';
Lor:		'||';
Xor:		'^';
Eq:			'=';
Ne:			'!=';
Shl:		'<<';
Shr:		'>>';
Usr:		'>>>';	// Usr must be last math operator
Comma:		',';
Semi:		';';
Null:		'null';
True:		'true';
False:		'false';
Qm:			'?';
Colon:		':';
Paren:		'(';
Nerap:		')';
Curly:		'{';
Ylruc:		'}';
Square:		'[';
Erauqs:		']';


/**
 * The entry point for Oak evaluation.
 */
oak : load* pair EOF;

/**
 * The entry point for expression evaluation.
 */
expression:  expr EOF;

load
	:	'#load' TextLiteral
	;

pair
	:	IdentifierLiteral Colon value Semi	// E.g. myKey: 32; 
	|	IdentifierLiteral object				// E.g. MyKey { ... }
	|	IdentifierLiteral array				// E.g. myKey [ 1, 2, 3 ]
	|	IdentifierLiteral Semi				// Shorthand for myKey: true; 
	;

array
	:	Square value (Comma value)* Erauqs		// Either [ 1, 2, 3 ]...
	|	Square object (Comma object)* Erauqs	// ...or [ {...}, {...}, {...} ]
	;

object
	:	Curly pair+ Ylruc		// E.g. { a:1; ... }
	;
	
value
	:	literal
	|	expr
	|	cardinality
	;

// Members are in operator precedence order
expr
	:	primary
	|	IdentifierLiteral Paren ( expr ( Comma expr)* )? Nerap // Function
	|	(Minus|Not|Comp) expr // Unary
	|	expr (Multiply|Divide|Mod) expr
	|	expr (Plus|Minus) expr
	|	expr (Shl|Shr|Usr) expr
	|	expr (Lte | Gte | Gt | Lt) expr
	|	expr (Eq | Ne) expr
	|	expr And expr
	|	expr Xor expr
	|	expr Or expr
	|	expr Land expr
	|	expr Lor expr
	|	expr Qm expr Colon expr // condition ? t : f
	;

primary
	:	Paren expr Nerap 
	|	literal
	;
		
literal
	:	IdentifierLiteral
	|	IntegerLiteral
	|	BinaryIntegerLiteral
	|	HexIntegerLiteral
	|	BigIntegerLiteral
	|	FloatingPointLiteral
	|	BigFloatingPointLiteral
	|	TextLiteral
	|	PathLiteral
	|	DateTimeLiteral
	|	DateLiteral
	|	TimeLiteral
	|	True
	|	False
	|	Null
	;


identifier: IdentifierLiteral;

// Lexer

WS  :  [ \t\r\n\u000C]+ -> skip
	;

COMMENT
	:	'/*' .*? '*/' -> skip
	;

LINE_COMMENT
	:	'//' ~[\r\n]* -> skip
	;



// I D E N T I F I E R

IdentifierLiteral: [A-Za-z_][0-9A-Za-z_]* ;

// P A T H


PathLiteral
	:	'`' ~[`]*  '`'
	;


// C A R D I N A L I T Y

cardinality : Cardinality ;

Cardinality
	:	Integer '..' (Integer | '*')
	;



// T E X T

TextLiteral
	:	'"' TextCharacter*  '"'
	;

fragment TextCharacter
	:	~["\\]
	|	EscapeSequence
	;

fragment EscapeSequence
	:	'\\' [tn"\\]
	|	UnicodeEscape
	;

fragment UnicodeEscape
	:	'\\' 'u' HexDigit HexDigit? HexDigit? HexDigit?
	;
	

// T E M P O R A L   C O N S T R U C T S

DateTimeLiteral
	:	'@' Date WS Time
	;

DateLiteral
	:	'@' Date
	;

TimeLiteral
	:	'@' Time
	;

// yyyy/mm/dd
fragment Date
	:	Digit+ '/' Digit+ '/' Digit+
	;

// hh:mm[:ss[:ms]]
fragment Time
	:	Digit+ ':' Digit+ ( ':' Digit+ (':' Digit+)? )?
	;


// F L O A T I N G   P O I N T S

BigFloatingPointLiteral
	:	Integer 'F'
	|	Integer '.' Digit+ 'F'
	|	Integer ('.' Digit+)? ExponentPart 'F'
	;

FloatingPointLiteral
	:	Integer 'f'
	|	Integer '.' Digit+ 'f'?
	|	Integer ('.' Digit+)? ExponentPart 'f'?
	;

fragment ExponentPart
	:	'e' SignedInteger
	;

fragment SignedInteger
	:	'0'
	|	Minus? Digits
	;
	
// I N T E G E R S

// Decimal

BigIntegerLiteral
	:	Integer 'Z'
	;

IntegerLiteral
	:	Integer 'z'?
	;

fragment Integer
	:	'0'
	|	Digits
	;

fragment Digits
	:	NonZeroDigit (DigitOrUnderscore* Digit)?
	;

fragment Digit
	:	[0-9]
	;

fragment NonZeroDigit
	:	[1-9]
	;

fragment DigitOrUnderscore
	:	[0-9_]
	;


// Binary

BinaryIntegerLiteral
	:	'0b' BinaryDigits
	;

fragment BinaryDigits
	:	BinaryDigit (BinaryDigitOrUnderscore* BinaryDigit)?
	;

fragment BinaryDigit
	:	[01]
	;

fragment BinaryDigitOrUnderscore
	:	[01_]
	;


// Hexadecimal

HexIntegerLiteral
	:	'0x' HexDigits
	;

fragment HexDigits
	:	HexDigit (HexDigitOrUnderscore* HexDigit)?
	;

fragment HexDigitOrUnderscore
	:	[0-9a-fA-F_]
	;

fragment HexDigit
	:	[0-9a-fA-F]
	;


