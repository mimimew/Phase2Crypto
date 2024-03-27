import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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

public class CryptoElgamal{
    public static long GenGenerator(long p){
        Random random = new Random();
        long rand = random.nextLong(p - 1) + 2;
        // rand = fastExpo(rand, 2, p);
        if (CryptoUtility.IsPrime((p - 1) / 2)) {
            long n = (p - 1) / 2;
            if (CryptoUtility.FastExpo(rand, n, p) != 1) {
                return rand;
            } else {
                return -rand + p;
            }
        } else {
            Set<Long> s = GenPrimeFactor(p - 1);
            while (!CheckGenerator(rand, s, p)) {
                // rand = random.nextInt(p - 1) + 2;
                rand = (rand + 1) % p;
                //System.out.println(rand);
                // rand = fastExpo(rand, 2, p);
            }
            return rand;
        }
    }

    public static boolean CheckGenerator(long g, Set<Long> s, long p) {
        if (CryptoUtility.GCD(g, p) != 1) {
            return false;
        }
        for (long i : s) {
            if (CryptoUtility.FastExpo(g, (p - 1) / i, p) == 1) {
                return false;
            }
        }
        return true;
    }

    public static Set<Long> GenPrimeFactor(long p) {
        Set<Long> s = new HashSet<>();
        while (p % 2 == 0) {
            s.add((long)2);
            p /= 2;
        }
        
        for (long i = 3; i * i <= p; i += 2) {
            while (p % i == 0) {
                s.add(i);
                p /= i;
            }
        }

        if (p > 2) {
            s.add(p);
        }
        return s;
    }

    public static Map<String, Object> ElgamalKeyGen(long p) {
        Map<String, Object> publicKey = new HashMap<>();
        Map<String, Object> privateKey = new HashMap<>();

        long g = GenGenerator(p);
        Random random = new Random();
        long u = random.nextLong(p - 1) + 2;
        long y = CryptoUtility.FastExpo(g, u, p);

        System.out.println("Public key (p, g, y) : (" + p + ", " + g + ", " + y + ")");
        // System.out.println("Private key : " + u);

        publicKey.put("p", p);
        publicKey.put("g", g);
        publicKey.put("y", y);

        privateKey.put("u", u);
        privateKey.put("p", p);

        System.out.println("Private Key (u, p) : (" + u + ", " + p +")");

        return Map.of("publicKey", publicKey, "privateKey", privateKey);
    }

    public static List<Map<String, Long>> ElgamalEncrypt(Map<String, Object> pk, List<Integer> readF) {
        List<Map<String, Long>> cipher = new ArrayList<>();
        for (long ele : readF) {
            Random random = new Random();
            long p = ((Number) pk.get("p")).longValue();
            long k = (long) random.nextLong(p - 1) + 2;
            while (CryptoUtility.GCD(k, p - 1) != 1) {
                k = (long) random.nextLong(p - 1) + 2;
            }
            cipher.add(GenAB(pk, k, ele));
        }
        return cipher;
    }

    public static Map<String, Long> GenAB(Map<String, Object> pk, long k, long txt) {
        long g = (int) pk.get("g");
        long p = (int) pk.get("p");
        long y = (int) pk.get("y");
        long a = CryptoUtility.FastExpo(g, k, p);
        long b = (CryptoUtility.FastExpo(y, k, p) * txt) % p;

        return Map.of("a", a, "b", b);
    }

    public static List<Long> ElgamalDecrypt(Map<String, Object> sk, List<Map<String, Long>> cipher) {
        List<Long> res = new ArrayList<>();
        int exp = (int) sk.get("p") - 1 - (int) sk.get("u"); // แก้ให้เป็น int
        for (Map<String, Long> ele : cipher) {
            long decrypted = (CryptoUtility.FastExpo(ele.get("a"), exp, ((Integer) sk.get("p")).intValue()) * ele.get("b")) % ((Integer) sk.get("p")).intValue(); // แก้ไขให้เป็น int
            res.add(decrypted);
        }
        return res;
    }            

    public static List<Integer> readPlainText(String filename, long p) throws IOException {
        int blocksize = Long.toBinaryString(p).length() - 1;
        Path path = Paths.get(filename);
        byte[] data = Files.readAllBytes(path);
        String bitsString = CryptoUtility.bytesToBitsBinary(data);
        List<Integer> block = new ArrayList<>();
    
        for (long i = 0; i < bitsString.length(); i += blocksize) {
            int endIndex = (int) Math.min(i + blocksize, bitsString.length()); // แปลงเป็น int
            String ele = bitsString.substring((int) i, endIndex); // แปลงเป็น int
            if (ele.length() != blocksize) {
                ele += "0".repeat(blocksize - ele.length());
            }
            block.add(Integer.parseInt(ele, 2));
        }
    
        return block;
    }

