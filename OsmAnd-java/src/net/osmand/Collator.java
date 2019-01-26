package net.osmand;

/**
 * Wrapper of java.text. Collator  
 */
public interface Collator extends java.util.Comparator<Object>, Cloneable {
		
	boolean equals(String source, String target);
	
	int compare(String source, String target);
}