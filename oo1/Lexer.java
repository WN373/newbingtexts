import java.util.ArrayList;

public class Lexer {
    private ArrayList<Token> tokens;

    private String input;

    public Lexer(String input) throws Exception {
        this.input = input;
        try {
            this.tokenize();
            // this.checkFormat();
        } catch (Exception e) {
            // System.out.println(e);
            // System.out.println("Wrong Format!");
            throw e;
        }
    }

    public void checkFormat() throws Exception {
        int depth = 0;
        for (Token token : this.tokens) {
            if (token.getType().equals("left-parenthesis")) {
                depth++;
            } else if (token.getType().equals("right-parenthesis")) {
                depth--;
            }
            if (depth > 1) {
                throw new Exception("too many parenthesis");
            }
        }
    }

    public ArrayList<Token> getTokens() {
        return this.tokens;
    }

    private String checkCharType(char c) {
        if (c == ' ' || c == '\t') {
            return "whitespace";
        } else if (c == '+') {
            return "addition";
        } else if (c == '-') {
            return "subtraction";
        } else if (c == '*') {
            return "multiplication";
        } else if (c == '(') {
            return "left-parenthesis";
        } else if (c == ')') {
            return "right-parenthesis";
        } else if (c <= '9' && c >= '0') {
            return "digits";
        } else if (c <= 'z' && c >= 'a') {
            return "alphas";
        } else if (c == '=') {
            return "equal";
        } else if (c == ',') {
            return "comma";
        } else {
            return "unknown";
        }
    }

    private Token mergeAddSub(Token prev, Token next) {
        if (prev.getType().equals("addition") && next.getType().equals("subtraction")) {
            return new Token("subtraction", "-");
        } else if (prev.getType().equals("subtraction") && next.getType().equals("addition")) {
            return new Token("subtraction", "-");
        } else if (prev.getType().equals("subtraction") && next.getType().equals("subtraction")) {
            return new Token("addition", "+");
        } else {
            return new Token("addition", "+");
        }
    }

    private void tokenClear(ArrayList<Token> rawTokens) {
        int isSign = 0;
        for (int i = 0; i < rawTokens.size(); i++) {
            String type = rawTokens.get(i).getType();
            String value = rawTokens.get(i).getValue();
            if (type.equals("addition") || type.equals("subtraction")) {
                if (!this.tokens.isEmpty() &&
                        (tokens.get(tokens.size() - 1).getType().equals("addition") ||
                            tokens.get(tokens.size() - 1).getType().equals("subtraction"))) {
                    this.tokens.get(this.tokens.size() - 1).extendAddSub(rawTokens.get(i));
                    continue;
                }
                if (isSign == 1 || isSign == 2) {
                    isSign = 2;
                } else {
                    isSign = 0;
                }
            } else if (type.equals("multiplication")) {
                if (rawTokens.get(i).getValue().length() == 2) {
                    rawTokens.get(i).setType("power");
                }
                isSign = 1;
            } else if (type.equals("whitespace")) {
                continue;
            } else if (type.equals("digits")) {
                if (isSign == 2) {
                    tokens.get(tokens.size() - 1).extendValue(rawTokens.get(i).getValue());
                    tokens.get(tokens.size() - 1).setType(rawTokens.get(i).getType());
                    isSign = 0;
                    continue;
                }
                isSign = 0;
            } else if (type.equals("alphas")) {
                if (isSign == 2) {
                    tokens.get(tokens.size() - 1).extendValue("1");
                    tokens.get(tokens.size() - 1).setType("digits");
                    tokens.add(new Token("multiplication", "*"));

                }
                if (value.equals("cos") || value.equals("sin")) {
                    rawTokens.get(i).setType("triangle");
                } else if (!value.isEmpty() && value.charAt(0) == 'd') {
                    rawTokens.get(i).setType("partial");
                } else if (value.equals("x") || value.equals("y") || value.equals("z")) {
                    rawTokens.get(i).setType("variants");
                } else if (value.equals("f") || value.equals("g") || value.equals("h")) {
                    rawTokens.get(i).setType("function");
                }
                isSign = 0;
            } else {
                isSign = 0;
            }
            this.tokens.add(rawTokens.get(i));
        }
    }

    private void tokenize() throws Exception {
        ArrayList<Token> rawTokens = new ArrayList<>();
        this.tokens = new ArrayList<>();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            String charType = checkCharType(c);
            if (charType.equals("unknown")) {
                throw new Exception("Unknown character: " + c);
            }
            if (rawTokens.isEmpty()) {
                rawTokens.add(new Token(charType, Character.toString(c)));
            } else if (charType.equals(rawTokens.get(rawTokens.size() - 1).getType()) &&
                    (charType.equals("digits") || charType.equals("whitespace") ||
                            charType.equals("multiplication") || charType.equals("alphas"))) {
                rawTokens.get(rawTokens.size() - 1).extendValue(Character.toString(c));
            } else {
                rawTokens.add(new Token(charType, Character.toString(c)));
            }
        }
        this.tokenClear(rawTokens);
        // return this.tokens;
    }
}
