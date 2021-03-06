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

import org.jamwiki.parser.ParserInput;
import org.jamwiki.parser.ParserDocument;
import org.jamwiki.parser.ParserTag;
import org.jamwiki.parser.TableOfContents;
import org.jamwiki.utils.LinkUtil;
import org.jamwiki.utils.Utilities;
import org.jamwiki.utils.WikiLogger;
import org.springframework.util.StringUtils;

/**
 *
 */
public class WikiHeadingTag implements ParserTag {

	private static WikiLogger logger = WikiLogger.getLogger(WikiHeadingTag.class.getName());

	/**
	 *
	 */
	private String buildSectionEditLink(ParserInput parserInput, int section) {
		if (!parserInput.getAllowSectionEdit()) return "";
		// FIXME - template inclusion causes section edits to break, so disable for now
		String inclusion = (String)parserInput.getTempParams().get(TemplateTag.TEMPLATE_INCLUSION);
		boolean disallowInclusion = (inclusion != null && inclusion.equals("true"));
		if (disallowInclusion) return "";
		String output = "<div style=\"font-size:90%;float:right;margin-left:5px;\">[";
		String url = "";
		try {
			url = LinkUtil.buildEditLinkUrl(parserInput, null, section);
		} catch (Exception e) {
			logger.severe("Failure while building link for topic " + parserInput.getVirtualWiki() + " / " + parserInput.getTopicName(), e);
		}
		output += "<a href=\"" + url + "\">";
		output += Utilities.formatMessage("common.sectionedit", parserInput.getLocale());
		output += "</a>]</div>";
		return output;
	}

	/**
	 * Parse a Mediawiki heading of the form "==heading==" and return the
	 * resulting HTML output.
	 */
	public String parse(ParserInput parserInput, ParserDocument parserDocument, int mode, String raw) throws Exception {
		int level = 0;
		if (raw.startsWith("=====") && raw.endsWith("=====")) {
			level = 5;
		} else if (raw.startsWith("====") && raw.endsWith("====")) {
			level = 4;
		} else if (raw.startsWith("===") && raw.endsWith("===")) {
			level = 3;
		} else if (raw.startsWith("==") && raw.endsWith("==")) {
			level = 2;
		} else if (raw.startsWith("=") && raw.endsWith("=")) {
			level = 1;
		} else {
			return raw;
		}
		String tagText = raw.substring(level, raw.length() - level).trim();
		String tocText = this.stripMarkup(tagText);
		String tagName = tocText;
		if (mode <= JFlexParser.MODE_SLICE) {
			parserDocument.setSectionName(Utilities.encodeForURL(tagName));
			return raw;
		}
		String output = this.updateToc(parserInput, tagName, tocText, level);
		int nextSection = parserInput.getTableOfContents().size();
		output += this.buildSectionEditLink(parserInput, nextSection);
		output += "<h" + level + " id=\"" + Utilities.encodeForURL(tagName) + "\">";
		output += ParserUtil.parseFragment(parserInput, tagText, mode);
		output += "</h" + level + ">";
		return output;
	}

	/**
	 * Strip Wiki markup from text
	 */
	private String stripMarkup(String text) {
		// FIXME - this could be a bit more thorough and also strip HTML
		text = StringUtils.delete(text, "'''");
		text = StringUtils.delete(text, "''");
		text = StringUtils.delete(text, "[[");
		text = StringUtils.delete(text, "]]");
		return text;
	}

	/**
	 *
	 */
	private String updateToc(ParserInput parserInput, String name, String text, int level) {
		String output = "";
		if (parserInput.getTableOfContents().getStatus() == TableOfContents.STATUS_TOC_UNINITIALIZED) {
			output += "__TOC__";
		}
		parserInput.getTableOfContents().addEntry(name, text, level);
		return output;
	}
}
