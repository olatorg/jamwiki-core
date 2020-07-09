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
package org.jamwiki.utils;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.jamwiki.WikiMediaDimension;
import org.jamwiki.model.Topic;
import org.jamwiki.model.WikiFile;
import org.jamwiki.model.WikiImage;
import org.jamwiki.parser.ParserInput;
import org.springframework.util.StringUtils;

/**
 * General utility methods for handling both wiki topic links such as
 * "Topic#Section?query=param", as well as HTML links of the form
 * http://example.com/.
 */
public class LinkUtil {

	private static final WikiLogger logger = WikiLogger.getLogger(LinkUtil.class.getName());
	private static Pattern TOPIC_LINK_PATTERN = null;
	
	private static int counter = 0;

	static {
		try {
			TOPIC_LINK_PATTERN = Pattern.compile("(((([^\\n\\r\\?\\#\\:\\\"]+)\\:)?([^\\n\\r\\?\\#\\\"]+))?(#([^\\n\\r\\?]+))?)+(\\?([^\\n\\r]+))?");
		} catch (Exception e) {
			logger.severe("Unable to compile pattern", e);
		}
	}

	/**
	 * Build a query parameter.  If root is empty, this method returns
	 * "?param=value".  If root is not empty this method returns root +
	 * "&amp;param=value".  Note that param and value will be  URL encoded,
	 * and if "query" does not start with a "?" then one will be pre-pended.
	 *
	 * @param query The existing query parameter, if one is available.  If the
	 *  query parameter does not start with "?" then one will be pre-pended.
	 * @param param The name of the query parameter being appended.  This
	 *  value will be URL encoded.
	 * @param value The value of the query parameter being appended.  This
	 *  value will be URL encoded.
	 * @return The full query string generated using the input parameters.
	 */
	private static String appendQueryParam(String query, String param, String value) {
		String url = "";
		if (StringUtils.hasText(query)) {
			if (!query.startsWith(":")) {
				query = ":" + query;
			}
			url = query + "&amp;";
		} else {
			url = ":";
		}
		if (!StringUtils.hasText(param)) return query;
		url += Utilities.encodeForURL(param) + "=";
		if (StringUtils.hasText(value)) {
			//url += Utilities.encodeForURL(value);
			url += value;
		}
		return url;
	}

	/**
	 * Utility method for building a URL link to a wiki edit page for a
	 * specified topic.
	 *
	 * @param context The servlet context for the link that is being created.
	 * @param virtualWiki The virtual wiki for the link that is being created.
	 * @param topic The name of the topic for which an edit link is being
	 *  created.
	 * @param query Any existing query parameters to append to the edit link.
	 *  This value may be either <code>null</code> or empty.
	 * @param section The section defined by the name parameter within the
	 *  HTML page for the topic being edited.  If provided then the edit link
	 *  will allow editing of only the specified section.
	 * @return A url that links to the edit page for the specified topic.
	 *  Note that this method returns only the URL, not a fully-formed HTML
	 *  anchor tag.
	 * @throws Exception Thrown if any error occurs while builing the link URL.
	 */
	public static String buildEditLinkUrl(ParserInput input, String query, int section) throws Exception {
		query = LinkUtil.appendQueryParam(query, "topic", input.getTopicName());
		if (section > 0) {
			query += "&amp;section=" + section;
		}
		WikiLink wikiLink = new WikiLink();
		// FIXME - hard coding
		wikiLink.setDestination("Special:Edit");
		wikiLink.setQuery(query);
		return LinkUtil.buildInternalLinkUrl(input, wikiLink);
	}

