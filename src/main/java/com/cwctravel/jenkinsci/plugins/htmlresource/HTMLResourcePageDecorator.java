package com.cwctravel.jenkinsci.plugins.htmlresource;

import java.util.List;

import com.cwctravel.jenkinsci.plugins.htmlresource.config.HTMLResourceConfiguration;

import hudson.Extension;
import hudson.model.PageDecorator;

@Extension
public class HTMLResourcePageDecorator extends PageDecorator {

	public List<String> getJSResourcePaths() {
		HTMLResourceManagement htmlResourceManagement = HTMLResourceUtil.getHTMLResourceManagement();
		HTMLResourceConfiguration config = htmlResourceManagement.getConfiguration();
		return HTMLResourceUtil.getResourcePathsMatchingExtension(config, "js");
	}

	public List<String> getCSSResourcePaths() {
		HTMLResourceManagement htmlResourceManagement = HTMLResourceUtil.getHTMLResourceManagement();
		HTMLResourceConfiguration config = htmlResourceManagement.getConfiguration();
		return HTMLResourceUtil.getResourcePathsMatchingExtension(config, "css");
	}

	public List<String> getInitializationScripts() {
		HTMLResourceManagement htmlResourceManagement = HTMLResourceUtil.getHTMLResourceManagement();
		HTMLResourceConfiguration config = htmlResourceManagement.getConfiguration();
		return HTMLResourceUtil.getInitializationScripts(config);
	}
}
