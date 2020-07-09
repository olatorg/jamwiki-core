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
package org.jamwiki;

import java.io.File;

import org.jamwiki.model.Topic;
import org.jamwiki.model.WikiFile;

/**
 * This interface provides all methods needed when retrieving or modifying
 * Wiki data.  Any database or other persistency class must implement
 * this interface, and there should also be a corresponding
 * &lt;data-handler&gt; entry for the class in the jamwiki-configuration.xml
 * file.
 */
public interface DataHandler {

	
	/**
	 * Retrieve a Topic object that matches the given virtual wiki and topic
	 * name.
	 *
	 * @param virtualWiki The virtual wiki for the topic being queried.
	 * @param topicName The name of the topic being queried.
	 * @param deleteOK Set to <code>true</code> if deleted topics can be
	 *  retrieved, <code>false</code> otherwise.
	 * @param transactionObject If this method is being called as part of a
	 *  transaction then this parameter should contain the transaction object,
	 *  such as a database connection.  If this method is not part of a
	 *  transaction then this value should be <code>null</code>.
	 * @return A Topic object that matches the given virtual wiki and topic
	 *  name, or <code>null</code> if no matching topic exists.
	 * @throws Exception Thrown if any error occurs during method execution.
	 */
	public Topic lookupTopic(String virtualWiki, String topicName, boolean deleteOK, Object transactionObject) throws Exception;
	
	/**
	 * Retrieve a WikiFile object for a given virtual wiki and topic name.
	 *
	 * @param virtualWiki The virtual wiki for the file being queried.
	 * @param topicName The topic name for the file being queried.
	 * @return The WikiFile object for the given virtual wiki and topic name,
	 *  or <code>null</code> if no matching WikiFile exists.
	 * @throws Exception Thrown if any error occurs during method execution.
	 */
	public WikiFile lookupWikiFile(String virtualWiki, String topicName) throws Exception;
	
	/**
	 * check to see wheter a topic exists or not
	 * @param virtualWiki
	 * @param topic
	 * @return true if topic exists
	 */
	public boolean exists(String virtualWiki, String topic);
	
	public WikiMediaDimension getImageDimension(File file);
	
	public WikiMediaDimension getVideoDimension(File file);
}
