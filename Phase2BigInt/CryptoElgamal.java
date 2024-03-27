import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class CryptoElgamal {
    public static BigInteger GenGenerator(BigInteger p) {
        Random random = new Random();
        BigInteger rand = new BigInteger(p.bitLength(), random);
        while (rand.compareTo(p.subtract(BigInteger.ONE)) >= 0 || rand.compareTo(BigInteger.TWO) <= 0) {
            rand = new BigInteger(p.bitLength(), random);
        }

        if (CryptoUtility.IsPrime((p.subtract(BigInteger.ONE)).divide(BigInteger.TWO))) {
            BigInteger n = (p.subtract(BigInteger.ONE)).divide(BigInteger.TWO);
            if (!CryptoUtility.FastExpo(rand, n, p).equals(BigInteger.ONE)) {
                return rand;
            } else {
                return rand.negate().add(p);
            }
        } else {
            Set<BigInteger> s = GenPrimeFactor(p.subtract(BigInteger.ONE));
            while (!CheckGenerator(rand, s, p)) {
                rand = rand.add(BigInteger.ONE).mod(p);
            }
            return rand;
        }
    }

    public static boolean CheckGenerator(BigInteger g, Set<BigInteger> s, BigInteger p) {
        if (!g.gcd(p).equals(BigInteger.ONE)) {
            return false;
        }
        for (BigInteger i : s) {
            if (CryptoUtility.FastExpo(g, (p.subtract(BigInteger.ONE)).divide(i), p).equals(BigInteger.ONE)) {
                return false;
            }
        }
        return true;
    }

    public static Set<BigInteger> GenPrimeFactor(BigInteger p) {
        Set<BigInteger> s = new HashSet<>();
        BigInteger two = BigInteger.TWO;
        while (p.mod(two).equals(BigInteger.ZERO)) {
            s.add(two);
            p = p.divide(two);
        }

        for (BigInteger i = BigInteger.valueOf(3); i.multiply(i).compareTo(p) <= 0; i = i.add(BigInteger.TWO)) {
            while (p.mod(i).equals(BigInteger.ZERO)) {
                s.add(i);
                p = p.divide(i);
            }
        }

        if (p.compareTo(two) > 0) {
            s.add(p);
        }
        return s;
    }

    public static Map<String, Object> ElgamalKeyGen(BigInteger p) {
        Map<String, Object> publicKey = new HashMap<>();
        Map<String, Object> privateKey = new HashMap<>();

        BigInteger g = GenGenerator(p);
        Random random = new Random();
        BigInteger u = new BigInteger(p.bitLength(), random);
        while (u.compareTo(p.subtract(BigInteger.ONE)) >= 0 || u.compareTo(BigInteger.TWO) <= 0) {
            u = new BigInteger(p.bitLength(), random);
        }
        BigInteger y = CryptoUtility.FastExpo(g, u, p);

        System.out.println("Public key (p, g, y) : (" + p + ", " + g + ", " + y + ")");
        // System.out.println("Private key : " + u);

        publicKey.put("p", p);
        publicKey.put("g", g);
        publicKey.put("y", y);

        privateKey.put("u", u);
        privateKey.put("p", p);

        System.out.println("Private Key (u, p) : (" + u + ", " + p + ")");

        return Map.of("publicKey", publicKey, "privateKey", privateKey);
    }

    public static List<Map<String, BigInteger>> ElgamalEncrypt(Map<String, Object> pk, List<BigInteger> plainText) {
        List<Map<String, BigInteger>> cipher = new ArrayList<>();
        for (BigInteger ele : plainText) {
            Random random = new Random();
            BigInteger p = (BigInteger) pk.get("p");
            BigInteger k = new BigInteger(p.bitLength(), random);
            while (k.compareTo(p.subtract(BigInteger.ONE)) >= 0 || k.compareTo(BigInteger.TWO) <= 0
                    || !k.gcd(p.subtract(BigInteger.ONE)).equals(BigInteger.ONE)) {
                k = new BigInteger(p.bitLength(), random);
            }
            cipher.add(GenAB(pk, k, ele));
        }
        return cipher;
    }

    public static Map<String, BigInteger> GenAB(Map<String, Object> pk, BigInteger k, BigInteger txt) {
        BigInteger g = (BigInteger) pk.get("g");
        BigInteger p = (BigInteger) pk.get("p");
        BigInteger y = (BigInteger) pk.get("y");
        BigInteger a = CryptoUtility.FastExpo(g, k, p);
        BigInteger b = CryptoUtility.FastExpo(y, k, p).multiply(txt).mod(p);

        return Map.of("a", a, "b", b);
    }

    public static List<BigInteger> ElgamalDecrypt(Map<String, Object> sk, List<Map<String, BigInteger>> cipher) {
        List<BigInteger> res = new ArrayList<>();
        BigInteger exp = ((BigInteger) sk.get("p")).subtract(BigInteger.ONE).subtract((BigInteger) sk.get("u"));
        for (Map<String, BigInteger> ele : cipher) {
            BigInteger decrypted = CryptoUtility.FastExpo(ele.get("a"), exp, (BigInteger) sk.get("p"))
                    .multiply(ele.get("b")).mod((BigInteger) sk.get("p"));
            res.add(decrypted);
        }
        return res;
    }

    public static List<BigInteger> readPlainText(String filename, BigInteger p) throws IOException {
        int blocksize = p.bitLength() - 1;
        Path path = Paths.get(filename);
        byte[] data = Files.readAllBytes(path);
        String bitsString = CryptoUtility.bytesToBitsBinary(data);
        List<BigInteger> block = new ArrayList<>();

        for (int i = 0; i < bitsString.length(); i += blocksize) {
            int endIndex = Math.min(i + blocksize, bitsString.length());
            String ele = bitsString.substring(i, endIndex);
            if (ele.length() != blocksize) {
                ele += "0".repeat(blocksize - ele.length());
            }
            block.add(new BigInteger(ele, 2));
        }

        return block;
    }

    public static void writePlainText(List<BigInteger> plainText, BigInteger p, String file) throws IOException {
        int blocksize = p.bitLength() - 1;
        StringBuilder res = new StringBuilder();

        for (BigInteger ele : plainText) {
            String b = ele.toString(2);
            res.append("0".repeat(blocksize - b.length())).append(b);
        }

        byte[] resBytes = CryptoUtility.bitsToBytes(res.toString());
        Files.write(Paths.get(file), resBytes);
    }

    public static void outputCipher(List<Map<String, BigInteger>> cipher, BigInteger p, String file)
            throws IOException {
        int blocksize = p.bitLength();
        FileOutputStream f = new FileOutputStream(file);
        StringBuilder res = new StringBuilder();

        for (Map<String, BigInteger> ele : cipher) {
            String a = ele.get("a").toString(2);
            String b = ele.get("b").toString(2);
            res.append("0".repeat(blocksize - a.length())).append(a);
            res.append("0".repeat(blocksize - b.length())).append(b);
        }

        byte[] resBytes = CryptoUtility.bitsToBytes(res.toString());
        f.write(resBytes);
        f.close();
    }

    public static List<Map<String, BigInteger>> inputCipher(String file, BigInteger p) throws IOException {
        byte[] data = Files.readAllBytes(Paths.get(file));
        String bitsString = CryptoUtility.bytesToBitsBinary(data);
        int blocksize = p.bitLength();
        List<String> dataBlocks = new ArrayList<>();

        // Check if each data block has the correct length
        for (int i = 0; i < bitsString.length(); i += blocksize) {
            dataBlocks.add(bitsString.substring(i, Math.min(i + blocksize, bitsString.length())));
        }

        List<Map<String, BigInteger>> res = new ArrayList<>();
        for (int i = 0; i < dataBlocks.size(); i += 2) {
            if (i + 1 < dataBlocks.size()) {
                // Convert string to BigInteger with base 2
                BigInteger a = new BigInteger(dataBlocks.get(i), 2);
                BigInteger b = new BigInteger(dataBlocks.get(i + 1), 2);

                // Create map with BigInteger values
                Map<String, BigInteger> ele = Map.of("a", a, "b", b);
                res.add(ele);
            }
        }

        return res;
    }

    public static Map<String, Map<String, Object>> readPublicKey() throws IOException {
        String filePath = "pk.txt";
        Map<String, Map<String, Object>> res = new HashMap<>();
        BufferedReader reader = new BufferedReader(new FileReader(filePath));

        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(" ");
            if (parts.length > 1) {
                Map<String, Object> publicKey = new HashMap<>();
                BigInteger p = new BigInteger(parts[1]);
                BigInteger g = new BigInteger(parts[2]);
                BigInteger y = new BigInteger(parts[3]);
                publicKey.put("p", p);
                publicKey.put("g", g);
                publicKey.put("y", y);
                res.put(parts[0], publicKey);
            }
        }

        reader.close();
        return res;
    }

    public static Map<String, Object> readPublicKeyWho(String who) throws IOException {
        Map<String, Map<String, Object>> pkList = readPublicKey();
        return pkList.get(who);
    }

    public static Map<String, Object> readPrivateKey() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader("sk.txt"));
        String line = reader.readLine();
        reader.close();
        String[] data = line.split(" ");
        BigInteger u = new BigInteger(data[0]);
        BigInteger p = new BigInteger(data[1]);
        return Map.of("u", u, "p", p);
    }

    public static void writePublicKey(String owner, Object publicKeyObj) throws IOException {
        Map<String, Map<String, Object>> pkList = readPublicKey();
        Map<String, Object> publicKey = (Map<String, Object>) publicKeyObj;
        pkList.put(owner, publicKey);
        StringBuilder out = new StringBuilder();

        for (String publicKeyOwner : pkList.keySet()) {
            Map<String, Object> publicKeyData = pkList.get(publicKeyOwner);
            out.append(publicKeyOwner).append(" ");
            BigInteger p = (BigInteger) publicKeyData.get("p");
            BigInteger g = (BigInteger) publicKeyData.get("g");
            BigInteger y = (BigInteger) publicKeyData.get("y");
            out.append(p.toString()).append(" ");
            out.append(g.toString()).append(" ");
            out.append(y.toString()).append("\n");
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("pk.txt"))) {
            writer.write(out.toString());
        }
    }

    public static void writePrivateKey(Object privateKeyObj) throws IOException {
        Map<String, Object> privateKey = (Map<String, Object>) privateKeyObj;
        BigInteger u = (BigInteger) privateKey.get("u");
        BigInteger p = (BigInteger) privateKey.get("p");
        String out = u.toString() + " " + p.toString();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("sk.txt"))) {
            writer.write(out);
        }
    }

}