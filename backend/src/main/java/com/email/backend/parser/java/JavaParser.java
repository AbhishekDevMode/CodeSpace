package com.email.backend.parser.java;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import java.util.ArrayList;
import java.util.List;

public class JavaParser extends Parser {

    public JavaParser(TokenStream input) {
        super(input);
    }

    @Override
    public String[] getRuleNames() {
        return new String[] { "compilationUnit", "packageDeclaration", "importDeclaration", "classDeclaration", "methodDeclaration", "fieldDeclaration" };
    }

    @Override
    @Deprecated
    public String[] getTokenNames() {
        return new String[0];
    }

    @Override
    public Vocabulary getVocabulary() {
        return JavaLexer.VOCABULARY;
    }

    @Override
    public String getGrammarFileName() { return "Java.g4"; }

    @Override
    public org.antlr.v4.runtime.atn.ATN getATN() { return null; }

    public static class CompilationUnitContext extends ParserRuleContext {
        public PackageDeclarationContext packageDeclaration() { return getRuleContext(PackageDeclarationContext.class, 0); }
        public List<ImportDeclarationContext> importDeclaration() { return getRuleContexts(ImportDeclarationContext.class); }
        public List<ClassDeclarationContext> classDeclaration() { return getRuleContexts(ClassDeclarationContext.class); }

        public CompilationUnitContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }
        @Override public int getRuleIndex() { return 0; }
        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof JavaParserVisitor) return ((JavaParserVisitor<? extends T>)visitor).visitCompilationUnit(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class PackageDeclarationContext extends ParserRuleContext {
        public String packageName;
        public PackageDeclarationContext(ParserRuleContext parent, int invokingState) { super(parent, invokingState); }
        @Override public int getRuleIndex() { return 1; }
        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof JavaParserVisitor) return ((JavaParserVisitor<? extends T>)visitor).visitPackageDeclaration(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class ImportDeclarationContext extends ParserRuleContext {
        public String importedName;
        public ImportDeclarationContext(ParserRuleContext parent, int invokingState) { super(parent, invokingState); }
        @Override public int getRuleIndex() { return 2; }
        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof JavaParserVisitor) return ((JavaParserVisitor<? extends T>)visitor).visitImportDeclaration(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class ClassDeclarationContext extends ParserRuleContext {
        public String className;
        public String superClassName;
        public List<String> interfaces = new ArrayList<>();
        public List<MethodDeclarationContext> methods = new ArrayList<>();
        public List<FieldDeclarationContext> fields = new ArrayList<>();

        public ClassDeclarationContext(ParserRuleContext parent, int invokingState) { super(parent, invokingState); }
        @Override public int getRuleIndex() { return 3; }
        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof JavaParserVisitor) return ((JavaParserVisitor<? extends T>)visitor).visitClassDeclaration(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class MethodDeclarationContext extends ParserRuleContext {
        public String methodName;
        public String returnType;
        public String parameters;

        public MethodDeclarationContext(ParserRuleContext parent, int invokingState) { super(parent, invokingState); }
        @Override public int getRuleIndex() { return 4; }
        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof JavaParserVisitor) return ((JavaParserVisitor<? extends T>)visitor).visitMethodDeclaration(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class FieldDeclarationContext extends ParserRuleContext {
        public String fieldName;
        public String fieldType;

        public FieldDeclarationContext(ParserRuleContext parent, int invokingState) { super(parent, invokingState); }
        @Override public int getRuleIndex() { return 5; }
        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof JavaParserVisitor) return ((JavaParserVisitor<? extends T>)visitor).visitFieldDeclaration(this);
            else return visitor.visitChildren(this);
        }
    }

    public CompilationUnitContext compilationUnit() {
        CompilationUnitContext _localctx = new CompilationUnitContext(_ctx, getState());
        enterRule(_localctx, 0, 0);
        return _localctx;
    }
}
