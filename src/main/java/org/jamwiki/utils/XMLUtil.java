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
import java.io.FileInputStream;
import java.sql.Timestamp;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Provides utility methods useful for parsing and processing XML data.
 */
public class XMLUtil {

	/** Logger */
	public static final WikiLogger logger = WikiLogger.getLogger(XMLUtil.class.getName());

	/**
	 * Utiltiy method for building an XML tag of the form &lt;tagName&gt;value&lt;/tagName&gt;.
	 *
	 * @param tagName The name of the XML tag, such as &lt;tagName&gt;value&lt;/tagName&gt;.
	 * @param tagValue The value of the XML tag, such as &lt;tagName&gt;value&lt;/tagName&gt;.
	 * @param escape If <code>true</code> then any less than, greater than, quotation mark,
	 *  apostrophe or ampersands in tagValue will be XML-escaped.
	 * @return An XML representations of the tagName and tagValue parameters.
	 */
	public static String buildTag(String tagName, String tagValue, boolean escape) {
		if (tagValue == null) {
			return "";
		}
		StringBuffer buffer = new StringBuffer();
		buffer.append('<').append(tagName).append('>');
		if (escape) {
			tagValue = Utilities.escapeHTML(tagValue);
		}
		buffer.append(tagValue);
		buffer.append("</").append(tagName).append('>');
		return buffer.toString();
	}

	/**
	 * Utiltiy method for building an XML tag of the form &lt;tagName&gt;value&lt;/tagName&gt;.
	 *
	 * @param tagName The name of the XML tag, such as &lt;tagName&gt;value&lt;/tagName&gt;.
	 * @param tagValue The value of the XML tag, such as &lt;tagName&gt;value&lt;/tagName&gt;.
	 * @return An XML representations of the tagName and tagValue parameters.
	 */
	public static String buildTag(String tagName, int tagValue) {
		return XMLUtil.buildTag(tagName, Integer.toString(tagValue), false);
	}

	/**
	 * Utiltiy method for building an XML tag of the form &lt;tagName&gt;value&lt;/tagName&gt;.
	 *
	 * @param tagName The name of the XML tag, such as &lt;tagName&gt;value&lt;/tagName&gt;.
	 * @param tagValue The value of the XML tag, such as &lt;tagName&gt;value&lt;/tagName&gt;.
	 * @return An XML representations of the tagName and tagValue parameters.
	 */
	public static String buildTag(String tagName, Integer tagValue) {
		return (tagValue == null) ? "" : XMLUtil.buildTag(tagName, tagValue.toString(), false);
	}

	/**
	 * Utiltiy method for building an XML tag of the form &lt;tagName&gt;value&lt;/tagName&gt;.
	 *
	 * @param tagName The name of the XML tag, such as &lt;tagName&gt;value&lt;/tagName&gt;.
	 * @param tagValue The value of the XML tag, such as &lt;tagName&gt;value&lt;/tagName&gt;.
	 * @return An XML representations of the tagName and tagValue parameters.
	 */
	public static String buildTag(String tagName, boolean tagValue) {
		return XMLUtil.buildTag(tagName, Boolean.toString(tagValue), false);
	}

	/**
	 * Utiltiy method for building an XML tag of the form &lt;tagName&gt;value&lt;/tagName&gt;.
	 *
	 * @param tagName The name of the XML tag, such as &lt;tagName&gt;value&lt;/tagName&gt;.
	 * @param tagValue The value of the XML tag, such as &lt;tagName&gt;value&lt;/tagName&gt;.
	 * @return An XML representations of the tagName and tagValue parameters.
	 */
	public static String buildTag(String tagName, Timestamp tagValue) {
		return (tagValue == null) ? "" : XMLUtil.buildTag(tagName, tagValue.toString(), false);
	}

	/**
	 * Utiltiy method for building an XML tag of the form &lt;tagName&gt;value&lt;/tagName&gt;.
	 *
	 * @param tagName The name of the XML tag, such as &lt;tagName&gt;value&lt;/tagName&gt;.
	 * @param tagValue The value of the XML tag, such as &lt;tagName&gt;value&lt;/tagName&gt;.
	 * @return An XML representations of the tagName and tagValue parameters.
	 */
	public static String buildTag(String tagName, long tagValue) {
		return XMLUtil.buildTag(tagName, Long.toString(tagValue), false);
	}

	/**
	 * Get XML <code>Node</code> text content.  This method duplicates the
	 * org.w3c.dom.Node.getTextContent() method in JDK 1.5.
	 *
	 * @param baseNode The XML node from which the content is being retrieved.
	 * @return The text content of the XML node.
	 */
	public static String getTextContent(Node baseNode) throws Exception {
		// if element, first child will be a text element with content
		Node child = baseNode.getFirstChild();
		if (child != null && child.getNodeType() == Node.TEXT_NODE) {
			return child.getNodeValue();
		}
		return "";
	}

	/**
	 * Given a <code>File</code> object that points to an XML file, parse the
	 * XML and return a parsed <code>Document</code> object.
	 *
	 * @param file The File object that points to the XML file.
	 * @param validating Set to <code>true</code> if the parser should
	 *  validate against a DTD.
	 * @return A parsed Document object.
	 */
	public static Document parseXML(File file, boolean validating) throws Exception {
		if (!file.exists()) {
			throw new Exception("File " + file.getAbsolutePath() + " does not exist");
		}
		FileInputStream stream = null;
		try {
			stream = new FileInputStream(file);
			InputSource source = new InputSource(stream);
			try {
				return XMLUtil.parseXML(source, validating);
			} catch (SAXException e) {
				// invalid XML
				logger.severe("The file " + file.getAbsolutePath() + " contains invalid XML", e);
				throw new Exception("The file " + file.getAbsolutePath() + " contains invalid XML: " + e.getMessage());
			}
		} finally {
			if (stream != null) {
				stream.close();
			}
		}
	}

	/**
	 * Given an <code>InputSource</code> object that points to XML data, parse
	 * the XML and return a parsed <code>Document</code> object.
	 *
	 * @param source The InputSource object that points to XML data.
	 * @param validating Set to <code>true</code> if the parser should
	 *  validate against a DTD.
	 * @return A parsed Document object.
	 */
	public static Document parseXML(InputSource source, boolean validating) throws Exception {
		// Create a builder factory
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(validating);
		// Create the builder and parse the file
		return factory.newDocumentBuilder().parse(source);
	}
}
