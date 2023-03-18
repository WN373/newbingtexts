import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class MonoSum extends Monomial {
    private HashMap<Monomial, BigInteger> monomials;

    public MonoSum() {
        super();
        this.monomials = new HashMap<>();
    }

    public MonoSum(BigInteger k, Monomial x) {
        super();
        this.monomials = new HashMap<>();
        if (x.isSplittable() && x instanceof MonoSum) {
            for (Map.Entry<Monomial, BigInteger> entry : ((MonoSum) x).monomials.entrySet()) {
                this.monomials.put(entry.getKey(), entry.getValue().multiply(k));
            }
        } else if (!k.equals(BigInteger.ZERO)) {
            this.monomials.put(x, k);
        }
    }

    public MonoSum(Monomial x, Monomial y) {
        this();
        if (x instanceof MonoSum) {
            this.buildSum((MonoSum) x);
        } else {
            this.buildSum((MonoProd) x);
        }
        if (y instanceof MonoSum) {
            this.buildSum((MonoSum) y);
        } else {
            this.buildSum((MonoProd) y);
        }
    }

    @Override
    public Monomial multiply(Monomial other) { //
        if (this.isSplittable()) {
            MonoSum ret = new MonoSum();
            for (Map.Entry<Monomial, BigInteger> entry : this.monomials.entrySet()) {
                Monomial key = entry.getKey();
                BigInteger value = entry.getValue();
                if (other.isSplittable() && other instanceof MonoSum) {
                    for (Map.Entry<Monomial, BigInteger> entry1 :
                            ((MonoSum) other).monomials.entrySet()) {
                        Monomial key1 = entry1.getKey();
                        BigInteger value1 = entry1.getValue();
                        ret.buildSum(new MonoSum(value.multiply(value1), key.multiply(key1)));
                    }
                } else {
                    ret.buildSum(new MonoSum(value, key.multiply(other)));
                }
            }
            return ret;
        } else if (other instanceof MonoSum && other.isSplittable()) {
            return ((MonoSum) other).multiply(this);
        } else {
            return new MonoProd(this, other);
        }
    }

    @Override
    public Monomial negate() {
        if (this.isSplittable()) {
            MonoSum ret = new MonoSum();
            for (Map.Entry<Monomial, BigInteger> entry : this.monomials.entrySet()) {
                ret.monomials.put(entry.getKey(), entry.getValue().negate());
            }
            return ret;
        } else {
            return new MonoSum(BigInteger.ONE.negate(), this);
        }
    }

    @Override
    public Monomial add(Monomial other) { // finished sum.add
        MonoSum ret = new MonoSum();
        ret.buildSum(this);
        ret.buildSum(other);
        return ret;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        MonoSum ret = new MonoSum();
        ret.setFront(this.getFront());
        for (Map.Entry<Monomial, BigInteger> entry : monomials.entrySet()) {
            ret.monomials.put((Monomial) entry.getKey().clone(), entry.getValue());
        }
        return ret;
    }

    @Override
    public Monomial deriviate(String var) throws Exception {
        if (this.isSplittable()) {
            MonoSum ret = new MonoSum();
            for (Map.Entry<Monomial, BigInteger> entry : this.monomials.entrySet()) {
                ret.buildSum(new MonoSum(entry.getValue(), entry.getKey().deriviate(var)));
            }
            return ret;
        } else {
            Monomial left = (Monomial) this.clone();
            Monomial right = (Monomial) this.clone();
            left.setFront("");
            left = left.deriviate(var);
            if (this.getFront().equals("cos")) {
                right.setFront("sin");
                left = left.multiply(right.negate());
            } else if (this.getFront().equals("sin")) {
                right.setFront("cos");
                left = left.multiply(right);
            }
            return left;
        }
    }

    @Override
    public Monomial power(BigInteger expo) {
        if (this.isSplittable()) {
            if (expo.equals(BigInteger.ZERO)) {
                return new MonoProd();
            } else if (expo.equals(BigInteger.ONE)) {
                return this;
            } else {
                Monomial ret = new MonoSum(BigInteger.ONE, new MonoProd());
                Monomial base = this;
                BigInteger p = expo;
                while (p.compareTo(BigInteger.ZERO) > 0) {
                    if (p.mod(BigInteger.valueOf(2)).equals(BigInteger.ONE)) {
                        ret = ret.multiply(base);
                    }
                    p = p.divide(BigInteger.valueOf(2));
                    base = base.multiply(base);
                }
                return ret;
            }
        } else {
            return new MonoProd(this, expo);
        }
    }

    private static int mod = 998244353;

    @Override
    public int hashCode() {
        long hc = 0;
        for (Map.Entry<Monomial, BigInteger> entry : this.monomials.entrySet()) {
            hc = (hc + entry.getKey().hashCode() * entry.getValue().longValue() + mod) % mod;
        }
        if (this.getFront().equals("cos")) {
            hc = hc * 7 % 998244353;
        } else if (this.getFront().equals("sin")) {
            hc = hc * 5 % 998244353;
        }
        return (int)hc;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MonoSum) {
            if (monomials.size() == ((MonoSum) obj).monomials.size() &&
                    this.getFront().equals(((MonoSum) obj).getFront())) {
                for (Map.Entry<Monomial, BigInteger> entry : this.monomials.entrySet()) {
                    Monomial key = entry.getKey();
                    BigInteger value = entry.getValue();
                    if (!((MonoSum) obj).monomials.containsKey(key) ||
                            !((MonoSum) obj).monomials.get(key).equals(value)) {
                        return false;
                    }
                }
                return true;
            }
        } else if (obj instanceof MonoProd) {
            if (this.getFront().equals(((MonoProd) obj).getFront())) {
                if (monomials.size() == 1 && monomials.containsKey(obj) &&
                        monomials.get(obj).equals(BigInteger.ONE)) {
                    return true;
                }
            }
        } else {
            return false;
        }
        return false;
    }

    @Override
    public Monomial simplify() {
        HashMap<Monomial, BigInteger> newMonomials = removeZero(monomials);
        if (newMonomials.size() == 1 && this.isSplittable() &&
                newMonomials.entrySet().iterator().next().getValue().equals(BigInteger.ONE)) {
            return newMonomials.entrySet().iterator().next().getKey();
        } else if (false && newMonomials.size() == 0) {
            if (this.getFront().equals("cos")) {
                return new MonoProd();
            } else if (this.getFront().equals("sin")) {
                return new MonoSum();
            } else {
                return new MonoSum();
            }
        } else {
            this.monomials = newMonomials;
            return this;
        }
    }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder();
        for (Map.Entry<Monomial, BigInteger> entry : this.monomials.entrySet()) {
            Monomial key = entry.getKey();
            BigInteger value = entry.getValue();
            String keyStr = key.toString();
            if (keyStr.equals("0")) {
                continue;
            }
            if (value.compareTo(BigInteger.ZERO) > 0) {
                if (ret.length() > 0) {
                    ret.append("+");
                }
                if (value.compareTo(BigInteger.ONE) > 0) {
                    ret.append(value);
                    if (!keyStr.equals("1")) {
                        ret.append("*").append(keyStr);
                    }
                } else {
                    ret.append(keyStr);
                }
            } else if (value.compareTo(BigInteger.ZERO) < 0) {
                ret.append("-");
                if (value.compareTo(BigInteger.ONE.negate()) < 0) {
                    ret.append(value.negate());
                    if (!keyStr.equals("1")) {
                        ret.append("*").append(keyStr);
                    }
                } else {
                    ret.append(keyStr);
                }
            }
        }
        if (ret.length() == 0) {
            ret.append("0");
        }
        if (!this.getFront().isEmpty()) {
            ret = new StringBuilder(this.getFront() + "((" + ret + "))");
        }
        return ret.toString();
    }

    @Override
    public Monomial apply(HashMap<String, Monomial> args) {
        Monomial ret = new MonoSum();
        for (Map.Entry<Monomial, BigInteger> entry : this.monomials.entrySet()) {
            Monomial key = entry.getKey();
            BigInteger value = entry.getValue();
            ret = ret.add(new MonoSum(value, key.apply(args)));
        }
        ret.setFront(this.getFront());
        return ret;
    }

    private void buildSum(Monomial x) {
        if (x instanceof MonoProd) {
            this.buildSum((MonoProd) x);
        } else if (x instanceof MonoSum) {
            this.buildSum((MonoSum) x);
        } else {
            System.out.println("TypeError: addToSelf");
        }
    }

    private void buildSum(MonoProd x) {
        if (this.monomials.containsKey(x)) {
            this.monomials.replace(x, this.monomials.get(x).add(BigInteger.ONE));
        } else {
            this.monomials.put(x, BigInteger.ONE);
        }
    }

    private void buildSum(MonoSum x) {
        if (x.isSplittable()) {
            for (Map.Entry<Monomial, BigInteger> entry : x.monomials.entrySet()) {
                Monomial key = entry.getKey();
                BigInteger value = entry.getValue();
                if (this.monomials.containsKey(key)) {
                    this.monomials.replace(key, this.monomials.get(key).add(value));
                } else {
                    this.monomials.put(key, value);
                }
            }
        } else {
            if (this.monomials.containsKey(x)) {
                this.monomials.replace(x, this.monomials.get(x).add(BigInteger.ONE));
            } else {
                this.monomials.put(x, BigInteger.ONE);
            }
        }
    }
}
