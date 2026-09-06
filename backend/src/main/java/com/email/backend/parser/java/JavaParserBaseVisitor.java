package com.email.backend.parser.java;

import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;

public class JavaParserBaseVisitor<T> extends AbstractParseTreeVisitor<T> implements JavaParserVisitor<T> {

    @Override
    public T visitCompilationUnit(JavaParser.CompilationUnitContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public T visitPackageDeclaration(JavaParser.PackageDeclarationContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public T visitImportDeclaration(JavaParser.ImportDeclarationContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public T visitClassDeclaration(JavaParser.ClassDeclarationContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public T visitMethodDeclaration(JavaParser.MethodDeclarationContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public T visitFieldDeclaration(JavaParser.FieldDeclarationContext ctx) {
        return visitChildren(ctx);
    }
}
