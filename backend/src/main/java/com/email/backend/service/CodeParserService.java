package com.email.backend.service;

import com.email.backend.parser.java.JavaLexer;
import com.email.backend.parser.java.JavaParser;
import com.email.backend.parser.java.JavaParserBaseVisitor;
import com.email.backend.parser.js.JavaScriptLexer;
import com.email.backend.parser.js.JavaScriptParser;
import com.email.backend.parser.js.JavaScriptParserBaseVisitor;
import com.email.backend.parser.python.Python3Lexer;
import com.email.backend.parser.python.Python3Parser;
import com.email.backend.parser.python.Python3ParserBaseVisitor;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CodeParserService {

    public ParsedFile parseFile(String content, String language) {
        if (content == null) content = "";
        String langLower = (language != null) ? language.toLowerCase() : "";

        switch (langLower) {
            case "java":
                return parseJava(content);
            case "python":
            case "py":
                return parsePython(content);
            case "javascript":
            case "js":
                return parseJavaScript(content);
            default:
                throw new UnsupportedOperationException("Unsupported language for parsing: " + language);
        }
    }

    private ParsedFile parseJava(String content) {
        JavaLexer lexer = new JavaLexer(CharStreams.fromString(content));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        JavaParser parser = new JavaParser(tokens);

        JavaASTVisitor visitor = new JavaASTVisitor(content);
        visitor.analyze();

        ParsedFile result = new ParsedFile();
        result.setLanguage("java");
        result.setPackageName(visitor.getPackageName());
        result.setClasses(visitor.getClasses());
        result.setMethods(visitor.getMethods());
        result.setImports(visitor.getImports());
        result.setFields(visitor.getFields());
        result.setDependencies(visitor.getDependencies());
        return result;
    }

    private ParsedFile parsePython(String content) {
        Python3Lexer lexer = new Python3Lexer(CharStreams.fromString(content));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        Python3Parser parser = new Python3Parser(tokens);

        PythonASTVisitor visitor = new PythonASTVisitor(content);
        visitor.analyze();

        ParsedFile result = new ParsedFile();
        result.setLanguage("python");
        result.setClasses(visitor.getClasses());
        result.setMethods(visitor.getMethods());
        result.setImports(visitor.getImports());
        result.setFields(visitor.getFields());
        result.setDependencies(visitor.getDependencies());
        return result;
    }

    private ParsedFile parseJavaScript(String content) {
        JavaScriptLexer lexer = new JavaScriptLexer(CharStreams.fromString(content));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        JavaScriptParser parser = new JavaScriptParser(tokens);

        JavaScriptASTVisitor visitor = new JavaScriptASTVisitor(content);
        visitor.analyze();

        ParsedFile result = new ParsedFile();
        result.setLanguage("javascript");
        result.setClasses(visitor.getClasses());
        result.setMethods(visitor.getMethods());
        result.setImports(visitor.getImports());
        result.setFields(visitor.getFields());
        result.setDependencies(visitor.getDependencies());
        return result;
    }

    // Java AST Visitor
    public static class JavaASTVisitor extends JavaParserBaseVisitor<Void> {
        private String packageName = "";
        private final List<String> classes = new ArrayList<>();
        private final List<String> methods = new ArrayList<>();
        private final Set<String> imports = new HashSet<>();
        private final List<String> fields = new ArrayList<>();
        private final Set<String> dependencies = new HashSet<>();
        private final String content;

        public JavaASTVisitor(String content) {
            this.content = content;
        }

        public void analyze() {
            // Package extraction
            Matcher pkgMatcher = Pattern.compile("package\\s+([a-zA-Z0-9_.]+);").matcher(content);
            if (pkgMatcher.find()) {
                this.packageName = pkgMatcher.group(1);
            }

            // Import extraction
            Matcher impMatcher = Pattern.compile("import\\s+(?:static\\s+)?([a-zA-Z0-9_.]+);").matcher(content);
            while (impMatcher.find()) {
                String imp = impMatcher.group(1);
                imports.add(imp);
                dependencies.add(imp);
            }

            // Class extraction
            Matcher classMatcher = Pattern.compile("(?:public|protected|private)?\\s*class\\s+([a-zA-Z0-9_]+)(?:\\s+extends\\s+([a-zA-Z0-9_]+))?(?:\\s+implements\\s+([a-zA-Z0-9_,\\s]+))?").matcher(content);
            while (classMatcher.find()) {
                String className = classMatcher.group(1);
                classes.add(className);
                if (classMatcher.group(2) != null) {
                    dependencies.add(classMatcher.group(2));
                }
                if (classMatcher.group(3) != null) {
                    for (String iface : classMatcher.group(3).split(",")) {
                        dependencies.add(iface.trim());
                    }
                }
            }

            // Method extraction
            Matcher methodMatcher = Pattern.compile("(?:public|protected|private|static|final|native|synchronized|\\s)+([a-zA-Z0-9_<>]+)\\s+([a-zA-Z0-9_]+)\\s*\\(([^)]*)\\)").matcher(content);
            while (methodMatcher.find()) {
                String returnType = methodMatcher.group(1);
                String methodName = methodMatcher.group(2);
                String params = methodMatcher.group(3);
                if (!returnType.equals("class") && !returnType.equals("package") && !returnType.equals("if")) {
                    methods.add(returnType + " " + methodName + "(" + params + ")");
                }
            }

            // Field extraction
            Matcher fieldMatcher = Pattern.compile("(?:private|protected|public)\\s+([a-zA-Z0-9_<>]+)\\s+([a-zA-Z0-9_]+)\\s*(?:=.*)?;").matcher(content);
            while (fieldMatcher.find()) {
                String type = fieldMatcher.group(1);
                String name = fieldMatcher.group(2);
                fields.add(type + " " + name);
            }
        }

        @Override
        public Void visitClassDeclaration(JavaParser.ClassDeclarationContext ctx) {
            if (ctx.className != null) classes.add(ctx.className);
            return super.visitClassDeclaration(ctx);
        }

        @Override
        public Void visitMethodDeclaration(JavaParser.MethodDeclarationContext ctx) {
            if (ctx.methodName != null) {
                methods.add(ctx.returnType + " " + ctx.methodName + "(" + ctx.parameters + ")");
            }
            return super.visitMethodDeclaration(ctx);
        }

        @Override
        public Void visitImportDeclaration(JavaParser.ImportDeclarationContext ctx) {
            if (ctx.importedName != null) {
                imports.add(ctx.importedName);
                dependencies.add(ctx.importedName);
            }
            return super.visitImportDeclaration(ctx);
        }

        @Override
        public Void visitFieldDeclaration(JavaParser.FieldDeclarationContext ctx) {
            if (ctx.fieldName != null) {
                fields.add(ctx.fieldType + " " + ctx.fieldName);
            }
            return super.visitFieldDeclaration(ctx);
        }

        public String getPackageName() { return packageName; }
        public List<String> getClasses() { return classes; }
        public List<String> getMethods() { return methods; }
        public Set<String> getImports() { return imports; }
        public List<String> getFields() { return fields; }
        public Set<String> getDependencies() { return dependencies; }
    }

    // Python AST Visitor
    public static class PythonASTVisitor extends Python3ParserBaseVisitor<Void> {
        private final List<String> classes = new ArrayList<>();
        private final List<String> methods = new ArrayList<>();
        private final Set<String> imports = new HashSet<>();
        private final List<String> fields = new ArrayList<>();
        private final Set<String> dependencies = new HashSet<>();
        private final String content;

        public PythonASTVisitor(String content) {
            this.content = content;
        }

        public void analyze() {
            // Import extraction
            Matcher impMatcher = Pattern.compile("(?:from\\s+([a-zA-Z0-9_.]+)\\s+import\\s+([a-zA-Z0-9_,\\s*]+)|import\\s+([a-zA-Z0-9_.]+))").matcher(content);
            while (impMatcher.find()) {
                if (impMatcher.group(1) != null) {
                    String fromPkg = impMatcher.group(1);
                    imports.add(fromPkg);
                    dependencies.add(fromPkg);
                } else if (impMatcher.group(3) != null) {
                    String imp = impMatcher.group(3);
                    imports.add(imp);
                    dependencies.add(imp);
                }
            }

            // Class extraction
            Matcher classMatcher = Pattern.compile("class\\s+([a-zA-Z0-9_]+)(?:\\(([^)]*)\\))?:").matcher(content);
            while (classMatcher.find()) {
                String className = classMatcher.group(1);
                classes.add(className);
                if (classMatcher.group(2) != null && !classMatcher.group(2).trim().isEmpty()) {
                    dependencies.add(classMatcher.group(2).trim());
                }
            }

            // Function/Method extraction
            Matcher funcMatcher = Pattern.compile("def\\s+([a-zA-Z0-9_]+)\\s*\\(([^)]*)\\)(?:\\s*->\\s*([a-zA-Z0-9_]+))?:").matcher(content);
            while (funcMatcher.find()) {
                String funcName = funcMatcher.group(1);
                String params = funcMatcher.group(2);
                String retType = funcMatcher.group(3) != null ? funcMatcher.group(3) : "void";
                methods.add(retType + " " + funcName + "(" + params + ")");
            }

            // Field extraction (e.g. self.field = val or var: type = val)
            Matcher fieldMatcher = Pattern.compile("(?:self\\.)?([a-zA-Z0-9_]+)\\s*:\\s*([a-zA-Z0-9_]+)|self\\.([a-zA-Z0-9_]+)\\s*=").matcher(content);
            while (fieldMatcher.find()) {
                if (fieldMatcher.group(1) != null) {
                    fields.add(fieldMatcher.group(2) + " " + fieldMatcher.group(1));
                } else if (fieldMatcher.group(3) != null) {
                    fields.add("Object " + fieldMatcher.group(3));
                }
            }
        }

        @Override
        public Void visitClassdef(Python3Parser.ClassdefContext ctx) {
            if (ctx.className != null) classes.add(ctx.className);
            return super.visitClassdef(ctx);
        }

        @Override
        public Void visitFuncdef(Python3Parser.FuncdefContext ctx) {
            if (ctx.functionName != null) {
                methods.add((ctx.returnType != null ? ctx.returnType : "void") + " " + ctx.functionName + "(" + ctx.parameters + ")");
            }
            return super.visitFuncdef(ctx);
        }

        @Override
        public Void visitImport_stmt(Python3Parser.Import_stmtContext ctx) {
            if (ctx.importedModule != null) {
                imports.add(ctx.importedModule);
                dependencies.add(ctx.importedModule);
            }
            return super.visitImport_stmt(ctx);
        }

        public List<String> getClasses() { return classes; }
        public List<String> getMethods() { return methods; }
        public Set<String> getImports() { return imports; }
        public List<String> getFields() { return fields; }
        public Set<String> getDependencies() { return dependencies; }
    }

    // JavaScript AST Visitor
    public static class JavaScriptASTVisitor extends JavaScriptParserBaseVisitor<Void> {
        private final List<String> classes = new ArrayList<>();
        private final List<String> methods = new ArrayList<>();
        private final Set<String> imports = new HashSet<>();
        private final List<String> fields = new ArrayList<>();
        private final Set<String> dependencies = new HashSet<>();
        private final String content;

        public JavaScriptASTVisitor(String content) {
            this.content = content;
        }

        public void analyze() {
            // Import extraction
            Matcher impMatcher = Pattern.compile("import\\s+([a-zA-Z0-9_{},\\s*]+)\\s+from\\s+['\"]([^'\"]+)['\"]").matcher(content);
            while (impMatcher.find()) {
                String module = impMatcher.group(2);
                imports.add(module);
                dependencies.add(module);
            }

            // Class extraction
            Matcher classMatcher = Pattern.compile("class\\s+([a-zA-Z0-9_$]+)(?:\\s+extends\\s+([a-zA-Z0-9_$]+))?").matcher(content);
            while (classMatcher.find()) {
                String className = classMatcher.group(1);
                classes.add(className);
                if (classMatcher.group(2) != null) {
                    dependencies.add(classMatcher.group(2));
                }
            }

            // Function/Method extraction
            Matcher funcMatcher = Pattern.compile("(?:function\\s+([a-zA-Z0-9_$]+)|([a-zA-Z0-9_$]+)\\s*\\(([^)]*)\\)\\s*\\{)").matcher(content);
            while (funcMatcher.find()) {
                String fnName = funcMatcher.group(1) != null ? funcMatcher.group(1) : funcMatcher.group(2);
                String params = funcMatcher.group(3) != null ? funcMatcher.group(3) : "";
                if (fnName != null && !fnName.equals("if") && !fnName.equals("for") && !fnName.equals("while") && !fnName.equals("switch")) {
                    methods.add("function " + fnName + "(" + params + ")");
                }
            }

            // Field extraction
            Matcher fieldMatcher = Pattern.compile("(?:this\\.)([a-zA-Z0-9_$]+)\\s*=").matcher(content);
            while (fieldMatcher.find()) {
                fields.add("var " + fieldMatcher.group(1));
            }
        }

        @Override
        public Void visitClassDeclaration(JavaScriptParser.ClassDeclarationContext ctx) {
            if (ctx.className != null) classes.add(ctx.className);
            return super.visitClassDeclaration(ctx);
        }

        @Override
        public Void visitFunctionDeclaration(JavaScriptParser.FunctionDeclarationContext ctx) {
            if (ctx.functionName != null) {
                methods.add("function " + ctx.functionName + "(" + ctx.parameters + ")");
            }
            return super.visitFunctionDeclaration(ctx);
        }

        @Override
        public Void visitImportStatement(JavaScriptParser.ImportStatementContext ctx) {
            if (ctx.modulePath != null) {
                imports.add(ctx.modulePath);
                dependencies.add(ctx.modulePath);
            }
            return super.visitImportStatement(ctx);
        }

        public List<String> getClasses() { return classes; }
        public List<String> getMethods() { return methods; }
        public Set<String> getImports() { return imports; }
        public List<String> getFields() { return fields; }
        public Set<String> getDependencies() { return dependencies; }
    }
}
