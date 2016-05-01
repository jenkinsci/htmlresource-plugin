package com.cwctravel.jenkinsci.plugins.htmlresource.util;

import java.io.Serializable;
import java.util.Comparator;

import com.cwctravel.jenkinsci.plugins.htmlresource.config.NamedResource;

public class ByIndexSorter implements Comparator<NamedResource>, Serializable {
	private static final long serialVersionUID = 2329578993755913687L;

	@Override
	public int compare(NamedResource o1, NamedResource o2) {
		int index1 = o1.getIndex();
		int index2 = o2.getIndex();

		if (index2 < 0 && index1 < 0) {
			return 0;
		}

		if (index1 < 0) {
			return 1;
		}

		if (index2 < 0) {
			return -1;
		}

		if (index1 < index2) {
			return -1;
		}

		if (index1 > index2) {
			return 1;
		}
		return 0;
	};
}