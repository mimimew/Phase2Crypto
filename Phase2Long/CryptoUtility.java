import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CryptoUtility {

    static long GCD(long a, long b)
    {
        if (b == 0)
            return a;

        return GCD(b, a % b);
    }

    static long FastExpo(long base, long exp, long N)
    {
        long t = 1L;
        while (exp > 0) {
 
            if (exp % 2 != 0)
                t = (t * base) % N;
 
            base = (base * base) % N;
            //System.out.println(t + " " + base);
            exp /= 2;
        }

        return t % N;
    }

    static boolean IsPrime(Long n)
    {
     
        Random rand = new Random(); 
         
        Long a = rand.nextLong(n - 3) + 2;
     
        Long e = (n - 1) / 2;

        int t = 100;
     
        while(t > 0)
        {

            if (GCD(a, n) > 1)
                return false;
     
            long result = FastExpo(a, e, n);
     
            if((result % n) == 1 || (result % n) == (n - 1))
            {
                a = rand.nextLong(n - 3) + 2;
                t -= 1;
            }
     
            else {
                //System.out.println(result);
                return false;
            }
                 
        }
         
        return true;
    }

    static long FindInverse(long A, long M)
    {
 
        long m0 = M;
        long b1 = 1, b2 = 0;
        
        //System.out.println(A + "\t" + M + "\t0\t" + b1 + "\t" + b2 );
            
        while (M > 1) {
            // q is quotient
            long q = A / M;
            
            // m is remainder now, process
            // same as Euclid's algo
            long t = M;
            M = A % M;
            A = t;
                
            // Update x and y
            t = b2;
            b2 = b1 - q * b2;
            b1 = t;
            //System.out.println(A + "\t" + M + "\t" + q + "\t" + b1 + "\t" + b2 );
        }
            
        // Make x positive
        if (b2 < 0)
            b2 = (b2 + m0) % m0;
 
        return b2;
    }

    static long Power(long base, long exp) {
        long res = base;

        for (int i = 0; i < exp; i++) {
            res *= base;
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