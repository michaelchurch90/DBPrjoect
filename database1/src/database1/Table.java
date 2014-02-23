
/*******************************************************************************
 * @file  Table.java
 *
 * @author   John Miller
 */

package database1;

import java.io.Serializable;

import static java.lang.Boolean.*;
import static java.lang.System.out;

import java.nio.ByteBuffer;
import java.util.*;



/*******************************************************************************
 * This class implements relational database tables (including attribute names,
 * domains and a list of tuples.  Five basic relational algebra operators are
 * provided: project, select, union, minus and join.  The insert data manipulation
 * operator is also provided.  Missing are update and delete data manipulation
 * operators.
 */
public class Table
       implements Serializable, Cloneable
{
    /** Debug flag, turn off once implemented
     */
    private static final boolean DEBUG = true;

    /** Counter for naming temporary tables.
     */
    private static int count = 0;

    /** Table name.
     */
    private final String name;

    /** Array of attribute names.
     */
    private final String [] attribute;

    /** Array of attribute domains: a domain may be
     *  integer types: Long, Integer, Short, Byte
     *  real types: Double, Float
     *  string types: Character, String
     */
    private final Class [] domain;

    /** Collection of tuples (data storage).
     */
    private final List <Comparable []> tuples;

    /** Primary key. 
     */
    private final String [] key;

    /** Index into tuples (maps key to tuple).
     */
    private final Map <KeyType, Comparable []> index;  

    /***************************************************************************
     * Construct an empty table from the meta-data specifications.
     * @param _name       the name of the relation
     * @param _attribute  the string containing attributes names
     * @param _domain     the string containing attribute domains (data types)
     * @param _key        the primary key
     */  
    public Table (String _name, String [] _attribute, Class [] _domain, String [] _key)
    {
        name      = _name;
        attribute = _attribute;
        domain    = _domain;
        key       = _key;
//        tuples    = new ArrayList <> ();                // also try FileList, see below
        tuples    = new FileList (this, tupleSize ());
        index     = new TreeMap <> ();                  // also try BPTreeMap, LinHash or ExtHash
    } // Table

    /***************************************************************************
     * Construct an empty table from the raw string specifications.
     * @param name        the name of the relation
     * @param attributes  the string containing attributes names
     * @param domains     the string containing attribute domains (data types)
     */
    public Table (String name, String attributes, String domains, String _key)
    {
        this (name, attributes.split (" "), findClass (domains.split (" ")), _key.split(" "));

        out.println ("DDL> create table " + name + " (" + attributes + ")");
    } // Table

    /***************************************************************************
     * Construct an empty table using the meta-data of an existing table.
     * @param tab     the table supplying the meta-data
     * @param suffix  the suffix appended to create new table name
     */
    public Table (Table tab, String suffix)
    {
        this (tab.name + suffix, tab.attribute, tab.domain, tab.key);
    } // Table

    /***************************************************************************
     * Project the tuples onto a lower dimension by keeping only the given attributes.
     * Check whether the original key is included in the projection.
     * #usage movie.project ("title year studioNo")
     * @param attributeList  the attributes to project onto
     * @return  the table consisting of projected tuples
     * @author Pan Huang
     */
    public Table project (String attributeList)
    {
        out.println ("RA> " + name + ".project (" + attributeList + ")");

        String [] pAttribute = attributeList.split (" ");
        int []    colPos     = match (pAttribute);
        Class []  colDomain  = extractDom (domain, colPos);
        String [] newKey     = null;    // FIX: original key if included, otherwise all atributes
        
        int nCount = 0; 
        //get the newkey
        for (int i = 0; i < pAttribute.length; i++)
        {
        	for(int j=0; j< key.length;j++)
        	{
        		if(pAttribute[i].equals(key[j]))
        		{
        			nCount++;
        		}
        		
        	}//for
        	
        }//for
		if (nCount == key.length) {
			newKey = key;
		} else {
			newKey = pAttribute;
		}
        
		Table result = new Table(name + count++, pAttribute, colDomain, newKey);

		Set<KeyType> testKey = this.index.keySet();  
		Iterator itTable1 = testKey.iterator();    
		KeyType tempKey = null;
		Comparable[] tempTup = null;
		while (itTable1.hasNext()) {
			tempKey = (KeyType) itTable1.next();
			if (!result.index.containsKey(tempKey)) {  
				tempTup = this.index.get(tempKey);
				result.tuples.add(extractTup(tempTup, colPos));
				Comparable[] keyVal = new Comparable[newKey.length];
				for (int j = 0; j < keyVal.length; j++)
					keyVal[j] = tempTup[colPos[j]];
				result.index.put(new KeyType(keyVal), tempTup);  
			}
		}

        return result;
    } // project

    /***************************************************************************
     * Select the tuples satisfying the given condition.
     * A condition is written as infix expression consists of 
     *   6 comparison operators: "==", "!=", "<", "<=", ">", ">="
     *   2 Boolean operators:    "&", "|"  (from high to low precedence)
     * #usage movie.select ("1979 < year & year < 1990")
     * @param condition  the check condition for tuples
     * @return  the table consisting of tuples satisfying the condition
     */
    public Table select (String condition)
    {
        out.println ("RA> " + name + ".select (" + condition + ")");

//        String [] postfix = { "year", "1980", "!=" };      // FIX: delete after impl
        String [] postfix = infix2postfix (condition);           // FIX: uncomment after impl
        Table     result  = new Table (name + count++, attribute, domain, key);

        Set<KeyType> testKey = this.index.keySet();
    	Iterator itTable1 = testKey.iterator();
    	KeyType tempKey = null;
    	Comparable[] tempTup = null;
    	while(itTable1.hasNext())
    	{
    		tempKey= (KeyType)itTable1.next();
    		tempTup = this.index.get(tempKey);
    		if (evalTup (postfix, tempTup)) 
    		{	
    			result.tuples.add(tempTup);
    			result.index.put(tempKey,tempTup);
    		}
    		
    	}

        return result;
    } // select

    /***************************************************************************
     * Union this table and table2.  Check that the two tables are compatible.
     * #usage movie.union (show)
     * @param table2  the rhs table in the union operation
     * @return  the table representing the union (this U table2)
     * @author Pan Huang
     */
    public Table union (Table table2)
    {
        out.println ("RA> " + name + ".union (" + table2.name + ")");

        Table result = new Table (name + count++, attribute, domain, key);
        
		Comparable[] tempTup = null;
		KeyType tempKey = null;
		Comparable[] keyVal = new Comparable[key.length];

		Set<KeyType> testKey = this.index.keySet();
		Iterator itTable1 = testKey.iterator();
		while (itTable1.hasNext()) {
			tempKey = (KeyType) itTable1.next();
			tempTup = this.index.get(tempKey);
			result.tuples.add(tempTup);
			result.index.put(tempKey, tempTup);

		}// while

		if (compatible(table2)) { 
			Set<KeyType> testKey2 = table2.index.keySet();
			Iterator itTable2 = testKey2.iterator();

			while (itTable2.hasNext()) {
				tempKey = (KeyType) itTable2.next();
				if (!result.index.containsKey(tempKey)) {
					tempTup = table2.index.get(tempKey);
					result.tuples.add(tempTup);
					result.index.put(tempKey, tempTup);
				}

			}// while

		}// if
        
        

        return result;
        
    } // union

    /***************************************************************************
     * Take the difference of this table and table2.  Check that the two tables
     * are compatible.
     * #usage movie.minus (show)
     * @param table2  the rhs table in the minus operation
     * @return  the table representing the difference (this - table2)
     * @author Ruichen Dai
     */
    public Table minus (Table table2)
    {
        out.println ("RA> " + name + ".minus (" + table2.name + ")");

        Table result = new Table (name + count++, attribute, domain, key);
        
        Comparable [] tempTup = null;
        KeyType tempKey = null;
        

        	Set<KeyType> testKey = this.index.keySet();
        	Iterator itTable1 = testKey.iterator();
        	
        	while(itTable1.hasNext())
        	{
        		tempKey= (KeyType)itTable1.next();
        		tempTup = this.index.get(tempKey);
        		if(!table2.index.containsValue(tempTup))
        		{
        		 result.tuples.add(tempTup);
        		 result.index.put(tempKey,tempTup);
        		}
        		
        	}//while
        	//project#2 end

        
        return result;
    } // minus

    /***************************************************************************
     * Join this table and table2.  If an attribute name appears in both tables,
     * assume it is from the first table unless it is qualified with the first
     * letter of the second table's name (e.g., "s.").
     * In the result, disambiguate the attribute names in a similar way
     * (e.g., prefix the second occurrence with "s_").
     * Caveat: the key parameter assumes joining the table with the foreign key
     * (this) to the table containing the primary key (table2).
     * #usage movie.join ("studioNo == name", studio);
     * #usage movieStar.join ("name == s.name", starsIn);
     * @param condition  the join condition for tuples
     * @param table2     the rhs table in the join operation
     * @return  the table representing the join (this |><| table2)
     */
    public Table join (String condition, Table table2)  
    {
    	out.println ("RA> " + name + ".join (" + condition + ", " + table2.name + ")");
    	
        int flength = this.attribute.length;
        int slength = table2.attribute.length;
        int rlength = flength+slength;
        
        String [] rAttribute = new String [rlength];//r means result
        Class []  rDomain  = new Class [rlength];
        
		
		for (int i = 0; i < flength; i++) {
			for (int j = 0; j < slength; j++) {
				if (this.attribute[i].equals(table2.attribute[j])) { 
					for (int k2 = 0; k2 < table2.key.length; k2++) {
						if (table2.key[k2].equals(table2.attribute[j])) {
							table2.key[k2] = table2.name.charAt(0) + "_" + table2.key[k2];  
						}
					}
					table2.attribute[j] = table2.name.charAt(0) + "_" + table2.attribute[j];
				}
			}
		}
          
        System.arraycopy(attribute, 0, rAttribute, 0, flength);   
        System.arraycopy(table2.attribute,0,rAttribute,flength,slength);
        
        System.arraycopy(domain, 0, rDomain, 0, flength);
        System.arraycopy(table2.domain,0,rDomain,flength,slength);
        
        String [] postfix = infix2postfix (condition);
        
        for (int i = 0; i < postfix.length; i++) {
		postfix[i] = postfix[i].replace('.', '_');
		}
        
        String foreignKey = postfix[0];  
        String primaryKey = postfix[1];
                
        String [] newKey = null;
        
       
        if (newKey == null)
        {
	        for(int i= 0; i< table2.key.length;i++)
	        {
	        	if(table2.key[i].equals(primaryKey))
	        	{
	        		newKey = new String [key.length];
	        		System.arraycopy(key, 0, newKey, 0, key.length);
	        		break;
	        	}
	        	
	        }
        }
        if (newKey == null)
        {
	        for(int i= 0; i<key.length;i++)
	        {
	        	if(key[i].equals(foreignKey))
	        	{
	        		newKey = new String [table2.key.length];
	        		System.arraycopy(table2.key, 0, newKey, 0, table2.key.length);
	        		break;
	        	}
	        	
	        }
        }
        if (newKey == null)
        {
	        newKey = new String [table2.key.length+key.length];
	        System.arraycopy(key, 0, newKey, 0, key.length);
	        System.arraycopy(table2.key, 0, newKey,key.length, table2.key.length);

        }
        
        Table result = new Table (name + count++, rAttribute, rDomain, newKey);
        //project#2 begin
        
        Comparable [] tempTupT = null;
        Comparable [] tempTupU = null;
        KeyType tempKeyT =null;
        KeyType tempKeyU =null;
        Set<KeyType> testKey = this.index.keySet();
    	Iterator itTable1 = testKey.iterator();
    	
    	Set<KeyType> testKey2 = table2.index.keySet();
    	Iterator itTable2=testKey2.iterator();
    	
    	while(itTable1.hasNext())//
    	{
    		tempKeyT= (KeyType)itTable1.next();
    		tempTupT = this.index.get(tempKeyT);
    		
    		while(itTable2.hasNext())
        	{
    			tempKeyU = (KeyType)itTable2.next();
    			tempTupU = table2.index.get(tempKeyU);
    			Comparable [] tempTup = new Comparable [rlength];
    			 System.arraycopy(tempTupT, 0, tempTup, 0, flength);
    		     System.arraycopy(tempTupU,0,tempTup,flength,slength);
        		if(result.evalTup(postfix,tempTup))
        		{	
        			result.tuples.add(tempTup);
        			result.index.put(new KeyType(newKey),tempTup);
        		}
        		 
        	}//while
    		itTable2=testKey2.iterator(); 
    	}//while
        //project#2 end
             //-----------------\\ 
            // TO BE IMPLEMENTED \\
           //---------------------\\ 
        return result;
    } // join

    /***************************************************************************
     * Insert a tuple to the table.
     * #usage movie.insert ("'Star_Wars'", 1977, 124, "T", "Fox", 12345)
     * @param tup  the array of attribute values forming the tuple
     * @return  whether insertion was successful
     */
    public boolean insert (Comparable [] tup)
    {
        out.println ("DML> insert into " + name + " values ( " + Arrays.toString (tup) + " )");

        if (typeCheck (tup, domain)) {
            tuples.add (tup);
            Comparable [] keyVal = new Comparable [key.length];
            int []        cols   = match (key);
            for (int j = 0; j < keyVal.length; j++) keyVal [j] = tup [cols [j]];
            index.put (new KeyType (keyVal), tup);
            return true;
        } else {
            return false;
        } // if
    } // insert

    /***************************************************************************
     * Get the name of the table.
     * @return  the table's name
     */
    public String getName ()
    {
        return name;
    } // getName

    /***************************************************************************
     * Print the table.
     */
    public void print ()
    {
        out.println ("\n Table " + name);

        out.print ("|-");
        for (int i = 0; i < attribute.length; i++) out.print ("---------------");
        out.println ("-|");
        out.print ("| ");
        for (String a : attribute) out.printf ("%15s", a);
        out.println (" |");

        if (DEBUG) {
            out.print ("|-");
            for (int i = 0; i < domain.length; i++) out.print ("---------------");
            out.println ("-|");
            out.print ("| ");
            for (Class d : domain) out.printf ("%15s", d.getSimpleName ());
            out.println (" |");
        } // if

        out.print ("|-");
        for (int i = 0; i < attribute.length; i++) out.print ("---------------");
        out.println ("-|");
        for (Comparable [] tup : tuples) {
            out.print ("| ");
            for (Comparable attr : tup) out.printf ("%15s", attr);
            out.println (" |");
        } // for
        out.print ("|-");
        for (int i = 0; i < attribute.length; i++) out.print ("---------------");
        out.println ("-|");
    } // print

    /***************************************************************************
     * Determine whether the two tables (this and table2) are compatible, i.e.,
     * have the same number of attributes each with the same corresponding domain.
     * @param table2  the rhs table
     * @return  whether the two tables are compatible
     * @author Zhaochong Liu
     */
    public boolean compatible (Table table2)
    {
    	if ( attribute.length == table2.attribute.length )
    	{
    		for(int i=0; i<attribute.length; i++)
    		{
    			if(!attribute[i].equals(table2.attribute[i]))
    				return false;
    		}
    		return true;
    	}
        return false;
    } // compatible

    /***************************************************************************
     * Return the column position for the given column/attribute name.
     * @param column  the given column/attribute name
     * @return  the column index position
     */
    private int columnPos (String column)
    {
        for (int j = 0; j < attribute.length; j++) {
           if (column.equals (attribute [j])) return j;
        } // for

        //out.println ("columnPos: error - " + column + " not found");
        return -1;  // column name not found in this table
    } // columnPos

    /***************************************************************************
     * Return all the column positions for the given column/attribute names.
     * @param columns  the array of column/attribute names
     * @return  the array of column index positions
     */
    private int [] match (String [] columns)
    {
        int [] colPos = new int [columns.length];

        for (int i = 0; i < columns.length; i++) {
            colPos [i] = columnPos (columns [i]);
        } // for

        return colPos;
    } // match

    /***************************************************************************
     * Check whether the tuple satisfies the condition.  Use a stack-based postfix
     * expression evaluation algorithm.
     * @param postfix  the postfix expression for the condition
     * @param tup      the tuple to check
     * @return  whether to keep the tuple
     * @author Sidi Liu
     */
    @SuppressWarnings("unchecked")
    private boolean evalTup (String [] postfix, Comparable [] tup)
    {
    	if (postfix == null) return true;
        Stack <Comparable <?>> s = new Stack <> ();
        int iopt = 0;
        String strOpt1 =null;
        String strOpt2 = null;
        int iattribute = -1;
        Comparable Operand1 = null;
        Comparable Operand2 = null;
        Comparable tempOperand1 = null;
        Comparable tempOperand2 = null;
        for (String token : postfix) 
        {
  	
        	if(isComparison(token))
            {
        		strOpt1= token;
        	}
        	else if(token.equals("&")||token.equals("|"))
        	{
        		strOpt2 = token;
        	}
        	else
        	{
        		s.push(token);
        		
        	}
        	
        	
        	if(strOpt1!=null)
        	{
        		tempOperand2=s.pop();
        		tempOperand1=s.pop();
        		iattribute = columnPos((String)tempOperand2);
        		
        		if((columnPos((String)tempOperand2)!=-1)&&(columnPos((String)tempOperand1)!=-1))  
        		{
        			Operand1 = tup[columnPos((String)tempOperand1)];
        			Operand2 = tup[columnPos((String)tempOperand2)];
        		}
        		else if(iattribute != -1) 
        		{
        			Operand2=tup[iattribute];
        			
        			
        			if (!(Operand2 instanceof Character) ) {  
        				Operand1=String2Type.cons(domain[iattribute], (String)tempOperand1);						
					}
        			
        			else {
        				Operand1=tempOperand1.toString();
        				Operand2=Operand2.toString();
					}	
        		}
        		else 
        		{
        			int temp = columnPos((String)tempOperand1);
        			Operand1=tup[temp];
        				
        			if (!(Operand1 instanceof Character)) {
        				Operand2=String2Type.cons(domain[temp], (String)tempOperand2);
        				
					}
        			else {
        				Operand2= tempOperand2.toString();
        				Operand1= Operand1.toString();
					}
        		}
        		
        		
        		if(compare(Operand1,strOpt1,Operand2))
        		{
        			s.push(true);
        		}
        		else
        		{
        			s.push(false);
        			
        		}
        		strOpt1=null;
        	}
        	
        	if(strOpt2!=null)
            {
            	Boolean result = false;
            	Boolean  bOperand1 = (Boolean)s.pop();
            	Boolean  bOperand2 = (Boolean)s.pop();
            	if(strOpt2.equals("&"))
            	{
            		result=(bOperand1) && (bOperand2);
            	}
            	else
            	{
            		result=(bOperand1) || (bOperand2);
            		
            	}
            	s.push(result);
            	strOpt2 = null;  
            }
        	//-----------------\\ 
            // TO BE IMPLEMENTED \\
           //---------------------\\ 

        } // for
		
        
        
      return (Boolean) s.pop ();         // FIX: uncomment after loop impl
   //     return true;                       // FIX: delete atfer loop impl
    }

    /***************************************************************************
     * Pack tuple tup into a record/byte-buffer (array of bytes).
     * @param tup  the array of attribute values forming the tuple
     * @return  a tuple packed into a record/byte-buffer
     */ 
    byte [] pack (Comparable [] tup)
    {
        byte [] record = new byte [tupleSize ()];
        byte [] b      = null;
        int     s      = 0;
        int     i      = 0;

        for (int j = 0; j < domain.length; j++) {
            switch (domain [j].getName ()) {
            case "java.lang.Integer":
                b = Conversions.int2ByteArray ((Integer) tup [j]);
                s = 4;
                break;
            case "java.lang.String":
                b = ((String) tup [j]).getBytes ();  
                s = 64;
                break;

             //-----------------\\ 
            // TO BE IMPLEMENTED \\
           //---------------------\\ 
                
            case "java.lang.Short": 
            	b = Conversions.short2ByteArray ((Short) tup [j]);
            	s = 4;   
            	break;
            case "java.lang.Long": 
            	b = Conversions.long2ByteArray ((Long) tup [j]);
            	s = 8; 
            	break;
            case "java.lang.Float": 
            	b = Conversions.float2ByteArray ((Float)tup[j]);
            	s = 4; 
            	break;
            case "java.lang.Double": 
            	b = Conversions.double2ByteArray ((Double) tup [j]);
            	s = 8; 
            	break;
            case "java.lang.Character": 
            	b=((Character) tup [j]).toString().getBytes();  
            	s = 2;  
            	break;     

            } // switch
            if (b == null) {
                out.println ("Table.pack: byte array b is null");
                return null;
            } // if
            for (int k = 0; k < s; k++){
				if (k < b.length)
					record[i++] = b[k]; 
				else  
					record[i++] = 0;
            }
        } // for
        return record;
    } // pack
  

    /***************************************************************************
     * Unpack the record/byte-buffer (array of bytes) to reconstruct a tuple.
     * @param record  the byte-buffer in which the tuple is packed
     * @return  an unpacked tuple
     */ 
    Comparable [] unpack (byte [] record)
    {
    	Comparable [] tup = new Comparable[this.attribute.length];
    	Comparable tempElement = null;
    	int s = 0;//the position of the record
    	
    	for (int j = 0; j < domain.length; j++) 
    	{
    		
            switch (domain [j].getName ()) {
            case "java.lang.Integer":
            case "java.lang.Short": 
            	byte [] tempByte1 = new byte[4]; 
            	for(int i= 0; i<4;i++)
            	{
            		tempByte1[i] = record[s+i];
            	}
                ByteBuffer buffer1 = ByteBuffer.wrap(tempByte1); 
                s=s+4;
                tempElement = buffer1.getInt();  
                break;
            case "java.lang.String":
            	int stringLength = 0;
            	for(int i =0;i<64;i++)
            	{
            		if(record[s+i]!=0)
            			stringLength++;
            		else
            			break;
            	}
            	byte [] tempByte2 = new byte[stringLength];
            	for(int i= 0; i<stringLength;i++)
            	{
            		tempByte2[i] = record[s+i];
             	}
                
                s = s+64;
                String tempStr = new String(tempByte2);
                tempElement = tempStr;
                break;
                
            case "java.lang.Long": 
            	byte [] tempByte7 = new byte[8];
               	for(int i= 0; i<8;i++)
               	{
               		tempByte7[i] = record[s+i];
               	}
                   
                   s = s+8;
                   ByteBuffer buffer7 = ByteBuffer.wrap(tempByte7);
                
                   tempElement = buffer7.getLong();

            	break;
            case "java.lang.Float": 
            	byte [] tempByte4 = new byte[4];
               	for(int i= 0; i<4;i++)
               	{
               		tempByte4[i] = record[s+i];
               	}
                   
                   s = s+4;
                   ByteBuffer buffer4 = ByteBuffer.wrap(tempByte4);
                
                   tempElement = buffer4.getFloat();
                break; 
            	
            case "java.lang.Double": 
            	byte [] tempByte5 = new byte[8];
               	for(int i= 0; i<8;i++)
               	{
               		tempByte5[i] = record[s+i];
               	}
                   
                   s = s+8;
                   ByteBuffer buffer5 = ByteBuffer.wrap(tempByte5);
                
                   tempElement = buffer5.getDouble();
                break; 
           case "java.lang.Character": 
        	   byte [] tempByte3 = new byte[2];
           		for(int i= 0; i<2;i++)
           		{
           			tempByte3[i] = record[s+i];
           		}
               
               s = s+2;
               tempElement = new String(tempByte3);
            	break;  
                
                
            } // switch
                        
            tup[j]=tempElement;
        }//for
                
             //-----------------\\ 
            // TO BE IMPLEMENTED \\
           //---------------------\\ 

        return tup;
    } // unpack
 

    /***************************************************************************
     * Determine the size of tuples in this table in terms of the number of bytes
     * required to store it in a record/byte-buffer.
     * @return  the size of packed-tuples in bytes
     */ 
    private int tupleSize () 
    {
        int s = 0;

        for (int j = 0; j < domain.length; j++) {
            switch (domain [j].getName ()) {
			case "java.lang.Integer":
				s += 4;
				break;
			case "java.lang.String":
				s += 64;
				break;
			case "java.lang.Short": 
				s += 4;
				break;
			case "java.lang.Long":
				s += 8;
				break;
			case "java.lang.Float":
				s += 4;
				break;
			case "java.lang.Double":
				s += 8;
				break;
			case "java.lang.Character":
				s += 2;
				break;

              //-----------------\\ 
             // TO BE IMPLEMENTED \\
            //---------------------\\ 

            } //switch
        } // for

        return s;
    } // tupleSize


    //------------------------ Static Utility Methods --------------------------

    /***************************************************************************
     * Check the size of the tuple (number of elements in list) as well as the
     * type of each value to ensure it is from the right domain. 
     * @param tup  the tuple as a list of attribute values
     * @param dom  the domains (attribute types)
     * @return  whether the tuple has the right size and values that comply
     *          with the given domains
     * @author Ruichen Dai
     */
    
    private boolean typeCheck (Comparable [] tup, Class [] dom)
    { 
    	if(tup.length != dom.length) return false;
    	for(int i=0; i<tup.length; i++){
    		if(!(tup[i].getClass().toString().equals(dom[i].toString()))){
    			out.println(tup[i].getClass().toString());
				return false;

    		}
    	}
        return true;
    } // typeCheck

    /***************************************************************************
     * Determine if the token/op is a comparison operator.
     * @param op  the token/op to check
     * @return  whether it a comparison operator
     */
    private static boolean isComparison (String op)
    {
        return op.equals ("==") || op.equals ("!=") ||
               op.equals ("<")  || op.equals ("<=") ||
               op.equals (">")  || op.equals (">=");
    } // isComparison

    /***************************************************************************
     * Compare values x and y according to the comparison operator.
     * @param   x   the first operand
     * @param   op  the comparison operator
     * @param   y   the second operand
     * @return  whether the comparison evaluates to true or false
     */
    @SuppressWarnings("unchecked")
    private static boolean compare (Comparable x, String op , Comparable y)
    {   	

        switch (op) {
        case "==": return x.compareTo (y) == 0;
        case "!=": return x.compareTo (y) != 0;
        case "<":  return x.compareTo (y) <  0;
        case "<=": return x.compareTo (y) <= 0;
        case ">":  return x.compareTo (y) >  0;
        case ">=": return x.compareTo (y) >= 0;
        default: { out.println ("compare: error - unexpected op"); return false; }
        } // switch
    } // compare

    /***************************************************************************
     * Convert an untokenized infix expression to a tokenized postfix expression.
     * This implementation does not handle parentheses ( ).
     * Ex: "1979 < year & year < 1990" --> { "1979", "year", "<", "year", "1990", "<", "&" } 
     * @param condition  the untokenized infix condition
     * @return  resultant tokenized postfix expression
     * @author Michael Church
     */
    
    private static String [] infix2postfix (String condition)
    {
        if (condition == null || condition.trim () == "") return null;
        String [] infix   = condition.split (" ");        // tokenize the infix
        
        
        for (int i = 0; i < infix.length; i++) {
			infix[i]=infix[i].replace("'", "");
		}
        
        String [] postfix = new String [infix.length];    // same size, since no ( ) 
        for(int i=0 ; i<infix.length; i++){
        	if( isComparison(infix[i]) ) {
        		postfix[i+1] = infix[i];
        		postfix[i] = infix[++i];
        	}
        	else if(infix[i].compareTo("&") == 0 || infix[i].compareTo("|") == 0){
        		postfix[i] = infix[++i];
        		postfix[i] = infix[i+2];
        		postfix[++i] = infix[i];
        		postfix[++i] = infix[i-3];
        	}
        	else
        		postfix [i] = infix [i];
        }
        return postfix;
    } // infix2postfix

    /***************************************************************************
     * Find the classes in the "java.lang" package with given names.
     * @param className  the array of class name (e.g., {"Integer", "String"})
     * @return  the array of Java classes for the corresponding names
     */
    private static Class [] findClass (String [] className)
    {
        Class [] classArray = new Class [className.length];

        for (int i = 0; i < className.length; i++) {
            try {
                classArray [i] = Class.forName ("java.lang." + className [i]);
            } catch (ClassNotFoundException ex) {
                out.println ("findClass: " + ex);
            } // try
        } // for

        return classArray;
    } // findClass

    /***************************************************************************
     * Extract the corresponding domains from the group.
     * @param group   where to extract from
     * @param colPos  the column positions to extract
     * @return  the extracted domains
     */
    private static Class [] extractDom (Class [] group, int [] colPos)
    {
        Class [] dom = new Class [colPos.length];

        for (int j = 0; j < colPos.length; j++) {
            dom [j] = group [colPos [j]];
        } // for

        return dom;
    } // extractDom

    /***************************************************************************
     * Extract the corresponding attribute values from the group.
     * @param group   where to extract from
     * @param colPos  the column positions to extract
     * @return  the extracted attribute values
     * @author Zhaochong Liu
     */
    private static Comparable [] extractTup (Comparable [] group, int [] colPos)
    {
        Comparable [] tup = new Comparable [colPos.length];

        for (int j = 0; j < colPos.length; j++) {
            tup [j] = group [colPos [j]];
        } // for
             
        return tup;
    } // extractTup

} // Table class
