import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class Monomial implements Cloneable {

    private String front;

    public Monomial() {
        this.setFront("");
    }

    public Monomial(String front) {
        this.setFront(front);
    }

    static HashMap<Monomial, BigInteger> removeZero(HashMap<Monomial, BigInteger> monomials) {
        HashMap<Monomial, BigInteger> newMonomials = new HashMap<>();
        for (Map.Entry<Monomial, BigInteger> entry : monomials.entrySet()) {
            if (!entry.getValue().equals(BigInteger.ZERO)) {
                Monomial newKey = entry.getKey().simplify();
                if (newMonomials.containsKey(newKey)) {
                    newMonomials.replace(newKey, newMonomials.get(newKey).add(entry.getValue()));
                } else {
                    newMonomials.put(newKey, entry.getValue());
                }
            }
        }
        return newMonomials;
    }

    public String getFront() {
        return this.front;
    }

    public void setFront(String front) {
        this.front = front;
    }

    public Monomial addFront(String newFront) {
        if (isSplittable()) {
            this.setFront(newFront);
            return this;
        } else {
            Monomial ret = new MonoSum(BigInteger.ONE, this);
            ret.setFront(newFront);
            return ret;
        }
    }

    public boolean isSplittable() {
        return front.isEmpty();
    }

    public Monomial negate() {
        return this;
    }

    public Monomial deriviate(String var) throws Exception {
        return this;
    }

    public static Monomial parseToken(Token token) {
        if (token.getType().equals("digits")) {
            return new MonoSum(new BigInteger(token.getValue()), new MonoProd());
        } else if (token.getType().equals("variants")) {
            MonoProd ret = new MonoProd();
            if (token.getValue().equals("x")) {
                ret.setExpX(BigInteger.ONE);
            } else if (token.getValue().equals("y")) {
                ret.setExpY(BigInteger.ONE);
            } else if (token.getValue().equals("z")) {
                ret.setExpZ(BigInteger.ONE);
            } else {
                System.out.println("Error: Monomial.parseToken");
            }
            return ret;
        } else {
            System.out.println("Error: Monomial.parseToken");
            return new Monomial();
        }
    }

    public Monomial multiply(Monomial other) {
        return this;
    }

    public Monomial add(Monomial other) {
        return this;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return this;
    }

    public Monomial power(BigInteger expo) {
        return this;
    }

    public Monomial simplify() {
        return this;
    }

    public Monomial apply(HashMap<String, Monomial> args) {
        return this;
    }

    protected int fastPow(int xx, int pp, int mod) {
        long ret = 1;
        int x = xx;
        int p = pp;
        while (p > 0) {
            if ((p & 1) == 1) {
                ret = ret * x % mod;
            }
            x = x * x % mod;
            p >>= 1;
        }
        return (int)ret;
    }

}

