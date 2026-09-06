package com.email.backend.parser.java;

import org.antlr.v4.runtime.tree.ParseTreeVisitor;

public interface JavaParserVisitor<T> extends ParseTreeVisitor<T> {
    T visitCompilationUnit(JavaParser.CompilationUnitContext ctx);
    T visitPackageDeclaration(JavaParser.PackageDeclarationContext ctx);
    T visitImportDeclaration(JavaParser.ImportDeclarationContext ctx);
    T visitClassDeclaration(JavaParser.ClassDeclarationContext ctx);
    T visitMethodDeclaration(JavaParser.MethodDeclarationContext ctx);
    T visitFieldDeclaration(JavaParser.FieldDeclarationContext ctx);
}
