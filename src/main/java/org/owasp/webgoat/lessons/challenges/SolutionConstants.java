/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.challenges;

public interface SolutionConstants {

  // TODO should be random generated when starting the server
  String PASSWORD = System.getenv("WEBGOAT_PASSWORD");
}
