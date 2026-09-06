grammar Java;

compilationUnit
    : packageDeclaration? importDeclaration* typeDeclaration* EOF
    ;

packageDeclaration
    : 'package' qualifiedName ';'
    ;

importDeclaration
    : 'import' 'static'? qualifiedName ('.' '*')? ';'
    ;

typeDeclaration
    : classDeclaration
    | interfaceDeclaration
    | ';'
    ;

classDeclaration
    : 'public'? 'abstract'? 'final'? 'class' IDENTIFIER ('extends' typeType)? ('implements' typeList)? classBody
    ;

interfaceDeclaration
    : 'public'? 'interface' IDENTIFIER ('extends' typeList)? classBody
    ;

typeList
    : typeType (',' typeType)*
    ;

classBody
    : '{' classBodyDeclaration* '}'
    ;

classBodyDeclaration
    : ';'
    | memberDeclaration
    ;

memberDeclaration
    : fieldDeclaration
    | methodDeclaration
    | classDeclaration
    ;

fieldDeclaration
    : typeType IDENTIFIER ('=' expression)? ';'
    ;

methodDeclaration
    : (typeType | 'void') IDENTIFIER formalParameters ';'
    | (typeType | 'void') IDENTIFIER formalParameters classBody
    ;

formalParameters
    : '(' formalParameterList? ')'
    ;

formalParameterList
    : formalParameter (',' formalParameter)*
    ;

formalParameter
    : typeType IDENTIFIER
    ;

qualifiedName
    : IDENTIFIER ('.' IDENTIFIER)*
    ;

typeType
    : IDENTIFIER ('<' typeType '>')?
    | 'int' | 'long' | 'boolean' | 'double' | 'float' | 'void' | 'String'
    ;

expression
    : IDENTIFIER
    | STRING_LITERAL
    | NUMBER
    ;

IDENTIFIER: [a-zA-Z_][a-zA-Z0-9_]* ;
STRING_LITERAL: '"' .*? '"' ;
NUMBER: [0-9]+ ;
WS: [ \t\r\n]+ -> skip ;
COMMENT: '//' ~[\r\n]* -> skip ;
BLOCK_COMMENT: '/*' .*? '*/' -> skip ;
