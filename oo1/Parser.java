import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;

public class Parser {
    private Lexer lexer;

    private final HashMap<String, Integer> preeminence = new HashMap<>();

    {
        preeminence.put("addition", 1);
        preeminence.put("subtraction", 1);
        preeminence.put("multiplication", 2);
        preeminence.put("power", 3);
        preeminence.put("function", 4);
        preeminence.put("triangle", 4);
        preeminence.put("partial", 4);
    }

    private HashMap<String, Monomial> functions;

    private HashMap<String, ArrayList<String>> definitions;

    private Monomial result;

    public Parser(ArrayList<Token> tokens, HashMap<String, Monomial> functions,
                  HashMap<String, ArrayList<String>> definitions) {
        this.functions = functions;
        this.definitions = definitions;
        try {
            this.result = parse(tokens);
        } catch (Exception e) {
            // System.out.println(e);
            // System.out.println("Wrong Format!");
        }
    }

    public Parser(ArrayList<Token> tokens) {
        this.functions = new HashMap<>();
        this.definitions = new HashMap<>();
        try {
            this.result = parse(tokens);
        } catch (Exception e) {
            // System.out.println(e);
            // System.out.println("Wrong Format!");
        }
    }

    public Monomial getResult() {
        return result;
    }

    private Monomial parse(ArrayList<Token> tokens) {
        if (tokens.isEmpty()) {
            return new MonoSum();
        }
        int mid = -1;
        for (int i = 0; i <= tokens.size() - 1; i++) {
            switch (tokens.get(i).getType()) {
                case "left-parenthesis":
                    int j = 1;
                    while (i < tokens.size() - 1 && j != 0) {
                        i++;
                        if (tokens.get(i).getType().equals("left-parenthesis")) {
                            j++;
                        } else if (tokens.get(i).getType().equals("right-parenthesis")) {
                            j--;
                        }
                    }
                    break;
                case "addition":
                case "subtraction":
                case "multiplication":
                case "power":
                case "function":
                case "triangle":
                case "partial":
                    if (mid == -1 || preeminence.get(tokens.get(mid).getType()) >=
                            preeminence.get(tokens.get(i).getType())) {
                        mid = i;
                    }
                    break;
                default:
                    break;
            }
        }
        if (mid == -1) {
            int l = 0;
            int r = tokens.size() - 1;
            if (tokens.get(l).getType().equals("left-parenthesis") &&
                    tokens.get(r).getType().equals("right-parenthesis")) {
                l++;
                r--;
            }
            if (l == r) {
                return Monomial.parseToken(tokens.get(l));
            } else {
                return parse(new ArrayList<>(tokens.subList(l, r + 1)));
            }
        }
        try {
            return recurse(tokens, mid);
        } catch (Exception e) {
            e.printStackTrace();
            return new MonoSum();
        }
    }

    private Monomial recurse(ArrayList<Token> tokens, int mid) throws Exception {
        if (tokens.get(mid).getType().equals("triangle")) {
            if (mid != 0) {
                throw new RuntimeException("triangle function should be at the beginning");
            }
            Monomial rightSide = parse(new ArrayList<>(tokens.subList(mid + 1, tokens.size())));
            rightSide = rightSide.addFront(tokens.get(mid).getValue());
            return rightSide;
        } else if (tokens.get(mid).getType().equals("function")) {
            if (mid != 0) {
                throw new RuntimeException("function should be at the beginning");
            }
            String funcName = tokens.get(mid).getValue();
            if (functions.containsKey(funcName)) {
                return functions.get(funcName).apply(parseArgs(tokens, funcName));
            } else {
                throw new RuntimeException("function " + funcName + " is not defined");
            }
        } else if (tokens.get(mid).getType().equals("partial")) {
            if (mid != 0) {
                throw new RuntimeException("function should be at the beginning");
            }
            String var = tokens.get(mid).getValue().substring(1);
            Monomial rightSide = parse(new ArrayList<>(tokens.subList(mid + 1, tokens.size())));
            return rightSide.deriviate(var);
        } else {
            Monomial leftSide = parse(new ArrayList<>(tokens.subList(0, mid)));
            Monomial rightSide = parse(new ArrayList<>(tokens.subList(mid + 1, tokens.size())));
            switch (tokens.get(mid).getType()) {
                case "addition":
                    return leftSide.add(rightSide);
                case "subtraction":
                    return leftSide.add(rightSide.negate());
                case "multiplication":
                    return leftSide.multiply(rightSide);
                case "power":
                    return leftSide.power(new BigInteger(tokens.get(mid + 1).getValue()));
                default:
                    return new MonoSum();
            }
        }
    }

    private HashMap<String, Monomial> parseArgs(ArrayList<Token> tokens, String funcName) {
        HashMap<String, Monomial> args = new HashMap<>();
        int l = 2;
        int r = tokens.size() - 2;
        int p = l;
        for (String argName : definitions.get(funcName)) {
            while (p <= r && !tokens.get(p).getType().equals("comma")) {
                if (tokens.get(p).getType().equals("left-parenthesis")) {
                    int j = 1;
                    while (p < r && j != 0) {
                        p++;
                        if (tokens.get(p).getType().equals("left-parenthesis")) {
                            j++;
                        } else if (tokens.get(p).getType().equals("right-parenthesis")) {
                            j--;
                        }
                    }
                }
                p++;
            }
            args.put(argName, parse(new ArrayList<>(tokens.subList(l, p))));
            l = p = p + 1;
        }
        return args;
    }
}
