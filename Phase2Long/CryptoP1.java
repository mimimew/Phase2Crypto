import java.io.FileInputStream;
import java.io.IOException;

public class CryptoP1 {
    public static long GenPrime(String string, int n) {
        long num = getNum(string, n);
        System.out.println("Number from file: " + num);
        if (num % 2 == 0 && CryptoUtility.IsPrime(num)) {
            System.out.println(num + " is Prime");
        } else {
            System.out.println(num + " is not Prime");
            num = findPrime(num, CryptoUtility.Power(2, n) - 1);
            System.out.println("Next Prime is: " + num);
        }
        return num;
    }

    public static int getNum(String file, int n) {
        try {
            FileInputStream fis = new FileInputStream(file);
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            fis.close();
            String bits = CryptoUtility.bytesToBitsBinary(data);
            while (bits.charAt(0) == '0') {
                bits = bits.substring(1);
            }
            if (bits.length() < n) {
                bits += "0".repeat(n - bits.length());
            } else {
                bits = bits.substring(0, n);
            }
            return Integer.parseInt(bits, 2);
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static long findPrime(long start, long bound) {
        if (start % 2 == 0) {
            start += 1;
        }
        while (!CryptoUtility.IsPrime(start)) {
            if (start > bound) {
                System.out.println("Out of Bound");
                System.exit(0);
            }
            start += 2;
        }
        return start;
    }

}