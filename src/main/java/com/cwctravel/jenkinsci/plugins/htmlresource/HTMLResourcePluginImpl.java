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
package com.cwctravel.jenkinsci.plugins.htmlresource;

import java.io.File;
import java.io.IOException;
import java.util.TreeSet;
import java.util.logging.Logger;

import com.cwctravel.jenkinsci.plugins.htmlresource.config.HTMLResource;
import com.cwctravel.jenkinsci.plugins.htmlresource.config.HTMLResourceConfiguration;

import hudson.Plugin;

/**
 * @author domi
 * 
 */
public class HTMLResourcePluginImpl extends Plugin {

	private final static Logger LOGGER = Logger.getLogger(HTMLResourcePluginImpl.class.getName());

	@Override
	public void start() throws Exception {
		super.start();
		synchronizeConfig();
	}

	/**
	 * Checks if all available scripts on the system are in the config and if
	 * all configured files are physically on the filesystem.
	 * 
	 * @throws IOException
	 */
	private void synchronizeConfig() throws IOException {
		LOGGER.info("initialize scriptler");
		if (!HTMLResourceManagement.getHTMLResourceHomeDirectory().exists()) {
			HTMLResourceManagement.getHTMLResourceHomeDirectory().mkdirs();
		}
		File webjarDirectory = HTMLResourceManagement.getWebJARDirectory();
		// create the directory for the scripts if not available
		if (!webjarDirectory.exists()) {
			webjarDirectory.mkdirs();
		}

		HTMLResourceConfiguration cfg = HTMLResourceConfiguration.load();
		if (cfg == null) {
			cfg = new HTMLResourceConfiguration(new TreeSet<HTMLResource>());
		}

		HTMLResourceUtil.syncDirWithCfg(webjarDirectory, cfg);

		cfg.save();

	}
}
