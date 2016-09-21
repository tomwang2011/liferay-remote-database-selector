/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package remote.util;

/**
 * @author Shuyang Zhou
 */
public class StringUtil {

	public static String replace(String s, String oldSub, String newSub) {
		if ((s == null) || (oldSub == null) || oldSub.isEmpty()) {
			return s;
		}

		if (newSub == null) {
			newSub = "";
		}

		int y = s.indexOf(oldSub);

		if (y < 0) {
			return s;
		}

		StringBuilder sb = new StringBuilder();

		int length = oldSub.length();
		int x = 0;

		while (x <= y) {
			sb.append(s.substring(x, y));
			sb.append(newSub);

			x = y + length;
			y = s.indexOf(oldSub, x);
		}

		sb.append(s.substring(x));

		return sb.toString();
	}

}