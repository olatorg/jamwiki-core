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
import org.jamwiki.utils.Utilities;
import org.jamwiki.utils.WikiLogger;
import org.springframework.web.util.HtmlUtils;

/**
 *
 */
public class CharacterTag implements ParserTag {

	private static WikiLogger logger = WikiLogger.getLogger(CharacterTag.class.getName());

	/**
	 *
	 */
	public String parse(ParserInput parserInput, ParserDocument parserDocument, int mode, String raw) throws Exception {
		if (mode < JFlexParser.MODE_PROCESS) {
			return raw;
		}
		if (mode > JFlexParser.MODE_PROCESS) {
			return raw;
		}
		if (isEntity(raw)) {
			return raw;
		}
		return Utilities.escapeHTML(raw);
	}

	/**
	 *
	 */
	private boolean isEntity(String raw) {
		String unescaped = HtmlUtils.htmlUnescape(raw);
		// see if it was successfully converted, in which case it is an entity
		return (!raw.equals(unescaped));
	}
}
