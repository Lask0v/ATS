package tests;

import FrontEnd.Lexer;
import FrontEnd.Token;
import FrontEnd.TokenType;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.AssertJUnit.assertEquals;

public class LexerTest {

    @Test
    void testTokenize() {
        String filePath = "example_source_code.txt";
        Lexer lexer = new Lexer();
        List<Token> tokens = lexer.tokenize(filePath);


        Token token0 = tokens.get(0);
        assertEquals(TokenType.PROCEDURE, token0.getType());
        assertEquals("procedure", token0.getValue());

        Token token1 = tokens.get(1);
        assertEquals(TokenType.NAME, token1.getType());
        assertEquals("Rectangle", token1.getValue());

        Token token2 = tokens.get(2);
        assertEquals(TokenType.LBRACE, token2.getType());
        assertEquals("{", token2.getValue());


    }
}
