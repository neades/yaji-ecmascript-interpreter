package FESI.AST;

import static org.junit.Assert.fail;

import java.util.Formatter;

import org.junit.Test;

import FESI.Parser.ParseException;

public class ASTIdentifierTest {

    private static final int ZWNJ = 0x200C;
    private static final int ZWJ = 0x200D;

    @Test
    public void allowsFullRangeOfIdentifierPartsAsUnicodeEscape() throws ParseException {
        StringBuilder sb = new StringBuilder(0x10000);
        sb.append('i');
        for ( int c=0; c<0x10000; c++ ) {
            int characterType = Character.getType(c);
            if ( isIdentifierPart(c,characterType) ) {
                sb.append(unicodeEscape(c));
            }
        }
        ASTIdentifier identifier = new ASTIdentifier(0);
        identifier.setName(sb.toString());
    }

    @Test
    public void allowsFullRangeOfIdentifierStartsAsUnicodeEscape() throws ParseException {
        ASTIdentifier identifier = new ASTIdentifier(0);
        for ( int c=0; c<0x10000; c++ ) {
            int characterType = Character.getType(c);
            if ( isIdentifierStart(c,characterType) ) {
                identifier.setName(unicodeEscape(c));
            }
        }
    }

    @Test
    public void disallowsAnyNonIdentiferPartCharactersAsUnicodeEscape() throws ParseException {
        ASTIdentifier identifier = new ASTIdentifier(0);
        for ( int c=0; c<0x10000; c++ ) {
            int characterType = Character.getType(c);
            if (!isIdentifierPart(c,characterType)) {
                try {
                    identifier.setName("i"+unicodeEscape(c));
                    fail("disallowed character "+c+ " was allowed");
                } catch (ParseException e) {
                    // expected
                }
            }
        }
    }
    
    @Test
    public void disallowsAnyNonIdentiferStartCharactersAsUnicodeEscape() throws ParseException {
        ASTIdentifier identifier = new ASTIdentifier(0);
        for ( int c=0; c<0x10000; c++ ) {
            int characterType = Character.getType(c);
            if (!isIdentifierStart(c,characterType)) {
                try {
                    identifier.setName(unicodeEscape(c));
                    fail("disallowed start character "+c+ " was allowed");
                } catch (ParseException e) {
                    // expected
                }
            }
        }
    }
    
    private String unicodeEscape(int c) {
        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb);
        formatter.format("\\u%04x", Integer.valueOf(c));
        return sb.toString();
    }
    
    private boolean isIdentifierStart(int c, int characterType) {
        return isUnicodeLetter(characterType) || c == '\\' || c == '$' || c == '_';
    }
    private boolean isUnicodeLetter(int characterType) {
        return characterType == Character.UPPERCASE_LETTER
            || characterType == Character.LOWERCASE_LETTER
            || characterType == Character.TITLECASE_LETTER
            || characterType == Character.MODIFIER_LETTER
            || characterType == Character.OTHER_LETTER
            || characterType == Character.LETTER_NUMBER;
    }
    
    private boolean isIdentifierPart(int c, int characterType) {
        return isIdentifierStart(c, characterType)
                || isUnicodeCombiningMark(characterType)
                || isUnicodeDigit(characterType)
                || isUnicodeConnectorPunctuation(characterType)
                || c == ZWNJ
                || c == ZWJ;
    }

    private boolean isUnicodeConnectorPunctuation(int characterType) {
        return characterType == Character.CONNECTOR_PUNCTUATION;
    }

    private boolean isUnicodeDigit(int characterType) {
        return characterType == Character.DECIMAL_DIGIT_NUMBER;
    }

    private boolean isUnicodeCombiningMark(int characterType) {
        return characterType == Character.COMBINING_SPACING_MARK 
                || characterType == Character.NON_SPACING_MARK;
    }

}
