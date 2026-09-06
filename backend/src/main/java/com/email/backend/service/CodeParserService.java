package com.email.backend.service;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class CodeParserService {

    public ParsedFile parseFile(String content, String language) {
        if (!"java".equalsIgnoreCase(language)) {
            throw new UnsupportedOperationEcxception("Language not supported yet:" + language);
        }
        JavaLexer lexer = new JavaLexer(CharStreams.fromString(content));
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        JavaParser parser = new JavaParser(tokens);
        ParseTree tree = parser.compilationUnit();

        JavaASTVisitor visitor = new JavaASTVisitor();
        visitor.visit(tree);

        return ParsedFile.builder().language(language).packageName(visitor.getPackageName()).imports(visitor.getImports()).classes(visitor.getMethods).dependencies(visitor.getDependencies()).sbuild();

    }
    private static class JavaASTVisitor extends JAvaParserBaseVisitor<Void>{
        private String packageName;
        private final Set<String> imports = new HashSet<>();
        private final List<String> classes= new ArrayList<>();
        private List<String>  methods= new ArrayList<>();
        private final Set<String> dependencies= new HashSet<>();

        public String getPackageName() {
            return packageName;
        }

        public Set<String> getImports() {
            return imports;
        }

        public List<String> getClasses() {
            return classes;
        }

        public List<String> getMethods() {
            return methods;
        }

        public void setMethods(List<String> methods) {
            this.methods = methods;
        }

        public Set<String> getDependencies() {
            return dependencies;
        }

        @Override
        public Void visitImportDeclaration(JavaParser.ImportDeclarationContext ctx) {
            String imported = ctx.qualifiedName().getText();
            imports.add(imported);
            dependencies.add(imported);
            return super.visitImportDeclaration(ctx);
        }

        @Override
        public Void visitClassDeclaration(JavaParser.ClassDeclarationContext ctx) {
            String className = ctx.identifier().getText();
            classes.add(className);

            // Extract extended class dependency
            if (ctx.typeType() != null) {
                dependencies.add(ctx.typeType().getText());
            }
            // Extract implemented interface dependencies
            if (ctx.typeList() != null) {
                ctx.typeList().typeType().forEach(t -> dependencies.add(t.getText()));
            }

            return super.visitClassDeclaration(ctx);
        }
        @Override
        public Void visitMethodDeclaration(JavaParser.MethodDeclarationContext ctx) {
            String methodName = ctx.identifier().getText();
            String returnType = ctx.typeTypeOrVoid().getText();
            String parameters = ctx.formalParameters().getText();

            methods.add(returnType + " " + methodName + parameters);
            return super.visitMethodDeclaration(ctx);
        }
    }
}
