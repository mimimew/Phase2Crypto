import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CryptoUtility {

    static BigInteger GCD(BigInteger a, BigInteger b) {
        if (b.equals(BigInteger.ZERO))
            return a;

        return GCD(b, a.mod(b));
    }

    static BigInteger FastExpo(BigInteger base, BigInteger exp, BigInteger N) {
        BigInteger t = BigInteger.ONE;
        while (!exp.equals(BigInteger.ZERO)) {

            if (exp.mod(BigInteger.valueOf(2)).equals(BigInteger.ONE))
                t = t.multiply(base).mod(N);

            base = base.multiply(base).mod(N);
            exp = exp.divide(BigInteger.valueOf(2));
        }

        return t.mod(N);
    }

    static boolean IsPrime(BigInteger n) {
        Random rand = new Random();
        BigInteger a = new BigInteger(n.subtract(BigInteger.valueOf(3)).bitLength(), rand).add(BigInteger.valueOf(2));
        BigInteger e = n.subtract(BigInteger.ONE).divide(BigInteger.valueOf(2));

        int t = 100;

        while (t > 0) {
            if (GCD(a, n).compareTo(BigInteger.ONE) > 0)
                return false;

            BigInteger result = FastExpo(a, e, n);

            if (result.mod(n).equals(BigInteger.ONE) || result.mod(n).equals(n.subtract(BigInteger.ONE))) {
                a = new BigInteger(n.subtract(BigInteger.valueOf(3)).bitLength(), rand).add(BigInteger.valueOf(2));
                t -= 1;
            } else {
                return false;
            }
        }

        return true;
    }

    static BigInteger FindInverse(BigInteger A, BigInteger M) {

        BigInteger m0 = M;
        BigInteger b1 = BigInteger.ONE, b2 = BigInteger.ZERO;

        while (!M.equals(BigInteger.ONE)) {
            BigInteger q = A.divide(M);
            BigInteger t = M;
            M = A.mod(M);
            A = t;
            t = b2;
            b2 = b1.subtract(q.multiply(b2));
            b1 = t;
        }

        if (b2.compareTo(BigInteger.ZERO) < 0)
            b2 = b2.add(m0).mod(m0);

        return b2;
    }

    static BigInteger Power(BigInteger base, BigInteger exp) {
        BigInteger res = base;

        for (BigInteger i = BigInteger.ZERO; i.compareTo(exp) < 0; i = i.add(BigInteger.ONE)) {
            res = res.multiply(base);
        }

        return res;
    }

    public static String bytesToBitsBinary(byte[] byteData) {
        StringBuilder bitsData = new StringBuilder();
        for (byte b : byteData) {
            bitsData.append(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
        }
        return bitsData.toString();
    }

    public static byte[] bitsToBytes(String bitString) {
        if (bitString.length() % 8 != 0) {
            bitString = bitString + "0".repeat(8 - (bitString.length() % 8));
        }
        List<Byte> byteValues = new ArrayList<>();
        for (int i = 0; i < bitString.length(); i += 8) {
            String chunk = bitString.substring(i, Math.min(i + 8, bitString.length()));
            byteValues.add((byte) Integer.parseInt(chunk, 2));
        }
        while (byteValues.get(byteValues.size() - 1) == 0) {
            byteValues.remove(byteValues.size() - 1);
        }
        byte[] byteArray = new byte[byteValues.size()];
        for (int i = 0; i < byteValues.size(); i++) {
            byteArray[i] = byteValues.get(i);
        }
        return byteArray;
    }

    public static byte[] bitsToBytesNoPad(String bitString) {
        if (bitString.length() % 8 != 0) {
            bitString = bitString + "0".repeat(8 - (bitString.length() % 8));
        }
        byte[] byteArray = new byte[bitString.length() / 8];
        for (int i = 0; i < bitString.length(); i += 8) {
            String chunk = bitString.substring(i, i + 8);
            byteArray[i / 8] = (byte) Integer.parseInt(chunk, 2);
        }
        return byteArray;
    }
}
