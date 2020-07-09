package org.jamwiki;

import java.io.File;

import org.jamwiki.model.Topic;
import org.jamwiki.model.WikiFile;

/**
 * 
 * Initial date: 15 mai 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DefaultDataHandler implements DataHandler {
	
	@Override
	public boolean exists(String virtualWiki, String topic) {
		return true;
	}

	@Override
	public Topic lookupTopic(String virtualWiki, String topicName, boolean deleteOK, Object transactionObject) throws Exception {
		return null;
	}

	@Override
	public WikiFile lookupWikiFile(String virtualWiki, String topicName) throws Exception {
		return null;
	}

	@Override
	public WikiMediaDimension getImageDimension(File file) {
		return null;
	}

	@Override
	public WikiMediaDimension getVideoDimension(File file) {
		return null;
	}
}
