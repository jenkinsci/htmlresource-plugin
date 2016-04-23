/*
 * The MIT License
 *
 * Copyright (c) 2010, Dominik Bartholdi
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

import java.io.File;
import java.io.IOException;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.cwctravel.jenkinsci.plugins.htmlresource.HTMLResourceManagement;
import com.cwctravel.jenkinsci.plugins.htmlresource.util.ByIdSorter;
import com.thoughtworks.xstream.XStream;

import hudson.BulkChange;
import hudson.XmlFile;
import hudson.model.Saveable;
import hudson.model.listeners.SaveableListener;
import hudson.util.XStream2;

/**
 */
public final class HTMLResourceConfiguration extends HTMLResourceSet implements Saveable {

	private final static Logger LOGGER = Logger.getLogger(HTMLResourceConfiguration.class.getName());

	public HTMLResourceConfiguration(SortedSet<HTMLResource> scripts) {
		if (scripts != null) {
			this.htmlResourceSet = scripts;
		}
	}

	@Override
	public synchronized void save() throws IOException {
		if (BulkChange.contains(this))
			return;
		getXmlFile().write(this);
		SaveableListener.fireOnChange(this, getXmlFile());
	}

	public static XmlFile getXmlFile() {
		return new XmlFile(XSTREAM,
				new File(HTMLResourceManagement.getHTMLResourceHomeDirectory(), "htmlresource.xml"));
	}

	public static HTMLResourceConfiguration load() throws IOException {
		XmlFile f = getXmlFile();
		if (f.exists()) {
			// As it might be that we have an unsorted set, we ensure the
			// sorting at load time.
			HTMLResourceConfiguration sc = (HTMLResourceConfiguration) f.read();
			SortedSet<HTMLResource> sorted = new TreeSet<HTMLResource>(new ByIdSorter());
			sorted.addAll(sc.getHTMLResources());
			sc.setHTMLResources(sorted);
			return sc;
		} else {
			return null;
		}
	}

	// always retrieve via getter
	private static transient HTMLResourceConfiguration cfg = null;

	public static HTMLResourceConfiguration getConfiguration() {
		if (cfg == null) {
			try {
				cfg = HTMLResourceConfiguration.load();
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, "Failed to load htmlresource configuration", e);
			}
		}
		return cfg;
	}

	private static final XStream XSTREAM = new XStream2();

	static {
		XSTREAM.alias("htmlResourceConfig", HTMLResourceConfiguration.class);
		XSTREAM.alias("htmlResource", HTMLResource.class);
		XSTREAM.alias("htmlResourceEntry", HTMLResourceEntry.class);
	}
}
