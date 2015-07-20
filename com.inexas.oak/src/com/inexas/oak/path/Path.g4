/**
 * Recognizer for paths, e.g. role:/sys/admin
 */
grammar Path;


path: protocol ? elementList ;
 
protocol: Key Colon;
 
elementList
	:	Switch Recurse ?						// Root
	|	(Switch element )+ recurse?				// Absolute path
	|	element (Switch element )* recurse?		// Relative path
	;
 
element:	key selector? ;

key: Key | Parent | Self ;
 
selector: Square ( Key | Posint ) Erauqs ;

recurse: Switch Recurse;
 
Key: [A-Za-z][A-Za-z0-9_]* ;
 
Posint: ( '0' | ([1-9][0-9]*) ) ;

Colon: ':';

Switch: '/' ;

Recurse: '@' '@'?;

Self: '.' ;

Parent: '..' ;

Square: '[' ;

Erauqs: ']' ;
