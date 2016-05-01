package com.cwctravel.jenkinsci.plugins.htmlresource;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.kohsuke.stapler.StaplerResponse;

import com.cwctravel.jenkinsci.plugins.htmlresource.config.HTMLResource;
import com.cwctravel.jenkinsci.plugins.htmlresource.config.HTMLResourceConfiguration;
import com.cwctravel.jenkinsci.plugins.htmlresource.config.HTMLResourceEntry;

import hudson.model.ManagementLink;
import jenkins.model.Jenkins;

public class HTMLResourceUtil {
	private static final Pattern RESOURCE_ENTRY_PATTERN = Pattern.compile("META-INF/resources/webjars/(.*\\.(css|js))");

	private final static Logger LOGGER = Logger.getLogger(HTMLResourceUtil.class.getName());

	public static void syncDirWithCfg(File scriptDirectory, HTMLResourceConfiguration cfg) throws IOException {

		List<File> availablePhysicalScripts = getAvailableScripts(scriptDirectory);

		// check if all physical files are available in the configuration
		// if not, add it to the configuration
		for (File file : availablePhysicalScripts) {
			if (cfg.getHTMLResourceById(file.getName()) == null) {
				cfg.addOrReplace(new HTMLResource(file.getName(), file.getName(), -1, null));
			}
		}

		// check if all scripts in the configuration are physically available
		// if not, mark it as missing
		Set<HTMLResource> unavailableScripts = new HashSet<HTMLResource>();
		for (HTMLResource s : cfg.getHTMLResources()) {
			// only check the scripts belonging to this repodir
			if ((new File(scriptDirectory, s.getResourcePath()).exists())) {
				s.setAvailable(true);
			} else {
				unavailableScripts
						.add(new HTMLResource(s.getId(), s.getName(), s.getIndex(), s.getInitializationScript()));
				LOGGER.info("for repo '" + scriptDirectory.getAbsolutePath() + "' " + s + " is not available!");
			}
		}

		for (HTMLResource script : unavailableScripts) {
			cfg.addOrReplace(script);
		}

		cfg.normalizeIndices();
	}

	/**
	 * search into the declared backup directory for backup archives
	 */
	private static List<File> getAvailableScripts(File scriptDirectory) throws IOException {
		LOGGER.log(Level.FINE, "Listing files of {0}", scriptDirectory.getAbsoluteFile());

		File[] scriptFiles = scriptDirectory.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".jar");
			}
		});

		List<File> fileList;
		if (scriptFiles == null) {
			fileList = new ArrayList<File>();
		} else {
			fileList = Arrays.asList(scriptFiles);
		}

		return fileList;
	}

	public static List<HTMLResourceEntry> computeResourceEntries(File scriptDirectory, HTMLResource resource)
			throws IOException {
		List<HTMLResourceEntry> result = new ArrayList<HTMLResourceEntry>();

		File webJARFile = new File(scriptDirectory, resource.getResourcePath());
		JarFile jarFile = new JarFile(webJARFile);

		Map<String, HTMLResourceEntry> allResourceEntriesMap = new HashMap<String, HTMLResourceEntry>();
		try {
			Enumeration<JarEntry> jarEntries = jarFile.entries();
			while (jarEntries.hasMoreElements()) {
				JarEntry jarEntry = jarEntries.nextElement();
				if (!jarEntry.isDirectory()) {
					Matcher resourceEntryMatcher = RESOURCE_ENTRY_PATTERN.matcher(jarEntry.getName());
					if (resourceEntryMatcher.matches()) {
						String resourceEntryPath = resourceEntryMatcher.group(1);
						HTMLResourceEntry resourceEntry = new HTMLResourceEntry(resourceEntryPath);
						allResourceEntriesMap.put(resourceEntryPath, resourceEntry);
					}
				}
			}
		} finally {
			jarFile.close();
		}

		List<HTMLResourceEntry> selectedResourceEntries = resource.getResourceEntries();
		if (selectedResourceEntries != null) {
			for (HTMLResourceEntry selectedResourceEntry : selectedResourceEntries) {
				HTMLResourceEntry resourceEntry = allResourceEntriesMap.get(selectedResourceEntry.getPath());
				if (resourceEntry != null) {
					resourceEntry.setSelected(true);
				}
			}
		}

		result.addAll(allResourceEntriesMap.values());
		Collections.sort(result);

		return result;
	}

	public static boolean hasResourceEntry(HTMLResource resource, String resourceEntryPath) {
		boolean result = false;
		if (resource != null) {
			List<HTMLResourceEntry> resourceEntries = resource.getResourceEntries();
			for (HTMLResourceEntry resourceEntry : resourceEntries) {
				if (resourceEntry.getPath().equals(resourceEntryPath)) {
					result = true;
					break;
				}
			}
		}

		return result;
	}

	public static void writeResourceEntry(HTMLResource resource, File webJARDirectory, String resourceEntryPath,
			StaplerResponse response) throws IOException {
		if (resource != null) {
			final File file = new File(webJARDirectory, resource.getId());
			if (file.exists()) {
				JarFile jarFile = new JarFile(file);
				try {
					JarEntry jarEntry = jarFile.getJarEntry("META-INF/resources/webjars/" + resourceEntryPath);
					if (jarEntry != null) {
						InputStream iS = jarFile.getInputStream(jarEntry);
						try {
							IOUtils.copy(iS, response.getOutputStream());
							response.flushBuffer();
						} finally {
							iS.close();
						}
					}
				} finally {
					jarFile.close();
				}
			}
		}
	}

	public static List<String> getResourcePathsMatchingExtension(HTMLResourceConfiguration config, String extension) {
		List<String> result = new ArrayList<String>();
		if (config != null) {
			List<HTMLResource> resources = config.getHTMLResources();
			for (HTMLResource resource : resources) {
				List<HTMLResourceEntry> resourceEntries = resource.getResourceEntries();
				if (resourceEntries != null) {
					for (HTMLResourceEntry resourceEntry : resourceEntries) {
						if (resourceEntry.getPath().endsWith("." + extension)) {
							result.add("webjars/" + resource.getId() + "/" + resourceEntry.getPath());
						}
					}
				}
			}
		}
		return result;
	}

	public static List<String> getInitializationScripts(HTMLResourceConfiguration config) {
		List<String> result = new ArrayList<String>();
		if (config != null) {
			List<HTMLResource> resources = config.getHTMLResources();
			for (HTMLResource resource : resources) {
				String initializationScript = resource.getInitializationScript();
				if (initializationScript != null) {
					result.add(initializationScript);
				}
			}
		}

		return result;
	}

	public static HTMLResourceManagement getHTMLResourceManagement() {
		HTMLResourceManagement result = null;
		Jenkins jenkinsInstance = Jenkins.getInstance();
		if(jenkinsInstance != null) {
			List<ManagementLink> managementLinks = jenkinsInstance.getManagementLinks();
			for (ManagementLink managementLink : managementLinks) {
				if (managementLink instanceof HTMLResourceManagement) {
					result = (HTMLResourceManagement) managementLink;
					break;
				}
			}
		}

		return result;
	}
}
