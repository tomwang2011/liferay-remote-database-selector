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

import java.io.BufferedWriter;
import java.io.IOException;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

import java.util.List;
import java.util.Properties;

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
			Properties properties = PropertiesUtil.loadProperties(
				Paths.get("build.properties"));

			Path portalPath = Paths.get(properties.getProperty("portal.dir"));

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

			_createTestExtProperties(portalTestExtPath, _dbType, properties);

			Path portalExtPath = portalPath.resolve(
				"portal-impl/src/portal-ext.properties");

			Path tomcatPortalExtPath = Paths.get(
				properties.getProperty("tomcat.dir"),
				"webapps/ROOT/WEB-INF/classes/portal-ext.properties");

			Files.copy(
				portalExtPath, tomcatPortalExtPath,
				StandardCopyOption.REPLACE_EXISTING);

			_createTestExtProperties(tomcatPortalExtPath, _dbType, properties);
		}
		catch (IOException ioe) {
			throw new BuildException(ioe);
		}
	}

	public void setDBType(String dbType) {
		_dbType = dbType;
	}

	private static void _createTestExtProperties(
			Path filePath, String dbType, Properties properties)
		throws IOException {

		StringBuilder sb = new StringBuilder();

		sb.append("liferay.home=");
		sb.append(properties.getProperty("liferay.home"));
		sb.append('\n');
		sb.append("lp.plugins.dir=");
		sb.append(properties.getProperty("lp.plugins.dir"));
		sb.append('\n');

		Properties jdbcSettings = PropertiesUtil.loadProperties(
			Paths.get("settings.properties"));

		sb.append("jdbc.default.driverClassName=");
		sb.append(
			jdbcSettings.getProperty(dbType + ".jdbc.default.driverClassName"));
		sb.append('\n');
		sb.append("jdbc.default.url=");
		sb.append(jdbcSettings.getProperty(dbType + ".jdbc.default.url"));
		sb.append('\n');
		sb.append("jdbc.default.username=");
		sb.append(jdbcSettings.getProperty(dbType + ".jdbc.default.username"));
		sb.append('\n');
		sb.append("jdbc.default.password=");
		sb.append(jdbcSettings.getProperty(dbType + ".jdbc.default.password"));
		sb.append('\n');

		if (Files.exists(filePath)) {
			List<String> lines = Files.readAllLines(filePath);

			for (String line : lines) {
				if (!line.startsWith("jdbc") &&
					!line.startsWith("liferay.home") &&
					!line.startsWith("lp.plugins.dir")) {

					sb.append(line);
					sb.append('\n');
				}
			}

			Files.delete(filePath);
		}

		Files.createFile(filePath);

		String string = StringUtil.replace(
			sb.toString(), "%ssh.tunneling.port%",
			properties.getProperty("ssh.tunneling.port"));

		try (BufferedWriter bufferedWriter = Files.newBufferedWriter(
				filePath, Charset.defaultCharset(), StandardOpenOption.APPEND))
		{
			bufferedWriter.append(string);
		}
	}

	private String _dbType;

}