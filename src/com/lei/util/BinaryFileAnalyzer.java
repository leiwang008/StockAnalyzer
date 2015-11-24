package com.lei.util;
//Add in master
//Add in master
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class BinaryFileAnalyzer {
	private boolean debug = false;
	
	//Modify on master branch
	//Add on master branch
	public void setDebug(boolean debug){
		this.debug = debug;
	}
	
	/**
	 * Reads the next "unsigned integer" from the input stream.
	 * <p>
	 * This method reads the next 4 bytes from the input stream, and convert 
	 * them to a long (because java does not support unsigned data types), using
	 * the first byte as the highest byte and treating each byte as unsigned.
	 * For example:
	 * <pre>
	 * 00 00 01 01 -> 257
	 * 00 00 00 FF -> 255
	 * </pre>
	 * <p>
	 * @param in	the STAF log input stream.
	 * @return		a <code>long</code> equal to the unsigned integer converted 
	 * 				from the bytes.
	 */
	protected long readUInt(DataInputStream in) throws IOException
	{
		long[] l = new long[4];
		for (int i = 0; i < 4; i++){
			l[i] = in.readUnsignedByte();
		}
		long retval = (l[0]<<24) + (l[1]<<16) + (l[2]<<8) + l[3];
		if(debug){
			System.out.println("BinaryFileAnalyzer.readUInt(): " +
			"read=" + l[0] + " " + l[1] + " " + l[2] + " " + l[3] + "; " +
			"return=" + retval);
		}
		in.readInt();
		return retval;
	}
	
	/**
	 * Read the next string from the STAF log.
	 * <p>
	 * Strings are saved in the STAF log as byte data, with the number of bytes 
	 * (saved as 4-byte long unsigned integer) preceeding the actual data.
	 * <p>
	 * @param in	the STAF log input stream.
	 * @return		the string value read from the input stream.
	 */
	protected String readString(DataInputStream in)
		throws IOException
	{
		long len = readUInt(in);
		if (len < 0) return null;
		if (len == 0) return "";

		byte[] b = new byte[(int)len];
		in.read(b);
		String retval = new String(b, "UTF-8");
		//String retval = new String(b);
		if(debug){
			System.out.println("BinaryFileAnalyzer.readString(): return=" + retval);
		}
		return retval;
	}
	
	public static void main(String[] args){
		DataOutputStream dos = null;
		
		try {
			dos = new DataOutputStream(new FileOutputStream(args[0]));
//			dos.writeChars("a test string ����");
//			dos.writeBytes("a test string ����");
//			dos.writeUTF("a test string ����");
//			dos.writeInt(67);
//			dos.writeLong(67L);
			dos.writeByte(129);
			dos.writeByte(128);
			dos.writeByte(127);
			dos.writeByte(61);
			dos.flush();
		} catch (FileNotFoundException e) {
			System.out.println("FileNotFoundException occured.");
			return;
		} catch (IOException e) {
			System.out.println("IOException occured.");
		}finally{
			if(dos!=null){
				try {
					dos.close();
				} catch (IOException e) {
				}
			}
		}
		
//		PrintWriter writer = null;
//		
//		try {
//			writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(args[0])));
//			writer.println("next test string");
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		}finally{
//			if(writer!=null){
//				writer.close();
//			}
//		}
		
	}
}
