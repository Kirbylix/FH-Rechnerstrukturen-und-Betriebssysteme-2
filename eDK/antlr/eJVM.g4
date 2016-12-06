grammar eJVM;

WS: [ \t];

LINEFEED: '\n';

CHARACTER: [_A-Za-z];

DIGIT: [0-9];

fragment
HEXDIGIT: [0-9A-Fa-f];

HEXNUMBER: HEX_SIGN HEXDIGIT+;

SIGN: [+-];

SPECIAL: [.:!?*#~/§$%&];

LPAR: '(';

RPAR: ')';

CS: ';';

fragment
HEX_SIGN: '0x';

newline: LINEFEED;

hexLiteral: HEXNUMBER;

decimalLiteral: SIGN? DIGIT+;

numberLiteral: hexLiteral | decimalLiteral;

all_char: (WS | CS | CHARACTER | DIGIT | SIGN | SPECIAL | LPAR | RPAR);

identifier: CHARACTER (CHARACTER | DIGIT)*;

string: '"' all_char* '"';

program: emptyLine* programDeclaration emptyLine* constantsSection? emptyLine* errorMessageTable? emptyLine* method (emptyLine | method)* emptyLine*;

programDeclaration: WS* '.program' WS+ programName WS* comment* newline;

programName: CHARACTER (WS | CHARACTER | DIGIT)*;

comment: (CS all_char*);

emptyLine: WS* comment* newline;

constantsSection: WS* '.constants' newline? constantDefinition* WS* '.end-constants' WS* newline;

constantDefinition: WS* identifier WS+ (numberLiteral) WS* comment* newline;

errorMessageTable: WS* '.errors' newline? errorMessageDefinition* WS* '.end-errors' newline;

errorMessageDefinition:  WS* identifier WS+ string WS* comment* newline;

method: WS* '.method' WS+ identifier WS* '(' (parameterList | parameterListEnd) newline+ localVariablesSection? emptyLine* WS* methodCode emptyLine* '.end-method' WS* comment* newline;

parameterList: WS* (identifier WS* ',' WS*)* identifier parameterListEnd;

parameterListEnd: WS* ')';

localVariablesSection: WS* '.vars' newline? localVariableDefinition+ WS* '.end-vars' newline;

localVariableDefinition: WS* identifier WS* comment* newline;

methodCode: emptyLine* codeLine (emptyLine | codeLine)*;

codeLine: WS* label? WS+ instruction (WS | argument)* WS* comment* newline;

label: identifier ':';

instruction: identifier;

argument: numberLiteral | identifier;