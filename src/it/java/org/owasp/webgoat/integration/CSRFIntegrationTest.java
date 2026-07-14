/*
 * SPDX-FileCopyrightText: Copyright © 2019 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.owasp.webgoat.container.lessons.Assignment;

public class CSRFIntegrationTest extends IntegrationTest {

  private static final String trickHTML3 =
      "<!DOCTYPE html><html><body><form action=\"WEBGOATURL\" method=\"POST\">\n"
          + "<input type=\"hidden\" name=\"csrf\" value=\"" + System.getenv("CSRF_TOKEN") + "\"/>\n"
          + "<input type=\"submit\" name=\"submit\" value=\"assignment 3\"/>\n"
          + "</form></body></html>";

  private static final String trickHTML4 =
      "<!DOCTYPE html><html><body><form action=\"WEBGOATURL\" method=\"POST\">\n"
          + "<input type=\"hidden\" name=\"reviewText\" value=\"hoi\"/>\n"
          + "<input type=\"hidden\" name=\"starts\" value=\"3\"/>\n"
          + "<input type=\"hidden\" name=\"validateReq\" value=\"" + System.getenv("VALIDATION_TOKEN") + "\"/>\n"
          + "<input type=\"submit\" name=\"submit\" value=\"assignment 4\"/>\n"
          + "</form>\n"
          + "</body></html>";

  private static final String trickHTML7 =
      "<!DOCTYPE html><html><body><form action=\"WEBGOATURL\" enctype=\"application/x-www-form-urlencoded\" method=\"POST\">\n"
          + "<input type=\"hidden\" name=\"feedback\" value=\"WebGoat is the best!!\"/>\n"
          + "<input type=\"submit\" value=\"assignment 7\"/>\n"
          + "</form></body></html>";

  private static final String trickHTML8 =
      "<!DOCTYPE html><html><body><form action=\"WEBGOATURL\" method=\"POST\">\n"
          + "<input type=\"hidden\" name=\"username\" value=\"" + System.getenv("USERNAME") + "\"/>\n"
          + "<input type=\"hidden\" name=\"password\" value=\"" + System.getenv("PASSWORD") + "\"/>\n"
          + "<input type=\"hidden\" name=\"matchingPassword\" value=\"" + System.getenv("PASSWORD") + "\"/>\n"
          + "<input type=\"hidden\" name=\"agree\" value=\"agree\"/>\n"
          + "<input type=\"submit\" value=\"assignment 8\"/>\n"
          + "</form></body></html>";

  private String webwolfFileDir;

  @BeforeEach
  @SneakyThrows
  public void init() {
    startLesson("CSRF");
    webwolfFileDir = getWebWolfFileServerLocation();
      uploadTrickHtml("csrf3.html", trickHTML3.replace("WEBGOATURL", webGoatUrlConfig.url("csrf/basic-get-flag")));
      uploadTrickHtml("csrf4.html", trickHTML4.replace("WEBGOATURL", webGoatUrlConfig.url("csrf/review")));
      uploadTrickHtml("csrf7.html", trickHTML7.replace("WEBGOATURL", webGoatUrlConfig.url("csrf/feedback/message")));
      uploadTrickHtml(
        "csrf8.html",
        trickHTML8.replace("WEBGOATURL", webGoatUrlConfig.url("login")));
  }

  @TestFactory
  Iterable<DynamicTest> testCSRFLesson() {
    return Arrays.asList(
        dynamicTest("assignment 3", () -> checkAssignment3(callTrickHtml("csrf3.html"))),
        dynamicTest("assignment 4", () -> checkAssignment4(callTrickHtml("csrf4.html"))),
        dynamicTest("assignment 7", () -> checkAssignment7(callTrickHtml("csrf7.html"))),
        dynamicTest("assignment 8", () -> checkAssignment8(callTrickHtml("csrf8.html"))));
  }

  @AfterEach
  public void shutdown() throws IOException {
    login();
    startLesson("CSRF", false);
    checkResults("CSRF");
  }

  private void uploadTrickHtml(String htmlName, String htmlContent) throws IOException {

    Path webWolfFilePath = Paths.get(webwolfFileDir);
    if (webWolfFilePath.resolve(Paths.get(this.getUser(), htmlName)).toFile().exists()) {
      Files.delete(webWolfFilePath.resolve(Paths.get(this.getUser(), htmlName)));
    }

    RestAssured.given()
        .when()
        .relaxedHTTPSValidation()
        .cookie("WEBWOLFSESSION", getWebWolfCookie())
        .multiPart("file", htmlName, htmlContent.getBytes())
        .post(webWolfUrlConfig.url("fileupload"))
        .then()
        .extract()
        .response()
        .getBody()
        .asString();
  }

  private String callTrickHtml(String htmlName) {
    String result =
        RestAssured.given()
            .when()
            .relaxedHTTPSValidation()
            .cookie("JSESSIONID", getWebGoatCookie())
            .cookie("WEBWOLFSESSION", getWebWolfCookie())
            .get(webWolfUrlConfig.url("files/%s/%s".formatted(this.getUser(), htmlName)))
            .then()
            .extract()
            .response()
            .getBody()
            .asString();
    result = result.substring(8 + result.indexOf("action=\""));
    result = result.substring(0, result.indexOf("\""));

    return result;
  }

  private void checkAssignment3(String goatURL) {
    String flag =
        RestAssured.given()
            .when()
            .relaxedHTTPSValidation()
            .cookie("JSESSIONID", getWebGoatCookie())
            .header("Referer", webWolfUrlConfig.url("files/fake.html"))
            .post(goatURL)
            .then()
            .extract()
            .path("flag")
            .toString();

    Map<String, Object> params = new HashMap<>();
    params.put("confirmFlagVal", flag);
      checkAssignment(webGoatUrlConfig.url("csrf/confirm-flag-1"), params, true);
  }
}