	/**
	 * Utility method for building an anchor tag that links to an image page
	 * and includes the HTML image tag to display the image.
	 *
	 * @param context The servlet context for the link that is being created.
	 * @param virtualWiki The virtual wiki for the link that is being created.
	 * @param topicName The name of the image for which a link is being
	 *  created.
	 * @param frame Set to <code>true</code> if the image should display with
	 *  a frame border.
	 * @param thumb Set to <code>true</code> if the image should display as a
	 *  thumbnail.
	 * @param align Indicates how the image should horizontally align on the
	 *  page.  Valid values are "left", "right" and "center".
	 * @param caption An optional text caption to display for the image.  If
	 *  no caption is used then this value should be either empty or
	 *  <code>null</code>.
	 * @param maxDimension A value in pixels indicating the maximum width or
	 *  height value allowed for the image.  Images will be resized so that
	 *  neither the width or height exceeds this value.
	 * @param suppressLink If this value is <code>true</code> then the
	 *  generated HTML will include the image tag without a link to the image
	 *  topic page.
	 * @param style The CSS class to use with the img HTML tag.  This value
	 *  can be <code>null</code> or empty if no custom style is used.
	 * @param escapeHtml Set to <code>true</code> if the caption should be
	 *  HTML escaped.  This value should be <code>true</code> in any case
	 *  where the caption is not guaranteed to be free from potentially
	 *  malicious HTML code.
	 * @return The full HTML required to display an image enclosed within an
	 *  HTML anchor tag that links to the image topic page.
	 * @throws Exception Thrown if any error occurs while builing the image
	 *  HTML.
	 */
	public static String buildImageLinkHtml(ParserInput input, String context, String virtualWiki, String topicName, boolean frame, boolean thumb, String align, String caption, int maxDimension, boolean suppressLink, String style, boolean escapeHtml) throws Exception {
		Topic topic = input.getDataHandler().lookupTopic(virtualWiki, topicName, false, null);
		if (topic == null) {
			return buildNullTopicLinkHtml(input, context, virtualWiki, topicName);
		}
		WikiFile wikiFile = input.getDataHandler().lookupWikiFile(virtualWiki, topicName);
		if (topic.getTopicType() == Topic.TYPE_FILE) {
			// file, not an image
			return buildFileTopicLinkHtml(input, wikiFile, topicName, caption);
		}
		String html = "";
		WikiImage wikiImage = ImageUtil.initializeImage(wikiFile, maxDimension, input.getDataHandler());
		if (caption == null) caption = "";
		if (frame || thumb || StringUtils.hasText(align)) {
			html += "<div class=\"";
			if (thumb || frame) {
				html += "imgthumb ";
			}
			if (align != null && align.equalsIgnoreCase("left")) {
				html += "imgleft ";
			} else if (align != null && align.equalsIgnoreCase("center")) {
				html += "imgcenter ";
			} else {
				// default right alignment
				html += "imgright ";
			}
			html = html.trim() + "\">";
		}
		if (wikiImage.getWidth() > 0) {
			html += "<div style=\"width:" + (wikiImage.getWidth() + 2) + "px\">";
		}
		if(StringUtils.hasText(input.getURLTarget())) {
			if (!suppressLink) html += "<a class=\"wikiimg\" href=\"" + LinkUtil.buildInternalLinkUrl(input, context, virtualWiki, topicName) + "\" "+input.getURLTarget()+">";
		} else {
			if (!suppressLink) html += "<a class=\"wikiimg\" href=\"" + LinkUtil.buildInternalLinkUrl(input, context, virtualWiki, topicName) + "\">";
		}
		if (!StringUtils.hasText(style)) style = "wikiimg";
		html += "<img class=\"" + style + "\" src=\"";
		String url = FilenameUtils.normalize(wikiFile.getUrl());
		url = FilenameUtils.separatorsToUnix(url);
		html += url;
		html += "\"";
		html += " width=\"" + wikiImage.getWidth() + "\"";
		html += " height=\"" + wikiImage.getHeight() + "\"";
		html += " alt=\"" + Utilities.escapeHTML(caption) + "\"";
		html += " />";
		if (!suppressLink) html += "</a>";
		if (StringUtils.hasText(caption)) {
			html += "<div class=\"imgcaption\">";
			if (escapeHtml) {
				html += Utilities.escapeHTML(caption);
			} else {
				html += caption;
			}
			html += "</div>";
		}
		if (wikiImage.getWidth() > 0) {
			html += "</div>";
		}
		if (frame || thumb || StringUtils.hasText(align)) {
			html += "</div>";
		}
		return html;
	}
	
