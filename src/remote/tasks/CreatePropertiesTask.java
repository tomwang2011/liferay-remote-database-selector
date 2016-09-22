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

package remote.tasks;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import remote.util.PropertiesUtil;
import remote.util.StringUtil;

/**
 * @author Tom Wang
 */
public class CreatePropertiesTask extends Task {

	@Override
	public void execute() throws BuildException {
		try {
			Properties buildProperties = PropertiesUtil.loadProperties(
				Paths.get("build.properties"));

			Path portalPath = Paths.get(
				buildProperties.getProperty("portal.dir"));

			Path portalImplTestPath = portalPath.resolve("portal-impl/test");

			Path portalTestExtBackup = portalImplTestPath.resolve(
				"portal-test-ext.properties.backup");

			Path portalTestExtPath = portalImplTestPath.resolve(
				"portal-test-ext.properties");

			if (Files.exists(portalTestExtPath)) {
				Files.copy(
					portalTestExtPath, portalTestExtBackup,
					StandardCopyOption.REPLACE_EXISTING);
			}

			_createTestExtProperties(
				portalTestExtPath, _dbType, buildProperties);

			Path portalExtPath = portalPath.resolve(
				"portal-impl/src/portal-ext.properties");

			Path tomcatPortalExtPath = Paths.get(
				buildProperties.getProperty("tomcat.dir"),
				"webapps/ROOT/WEB-INF/classes/portal-ext.properties");

			Files.copy(
				portalExtPath, tomcatPortalExtPath,
				StandardCopyOption.REPLACE_EXISTING);

			_createTestExtProperties(
				tomcatPortalExtPath, _dbType, buildProperties);
		}
		catch (IOException ioe) {
			throw new BuildException(ioe);
		}
	}

	public void setDBType(String dbType) {
		_dbType = dbType;
	}

	private static void _createTestExtProperties(
			Path extPropertiesFilePath, String dbType,
			Properties buildProperties)
		throws IOException {

		Properties extProperties = new Properties();

		if (Files.exists(extPropertiesFilePath)) {

			// Load original ext properties and clean up jdbc settings.

			try (Reader reader = Files.newBufferedReader(
					extPropertiesFilePath)) {

				buildProperties.load(reader);

				Set<Entry<Object, Object>> entries = buildProperties.entrySet();

				Iterator<Entry<Object, Object>> iterator = entries.iterator();

				while (iterator.hasNext()) {
					Entry<Object, Object> entry = iterator.next();

					String name = String.valueOf(entry.getKey());

					if (name.startsWith("jdbc.")) {
						iterator.remove();
					}
				}
			}
		}

		extProperties.setProperty(
			"liferay.home", buildProperties.getProperty("liferay.home"));
		extProperties.setProperty(
			"lp.plugins.dir", buildProperties.getProperty("lp.plugins.dir"));

		Properties jdbcSettings = PropertiesUtil.loadProperties(
			Paths.get("settings.properties"));

		extProperties.setProperty(
			"jdbc.default.driverClassName",
			jdbcSettings.getProperty(dbType + ".jdbc.default.driverClassName"));

		String sshTunnelingPort = buildProperties.getProperty(
			"ssh.tunneling.port");

		String jdbcDefaultURL = StringUtil.replace(
			jdbcSettings.getProperty(dbType + ".jdbc.default.url"),
			"%ssh.tunneling.port%", sshTunnelingPort);

		extProperties.setProperty("jdbc.default.url", jdbcDefaultURL);
		extProperties.setProperty(
			"jdbc.default.username",
			jdbcSettings.getProperty(dbType + ".jdbc.default.username"));
		extProperties.setProperty(
			"jdbc.default.password",
			jdbcSettings.getProperty(dbType + ".jdbc.default.password"));

		try (Writer writer = Files.newBufferedWriter(extPropertiesFilePath)) {
			Enumeration e = extProperties.propertyNames();

			StringBuilder sb = new StringBuilder();

			while (e.hasMoreElements()) {
				String key = (String)e.nextElement();

				sb.append(key);
				sb.append('=');
				sb.append(extProperties.getProperty(key));
				sb.append('\n');
			}

			writer.append(sb);
		}
	}

	private String _dbType;

}