import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;

public class CryptoP1 {
    public static BigInteger GenPrime(String string, int n) {
        BigInteger num = getNum(string, n);
        System.out.println("Number from file: " + num);
        if (num.mod(BigInteger.valueOf(2)).equals(BigInteger.ZERO) && CryptoUtility.IsPrime(num)) {
            System.out.println(num + " is Prime");
            return num; // ถ้าตัวเลขจากไฟล์เป็นเลขคู่และเป็นจำนวนเฉพาะ ให้ใช้เลขนั้น
        } else {
            System.out.println(num + " is not Prime");
            BigInteger nextPrime = findNextPrime(num);
            System.out.println("Next Prime is: " + nextPrime);
            return nextPrime;
        }
    }

    private static BigInteger findNextPrime(BigInteger num) {
        num = num.add(BigInteger.ONE); // เพิ่มค่าไปหนึ่งเพื่อทำให้เป็นจำนวนเลขคี่
        while (!CryptoUtility.IsPrime(num)) { // วนลูปจนกว่าจะเจอจำนวนเฉพาะ
            num = num.add(BigInteger.ONE);
        }
        return num;
    }

    public static BigInteger getNum(String file, int n) {
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
            return new BigInteger(bits, 2);
        } catch (IOException e) {
            e.printStackTrace();
            return BigInteger.valueOf(-1);
        }
    }
}
