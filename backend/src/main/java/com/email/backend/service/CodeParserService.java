package com.email.backend.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class CodeParserService {

    public static class ParsedFile {
        private String language;
        private String packageName;
        private Set<String> imports = new HashSet<>();
        private List<String> classes = new ArrayList<>();
        private List<String> methods = new ArrayList<>();
        private Set<String> dependencies = new HashSet<>();

        public ParsedFile() {}

        public ParsedFile(String language, String packageName, Set<String> imports, List<String> classes, List<String> methods, Set<String> dependencies) {
            this.language = language;
            this.packageName = packageName;
            this.imports = imports;
            this.classes = classes;
            this.methods = methods;
            this.dependencies = dependencies;
        }

        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }
        public String getPackageName() { return packageName; }
        public void setPackageName(String packageName) { this.packageName = packageName; }
        public Set<String> getImports() { return imports; }
        public void setImports(Set<String> imports) { this.imports = imports; }
        public List<String> getClasses() { return classes; }
        public void setClasses(List<String> classes) { this.classes = classes; }
        public List<String> getMethods() { return methods; }
        public void setMethods(List<String> methods) { this.methods = methods; }
        public Set<String> getDependencies() { return dependencies; }
        public void setDependencies(Set<String> dependencies) { this.dependencies = dependencies; }
    }

    public ParsedFile parseFile(String content, String language) {
        if (!"java".equalsIgnoreCase(language)) {
            throw new UnsupportedOperationException("Language not supported yet: " + language);
        }
        ParsedFile parsedFile = new ParsedFile();
        parsedFile.setLanguage(language);
        return parsedFile;
    }
}
