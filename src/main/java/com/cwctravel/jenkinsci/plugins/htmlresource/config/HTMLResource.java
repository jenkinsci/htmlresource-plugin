/*
 * The MIT License
 *
 * Copyright (c) 2010, Vimil
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.cwctravel.jenkinsci.plugins.htmlresource.config;

import java.util.List;

import org.kohsuke.stapler.DataBoundConstructor;

public class HTMLResource implements Comparable<HTMLResource>, NamedResource {
	private int index;
	private String id;
	private final String name;
	private String initializationScript;

	private List<HTMLResourceEntry> resourceEntries;

	public boolean available = true;

	/**
	 * used to create/update a new script in the UI
	 */
	@DataBoundConstructor
	public HTMLResource(String id, String name, int index, String initializationScript) {
		this.id = id;
		this.name = name;
		this.initializationScript = initializationScript;
	}

	@Override
	public String getName() {
		return name;
	}

	public String getResourcePath() {
		return id;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public List<HTMLResourceEntry> getResourceEntries() {
		return resourceEntries;
	}

	public void setResourceEntries(List<HTMLResourceEntry> resourceEntries) {
		this.resourceEntries = resourceEntries;
	}

	public void setAvailable(boolean available) {
		this.available = available;
	}

	public String getInitializationScript() {
		return initializationScript;
	}

	public void setInitializationScript(String initializationScript) {
		this.initializationScript = initializationScript;
	}

	@Override
	public int compareTo(HTMLResource o) {
		return id.compareTo(o.id);
	}

	public HTMLResource copy() {
		return new HTMLResource(id, name, index, initializationScript);
	}

	/**
	 * Previously we used not to have an id, but only a name.
	 */
	public Object readResolve() {
		if (id == null) {
			id = name;
		}
		return this;
	}

	/**
	 * @return the id
	 */
	@Override
	public String getId() {
		return id;
	}

	@Override
	public String toString() {
		return "[Script: " + id + ":" + name + "]";
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		HTMLResource other = (HTMLResource) obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		return true;
	}

}
