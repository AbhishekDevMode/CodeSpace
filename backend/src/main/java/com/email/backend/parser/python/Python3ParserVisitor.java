package com.email.backend.parser.python;

import org.antlr.v4.runtime.tree.ParseTreeVisitor;

public interface Python3ParserVisitor<T> extends ParseTreeVisitor<T> {
    T visitFile_input(Python3Parser.File_inputContext ctx);
    T visitImport_stmt(Python3Parser.Import_stmtContext ctx);
    T visitClassdef(Python3Parser.ClassdefContext ctx);
    T visitFuncdef(Python3Parser.FuncdefContext ctx);
    T visitExpr_stmt(Python3Parser.Expr_stmtContext ctx);
}
