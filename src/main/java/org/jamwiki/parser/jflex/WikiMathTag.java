/**
 * 
 */
package org.jamwiki.parser.jflex;

import org.jamwiki.parser.ParserDocument;
import org.jamwiki.parser.ParserInput;
import org.jamwiki.parser.ParserTag;
import org.jamwiki.utils.WikiLogger;

/**
 * OLAT - Online Learning and Training
 * http://www.olat.org
 * 
 * This software is protected by the OLAT software license.
 * Use is subject to license terms.
 * See LICENSE.TXT in this distribution for details.
 * 
 * Copyright (c) 2003 OLAT Zentrum, University of Zurich, Switzerland.
 * All rights reserved.
 * 
 * Description:
 * TODO: guido Class Description for WikiMathTag
 *
 * @author guido
 */
public class WikiMathTag implements ParserTag {
	
	private static WikiLogger logger = WikiLogger.getLogger(WikiMathTag.class.getName());

	/**
	 * @see ParserTag#parse(ParserInput, ParserDocument, int, String)
	 */
	public String parse(ParserInput parserInput, ParserDocument parserDocument, int mode, String raw) throws Exception {
		if (mode < JFlexParser.MODE_PROCESS) {
			return raw;
		}
		
		String content = ParserUtil.tagContent(raw);
		
		StringBuffer html = new StringBuffer();
		html.append("<div class=\"math\">");
		html.append(content);
		html.append("</div>");
		return html.toString();
	}

}
