package com.email.backend.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CodeParserServiceTest {

    private final CodeParserService parserService = new CodeParserService();

    @Test
    void testParseJavaFile() {
        String javaCode = """
            package com.example.demo;
            
            import java.util.List;
            import java.util.Map;
            
            public class UserService extends BaseService implements IUserService {
                private String userStore;
                
                public String getUser(String id) {
                    return id;
                }
            }
            """;

        ParsedFile result = parserService.parseFile(javaCode, "java");
        assertEquals("java", result.getLanguage());
        assertEquals("com.example.demo", result.getPackageName());
        assertTrue(result.getClasses().contains("UserService"));
        assertTrue(result.getImports().contains("java.util.List"));
        assertTrue(result.getDependencies().contains("BaseService"));
        assertFalse(result.getMethods().isEmpty());
    }

    @Test
    void testParsePythonFile() {
        String pyCode = """
            import os
            from datetime import datetime

            class AccountManager(BaseManager):
                def process_account(self, acc_id: str) -> bool:
                    pass
            """;

        ParsedFile result = parserService.parseFile(pyCode, "python");
        assertEquals("python", result.getLanguage());
        assertTrue(result.getClasses().contains("AccountManager"));
        assertTrue(result.getImports().contains("os"));
        assertTrue(result.getDependencies().contains("BaseManager"));
        assertFalse(result.getMethods().isEmpty());
    }

    @Test
    void testParseJavaScriptFile() {
        String jsCode = """
            import React from 'react';
            import { useState } from 'react';

            class AppHeader extends React.Component {
                render() {
                    return null;
                }
            }

            function main(args) {
                console.log(args);
            }
            """;

        ParsedFile result = parserService.parseFile(jsCode, "javascript");
        assertEquals("javascript", result.getLanguage());
        assertTrue(result.getClasses().contains("AppHeader"));
        assertTrue(result.getImports().contains("react"));
        assertFalse(result.getMethods().isEmpty());
    }
}
