package com.cwctravel.jenkinsci.plugins.htmlresource.config;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

public class HTMLResourceEntry implements Comparable<HTMLResourceEntry> {
	private String path;

	@XStreamOmitField
	private boolean selected;

	public HTMLResourceEntry(String path) {
		this.path = path;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HTMLResourceEntry other = (HTMLResourceEntry) obj;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		return true;
	}

	@Override
	public int compareTo(HTMLResourceEntry that) {
		return that == null ? 1 : (path.compareTo(that.path));
	}

}
