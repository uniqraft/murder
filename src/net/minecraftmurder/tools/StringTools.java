package net.minecraftmurder.tools;

public final class StringTools {
	/**
	 * Replaces all the occurances of specified replace string with specified
	 * replacement text in specified text.
	 * 
	 * @param text
	 *            to replace text in.
	 * @param replace
	 *            , what to find and replace
	 * @param replacement
	 *            , what to replace with
	 * @return text with all occurances of <code>replace</code> replaced with
	 *         <code>replacement</code>. If nothing was replaced the
	 *         <code>text</code> parsed in will be returned.
	 */
	public static String replaceAll(String text, String replace, String replacement) {
		if (replace == replacement)
			return text;
		
		String currentText = null;
		String newText = text;
		do {
			currentText = newText;
			newText = replaceFirst(newText, replace, replacement);
		} while (currentText != newText);
		return newText;
	}
	
	/**
	 * Replaces the first occurance of specified replace string with specified
	 * replacement text in specified text.
	 * 
	 * @param text
	 *            to replace text in.
	 * @param replace
	 *            , what to find and replace
	 * @param replacement
	 *            , what to replace with
	 * @return text with the first occurance of <code>replace</code> replaced
	 *         with <code>replacement</code>. If nothing was replaced the
	 *         <code>text</code> parsed in will be returned.
	 */
	public static String replaceFirst(String text, String replace, String replacement) {
		int found = 0;
		int foundAt = -1;
		
		// Loop through each char
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			// Does char match replace char?
			// Ignore case
			if (Character.toLowerCase(replace.charAt(found)) == Character.toLowerCase(c)) {
			//if (replace.charAt(found) == c) {
				// We found another match
				found++;
				
				// Have we found the entire replace text?
				if (found == replace.length()) {
					// Mark location we found match first at
					foundAt = i - found + 1;
					break;
				}
			} else {
				found = 0;
			}
		}
		
		
		// Did we get a match?
		if (foundAt != -1) {
			// Replace text
			return text.substring(0, foundAt) + replacement	+ text.substring(foundAt + replace.length(), text.length());
		}
		return text;
	}
}
