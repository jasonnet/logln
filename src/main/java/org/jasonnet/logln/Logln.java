package org.jasonnet.logln;

import java.lang.reflect.Field;

/**
 * A set of helper functions for logging progress of a 
 * program.  These functions make a point of logging the line 
 * number of the calling code. 
 * 
 * @author jasonnet (1/26/2015)
 */
public class Logln {
	
	/**
	 * hiding default constructor by declaring it private.
	 * 
	 * @author jasonnet (1/269/2015)
	 */
	private Logln() {
	}

	/**
	 * A simple static logging method that prefixes the log line 
	 * with the file name and linenumber of the caller.  It also 
	 * indents the message by the depth of caller on the stack. 
	 *  
	 * One can facilitate calling this routine by including a 
	 * <pre> 
	 * import static org.jasonnet.logln.Logln.logln; 
	 * </pre>
	 * at the top of the source java file.
	 * 
	 * @author jasonnet (01/26/2015)
	 * 
	 * @param msg
	 */
	public static void logln( String msg) {
		StackTraceElement els[] = Thread.currentThread().getStackTrace();
		_logln(msg, els, null);

	}

	/**
	 * This is similar tot he logln(msg) method except that the 
	 * caller can provide a shortened version of the filename to 
	 * display.
	 *  
	 * One can facilitate calling this routine by including a 
	 * <pre> 
	 * import static org.jasonnet.logln.Logln.logln; 
	 * </pre>
	 * at the top of the source java file.
	 * 
	 * @author jasonnet (01/26/2015)
	 * 
	 * @param msg
	 */
	public static void logln( String msg, String SHORTFN) {
		StackTraceElement els[] = Thread.currentThread().getStackTrace();
		_logln(msg, els, SHORTFN );

	}

	private static void _logln( String msg, StackTraceElement els[], String fn) {
		StringBuffer sb = new StringBuffer();
		if (null==fn) {
			// todo: provide code that looks in the class for SHORTFN static value.
			String clsnm = els[2].getClassName();
			try {
				Class cls = Class.forName(clsnm);
				if (cls!=null) {
					Field fld = cls.getDeclaredField("SHORTFN");
					if (fld!=null) {
						Object oo = fld.get(null);
						if (oo instanceof String) {
							fn = (String)oo;
						}
					}
				}
			} catch (Exception exc) {}
			if (fn==null) {
				int idx = clsnm.lastIndexOf('.');
				if (idx>0) {
					fn = clsnm.substring(idx+1);
				} else {
					fn = clsnm; // we shouldn't reach this line
				}
			}
		}
		System.out.printf("%10s:%4d: ", fn, els[2].getLineNumber() );
		for (int i=0; i<els.length; i++) sb.append(' ');
		sb.append(els[2].getMethodName());
		sb.append(": ");
		sb.append(msg);
		System.out.print(sb);
		System.out.println();
	}

}