
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

import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.util.concurrent.CountDownLatch;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

/**
 * @author Shuyang Zhou
 */
public class CloseFrame extends JFrame {

	public CloseFrame(CountDownLatch countDownLatch) {
		_countDownLatch = countDownLatch;

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		addWindowListener(
			new WindowAdapter() {

				@Override
				public void windowClosed(WindowEvent we) {
					_countDown();
				}

			});

		_closeButton.setText("Close Tunnel");

		_closeButton.addActionListener(
			(ActionEvent ae) -> {
				_countDown();
			});

		add(_closeButton);

		setSize(200, 100);
	}

	private void _countDown() {
		dispose();

		_countDownLatch.countDown();
	}

	private JButton _closeButton = new JButton();
	private final CountDownLatch _countDownLatch;

}