package com.email.backend.parser.js;

import org.antlr.v4.runtime.tree.ParseTreeVisitor;

public interface JavaScriptParserVisitor<T> extends ParseTreeVisitor<T> {
    T visitProgram(JavaScriptParser.ProgramContext ctx);
    T visitImportStatement(JavaScriptParser.ImportStatementContext ctx);
    T visitClassDeclaration(JavaScriptParser.ClassDeclarationContext ctx);
    T visitFunctionDeclaration(JavaScriptParser.FunctionDeclarationContext ctx);
    T visitFieldDefinition(JavaScriptParser.FieldDefinitionContext ctx);
}
