import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Main {
    private static Scanner in;

    private static Lexer lexer;

    private static HashMap<String, Monomial> functions;

    private static HashMap<String, ArrayList<String>> definitions;

    public static void main(String[] args) {
        in = new Scanner(System.in);
        definitions = new HashMap<>();
        functions = new HashMap<>();
        int n = in.nextInt();
        in.nextLine();
        for (int i = 0; i < n; i++) {
            String line = in.nextLine();
            try {
                lexer = new Lexer(line);
                ArrayList<Token> tokens = lexer.getTokens();
                ArrayList<String> variants = new ArrayList<>();
                String func = tokens.get(0).getValue();
                int j = 1;
                for (; j < tokens.size(); j++) {
                    if (tokens.get(j).getType().equals("equal")) {
                        break;
                    } else if (tokens.get(j).getType().equals("variants")) {
                        variants.add(tokens.get(j).getValue());
                    }
                }
                definitions.put(func, variants);
                functions.put(func, new Parser(new ArrayList<Token>(
                        tokens.subList(j + 1,tokens.size())), functions, definitions)
                        .getResult().simplify());
            } catch (Exception e) {
                System.out.println(e);
            }
        }
        try {
            Monomial result = new Parser(
                    new Lexer(in.nextLine()).getTokens(), functions, definitions).getResult();
            System.out.println(result.simplify());
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
