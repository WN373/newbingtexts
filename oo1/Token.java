public class Token {
    private String type;
    private String value;

    public Token(String type, String value) {
        this.type = type;
        this.value = value;
    }

    public Token() {
        this.type = new String();
        this.value = new String();
    }

    public String extendValue(String value) {
        this.value += value;
        return this.value;
    }

    public Token extendAddSub(Token next) {
        if (this.type == "addition" && next.type == "subtraction") {
            this.type = "subtraction";
        } else if (this.type == "subtraction" && next.type == "addition") {
            this.type = "subtraction";
        } else {
            this.type = "addition";
        }
        return this;
    }

    public String getType() {
        return this.type;
    }

    public String getValue() {
        return this.value;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String toString() {
        return "src.Token(" + this.type + ", " + this.value + ")";
    }
}
