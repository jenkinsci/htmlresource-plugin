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
package com.cwctravel.jenkinsci.plugins.htmlresource;

import hudson.Extension;
import hudson.Util;
import hudson.PluginWrapper;
import hudson.model.ManagementLink;
import hudson.model.Hudson;
import hudson.security.Permission;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;
import java.util.logging.Logger;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.ServletException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.HttpRedirect;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import com.cwctravel.jenkinsci.plugins.htmlresource.config.HTMLResource;
import com.cwctravel.jenkinsci.plugins.htmlresource.config.HTMLResourceConfiguration;
import com.cwctravel.jenkinsci.plugins.htmlresource.config.HTMLResourceEntry;

/**
 * Creates the link on the "manage Jenkins" page and handles all the web requests.
 * 
 * @author Dominik Bartholdi (imod)
 */
@Extension
public class HTMLResourceManagement extends ManagementLink {

	private final static Logger LOGGER = Logger.getLogger(HTMLResourceManagement.class.getName());

	/*
	 * (non-Javadoc)
	 * 
	 * @see hudson.model.ManagementLink#getUrlName()
	 */
	@Override
	public String getUrlName() {
		return "htmlresource";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see hudson.model.Action#getDisplayName()
	 */
	@Override
	public String getDisplayName() {
		return Messages.display_name();
	}

	@Override
	public String getDescription() {
		return Messages.description();
	}

	public HTMLResourceManagement getHTMLResourceManager() {
		return this;
	}

	public HTMLResourceConfiguration getConfiguration() {
		return HTMLResourceConfiguration.getConfiguration();
	}

	public String getPluginResourcePath() {
		PluginWrapper wrapper = Hudson.getInstance().getPluginManager().getPlugin(HTMLResourcePluginImpl.class);
		return Hudson.getInstance().getRootUrl() + "plugin/" + wrapper.getShortName() + "/";
	}

	public void doEditResourceEntries(StaplerRequest req, StaplerResponse rsp, @QueryParameter("id") String id) throws IOException, ServletException {
		checkPermission(Hudson.ADMINISTER);

		HTMLResource resource = getConfiguration().getHTMLResourceById(id);
		req.setAttribute("resource", resource);
		req.setAttribute("resourceEntries", HTMLResourceUtil.computeResourceEntries(getWebJARDirectory(), resource));
		req.getView(this, "editResourceEntries.jelly").forward(req, rsp);
	}

	/**
	 * Removes a script from the config and filesystem.
	 * 
	 * @param res
	 *        response
	 * @param rsp
	 *        request
	 * @param name
	 *        the name of the file to be removed
	 * @return forward to 'index'
	 * @throws IOException
	 */
	public HttpResponse doRemoveHTMLResource(StaplerRequest res, StaplerResponse rsp, @QueryParameter("id") String id) throws IOException {
		checkPermission(Hudson.ADMINISTER);

		// remove the file
		File oldScript = new File(getWebJARDirectory(), id);
		oldScript.delete();

		// remove the meta information
		HTMLResourceConfiguration cfg = getConfiguration();
		cfg.removeHTMLResource(id);
		cfg.save();

		return new HttpRedirect("index");
	}

	public HttpResponse doMoveResourceUp(StaplerRequest res, StaplerResponse rsp, @QueryParameter("id") String id) throws IOException {
		checkPermission(Hudson.ADMINISTER);
		HTMLResourceConfiguration cfg = getConfiguration();
		HTMLResource resource = cfg.getHTMLResourceById(id);
		cfg.moveHTMLResourceUp(resource);
		cfg.save();
		return new HttpRedirect("index");
	}

	public HttpResponse doMoveResourceDown(StaplerRequest res, StaplerResponse rsp, @QueryParameter("id") String id) throws IOException {
		checkPermission(Hudson.ADMINISTER);
		HTMLResourceConfiguration cfg = getConfiguration();
		HTMLResource resource = cfg.getHTMLResourceById(id);
		cfg.moveHTMLResourceDown(resource);
		cfg.save();
		return new HttpRedirect("index");
	}

	/**
	 * Uploads a script and stores it with the given filename to the configuration. It will be stored on the filessytem.
	 * 
	 * @param req
	 *        request
	 * @return forward to index page.
	 * @throws IOException
	 * @throws ServletException
	 */
	public HttpResponse doUploadHTMLResource(StaplerRequest req) throws IOException, ServletException {
		checkPermission(Hudson.ADMINISTER);
		try {

			FileItem fileItem = req.getFileItem("file");
			String fileName = Util.getFileName(fileItem.getName());
			if(StringUtils.isEmpty(fileName)) {
				return new HttpRedirect(".");
			}
			saveHTMLResource(fileItem, fileName);

			return new HttpRedirect("index");
		}
		catch(IOException e) {
			throw e;
		}
		catch(Exception e) {
			throw new ServletException(e);
		}
	}

	public HttpResponse doUpdateResourceEntries(StaplerRequest req, @QueryParameter("id") String id) throws IOException, ServletException {
		checkPermission(Hudson.ADMINISTER);

		JSONObject jsonData = req.getSubmittedForm();
		String initializationScript = jsonData.optString("initializationScript");
		JSONArray entries = jsonData.optJSONArray("entries");
		if(entries == null && jsonData.optBoolean("entries")) {
			entries = new JSONArray();
			entries.add(true);
		}

		if(entries != null) {
			HTMLResource resource = getConfiguration().getHTMLResourceById(id);
			if(resource != null) {
				List<HTMLResourceEntry> selectedResourcEntries = new ArrayList<HTMLResourceEntry>();
				List<HTMLResourceEntry> resourceEntries = HTMLResourceUtil.computeResourceEntries(getWebJARDirectory(), resource);
				if(resourceEntries != null && resourceEntries.size() <= entries.size()) {
					for(int i = 0; i < entries.size(); i++) {
						if(entries.getBoolean(i)) {
							HTMLResourceEntry selectedResourceEntry = resourceEntries.get(i);
							selectedResourceEntry.setSelected(true);
							selectedResourcEntries.add(selectedResourceEntry);
						}
					}
				}
				resource.setResourceEntries(selectedResourcEntries);
				resource.setInitializationScript(initializationScript);

				HTMLResourceConfiguration config = getConfiguration();
				config.addOrReplace(resource);
				config.save();
			}
		}

		return new HttpRedirect("index");
	}

	private String determineContentType(StaplerRequest req) {
		String contentType = null;
		String resourcePath = req.getPathInfo();
		int lastIndexOfDot = resourcePath.lastIndexOf('.');
		String extension = resourcePath.substring(lastIndexOfDot);
		if(".css".equals(extension)) {
			contentType = "text/css";
		}
		else if(".js".equals(extension)) {
			contentType = "application/x-javascript";
		}
		else if(".png".equals(extension)) {
			contentType = "image/png";
		}
		else if(".gif".equals(extension)) {
			contentType = "image/gif";
		}
		else {
			MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
			contentType = mimeTypesMap.getContentType(resourcePath);
		}

		return contentType;
	}

	public void doWebjars(StaplerRequest req, StaplerResponse resp) throws IOException, ServletException {
		String contentType = determineContentType(req);
		writeHTMLResource(req, resp, contentType);
	}

	private void writeHTMLResource(StaplerRequest req, StaplerResponse resp, String contentType) throws IOException {
		String resourcePath = req.getPathInfo();
		int indexOfSlash = resourcePath.indexOf('/');
		indexOfSlash = indexOfSlash >= 0 ? resourcePath.indexOf('/', indexOfSlash + 1) : -1;
		indexOfSlash = indexOfSlash >= 0 ? resourcePath.indexOf('/', indexOfSlash + 1) : -1;
		if(indexOfSlash > 0) {
			int indexOf4thSlash = resourcePath.indexOf('/', indexOfSlash + 1);
			if(indexOf4thSlash > 0) {
				String resourceId = resourcePath.substring(indexOfSlash + 1, indexOf4thSlash);
				String resourceEntryPath = resourcePath.substring(indexOf4thSlash + 1);
				HTMLResource resource = getConfiguration().getHTMLResourceById(resourceId);
				resp.setContentType(contentType);
				HTMLResourceUtil.writeResourceEntry(resource, getWebJARDirectory(), resourceEntryPath, resp);
			}
		}
	}

	void saveHTMLResource(FileItem fileItem, String fileName) throws Exception, IOException {
		File rootDir = getWebJARDirectory();
		final File f = new File(rootDir, fileName);

		fileItem.write(f);

		if(validateWebJAR(f)) {
			HTMLResource resource = new HTMLResource(fileName, fileName, -1, null);
			HTMLResourceConfiguration config = getConfiguration();
			config.addOrReplace(resource);
			config.save();
		}
	}

	private boolean validateWebJAR(File file) throws IOException {
		boolean result = false;
		if(file.getName().endsWith(".jar")) {
			JarFile jarFile = new JarFile(file);
			try {
				result = jarFile.size() > 0;
			}
			finally {
				jarFile.close();
			}
		}

		return result;
	}

	/**
	 * returns the directory where the script files get stored
	 * 
	 * @return the script directory
	 */
	public static File getWebJARDirectory() {
		return new File(getHTMLResourceHomeDirectory(), "webjars");
	}

	public static File getHTMLResourceHomeDirectory() {
		return new File(Hudson.getInstance().getRootDir(), "htmlresource");
	}

	private void checkPermission(Permission permission) {
		Hudson.getInstance().checkPermission(permission);
	}

	@Override
	public String getIconFileName() {
		return "notepad.gif";
	}
}
