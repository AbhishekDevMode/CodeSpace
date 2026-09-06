grammar JavaScript;

program
    : statement* EOF
    ;

statement
    : importStatement
    | classDeclaration
    | functionDeclaration
    | variableStatement
    | emptyStatement
    ;

importStatement
    : 'import' importClause 'from' STRING ';'
    | 'import' STRING ';'
    ;

importClause
    : IDENTIFIER
    | '{' IDENTIFIER (',' IDENTIFIER)* '}'
    | '*' 'as' IDENTIFIER
    ;

classDeclaration
    : 'class' IDENTIFIER ('extends' IDENTIFIER)? classBody
    ;

classBody
    : '{' classElement* '}'
    ;

classElement
    : methodDefinition
    | fieldDefinition
    | emptyStatement
    ;

fieldDefinition
    : IDENTIFIER ('=' expression)? ';'
    ;

methodDefinition
    : IDENTIFIER formalParameters '{' statement* '}'
    ;

functionDeclaration
    : 'function' IDENTIFIER formalParameters '{' statement* '}'
    ;

formalParameters
    : '(' parameterList? ')'
    ;

parameterList
    : IDENTIFIER (',' IDENTIFIER)*
    ;

variableStatement
    : ('var' | 'let' | 'const') IDENTIFIER ('=' expression)? ';'
    ;

emptyStatement
    : ';'
    ;

expression
    : IDENTIFIER
    | STRING
    | NUMBER
    ;

IDENTIFIER: [a-zA-Z_$][a-zA-Z0-9_$]* ;
STRING: '"' .*? '"' | '\'' .*? '\'' | '`' .*? '`' ;
NUMBER: [0-9]+ ;
WS: [ \t\r\n]+ -> skip ;
COMMENT: '//' ~[\r\n]* -> skip ;
BLOCK_COMMENT: '/*' .*? '*/' -> skip ;
