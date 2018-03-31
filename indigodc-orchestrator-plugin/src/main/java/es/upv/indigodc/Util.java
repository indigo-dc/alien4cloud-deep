package es.upv.indigodc;

import java.io.PrintWriter;
import java.io.StringWriter;

public class Util {
	
	public static String throwableToString(Throwable e) {
    	StringWriter sw = new StringWriter();
    	e.printStackTrace(new PrintWriter(sw));
    	return sw.toString();
	}

}
