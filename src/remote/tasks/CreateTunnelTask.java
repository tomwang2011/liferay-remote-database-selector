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

import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.JOptionPane;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import remote.util.PropertiesUtil;

/**
 * @author tom
 */
public class CreateTunnelTask extends Task {

	@Override
	public void execute() throws BuildException {
		try {
			Properties buildProperties = PropertiesUtil.loadProperties(
				Paths.get("build.properties"));

			List<String> tunnelTask = new ArrayList<>();

			String remoteHost = buildProperties.getProperty("remote.host");

			tunnelTask.add("ssh");
			tunnelTask.add(
				buildProperties.getProperty("remote.username") + "@" +
					remoteHost);
			tunnelTask.add("-L");

			Properties jdbcSettings = PropertiesUtil.loadProperties(
				Paths.get("settings.properties"));

			tunnelTask.add(
				buildProperties.getProperty("ssh.tunneling.port") + ":" +
					remoteHost + ":" +
					jdbcSettings.getProperty(_dbType + ".port"));

			tunnelTask.add("-N");

			ProcessBuilder processBuilder = new ProcessBuilder(tunnelTask);

			Process process = processBuilder.start();

			if (JOptionPane.showConfirmDialog(
					null, "click yes to end", "Stop Tunnel",
					JOptionPane.YES_OPTION) == 0) {

				process.destroy();
			}

			process.waitFor();
		}
		catch (Exception e) {
			throw new BuildException(e);
		}
	}

	public void setDBType(String dbType) {
		_dbType = dbType;
	}

	private String _dbType;

}