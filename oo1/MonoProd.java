import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class MonoProd extends Monomial {
    private HashMap<Monomial, BigInteger> monomials;

    private BigInteger expX;

    private BigInteger expY;

    private BigInteger expZ;

    public BigInteger getExpX() {
        return expX;
    }

    public void setExpX(BigInteger expX) {
        this.expX = expX;
    }

    public BigInteger getExpY() {
        return expY;
    }

    public void setExpY(BigInteger expY) {
        this.expY = expY;
    }

    public BigInteger getExpZ() {
        return expZ;
    }

    public void setExpZ(BigInteger expZ) {
        this.expZ = expZ;
    }

    public MonoProd() {
        super();
        this.monomials = new HashMap<>();
        this.expX = BigInteger.ZERO;
        this.expY = BigInteger.ZERO;
        this.expZ = BigInteger.ZERO;
    }

    public MonoProd(Monomial x, BigInteger exp) {
        this();
        if (exp.equals(BigInteger.ZERO)) {
            return;
        }
        if (x instanceof MonoSum) {
            if (!x.isSplittable()) {
                this.monomials.put(x, exp);
            } else {
                System.out.println("Logical Error: Sum should not exist here");
            }
        } else if (x instanceof MonoProd) {
            if (x.isSplittable()) {
                for (Map.Entry<Monomial, BigInteger> entry : ((MonoProd) x).monomials.entrySet()) {
                    this.monomials.put(entry.getKey(), entry.getValue().multiply(exp));
                }
                this.expX = ((MonoProd) x).expX.multiply(exp);
                this.expY = ((MonoProd) x).expY.multiply(exp);
                this.expZ = ((MonoProd) x).expZ.multiply(exp);
            } else {
                this.monomials.put(x,exp);
            }
        } else {
            System.out.println("TypeError: x doesn't match any type");
        }
    }

    public MonoProd(Monomial x, Monomial y) {
        this();
        try {
            this.buildProd(x);
            this.buildProd(y);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void buildProd(Monomial x) throws Exception {
        if (x instanceof MonoSum) {
            this.buildProd((MonoSum) x);
        } else if (x instanceof MonoProd) {
            this.buildProd((MonoProd) x);
        } else {
            throw new Exception("Error: Monomial is not MonoSum or MonoProd.");
        }
    }

    private void buildProd(MonoSum x) throws Exception {
        if (x.isSplittable()) {
            throw new Exception("Error: MonoSum is splittable.");
        }
        if (this.monomials.containsKey(x)) {
            this.monomials.replace(x, this.monomials.get(x).add(BigInteger.ONE));
        } else {
            this.monomials.put(x, BigInteger.ONE);
        }
    }

    private void buildProd(MonoProd x) {
        if (x.isSplittable()) {
            this.expX = this.expX.add(x.expX);
            this.expY = this.expY.add(x.expY);
            this.expZ = this.expZ.add(x.expZ);
            for (Monomial key : x.monomials.keySet()) {
                if (this.monomials.containsKey(key)) {
                    this.monomials.replace(key, this.monomials.get(key).add(x.monomials.get(key)));
                } else {
                    this.monomials.put(key, x.monomials.get(key));
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

    @Override
    public Monomial multiply(Monomial other) {
        if (other instanceof MonoProd) {
            return new MonoProd(this, (MonoProd) other);
        } else if (other instanceof MonoSum) {
            if (!other.isSplittable()) {
                return new MonoProd(this, (MonoSum) other);
            } else {
                return other.multiply(this);
            }
        } else {
            return null;
        }
    }

    @Override
    public Monomial deriviate(String var) throws Exception {
        if (this.isSplittable()) {
            Monomial left;
            if (this.monomials.isEmpty()) {
                switch (var) {
                    case "x":
                        if (expX.equals(BigInteger.ZERO)) {
                            return new MonoSum();
                        } else {
                            left = (MonoProd) this.clone();
                            ((MonoProd) left).expX = expX.add(BigInteger.ONE.negate());
                            return new MonoSum(((MonoProd) left).expX.add(BigInteger.ONE),
                                    ((MonoProd) left));
                        }
                    case "y":
                        if (expY.equals(BigInteger.ZERO)) {
                            return new MonoSum();
                        } else {
                            left = (MonoProd) this.clone();
                            ((MonoProd) left).expY = expY.add(BigInteger.ONE.negate());
                            return new MonoSum(((MonoProd) left).expY.add(BigInteger.ONE),
                                    ((MonoProd) left));
                        }
                    case "z":
                        if (expZ.equals(BigInteger.ZERO)) {
                            return new MonoSum();
                        } else {
                            left = (MonoProd) this.clone();
                            ((MonoProd) left).expZ = expZ.add(BigInteger.ONE.negate());
                            return new MonoSum(((MonoProd) left).expZ.add(BigInteger.ONE),
                                    ((MonoProd) left));
                        }
                    default:
                        throw new Exception("Error: Variable not found.");
                }
            } else {
                Map.Entry<Monomial, BigInteger> leftPair = monomials.entrySet().iterator().next();
                this.monomials.remove(leftPair.getKey());
                Monomial leftD;
                if (leftPair.getValue().compareTo(BigInteger.ONE) > 0) {
                    left = leftPair.getKey().power(leftPair.getValue());
                    leftD = leftPair.getKey().power(leftPair.getValue().add(
                            BigInteger.ONE.negate())).multiply(new MonoSum(
                                    leftPair.getValue(),leftPair.getKey().deriviate(var)));
                } else {
                    left = leftPair.getKey();
                    leftD = left.deriviate(var);
                }
                Monomial right = left.multiply(this.deriviate(var)).add(leftD.multiply(this));
                this.monomials.put(leftPair.getKey(), leftPair.getValue());
                return right;
            }
        } else {
            return deriviateTriple(var);
        }
    }

    private Monomial deriviateTriple(String var) throws Exception {
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

    @Override
    public Monomial add(Monomial other) {   // finished prod.add
        return new MonoSum(this, other);
    }

    @Override
    public Monomial negate() {
        try {
            return new MonoSum(BigInteger.ONE.negate(), (Monomial) this);
        } catch (Exception e) {
            System.out.println(e);
            return new MonoSum();
        }
    }

    @Override
    public Monomial simplify() {
        int size = 0;
        if (!this.expX.equals(0)) {
            size += 1;
        }
        if (!this.expY.equals(0)) {
            size += 1;
        }
        if (!this.expZ.equals(0)) {
            size += 1;
        }
        HashMap<Monomial, BigInteger> newMonomials = Monomial.removeZero(monomials);
        if (size == 0 && newMonomials.size() == 1 && this.isSplittable()) {
            return newMonomials.keySet().iterator().next();
        } else {
            this.monomials = newMonomials;
            return this;
        }
    }

    private static int mod = 998244353;

    @Override
    public int hashCode() {
        long ret = 0;
        ret = (ret + fastPow(13, expX.intValue(), mod)) % mod;
        ret = (ret + fastPow(17, expY.intValue(), mod)) % mod;
        ret = (ret + fastPow(19, expZ.intValue(), mod)) % mod;
        for (Map.Entry<Monomial, BigInteger> entry : this.monomials.entrySet()) {
            ret = (fastPow(entry.getKey().hashCode(), entry.getValue().intValue(), mod)
                    + ret + mod) % mod;
        }
        if (this.getFront().equals("cos")) {
            ret = ret * 7 % 998244353;
        } else if (this.getFront().equals("sin")) {
            ret = ret * 5 % 998244353;
        }
        return (int)ret;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MonoProd) {
            MonoProd other = (MonoProd) obj;
            if (this.expX.equals(other.expX) && this.expY.equals(other.expY) &&
                    this.expZ.equals(other.expZ) && this.getFront().equals(other.getFront())) {
                if (this.monomials.size() == other.monomials.size()) {
                    for (Map.Entry<Monomial, BigInteger> entry : this.monomials.entrySet()) {
                        if (!other.monomials.containsKey(entry.getKey()) ||
                                !other.monomials.get(entry.getKey()).equals(entry.getValue())) {
                            return false;
                        }
                    }
                    return true;
                }
            }
        } else if (obj instanceof MonoSum) {
            return ((MonoSum) obj).equals(this);
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder();
        if (this.expX.compareTo(BigInteger.ZERO) > 0) {
            ret.append("x");
            if (this.expX.compareTo(BigInteger.ONE) > 0) {
                ret.append("**").append(this.expX);
            }
        }
        if (this.expY.compareTo(BigInteger.ZERO) > 0) {
            if (ret.length() > 0) {
                ret.append("*");
            }
            ret.append("y");
            if (this.expY.compareTo(BigInteger.ONE) > 0) {
                ret.append("**").append(this.expY);
            }
        }
        if (this.expZ.compareTo(BigInteger.ZERO) > 0) {
            if (ret.length() > 0) {
                ret.append("*");
            }
            ret.append("z");
            if (this.expZ.compareTo(BigInteger.ONE) > 0) {
                ret.append("**").append(this.expZ);
            }
        }
        for (Map.Entry<Monomial, BigInteger> entry : this.monomials.entrySet()) {
            if (ret.length() > 0) {
                ret.append("*");
            }
            if (entry.getValue().compareTo(BigInteger.ONE) > 0) {
                ret.append(entry.getKey()).append("**").append(entry.getValue());
            } else {
                ret.append(entry.getKey());
            }
        }
        if (ret.length() == 0) {
            ret.append("1");
        }
        if (!this.getFront().isEmpty()) {
            ret = new StringBuilder(this.getFront() + "((" + ret + "))");
        }
        return ret.toString();
    }

    @Override
    public Monomial apply(HashMap<String, Monomial> args) {
        Monomial ret = new MonoProd();
        if (this.getExpX().compareTo(BigInteger.ZERO) != 0) {
            ret = ret.multiply(args.get("x").power(this.getExpX()));
        }
        if (this.getExpY().compareTo(BigInteger.ZERO) != 0) {
            ret = ret.multiply(args.get("y").power(this.getExpY()));
        }
        if (this.getExpZ().compareTo(BigInteger.ZERO) != 0) {
            ret = ret.multiply(args.get("z").power(this.getExpZ()));
        }
        for (Map.Entry<Monomial, BigInteger> entry : this.monomials.entrySet()) {
            ret = ret.multiply(entry.getKey().apply(args).power(entry.getValue()));
        }
        ret.setFront(this.getFront());
        return ret;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        MonoProd ret = new MonoProd();
        ret.setFront(this.getFront());
        for (Map.Entry<Monomial, BigInteger> entry : monomials.entrySet()) {
            ret.monomials.put((Monomial) entry.getKey().clone(), entry.getValue());
        }
        ret.expX = this.expX;
        ret.expY = this.expY;
        ret.expZ = this.expZ;
        return ret;
    }

    @Override
    public Monomial power(BigInteger expo) {
        if (this.isSplittable()) {
            MonoProd ret = new MonoProd();
            for (Map.Entry<Monomial, BigInteger> entry : this.monomials.entrySet()) {
                ret.monomials.put(entry.getKey(), entry.getValue().multiply(expo));
            }
            ret.expX = this.expX.multiply(expo);
            ret.expY = this.expY.multiply(expo);
            ret.expZ = this.expZ.multiply(expo);
            return ret;
        } else {
            return new MonoProd(this, expo);
        }
    }
}
