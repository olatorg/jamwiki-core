package org.jamwiki;

/**
 * 
 * Initial date: 15 mai 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class WikiMediaDimension {
	
	private final int width;
	private final int height;
	
	public WikiMediaDimension(int width, int height) {
		this.width = width;
		this.height = height;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}
}
