package lexer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.stream.Stream;

public class ScannerMain {
    public static final void main(String... args) throws Exception {
        ClassLoader cloader = ScannerMain.class.getClassLoader();
        File file = new File(cloader.getResource("lexer/as04.txt").getFile());
        File out = new File("src/lexer/hw04.txt");
        testTokenStream(file);
        writeTokenStream(file, out);
    }
    
    // use tokens as a Stream 
    private static void testTokenStream(File file) throws IOException {	
        Stream<Token> tokens = Scanner.stream(file);
        tokens.map(ScannerMain::toString).forEach(System.out::println);
    }
    
    private static void writeTokenStream(File input, File output) throws IOException {
    	Stream<Token> tokens = Scanner.stream(input);
    	BufferedWriter bw = new BufferedWriter( new FileWriter(output) );
    	tokens.map(ScannerMain::toString).forEach(
    		t -> {
				try {
					bw.write(t+"\n");
					bw.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
    	);
    	bw.close();
    }
    
    private static String toString(Token token) {
        return String.format("%-3s\t %s", token.type().name(), token.lexme());
    }
}