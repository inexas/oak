/**
 * 'Oak'
 *
 * This grammar is basically JSON with a few things ironed out
 *
 * * Comments are allowed, same as Java
 *
 * * Keys don't have "quotes"
 *
 * + Expressions are supported: "TimeoutInMs: 60*60*1000 "
 *
 */
grammar Oak;

/**
 * This is the root of Oak
 */
oak : pair;

pair
	:	Key Colon value Semi	// E.g. myKey: 32; 
	|	Key object				// E.g. MyKey { ... }
	|	Key array				// E.g. myKey [ 1, 2, 3 ]
	|	Key Semi				// Shorthand for myKey: true; 
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
	|	path
	|	expression
	|	cardinality
	;

/**
 * This is the root for expression evaluation.
 * 
 * The members is in operator precedence order
 */
expression
	:	primary
	|	Key Paren ( expression ( Comma expression)* )? Nerap // Function
	|	(Plus|Minus|Not|Comp) expression // Unary
	|	expression (Multiply|Divide|Mod) expression
	|	expression (Plus|Minus) expression
	|	expression (Shl|Shr|Usr) expression
	|	expression (Lte | Gte | Gt | Lt) expression
	|	expression (Eq | Ne) expression
	|	expression And expression
	|	expression Xor expression
	|	expression Or expression
	|	expression Land expression
	|	expression Lor expression
	|	expression Qm expression Colon expression // condition ? t : f
;

primary
	:	Paren expression Nerap 
	|	literal
	;
		
literal
	:	IntegerLiteral
	|	FloatingPointLiteral
	|	BigDecimalLiteral
	|	StringLiteral
	|	DateTimeLiteral
	|	True
	|	False
	|	Null
	;

path: Path | Key | Divide;

cardinality: Cardinality;

WS  :  [ \t\r\n\u000C]+ -> skip
	;

COMMENT
	:	'/*' .*? '*/' -> skip
	;

LINE_COMMENT
	:	'//' ~[\r\n]* -> skip
	;


Path
	:	Children					// Children of root and children and root 
	|	( '/' Key)+ Children?		// Absolute path
	|	Key ( '/' Key)+ Children?	// Relative path
	;

fragment Children
	:	'/@'		// Children of node
	|	'/@@'		// Children and node
	;

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
Dots:		'..';


Key: [A-Za-z_][0-9A-Za-z_]* ;

Cardinality
	:	DecimalIntegerLiteral '..' (DecimalIntegerLiteral | '*')
	;

Exponent: ('e'|'E') '-'? [1-9] Digit* ;


IntegerLiteral
	:	DecimalIntegerLiteral
	|	HexIntegerLiteral
	|	BinaryIntegerLiteral
	;

fragment HexIntegerLiteral
	:	HexNumeral
	;

fragment BinaryIntegerLiteral
	:	BinaryNumeral
	;

fragment DecimalIntegerLiteral
	:	'0'
	|	NonZeroDigit (Digits? | Underscores Digits)
	;

fragment Digits
	:	Digit (DigitOrUnderscore* Digit)?
	;

fragment Digit
	:	'0'
	|	NonZeroDigit
	;

fragment NonZeroDigit
	:	[1-9]
	;

fragment DigitOrUnderscore
	:	Digit
	|	'_'
	;

fragment Underscores
	:	'_'+
	;

fragment HexNumeral
	:	'0' [xX] HexDigits
	;

fragment HexDigits
	:	HexDigit (HexDigitOrUnderscore* HexDigit)?
	;

fragment HexDigit
	:	[0-9a-fA-F]
	;

fragment HexDigitOrUnderscore
	:	HexDigit
	|	'_'
	;

fragment BinaryNumeral
	:	'0' [bB] BinaryDigits
	;

fragment BinaryDigits
	:	BinaryDigit (BinaryDigitOrUnderscore* BinaryDigit)?
	;

fragment BinaryDigit
	:	[01]
	;

fragment BinaryDigitOrUnderscore
	:	BinaryDigit
	|	'_'
	;

FloatingPointLiteral
	:	DecimalFloatingPointLiteral
	|	HexadecimalFloatingPointLiteral
	;

BigDecimalLiteral
	:	'0' [sS] DecimalFloatingPointLiteral
	;

fragment DecimalFloatingPointLiteral
	:	Digits '.' Digits? ExponentPart?
	|	'.' Digits ExponentPart?
	|	Digits ExponentPart
	;

fragment ExponentPart
	:	ExponentIndicator SignedInteger
	;

fragment ExponentIndicator
	:	[eE]
	;

fragment SignedInteger
	:	Sign? Digits
	;

fragment Sign
	:	[+-]
	;

fragment HexadecimalFloatingPointLiteral
	:	HexSignificand BinaryExponent
	;

fragment HexSignificand
	:	HexNumeral '.'?
	|	'0' [xX] HexDigits? '.' HexDigits
	;

fragment BinaryExponent
	:	BinaryExponentIndicator SignedInteger
	;

fragment BinaryExponentIndicator
	:	[pP]
	;

StringLiteral
	:	'"' StringCharacter*  '"'
	;

DateTimeLiteral
	:	'@' Date WS Time
	|	'@' Date
	|	'@' Time
	;

// yyyy/mm/dd
fragment Date
	:	Digit+ '/' Digit+ '/' Digit+
	;

// hh:mm[:ss[:ms]]
fragment Time
	:	Digit+ ':' Digit+ ( ':' Digit+ (':' Digit+)? )?
	;

fragment StringCharacter
	:	~["\\]
	|	EscapeSequence
	;

// Unused
fragment SingleCharacter
	:	~['\\]
	;

fragment EscapeSequence
	:	'\\' [btnfr"'\\]
	|	UnicodeEscape
	;

fragment UnicodeEscape
	:	'\\' 'u' HexDigit HexDigit HexDigit HexDigit
	;

