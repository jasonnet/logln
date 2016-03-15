package org.jasonnet.logln;

import java.io.PrintStream;
import java.util.Date;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;

/**
 * A set of helper functions for logging progress of a 
 * program.  These functions make a point of logging the line 
 * number of the calling code. 
 *  
 * The LOGLN environment variable should be set to the file to 
 * receive the logging information.  If it's unset, the logging 
 * info will be routed to System.out which usually is stdout. 
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
	 * Determines if the logging code should determine the calling
	 * stack and log it if it's different than the the calling stack 
	 * of the previous log message.  By setting the LOGLN_LOGSTACK 
	 * environtment variable to 1, one can turn this on, otherwise 
	 * it is off.  This can create verbose listings, but it can also 
	 * be helpful determining the call stack.  Note: the logged 
	 * information is not necessarily a complete call trace.  It 
	 * only represents the results of a sampling.   For example it  
	 * doesn't reveal when a logged method was exited and reentered 
	 * unless logln is also called after the exit and before the 
	 * reentry. The more logln calls that are made in this mode, the
	 * clearer the picture of the code path. For example, in this 
	 * mode, one can reduce ambiguity by calling logln() at the 
	 * beginning of each loop iteration. 
	 * 
	 * @author jasonnet (2/17/2016)
	 */
	public static boolean boolInferAndLogStack = false;
	static {
		String inferstack = System.getenv().get("LOGLN_LOGSTACK");
		if (inferstack==null) {
			boolInferAndLogStack = false;
		} else {
			boolInferAndLogStack = inferstack.equals("1");
		}
	}
	
	private static int previous_depth_adjustment = 0;
	private static ThreadLocal previousstack = new ThreadLocal() {
			protected synchronized Object initialValue() {
				return null;
			};
		};
	/**
	 * This is the PrintStream that will receive logging.  It's made
	 * public here so that other code can also contribute to that 
	 * stream.  This can be suitable for calls to 
	 * throwable.printStackTrace(). 
	 * 
	 * @author jasonnet (2/11/2016)
	 */
	public final static PrintStream ps;
	static {
		String fn = System.getenv().get("LOGLN_FILE");
		if ((fn!=null) && (fn.length()>0)) {
			try {
				ps = new PrintStream( new java.io.FileOutputStream(fn));
			} catch (java.io.IOException exc) {
				throw new RuntimeException(exc);
			}
		} else {
			ps = System.out;
		}
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
	 * @param msg the diagnostic message to display
	 */
	public static void logln( String msg) {
		StackTraceElement els[] = Thread.currentThread().getStackTrace();
		//_logln(msg, els, 2);
		_logRecentCallersAndStackEntry(msg, els, /*depth_adjustment:*/0 );
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
	 * @author jasonnet (02/23/2015)
	 * 
	 * @param msg the diagnostic message to display
	 * @param depth_adjustment set to 1 if it's actually the caller 
	 *      		   that should be logged.  This can be
	 *      		   helpful if used in a logging routine
	 *      		   where it's not the logging routine
	 *      		   that should be called, but instead
	 *      		   the caller of the logging routine.
	 */
	public static void logln( String msg, int depth_adjustment) {
		StackTraceElement els[] = Thread.currentThread().getStackTrace();
		//_logln(msg, els, 2);
		_logRecentCallersAndStackEntry(msg, els, depth_adjustment);
	}

	protected static final String initial_datetemplate = "yyyy-MM-dd kk:mm:ss ";
	protected static SimpleDateFormat dateformat = new SimpleDateFormat(initial_datetemplate);

	/**
	 * Set the template for how to display the date in subsequent 
	 * log lines.  
	 * 
	 * @author ccjason (2/11/2015)
	 * 
	 * @param template This should be expressed in the same way that 
	 *      	   it is for the constructor of the {@link
	 *      	   SimpleDateFormat} class.
	 */
	public static void setDateTemplate(String template) {
		if (template==null) {
			dateformat = null;
		} else {
			dateformat = new SimpleDateFormat(template);
		}
	}

	static int calced_global_adjustment = -1;  // -1 means we still need to calculate the proper value

	private static void _logRecentCallersAndStackEntry(String msg, StackTraceElement els[], int depth_adjustment ) {
		if (calced_global_adjustment==-1) {
			// it appears to be JVM dependent at what level of this stack trace the calling method of getStackTrace() will be.  We'll figure out what that value is here.
			//ps.println("0 "+els[0].getMethodName()+"  "+els[0].getClassName());
			//ps.println("1 "+els[1].getMethodName());
			//ps.println("2 "+els[2].getMethodName());
			//ps.println("3 "+els[3].getMethodName());
			if (els[0].getClassName().equals("org.jasonnet.logln.Logln")) {
				calced_global_adjustment = 1;
			} else if (els[1].getClassName().equals("org.jasonnet.logln.Logln")) {
				calced_global_adjustment = 2;
			} else if (els[2].getClassName().equals("org.jasonnet.logln.Logln")) {
				calced_global_adjustment = 3;
			}
		}
		if (boolInferAndLogStack) {
			StackTraceElement elsOld[] = (StackTraceElement[]) previousstack.get();
			int idxOld = (elsOld==null) ? 0 : elsOld.length-1;
			int idxNew = els.length-1;
			boolean boolSoFarMatch = true;
			while (idxNew>(calced_global_adjustment+depth_adjustment)) {
				if ((elsOld==null) || (idxOld<(calced_global_adjustment+previous_depth_adjustment))) boolSoFarMatch = false;
				if (boolSoFarMatch) {
					if ((els[idxNew].getClassName().equals(elsOld[idxOld].getClassName())) 
					    && (els[idxNew].getLineNumber()==elsOld[idxOld].getLineNumber())
					    ) {
						// still match
					} else {
						boolSoFarMatch = false;
					}
				}
				if (!boolSoFarMatch) {
					_loglnStackEntry(":stk:", els, idxNew);
				}
				idxNew--; idxOld--;
			}
		}
		_loglnStackEntry(msg,els,calced_global_adjustment+depth_adjustment);
		previousstack.set(els);
		previous_depth_adjustment = depth_adjustment;
	}

	private static void _loglnStackEntry( String msg, StackTraceElement els[], int stkidx) {
		StringBuffer sb = new StringBuffer();
		String fn = null;
		if (null==fn) {
			// todo: provide code that looks in the class for SHORTFN static value.
			String clsnm = els[stkidx].getClassName();
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
		if (dateformat!=null) ps.print(dateformat.format(new Date()));
		ps.printf("%20.20s:%4d: ", fn, els[stkidx].getLineNumber() );
		for (int i=0; i<(els.length-stkidx+2); i++) sb.append(' ');
		sb.append(els[stkidx].getMethodName());
		sb.append(": ");
		sb.append(msg);
		ps.print(sb);
		ps.println();
		ps.flush();
	}


}