    public static void writePlainText(List<Long> plainText, long p, String file) throws IOException {
        int blocksize = Integer.toBinaryString((int) p).length() - 1;
        StringBuilder res = new StringBuilder();
        
        for (Long ele : plainText) {
            String b = Long.toBinaryString(ele);
            res.append("0".repeat(blocksize - b.length())).append(b);
        }

        byte[] resBytes = CryptoUtility.bitsToBytes(res.toString());
        Files.write(Paths.get(file), resBytes);
    }

    public static void outputCipher(List<Map<String, Long>> cipher, int p, String file) throws IOException {
        int blocksize = Integer.toBinaryString(p).length();
        FileOutputStream f = new FileOutputStream(file);
        StringBuilder res = new StringBuilder();
        
        for (Map<String, Long> ele : cipher) {
            String a = Long.toBinaryString(ele.get("a"));
            String b = Long.toBinaryString(ele.get("b"));
            res.append("0".repeat(blocksize - a.length())).append(a);
            res.append("0".repeat(blocksize - b.length())).append(b);
        }

        byte[] resBytes = CryptoUtility.bitsToBytes(res.toString());
        f.write(resBytes);
        f.close();
    }

    public static List<Map<String, Long>> inputCipher(String file, long p) throws IOException {
        byte[] data = Files.readAllBytes(Paths.get(file));
        String bitsString = CryptoUtility.bytesToBitsBinary(data);
        int blocksize = Integer.toBinaryString((int) p).length();
        List<String> dataBlocks = new ArrayList<>();
        
        for (int i = 0; i < bitsString.length(); i += blocksize) {
            dataBlocks.add(bitsString.substring(i, Math.min(i + blocksize, bitsString.length())));
        }

        List<Map<String, Long>> res = new ArrayList<>();
        for (int i = 0; i < dataBlocks.size(); i += 2) {
            if (i + 1 < dataBlocks.size()) {
                Map<String, Long> ele = Map.of(
                        "a", Long.parseLong(dataBlocks.get(i), 2),
                        "b", Long.parseLong(dataBlocks.get(i + 1), 2)
                );
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
                publicKey.put("p", Integer.parseInt(parts[1]));
                publicKey.put("g", Integer.parseInt(parts[2]));
                publicKey.put("y", Integer.parseInt(parts[3]));
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
        return Map.of("u", Integer.parseInt(data[0]), "p", Integer.parseInt(data[1]));
    }

    public static void writePublicKey(String owner, Object publicKeyObj) throws IOException {
        // อ่านข้อมูล public key ที่มีอยู่ในไฟล์ pk.txt
        Map<String, Map<String, Object>> pkList = readPublicKey();
    
        // แปลง publicKeyObj เป็นประเภทข้อมูลที่ถูกต้อง (Map<String, Object>)
        Map<String, Object> publicKey = (Map<String, Object>) publicKeyObj;
    
        // อัปเดตข้อมูลใน pkList ด้วยคีย์และค่าของคีย์สาธารณะ
        pkList.put(owner, publicKey);
    
        // สร้าง StringBuilder เพื่อสร้างสตริงที่จะเขียนลงในไฟล์
        StringBuilder out = new StringBuilder();
        
        // วนลูปเพื่อสร้างสตริงที่จะเขียนลงในไฟล์ โดยรวมข้อมูลของคีย์สาธารณะทั้งหมด
        for (String publicKeyOwner : pkList.keySet()) {
            Map<String, Object> publicKeyData = pkList.get(publicKeyOwner);
            out.append(publicKeyOwner).append(" ");
            out.append(publicKeyData.get("p")).append(" ");
            out.append(publicKeyData.get("g")).append(" ");
            out.append(publicKeyData.get("y")).append("\n");
        }
        
        // เปิดไฟล์ pk.txt เพื่อเขียนข้อมูลลงในไฟล์
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("pk.txt"))) {
            writer.write(out.toString());
        }
    }
    

    public static void writePrivateKey(Object privateKeyObj) throws IOException {
        Map<String, Object> privateKey = (Map<String, Object>) privateKeyObj;

        Object u = privateKey.get("u");
        Object p = privateKey.get("p");
        String out = u + " " + p;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("sk.txt"))) {
            writer.write(out);
        }
    }
    
}