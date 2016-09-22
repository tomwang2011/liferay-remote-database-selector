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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import remote.util.PropertiesUtil;

/**
 * @author Tom Wang
 */
public class DeployDaoDBTask extends Task {

	@Override
	public void execute() throws BuildException {
		try {
			Properties properties = PropertiesUtil.loadProperties(
				Paths.get("build.properties"));

			Path portalPath = Paths.get(properties.getProperty("portal.dir"));

			Path gradlewPath = portalPath.resolve("gradlew");

			List<String> deployTask = new ArrayList<>();

			deployTask.add(gradlewPath.toString());
			deployTask.add("clean");
			deployTask.add("deploy");

			ProcessBuilder processBuilder = new ProcessBuilder(deployTask);

			Path daoDBPath = portalPath.resolve(
				Paths.get(
					"modules/private/apps/foundation/portal/" +
						"portal-dao-db"));

			processBuilder.directory(daoDBPath.toFile());

			Process process = processBuilder.start();

			boolean displayGradleProcessOutput = Boolean.parseBoolean(
				properties.getProperty("display.gradle.output"));

			if (displayGradleProcessOutput) {
				String line = null;

				try (BufferedReader br = new BufferedReader(
						new InputStreamReader(process.getInputStream()))) {

					while ((line = br.readLine()) != null) {
						System.out.println(line);
					}
				}

				try (BufferedReader br = new BufferedReader(
						new InputStreamReader(process.getErrorStream()))) {

					while ((line = br.readLine()) != null) {
						System.out.println(line);
					}
				}
			}

			int exitCode = process.waitFor();

			if (exitCode != 0) {
				throw new IOException(
					"Process " + processBuilder.command() + " failed with " +
						exitCode);
			}
		}
		catch (Exception e) {
			throw new BuildException(e);
		}
	}

}