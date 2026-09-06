package com.email.backend.parser.js;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import java.util.ArrayList;
import java.util.List;

public class JavaScriptParser extends Parser {

    public JavaScriptParser(TokenStream input) {
        super(input);
    }

    @Override
    public String[] getRuleNames() {
        return new String[] { "program", "importStatement", "classDeclaration", "functionDeclaration", "fieldDefinition" };
    }

    @Override
    @Deprecated
    public String[] getTokenNames() {
        return new String[0];
    }

    @Override
    public Vocabulary getVocabulary() { return JavaScriptLexer.VOCABULARY; }

    @Override
    public String getGrammarFileName() { return "JavaScript.g4"; }

    @Override
    public org.antlr.v4.runtime.atn.ATN getATN() { return null; }

    public static class ProgramContext extends ParserRuleContext {
        public List<ImportStatementContext> imports = new ArrayList<>();
        public List<ClassDeclarationContext> classes = new ArrayList<>();
        public List<FunctionDeclarationContext> functions = new ArrayList<>();
        public List<FieldDefinitionContext> fields = new ArrayList<>();

        public ProgramContext(ParserRuleContext parent, int invokingState) { super(parent, invokingState); }
        @Override public int getRuleIndex() { return 0; }
        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof JavaScriptParserVisitor) return ((JavaScriptParserVisitor<? extends T>)visitor).visitProgram(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class ImportStatementContext extends ParserRuleContext {
        public String modulePath;
        public String importedSymbols;
        public ImportStatementContext(ParserRuleContext parent, int invokingState) { super(parent, invokingState); }
        @Override public int getRuleIndex() { return 1; }
        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof JavaScriptParserVisitor) return ((JavaScriptParserVisitor<? extends T>)visitor).visitImportStatement(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class ClassDeclarationContext extends ParserRuleContext {
        public String className;
        public String superClass;
        public ClassDeclarationContext(ParserRuleContext parent, int invokingState) { super(parent, invokingState); }
        @Override public int getRuleIndex() { return 2; }
        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof JavaScriptParserVisitor) return ((JavaScriptParserVisitor<? extends T>)visitor).visitClassDeclaration(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class FunctionDeclarationContext extends ParserRuleContext {
        public String functionName;
        public String parameters;
        public FunctionDeclarationContext(ParserRuleContext parent, int invokingState) { super(parent, invokingState); }
        @Override public int getRuleIndex() { return 3; }
        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof JavaScriptParserVisitor) return ((JavaScriptParserVisitor<? extends T>)visitor).visitFunctionDeclaration(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class FieldDefinitionContext extends ParserRuleContext {
        public String fieldName;
        public FieldDefinitionContext(ParserRuleContext parent, int invokingState) { super(parent, invokingState); }
        @Override public int getRuleIndex() { return 4; }
        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof JavaScriptParserVisitor) return ((JavaScriptParserVisitor<? extends T>)visitor).visitFieldDefinition(this);
            else return visitor.visitChildren(this);
        }
    }

    public ProgramContext program() {
        ProgramContext _localctx = new ProgramContext(_ctx, getState());
        enterRule(_localctx, 0, 0);
        return _localctx;
    }
}
