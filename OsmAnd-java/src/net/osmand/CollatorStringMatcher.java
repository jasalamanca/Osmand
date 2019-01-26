package net.osmand;

import java.util.Locale;


/**
 * Abstract collator matcher that basically supports subclasses with some collator
 * matching.
 * 
 * @author pavol.zibrita
 */
public class CollatorStringMatcher implements StringMatcher {
	private final Collator collator;
	private final StringMatcherMode mode;
	private final String part;
	
	public enum StringMatcherMode {
		CHECK_ONLY_STARTS_WITH,
		CHECK_STARTS_FROM_SPACE,
		CHECK_STARTS_FROM_SPACE_NOT_BEGINNING,
		CHECK_EQUALS_FROM_SPACE,
		CHECK_CONTAINS
	}

	public CollatorStringMatcher(String part, StringMatcherMode mode) {
		this.collator = OsmAndCollator.primaryCollator();
		this.part = part.toLowerCase(Locale.getDefault());
		this.mode = mode;
	}

	public Collator getCollator() {
		return collator;
	}
	
	@Override
	public boolean matches(String name) {
		return cmatches(collator, name, part, mode);
	}
	
	public static boolean cmatches(Collator collator, String base, String part, StringMatcherMode mode){
		switch (mode) {
		case CHECK_CONTAINS:
			return ccontains(collator, base, part); 
		case CHECK_EQUALS_FROM_SPACE:
			return cstartsWith(collator, base, part, true, true, true);
		case CHECK_STARTS_FROM_SPACE:
			return cstartsWith(collator, base, part, true, true, false);
		case CHECK_STARTS_FROM_SPACE_NOT_BEGINNING:
			return cstartsWith(collator, base, part, false, true, false);
		case CHECK_ONLY_STARTS_WITH:
			return cstartsWith(collator, base, part, true, false, false);
		}
		return false;
	}

	/**
	 * Check if part contains in base
	 *
	 * @param collator Collator to use
	 * @param part String to search
	 * @param base String where to search
	 * @return true if part is contained in base
	 */
	private static boolean ccontains(Collator collator, String base, String part) {
		if (base.length() <= part.length())
			return collator.equals(base, part);
		
		for (int pos = 0; pos <= base.length() - part.length() + 1; pos++) {
			String temp = base.substring(pos, base.length());
			
			for (int length = temp.length(); length >= 0; length--) {
				String temp2 = temp.substring(0,  length);
				if (collator.equals(temp2, part)) 
					return true;
			}
		}
		
		return false;
	}

    /**
	 * Checks if string starts with another string.
	 * Special check try to find as well in the middle of name
	 * 
	 * @param collator
	 * @param searchIn
	 * @param theStart
	 * @return true if searchIn starts with token
	 */
	private static boolean cstartsWith(Collator collator, String searchInParam, String theStart,
                                       boolean checkBeginning, boolean checkSpaces, boolean equals) {
		String searchIn = searchInParam.toLowerCase(Locale.getDefault());
		int startLength = theStart.length();
		int searchInLength = searchIn.length();
		if (startLength == 0) {
			return true;
		}
		if (startLength > searchInLength) {
			return false;
		}
		// simulate starts with for collator
		if (checkBeginning) {
			boolean starts = collator.equals(searchIn.substring(0, startLength), theStart);
			if (starts) {
				if (equals) {
					if (startLength == searchInLength || isSpace(searchIn.charAt(startLength))) {
						return true;
					}
				} else {
					return true;
				}
			}
		}
		if (checkSpaces) {
			for (int i = 1; i <= searchInLength - startLength; i++) {
				if (isSpace(searchIn.charAt(i - 1)) && !isSpace(searchIn.charAt(i))) {
					if (collator.equals(searchIn.substring(i, i + startLength), theStart)) {
						if(equals) {
							if(i + startLength == searchInLength || isSpace(searchIn.charAt(i + startLength))) {
								return true;
							}
						} else {
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	
	private static boolean isSpace(char c){
		return !Character.isLetter(c) && !Character.isDigit(c);
	}
}