	private static String buildNullTopicLinkHtml(ParserInput input, String context, String virtualWiki, String topicName)
	throws Exception {
		WikiLink uploadLink = LinkUtil.parseWikiLink("Special:Upload");
		return LinkUtil.buildInternalLinkHtml(input, context, virtualWiki, uploadLink, topicName, "edit", null, true);
	}
	
	private static String buildFileTopicLinkHtml(ParserInput input, WikiFile wikiFile, String topicName, String caption) {
		// file, not an image
		if (!StringUtils.hasText(caption)) {
			caption = topicName.substring(NamespaceHandler.NAMESPACE_IMAGE.length() + 1);
		}
		String url = FilenameUtils.normalize(wikiFile.getUrl());
		url = FilenameUtils.separatorsToUnix(url);
		if(StringUtils.hasText(input.getURLTarget())) {
			return "<a href=\"" + url + "\" "+input.getURLTarget()+">" + Utilities.escapeHTML(caption) + "</a>";
		} else {
			return "<a href=\"" + url + "\">" + Utilities.escapeHTML(caption) + "</a>";
		}
	}
	
	public static String buildVideoHtml(ParserInput input, String context, String virtualWiki, String topicName, String caption)
	throws Exception {
		Topic topic = input.getDataHandler().lookupTopic(virtualWiki, topicName, false, null);
		if (topic == null) {
			return buildNullTopicLinkHtml(input, context, virtualWiki, topicName);
		}
		WikiFile wikiFile = input.getDataHandler().lookupWikiFile(virtualWiki, topicName);
		if (topic.getTopicType() == Topic.TYPE_FILE) {
			return buildFileTopicLinkHtml(input, wikiFile, topicName, caption);
		}

		StringBuilder html = new StringBuilder();
		String url = FilenameUtils.normalize(wikiFile.getUrl());
		html.append("<div id=\"wiki_video_").append(++counter).append("\" class=\"wikivideo\"><video src=\"").append(url).append("\" ");
		
		File videoFile = new File(wikiFile.getAbsUrl(), wikiFile.getFileName());
		WikiMediaDimension size = input.getDataHandler().getVideoDimension(videoFile);
		if(size != null) {
			html.append(" width=\"").append(size.getWidth()).append("\"")
			    .append(" height=\"").append(size.getHeight()).append("\"");
		}
		html.append("></video></div>");
		return html.toString();
	}
	
	public static String buildAudioHtml(ParserInput input, String context, String virtualWiki, String topicName, String caption)
	throws Exception {
		Topic topic = input.getDataHandler().lookupTopic(virtualWiki, topicName, false, null);
		if (topic == null) {
			return buildNullTopicLinkHtml(input, context, virtualWiki, topicName);
		}
		WikiFile wikiFile = input.getDataHandler().lookupWikiFile(virtualWiki, topicName);
		if (topic.getTopicType() == Topic.TYPE_FILE) {
			return buildFileTopicLinkHtml(input, wikiFile, topicName, caption);
		}

		StringBuilder html = new StringBuilder();
		String url = FilenameUtils.normalize(wikiFile.getUrl());
		html.append("<div id=\"wiki_audio_").append(++counter)
		    .append("\" class=\"wikiaudio\"><audio src=\"").append(url).append("\" controls></audio></div>");
		return html.toString();
	}

