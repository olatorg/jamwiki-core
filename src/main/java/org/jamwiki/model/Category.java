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
package org.jamwiki.model;

import org.jamwiki.utils.WikiLogger;

/**
 * Provides an object representing a Wiki category.
 */
public class Category {

	private static WikiLogger logger = WikiLogger.getLogger(Category.class.getName());
	private String childTopicName = null;
	private String name = null;
	private String sortKey = null;
	private String virtualWiki = null;

	/**
	 *
	 */
	public Category() {
	}

	/**
	 *
	 */
	public String getChildTopicName() {
		return this.childTopicName;
	}

	/**
	 *
	 */
	public void setChildTopicName(String childTopicName) {
		this.childTopicName = childTopicName;
	}

	/**
	 *
	 */
	public String getName() {
		return this.name;
	}

	/**
	 *
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 *
	 */
	public String getSortKey() {
		return this.sortKey;
	}

	/**
	 *
	 */
	public void setSortKey(String sortKey) {
		this.sortKey = sortKey;
	}

	/**
	 *
	 */
	public String getVirtualWiki() {
		return this.virtualWiki;
	}

	/**
	 *
	 */
	public void setVirtualWiki(String virtualWiki) {
		this.virtualWiki = virtualWiki;
	}
}