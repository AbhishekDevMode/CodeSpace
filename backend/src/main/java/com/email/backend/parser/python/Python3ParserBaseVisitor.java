package com.email.backend.parser.python;

import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;

public class Python3ParserBaseVisitor<T> extends AbstractParseTreeVisitor<T> implements Python3ParserVisitor<T> {

    @Override
    public T visitFile_input(Python3Parser.File_inputContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public T visitImport_stmt(Python3Parser.Import_stmtContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public T visitClassdef(Python3Parser.ClassdefContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public T visitFuncdef(Python3Parser.FuncdefContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public T visitExpr_stmt(Python3Parser.Expr_stmtContext ctx) {
        return visitChildren(ctx);
    }


}
