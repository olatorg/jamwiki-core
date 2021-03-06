/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, version 2.1, dated February 1999.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the latest version of the GNU Lesser General
 * Public License as published by the Free Software Foundation;
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program (LICENSE.txt); if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.jamwiki.parser.jflex;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jamwiki.Environment;
import org.jamwiki.parser.ParserInput;
import org.jamwiki.parser.ParserDocument;
import org.jamwiki.utils.WikiLogger;
import org.springframework.util.StringUtils;

/**
 * Utility methods used with the Mediawiki lexers.
 */
public class ParserUtil {

	private static WikiLogger logger = WikiLogger.getLogger(ParserUtil.class.getName());
	private static Pattern TAG_PATTERN = null;
	private static Pattern JAVASCRIPT_PATTERN1 = null;
	private static Pattern JAVASCRIPT_PATTERN2 = null;

	static {
		try {
			TAG_PATTERN = Pattern.compile("(<[ ]*[/]?[ ]*)([^\\ />]+)([ ]*(.*?))([/]?[ ]*>)");
			// catch script insertions of the form "onsubmit="
			JAVASCRIPT_PATTERN1 = Pattern.compile("( on[^=]{3,}=)+", Pattern.CASE_INSENSITIVE);
			// catch script insertions that use a javascript url
			JAVASCRIPT_PATTERN2 = Pattern.compile("(javascript[ ]*\\:)+", Pattern.CASE_INSENSITIVE);
		} catch (Exception e) {
			logger.severe("Unable to compile pattern", e);
		}
	}

	/**
	 * Provide a way to run the pre-processor against a fragment of text, such
	 * as an image caption.  This method should be used sparingly since it is
	 * not very efficient.
	 */
	protected static String parseFragment(ParserInput parserInput, String raw, int mode) throws Exception {
		if (!StringUtils.hasText(raw)) return raw;
		JFlexParser parser = new JFlexParser(parserInput);
		ParserDocument parserDocument = parser.parseFragment(raw, mode);
		return parserDocument.getContent();
	}

	/**
	 * Clean up HTML tags to make them XHTML compliant (lowercase, no
	 * unnecessary spaces).
	 */
	protected static String sanitizeHtmlTag(String tag) {
		tag = tag.trim();
		tag = StringUtils.deleteAny(tag, " ").toLowerCase();
		if (tag.endsWith("/>")) {
			// spaces were stripped, so make sure tag is of the form "<br />"
			tag = tag.substring(0, tag.length() - 2) + " />";
		}
		return tag;
	}

	/**
	 * Given a tag of the form "<tag>content</tag>", return all content between
	 * the tags.  Consider the following examples:
	 *
	 * "<tag>content</tag>" returns "content".
	 * "<tag />" returns and empty string.
	 * "<tag><sub>content</sub></tag>" returns "<sub>content</sub>".
	 *
	 * @param raw The raw tag content to be analyzed.
	 * @return The content for the tag being analyzed.
	 */
	protected static String tagContent(String raw) {
		int start = raw.indexOf('>') + 1;
		int end = raw.lastIndexOf('<');
		if (start == 0) {
			// no tags
			return raw;
		}
		if (end <= start) {
			return "";
		}
		return raw.substring(start, end);
	}

	/**
	 * Allowing Javascript action tags to be used as attributes (onmouseover, etc) is
	 * a bad thing, so clean up HTML tags to remove any such attributes.
	 */
	protected static String validateHtmlTag(String tag) {
		Matcher m = TAG_PATTERN.matcher(tag);
		if (!m.find()) {
			logger.severe("Failure while attempting to match html tag for pattern " + tag);
			return tag;
		}
		String tagOpen = m.group(1);
		String tagKeyword = m.group(2);
		String attributes = m.group(3);
		String tagClose = m.group(5);
		tag = "<";
		if (tagOpen.indexOf('/') != -1) {
			tag += "/";
		}
		tag += tagKeyword.toLowerCase().trim();
		if (StringUtils.hasText(attributes)) {
			attributes = ParserUtil.validateHtmlTagAttributes(attributes);
			tag += " " + attributes.trim();
		}
		if (tagClose.indexOf('/') != -1) {
			tagClose = " />";
		}
		tag += tagClose.trim();
		return tag;
	}

	/**
	 * Allowing Javascript action tags to be used as attributes (onmouseover, etc) is
	 * a bad thing, so clean up HTML tags to remove any such attributes.
	 */
	protected static String validateHtmlTagAttributes(String attributes) {
		if (!StringUtils.hasText(attributes)) return attributes;
		if (!Environment.getBooleanValue(Environment.PROP_PARSER_ALLOW_JAVASCRIPT)) {
			// FIXME - can these two patterns be combined into one?
			// pattern requires a space prior to the "onFoo", so make sure one exists
			Matcher m = JAVASCRIPT_PATTERN1.matcher(" " + attributes);
			if (m.find()) {
				logger.warning("Attempt to include Javascript in Wiki syntax " + attributes);
				return "";
			}
			m = JAVASCRIPT_PATTERN2.matcher(attributes);
			if (m.find()) {
				logger.warning("Attempt to include Javascript in Wiki syntax " + attributes);
				return "";
			}
		}
		return attributes;
	}
}
