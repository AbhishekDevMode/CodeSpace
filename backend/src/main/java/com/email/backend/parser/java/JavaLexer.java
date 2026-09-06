package com.email.backend.parser.java;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.VocabularyImpl;
import org.antlr.v4.runtime.atn.ATN;

public class JavaLexer extends Lexer {
    public static final int IDENTIFIER = 1, STRING_LITERAL = 2, NUMBER = 3, WS = 4, COMMENT = 5;

    public static final String[] channelNames = {
        "DEFAULT_TOKEN_CHANNEL", "HIDDEN"
    };

    public static final String[] modeNames = {
        "DEFAULT_MODE"
    };

    public static final String[] ruleNames = {
        "IDENTIFIER", "STRING_LITERAL", "NUMBER", "WS", "COMMENT"
    };

    private static final String[] _LITERAL_NAMES = { null, null, null, null };
    private static final String[] _SYMBOLIC_NAMES = { null, "IDENTIFIER", "STRING_LITERAL", "NUMBER", "WS", "COMMENT" };
    public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

    public JavaLexer(CharStream input) {
        super(input);
    }

    @Override public String[] getRuleNames() { return ruleNames; }
    @Override public Vocabulary getVocabulary() { return VOCABULARY; }
    @Override public String getGrammarFileName() { return "Java.g4"; }
    @Override public String[] getChannelNames() { return channelNames; }
    @Override public String[] getModeNames() { return modeNames; }
    @Override public ATN getATN() { return null; }
}