	/**
	 * Build the HTML anchor link to a topic page for a given WikLink object.
	 *
	 * @param context The servlet context for the link that is being created.
	 * @param virtualWiki The virtual wiki for the link that is being created.
	 * @param wikiLink The WikiLink object containing all relevant information
	 *  about the link being generated.
	 * @param text The text to display as the link content.
	 * @param style The CSS class to use with the anchor HTML tag.  This value
	 *  can be <code>null</code> or empty if no custom style is used.
	 * @param target The anchor link target, or <code>null</code> or empty if
	 *  no target is needed.
	 * @param escapeHtml Set to <code>true</code> if the link caption should
	 *  be HTML escaped.  This value should be <code>true</code> in any case
	 *  where the caption is not guaranteed to be free from potentially
	 *  malicious HTML code.
	 * @return An HTML anchor link that matches the given input parameters.
	 * @throws Exception Thrown if any error occurs while builing the link
	 *  HTML.
	 */
	public static String buildInternalLinkHtml(ParserInput input, String context, String virtualWiki, WikiLink wikiLink, String text, String style, String target, boolean escapeHtml) throws Exception {
		String url = LinkUtil.buildInternalLinkUrl(input, wikiLink);
		String topic = wikiLink.getDestination();
		if (!StringUtils.hasText(text)) text = topic;
		if (StringUtils.hasText(topic) && !StringUtils.hasText(style)) {
			if (InterWikiHandler.isInterWiki(virtualWiki)) {
				style = "interwiki";
			} else if (!input.getDataHandler().exists(virtualWiki, topic)) {
				style = "edit";
			}
		}
		if (StringUtils.hasText(style)) {
			style = " class=\"" + style + "\"";
		} else {
			style = "";
		}
		if (StringUtils.hasText(input.getURLTarget())) {
			target = input.getURLTarget();
		} else {
			target = "";
		}
		String html = "<a title=\"" + Utilities.escapeHTML(text) + "\" href=\"" + url + "\"" + style + target + ">";
		if (escapeHtml) {
			html += Utilities.escapeHTML(text);
		} else {
			html += text;
		}
		html += "</a>";
		return html;
	}

	/**
	 * Build a URL to the topic page for a given topic.
	 *
	 * @param context The servlet context path.  If this value is
	 *  <code>null</code> then the resulting URL will NOT include context path,
	 *  which breaks HTML links but is useful for servlet redirection URLs.
	 * @param virtualWiki The virtual wiki for the link that is being created.
	 * @param topic The topic name for the URL that is being generated.
	 * @throws Exception Thrown if any error occurs while builing the link
	 *  URL.
	 */
	public static String buildInternalLinkUrl(ParserInput input, String context, String virtualWiki, String topic) throws Exception {
		if (!StringUtils.hasText(topic)) {
			return null;
		}
		WikiLink wikiLink = LinkUtil.parseWikiLink(topic);
		return LinkUtil.buildInternalLinkUrl(input, wikiLink);
	}

	/**
	 * Build a URL to the topic page for a given topic.
	 *
	 * @param context The servlet context path.  If this value is
	 *  <code>null</code> then the resulting URL will NOT include context path,
	 *  which breaks HTML links but is useful for servlet redirection URLs.
	 * @param virtualWiki The virtual wiki for the link that is being created.
	 * @param wikiLink The WikiLink object containing all relevant information
	 *  about the link being generated.
	 * @throws Exception Thrown if any error occurs while builing the link
	 *  URL.
	 */
	public static String buildInternalLinkUrl(ParserInput parserInput,  WikiLink wikiLink) throws Exception {
		String topic = wikiLink.getDestination();
		String section = wikiLink.getSection();
		String query = wikiLink.getQuery();
		if (!StringUtils.hasText(topic) && StringUtils.hasText(section)) {
			return "#" + Utilities.encodeForURL(section);
		}
		if (!parserInput.getDataHandler().exists(parserInput.getVirtualWiki(), topic)) {
			return LinkUtil.buildEditLinkUrl(parserInput, query, -1);
		}
		String url = "";
		if (parserInput.getContext() != null) {
			url += parserInput.getContext();
		}
		// context never ends with a "/" per servlet specification
		url += "/";
		// get the virtual wiki, which should have been set by the parent servlet
		url += Utilities.encodeForURL(parserInput.getVirtualWiki());
		url += "/";
		url += Utilities.encodeForURL(topic);
		if (StringUtils.hasText(section)) {
			if (!section.startsWith("#")) url += "#";
			url += Utilities.encodeForURL(section);
		}
		if (StringUtils.hasText(query)) {
			if (!query.startsWith(":")) url += ":";
			url += query;
		}
		return url;
	}

	/**
	 * Generate the HTML for an interwiki anchor link.
	 *
	 * @param wikiLink The WikiLink object containing all relevant information
	 *  about the link being generated.
	 * @return The HTML anchor tag for the interwiki link.
	 */
	public static String interWiki(WikiLink wikiLink) {
		// remove namespace from link destination
		String destination = wikiLink.getDestination();
		String namespace = wikiLink.getNamespace();
		destination = destination.substring(wikiLink.getNamespace().length() + NamespaceHandler.NAMESPACE_SEPARATOR.length());
		String url = InterWikiHandler.formatInterWiki(namespace, destination);
		String text = (StringUtils.hasText(wikiLink.getText())) ? wikiLink.getText() : wikiLink.getDestination();
		return "<a class=\"interwiki\" rel=\"nofollow\" title=\"" + text + "\" href=\"" + url + "\">" + text + "</a>";
	}


