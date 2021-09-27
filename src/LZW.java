import java.util.*;
import java.io.*;
public class LZW {
	//All static variables used for fromAscii method, which was taken from http://www.java2s.com/Tutorial/Java/0180__File/Translatesbetweenbytearraysandstringsof0sand1s.htm
	private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
	private static final int BIT_0 = 1;
	/** Mask for bit 1 of a byte. */
	private static final int BIT_1 = 0x02;
	/** Mask for bit 2 of a byte. */
	private static final int BIT_2 = 0x04;
	/** Mask for bit 3 of a byte. */
	private static final int BIT_3 = 0x08;
	/** Mask for bit 4 of a byte. */
	private static final int BIT_4 = 0x10;
	/** Mask for bit 5 of a byte. */
	private static final int BIT_5 = 0x20;
	/** Mask for bit 6 of a byte. */
	private static final int BIT_6 = 0x40;
	/** Mask for bit 7 of a byte. */
	private static final int BIT_7 = 0x80;
	private static final int[] BITS = { BIT_0, BIT_1, BIT_2, BIT_3, BIT_4, BIT_5, BIT_6, BIT_7 };
	
	private HashMap <String, Integer> dict;//dictionary to store ascii characters. Has no max bound and no set byte length;
	private File originalFile;//input file that user wants to read in and compress
	private int dictLength;//length of dictionary
	
	ArrayList<String> originalString; //originalFile as string
	ArrayList<Integer> originalInts; //og file as int arraylist
	
	//constructor takes in a file to be compressed and intializes the dictionary of ascii characters.
	public LZW (File original)
	{
		originalFile = original;
		this.dict = new HashMap <String,Integer>();
		dictLength = 256;
		for (int k = 0; k < dictLength; k++)
		{
			dict.put("" + (char)k, k);
		}
		originalString = new ArrayList<String>(); 
		originalInts = new ArrayList<Integer>(); 
	}

	
	public void compress () throws IOException
	{
		try 
		{
			//This reader is used to read input file
			BufferedReader br = new BufferedReader (new FileReader(originalFile));
			//This stream is used to write to binary file
			FileOutputStream fileWriter = new FileOutputStream("/Users/ava/eclipse-workspace/Alex-LZW-Compression/compressedFile.bin");
			//current tracks the current character or string being checked in the algorithm
			String current = ""+ (char)br.read();
			//asciiVal is used to store the ascii value of the variable "current"
			int asciiVal;
			//binaryString is a concatentation of all the different asciiVal values in the while loop. The string contains only 1's and 0's
			String binaryString = "";
			//the mark() and reset() methods are used to make sure no characters are skipped in the buffered stream. This all works so no need to worry about it.
			br.mark(100);
			while (br.read() != -1)//checks that the input file still has things to read
			{
				br.reset();
				String next = "" + (char)br.read();
				if (dict.containsKey(current+next))
				{
					current = current + next;
				}
				else
				{
					if(current != "")
						originalString.add(current);
					
					asciiVal = dict.get(current);
					binaryString += LZW.toBinary(asciiVal, 8);//toBinary(int number, int length) number is turned into a string of 1's and 0's. length is length of binary string made by this method
					dict.put (current+next,dictLength);//not dictLength+1 because dictLength is always one value greater than dictionary index.
					current = next;
					dictLength++;
					
				}
				br.mark(100);
			}
			
			//these next two lines are neccessary to capture the bits of the last character of the file.
			asciiVal = dict.get(current);
			binaryString += LZW.toBinary(asciiVal, 8);
			
			//initializes charArray needed for fromAscii method
			char[] encodedChars = binaryString.toCharArray();
			//see fromAscii method. Resulting byte[] can be used to write to .bin file
			byte[] encodedBytes = LZW.fromAscii(encodedChars);
			fileWriter.write(encodedBytes);
			fileWriter.close();
			br.close();
			System.out.println ("File compressed");
		} 
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	//method also taken from internet.
	public static String toBinary(int x, int len)
    {
        if (len > 0)
        {
            return String.format("%" + len + "s",
                            Integer.toBinaryString(x)).replaceAll(" ", "0");
        }
 
        return null;
    }
	
	//method taken from http://www.java2s.com/Tutorial/Java/0180__File/Translatesbetweenbytearraysandstringsof0sand1s.htm
	public static byte[] fromAscii(char[] ascii) {
	    if (ascii == null || ascii.length == 0) {
	      return EMPTY_BYTE_ARRAY;
	    }
	    // get length/8 times bytes with 3 bit shifts to the right of the length
	    byte[] l_raw = new byte[ascii.length >> 3];
	    /*
	     * We decrease index jj by 8 as we go along to not recompute indices using
	     * multiplication every time inside the loop.
	     */
	    for (int ii = 0, jj = ascii.length - 1; ii < l_raw.length; ii++, jj -= 8) {
	      for (int bits = 0; bits < BITS.length; ++bits) {
	        if (ascii[jj - bits] == '1') {
	          l_raw[ii] |= BITS[bits];
	        }
	      }
	    }
	    return l_raw;
	  }
		

	public void decompress()
    {
		//create originaInts arraylist
		for(int i = 0; i < originalString.size(); i ++)
        {
			originalInts.add((Integer) dict.get(originalString.get(i)));
        }
		
		//building decode Dictionary
		HashMap<Integer, String> decodeDict = new HashMap<Integer, String>();

        for(int i= 0; i<256; i++)
        {
            decodeDict.put(i, ""+(char)i); 

        }
        
        int asciiVal = 256;
        
        Integer currentAsciiVal = 0;
        String current = "";
        Integer nextAsciiVal = 0;
        String next = "";
        String firstNextChar = "";
        for(int i = 0; i < originalInts.size()-1; i++)
        {
        	currentAsciiVal = originalInts.get(i);
        	current = decodeDict.get(currentAsciiVal);
        	
        	nextAsciiVal = originalInts.get(i+1);
        	next = decodeDict.get(nextAsciiVal);
        	
        	if (next != null)
        	{
        		firstNextChar = ""+next.charAt(0);
        	}
            decodeDict.put(asciiVal, current+firstNextChar);
            asciiVal++;
        }
        
        //printing output file as String
        String output = "";
        for(int i = 0; i < originalInts.size(); i++)
            output += decodeDict.get(originalInts.get(i));

        System.out.print("File decompressed\n");
        
        System.out.print("compressed text: "+output);
        
    }
	
	public static void main (String [] args) throws IOException
	{
		
		File f = new File ("/Users/ava/eclipse-workspace/Alex-LZW-Compression/lzw-file1.txt");
		LZW l = new LZW(f);
		l.compress();
		l.decompress();
		
	}
}