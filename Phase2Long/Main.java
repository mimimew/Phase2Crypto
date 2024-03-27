import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main extends CryptoElgamal{
    public static void main(String[] args) throws IOException{
        Scanner scanner = new Scanner(System.in);
        System.out.print("Input Mode (1:KeyGen, 2:Encrypt, 3:Decrypt) : ");
        int mode = scanner.nextInt();
        long p;
        if (mode == 1) {
            System.out.print("What Key File : ");
            Scanner sc = new Scanner(System.in);
            String keyFile = sc.nextLine();
    
            if (keyFile.isEmpty()) {
                p = CryptoP1.GenPrime("inp.txt", 20);
            } else {
                p = CryptoP1.GenPrime(keyFile, 20);
            }
            Map<String, Object> keyPair = ElgamalKeyGen(p);
            
            // Check if the public key and private key are of the expected type
            Object publicKeyObj = keyPair.get("publicKey");
            Object privateKeyObj = keyPair.get("privateKey");

            writePublicKey("me", publicKeyObj);
            writePrivateKey(privateKeyObj);

            sc.close();
        }
        else if (mode == 2) {
            Scanner sc = new Scanner(System.in);
            System.out.print("What File : ");
            String file = sc.nextLine();
            System.out.print("Send to : ");
            String who = sc.nextLine();
            System.out.print("Output File : ");
            String output = sc.nextLine();
            sc.close();
        
            String[] fileParts = file.split("\\.");
            String fileExtension = fileParts[fileParts.length - 1];
        
            Map<String, Object> publicKeyWho = readPublicKeyWho(who);
            
            System.out.println(publicKeyWho);

            if (publicKeyWho == null) {
                System.out.println("Error: Public key for recipient not found.");
                return;
            }
        
            try {
                List<Integer> plainText = readPlainText(file, ((Number) publicKeyWho.get("p")).longValue());
                List<Map<String, Long>> cipher = ElgamalEncrypt(publicKeyWho, plainText);
        
                if (output.equals("")) {
                    output = "output." + fileExtension;
                }
        
                outputCipher(cipher, (int) publicKeyWho.get("p"), output);
            } catch (IOException e) {
                System.out.println("Error: Failed to read plaintext file.");
                e.printStackTrace();
            }
        }else if (mode == 3) {
            String cipherFile, output;
            System.out.print("Cipher File : ");
            Scanner sc = new Scanner(System.in);
            cipherFile = sc.nextLine();
            
            System.out.print("Output File : ");
            output = sc.nextLine();
            
            Map<String, Object> sk = readPrivateKey();
            List<Map<String, Long>> newCipher = inputCipher(cipherFile, ((Integer) sk.get("p")).intValue());
            List<Long> plainText = ElgamalDecrypt(sk, newCipher);
            writePlainText(plainText, ((Integer) sk.get("p")).intValue(), output);
            sc.close();
        }else{
            System.out.println("Mode error!!");
        }
        scanner.close();
    }
}