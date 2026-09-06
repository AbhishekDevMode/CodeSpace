package com.email.backend.parser.js;

import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;

public class JavaScriptParserBaseVisitor<T> extends AbstractParseTreeVisitor<T> implements JavaScriptParserVisitor<T> {

    @Override
    public T visitProgram(JavaScriptParser.ProgramContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public T visitImportStatement(JavaScriptParser.ImportStatementContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public T visitClassDeclaration(JavaScriptParser.ClassDeclarationContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public T visitFunctionDeclaration(JavaScriptParser.FunctionDeclarationContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public T visitFieldDefinition(JavaScriptParser.FieldDefinitionContext ctx) {
        return visitChildren(ctx);
    }
}
