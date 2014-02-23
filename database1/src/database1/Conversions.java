
/*******************************************************************************
 * @file  Conversions.java
 *
 * @author   John Miller
 *
 * @see http://snippets.dzone.com/posts/show/93
 */

package database1;

import static java.lang.System.out;

import java.nio.ByteBuffer;

/*******************************************************************************
 * This class provides methods for converting Java's primitive data types into
 * byte arrays.
 */
public class Conversions
{
    /***************************************************************************
     * Convert short into a byte array.
     * @param value  the short value to convert
     * @return  a corresponding byte array
     */
    public static byte [] short2ByteArray (short value)   
    {
        return new byte [] { (byte) (value >>> 8),   
                             (byte) value };         
    } // short2ByteArray

    /***************************************************************************
     * Convert int into a byte array.
     * @param value  the int value to convert
     * @return  a corresponding byte array
     */
    public static byte [] int2ByteArray (int value)
    {
        return new byte [] { (byte) (value >>> 24),
                             (byte) (value >>> 16),
                             (byte) (value >>> 8),
                             (byte) value };
    } // int2ByteArray

    /***************************************************************************
     * Convert long into a byte array.
     * @param value  the long value to convert
     * @return  a corresponding byte array
     */
    public static byte [] long2ByteArray (long value)
    {
        return new byte [] { (byte) (value >>> 56),
                             (byte) (value >>> 48),
                             (byte) (value >>> 40),
                             (byte) (value >>> 32),
                             (byte) (value >>> 24),
                             (byte) (value >>> 16),
                             (byte) (value >>> 8),
                             (byte) value };
    } // long2ByteArray

    /***************************************************************************
     * Convert float into a byte array.
     * @param value  the float value to convert
     * @return  a corresponding byte array
     */
    public static byte [] float2ByteArray (float value)  
    {

    	byte [] bytes = ByteBuffer.allocate(4).putFloat(value).array();
    	return bytes;
    	

    } // float2ByteArray

    /***************************************************************************
     * Convert double into a byte array.
     * @param value  the double value to convert
     * @return  a corresponding byte array
     */
    public static byte [] double2ByteArray (double value)
    {
    	byte [] bytes = ByteBuffer.allocate(8).putDouble(value).array(); 
    	return bytes;
    	
//        return null;
    } // double2ByteArray

    
    
    public static void main (String [] args)  //for test
    {
    	short i = 12;
    	
    	byte[] result = short2ByteArray(i);
    	
    	for (byte temp : result ) {
    		out.println (temp);
		}
    	/*    	
    	Character b='M';
    	out.println (b.toString());
    	byte [] j =b.toString().getBytes();
    	for (byte temp : j ) {
    		out.println (temp);
		}
    	
    	if (b.getClass().toString().equals("class java.lang.Character")) {
    		out.println (b.getClass().toString());
		}
    							
	
    	
    	Comparable tempElement = " e r";
    	out.print(tempElement.getClass());
        tempElement = tempElement.toString().trim();
        out.print(tempElement);
        

        
        String s1,s2,s3 = "abc", s4 ="abc" ;
        s1 = new String("abc");
        s2 = new String("abc");
        System.out.println(s1 == s2);//£¨1£©true
        System.out.println(s1.equals(s3));//£¨2£©true
    	
//    	int temp=Float.floatToIntBits(i);
//    	out.print(temp);
*/       
    
    	
    } // main
    
    
} // Conversions




