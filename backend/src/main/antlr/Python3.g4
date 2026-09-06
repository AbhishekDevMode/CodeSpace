grammar Python3;

file_input
    : (stmt | NEWLINE)* EOF
    ;

stmt
    : simple_stmt
    | compound_stmt
    ;

simple_stmt
    : small_stmt (';' small_stmt)* ';'? NEWLINE
    ;

small_stmt
    : import_stmt
    | expr_stmt
    ;

import_stmt
    : import_name
    | import_from
    ;

import_name
    : 'import' dotted_as_names
    ;

import_from
    : 'from' dotted_name 'import' ('*' | import_as_names)
    ;

dotted_as_names
    : dotted_as_name (',' dotted_as_name)*
    ;

dotted_as_name
    : dotted_name ('as' IDENTIFIER)?
    ;

import_as_names
    : import_as_name (',' import_as_name)*
    ;

import_as_name
    : IDENTIFIER ('as' IDENTIFIER)?
    ;

dotted_name
    : IDENTIFIER ('.' IDENTIFIER)*
    ;

expr_stmt
    : IDENTIFIER ('=' expression)?
    ;

compound_stmt
    : classdef
    | funcdef
    ;

classdef
    : 'class' IDENTIFIER ('(' arglist? ')')? ':' suite
    ;

funcdef
    : 'def' IDENTIFIER parameters ( '->' IDENTIFIER )? ':' suite
    ;

parameters
    : '(' parameter_list? ')'
    ;

parameter_list
    : IDENTIFIER (',' IDENTIFIER)*
    ;

suite
    : simple_stmt
    | NEWLINE stmt+
    ;

arglist
    : IDENTIFIER (',' IDENTIFIER)*
    ;

expression
    : IDENTIFIER
    | STRING
    | NUMBER
    ;

IDENTIFIER: [a-zA-Z_][a-zA-Z0-9_]* ;
STRING: '"' .*? '"' | '\'' .*? '\'' ;
NUMBER: [0-9]+ ;
NEWLINE: [\r\n]+ ;
WS: [ \t]+ -> skip ;
COMMENT: '#' ~[\r\n]* -> skip ;
