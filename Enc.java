import java.util.Arrays;

public class Enc {
	// n is the block size, m is R's size
	// These should probably be project-global somewhere
	public static int n = 32;
	public static int m = 20;

	public static byte[][] toBlocks(String plaintext)
	{
		String[] strArray = plaintext.split("\\s+");
		int remainder;

		// find total number of blocks
		int blockNum = strArray.length;
		
		// convert string to byte array
		byte[][] wordArray = new byte[blockNum][n];
		for (int i = 0; i < blockNum; i++){
			remainder = n - strArray[i].length();
			// right pad the string with space
			if (remainder > 0) {
				String pad = String.format("%1$-"+ remainder +"s","");
				strArray[i] = strArray[i] + pad;
			}

			wordArray[i] = strArray[i].getBytes();
			//System.out.println(byteArray[i]);
		}
		return wordArray;
	}
	
	public static byte[] preEnc(byte[][] wordArray, int blockIndex, byte[] key2)
	{	
		byte[] word = wordArray[blockIndex];
		//encrypt Word with E(key2)
		return TwoBlockEncrypt.encrypt(word, key2);
	}
	
	public static byte[] getPubkey(byte[] X, byte[] key1)
	{
		// pass first n-m bytes of X to L
		byte[] L = new byte[n-m];
		for(int i = 0; i < n-m; i++){
			L[i] = X[i];
		}
		//generate public key using f and key1, k = fkey1(L);
		byte[] ki = PRF.PRF(L, key1);
		return ki;
	}
	
	public static byte[] getT(byte[] Si, byte[] key){
		byte[] FkeySi = PRF.PRF(Si, key);
		byte[] Ti = new byte[n];
		// Ti = <Si, Fkey(si)>
		System.arraycopy(Si, 0, Ti, 0, n-m);
		System.arraycopy(FkeySi, 0, Ti, n-m, m);
				
		return Ti;
	}
	
	public static byte[] xor(byte[]Xi, byte[]Ti){
		byte[] Ci = new byte[n];
		for(int k = 0; k < Xi.length; k++) {
			Ci[k] = (byte) (Xi[k] ^Ti[k]);
		}
		return Ci;
	}

	public static byte[] getNM(byte[] S){
		byte[] Si = new byte[n-m];
		for (int k = 0; k < Si.length; k++) {
			Si[k] = S[k];
		}
		return Si;
	}	
		
	public static byte[][] decrypt(byte[][] cipherText, byte[] streamKey, byte[] key1, byte[] key2) {
		StreamChunker stream = new StreamChunker(streamKey);
		byte[][] decrypted = new byte[cipherText.length][];
		for (int i = 0; i < cipherText.length; i++) {
			byte[] Ci = cipherText[i];
			byte[] Yi = Arrays.copyOfRange(Ci,0,n-m);
			byte[] Si = stream.getChunk();
			byte[] Li = Enc.xor(Si, Yi);
			byte[] ki = Enc.getPubkey(Li, key1);
			byte[] Ti = Enc.getT(Si, ki);
			
			byte[] Xi = Enc.xor(Ci, Ti);
			byte[] Wi = TwoBlockEncrypt.decrypt(Xi, key2);
			decrypted[i] = Wi;
		}
		return decrypted;
	}
}