	/**
	 * Parse a topic name of the form "Topic#Section?Query", and return a WikiLink
	 * object representing the link.
	 *
	 * @param raw The raw topic link text.
	 * @return A WikiLink object that represents the link.
	 */
	public static WikiLink parseWikiLink(String raw) {
		// note that this functionality was previously handled with a regular
		// expression, but the expression caused CPU usage to spike to 100%
		// with topics such as "Urnordisch oder Nordwestgermanisch?"
		String processed = raw.trim();
		WikiLink wikiLink = new WikiLink();
		if (!StringUtils.hasText(processed)) {
			return new WikiLink();
		}
		// first see if the link ends with a query param - "?..."
		int queryPos = processed.indexOf('?', 1);
		if (queryPos != -1 && queryPos < processed.length()) {
			String queryString = processed.substring(queryPos + 1);
			wikiLink.setQuery(queryString);
			processed = processed.substring(0, queryPos);
		}
		// now look for a section param - "#..."
		int sectionPos = processed.indexOf('#', 1);
		if (sectionPos != -1 && sectionPos < processed.length()) {
			String sectionString = processed.substring(sectionPos + 1);
			wikiLink.setSection(sectionString);
			processed = processed.substring(0, sectionPos);
		}
		// since we're having so much fun, let's find a namespace (default empty).
		String namespaceString = "";
		int namespacePos = processed.indexOf(':', 1);
		if (namespacePos != -1 && namespacePos < processed.length()) {
			namespaceString = processed.substring(0, namespacePos);
		}
		wikiLink.setNamespace(namespaceString);
		if (!namespaceString.equals("")) { //check namespaces
			if (!namespaceString.equals("Special") && !namespaceString.equals("Image") && !namespaceString.equals("Media")) return new WikiLink();		
		}
		
		String topic = processed;
		if (namespacePos > 0) {
			topic = processed.substring(namespacePos + 1);
		}
		wikiLink.setArticle(Utilities.decodeFromURL(topic));
		// destination is namespace + topic
		wikiLink.setDestination(Utilities.decodeFromURL(processed));
		
		if (wikiLink.getQuery() != null) return new WikiLink(); //olat does not allow "?" in links!
		if (wikiLink.getArticle().contains("\"")) return new WikiLink(); //olat does not allow " in links!
		return wikiLink;
	}

	/**
	 * Parse a topic name of the form "Topic#Section?Query", and return a WikiLink
	 * object representing the link.
	 *
	 * @param raw The raw topic link text.
	 * @return A WikiLink object that represents the link.
	 */
	public static WikiLink parseWikiLinkOld(String raw) {
		WikiLink wikiLink = new WikiLink();
		if (!StringUtils.hasText(raw)) {
			return new WikiLink();
		}
		raw = raw.trim();
		if (raw.indexOf("?") > -1) return new WikiLink();
		Matcher m = TOPIC_LINK_PATTERN.matcher(raw);
		if (!m.matches()) {
			return new WikiLink();
		}
		String namespace = m.group(4);
		if (namespace == null) {
			namespace = "";
		} else { //rejects wikiwords with ":" in it and are not media or images
			if (!namespace.equals("Special") && !namespace.equals("Image") && !namespace.equals("Media")) return new WikiLink();
		}
		wikiLink.setNamespace(namespace);
		String destination = m.group(2);
		if (StringUtils.hasText(destination)) destination = Utilities.decodeFromURL(destination);
		wikiLink.setDestination(destination);
		String article = m.group(5);
		if (StringUtils.hasText(article)) article = Utilities.decodeFromURL(article);
		wikiLink.setArticle(article);
		wikiLink.setSection(m.group(7));
		wikiLink.setQuery(m.group(9));
		return wikiLink;
	}

}
