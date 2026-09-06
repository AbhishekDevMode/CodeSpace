package com.email.backend.parser.python;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import java.util.ArrayList;
import java.util.List;

public class Python3Parser extends Parser {

    public Python3Parser(TokenStream input) {
        super(input);
    }

    @Override
    public String[] getRuleNames() {
        return new String[] { "file_input", "import_stmt", "classdef", "funcdef", "expr_stmt" };
    }

    @Override
    @Deprecated
    public String[] getTokenNames() {
        return new String[0];
    }

    @Override
    public Vocabulary getVocabulary() { return Python3Lexer.VOCABULARY; }

    @Override
    public String getGrammarFileName() { return "Python3.g4"; }

    @Override
    public org.antlr.v4.runtime.atn.ATN getATN() { return null; }

    public static class File_inputContext extends ParserRuleContext {
        public List<Import_stmtContext> imports = new ArrayList<>();
        public List<ClassdefContext> classes = new ArrayList<>();
        public List<FuncdefContext> functions = new ArrayList<>();
        public List<Expr_stmtContext> fields = new ArrayList<>();

        public File_inputContext(ParserRuleContext parent, int invokingState) { super(parent, invokingState); }
        @Override public int getRuleIndex() { return 0; }
        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof Python3ParserVisitor) return ((Python3ParserVisitor<? extends T>)visitor).visitFile_input(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Import_stmtContext extends ParserRuleContext {
        public String importedModule;
        public Import_stmtContext(ParserRuleContext parent, int invokingState) { super(parent, invokingState); }
        @Override public int getRuleIndex() { return 1; }
        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof Python3ParserVisitor) return ((Python3ParserVisitor<? extends T>)visitor).visitImport_stmt(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class ClassdefContext extends ParserRuleContext {
        public String className;
        public String superClass;
        public ClassdefContext(ParserRuleContext parent, int invokingState) { super(parent, invokingState); }
        @Override public int getRuleIndex() { return 2; }
        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof Python3ParserVisitor) return ((Python3ParserVisitor<? extends T>)visitor).visitClassdef(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class FuncdefContext extends ParserRuleContext {
        public String functionName;
        public String parameters;
        public String returnType;
        public FuncdefContext(ParserRuleContext parent, int invokingState) { super(parent, invokingState); }
        @Override public int getRuleIndex() { return 3; }
        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof Python3ParserVisitor) return ((Python3ParserVisitor<? extends T>)visitor).visitFuncdef(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Expr_stmtContext extends ParserRuleContext {
        public String fieldName;
        public Expr_stmtContext(ParserRuleContext parent, int invokingState) { super(parent, invokingState); }
        @Override public int getRuleIndex() { return 4; }
        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof Python3ParserVisitor) return ((Python3ParserVisitor<? extends T>)visitor).visitExpr_stmt(this);
            else return visitor.visitChildren(this);
        }
    }

    public File_inputContext file_input() {
        File_inputContext _localctx = new File_inputContext(_ctx, getState());
        enterRule(_localctx, 0, 0);
        return _localctx;
    }
}
