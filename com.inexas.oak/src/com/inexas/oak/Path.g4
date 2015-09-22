/**
 * NOTE: This is no longer used as we parse paths by 'hand' as it's easier
 * and faster. But it does serve to document the BNF.
 * 
 * Recognizer for paths, e.g. role:/sys/admin
 */
grammar Path;


path: protocol? elementList recurse? ;
 
protocol: Identifier Colon;
 
elementList
	:	Switch
	|	Switch? element (Switch element )*
	;
 
element: part selector? ;

part: Identifier | Parent | Self ;
 
selector: Square ( Identifier | Posint ) Erauqs ;

recurse: Recurse;
 
Identifier: [A-Za-z][A-Za-z0-9_]* ;
 
Posint: ( '0' | ([1-9][0-9]*) ) ;

Colon: ':';

Switch: '/' ;

Recurse: '*' '*'?;

Self: '.' ;

Parent: '..' ;

Square: '[' ;

Erauqs: ']' ;
