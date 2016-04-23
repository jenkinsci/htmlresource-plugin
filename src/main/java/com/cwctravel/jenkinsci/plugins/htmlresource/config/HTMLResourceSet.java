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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import com.cwctravel.jenkinsci.plugins.htmlresource.util.ByIndexSorter;

/**
 * @author imod
 * 
 */
public class HTMLResourceSet {
	private static final Logger LOGGER = Logger.getLogger(HTMLResourceSet.class.getName());

	// have it sorted
	protected Set<HTMLResource> htmlResourceSet = new TreeSet<HTMLResource>();

	public HTMLResource getHTMLResourceById(String id) {
		for (HTMLResource htmlResource : htmlResourceSet) {
			if (htmlResource.getId().equals(id)) {
				return htmlResource;
			}
		}
		return null;
	}

	public void removeHTMLResource(String id) {
		HTMLResource htmlResource = getHTMLResourceById(id);
		htmlResourceSet.remove(htmlResource);

		normalizeIndices();
	}

	public void addOrReplace(HTMLResource htmlResource) {
		if (htmlResource != null) {
			HTMLResource oldHTMLResource = this.getHTMLResourceById(htmlResource.getId());
			if (oldHTMLResource != null) {
				htmlResourceSet.remove(oldHTMLResource);
				htmlResourceSet.add(htmlResource);
			} else {
				htmlResourceSet.add(htmlResource);
			}
			normalizeIndices();
		}
	}

	public void normalizeIndices() {
		List<HTMLResource> htmlResources = new ArrayList<HTMLResource>();
		htmlResources.addAll(htmlResourceSet);
		Collections.sort(htmlResources, new ByIndexSorter());

		for (int i = 0; i < htmlResources.size(); i++) {
			HTMLResource htmlResource = htmlResources.get(i);
			htmlResource.setIndex(i);
		}

	}

	public void moveHTMLResourceUp(HTMLResource htmlResource) {
		if (htmlResource != null) {
			int resourceIndex = htmlResource.getIndex();

			int ceilingIndex = -1;

			HTMLResource ceilingIndexResource = null;
			for (HTMLResource currentHtmlResource : htmlResourceSet) {
				int currentResourceIndex = currentHtmlResource.getIndex();
				if (currentResourceIndex >= 0 && currentResourceIndex < resourceIndex
						&& (ceilingIndex < currentResourceIndex)) {
					ceilingIndex = currentResourceIndex;
					ceilingIndexResource = currentHtmlResource;
				}
			}

			if (ceilingIndexResource != null) {
				ceilingIndexResource.setIndex(resourceIndex);
				htmlResource.setIndex(ceilingIndex);
			}
		}
	}

	public void moveHTMLResourceDown(HTMLResource htmlResource) {
		if (htmlResource != null) {
			int resourceIndex = htmlResource.getIndex();

			int floorIndex = htmlResourceSet.size();

			HTMLResource floorIndexResource = null;
			for (HTMLResource currentHtmlResource : htmlResourceSet) {
				int currentResourceIndex = currentHtmlResource.getIndex();
				if (currentResourceIndex > resourceIndex && (floorIndex > currentResourceIndex)) {
					floorIndex = currentResourceIndex;
					floorIndexResource = currentHtmlResource;
				}
			}

			if (floorIndexResource != null) {
				floorIndexResource.setIndex(resourceIndex);
				htmlResource.setIndex(floorIndex);
			}
		}

	}

	public final List<HTMLResource> getHTMLResources() {
		List<HTMLResource> htmlResources = new ArrayList<HTMLResource>();
		htmlResources.addAll(htmlResourceSet);
		Collections.sort(htmlResources, new ByIndexSorter());
		return htmlResources;
	}

	public void setHTMLResources(Set<HTMLResource> htmlResourceSet) {
		this.htmlResourceSet = htmlResourceSet;
	}

}
