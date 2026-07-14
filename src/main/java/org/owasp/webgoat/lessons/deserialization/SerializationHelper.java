/*
 * SPDX-FileCopyrightText: Copyright © 2019 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.deserialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Base64;
import java.util.Objects;

public class SerializationHelper {

  private static final char[] hexArray = "0123456789ABCDEF".toCharArray();

  public static Object fromString(String s) throws IOException, ClassNotFoundException {
    Objects.requireNonNull(s, "Input string must not be null");
    byte[] data = Base64.getDecoder().decode(s);
    try (ObjectInputStream ois = new SafeObjectInputStream(new ByteArrayInputStream(data))) {
      return ois.readObject();
    }
  }

  public static String toString(Serializable o) throws IOException {
    Objects.requireNonNull(o, "Input object must not be null");
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
      oos.writeObject(o);
    }
    return Base64.getEncoder().encodeToString(baos.toByteArray());
  }

  public static String show() throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (DataOutputStream dos = new DataOutputStream(baos)) {
      dos.writeLong(-8699352886133051976L);
    }
    byte[] longBytes = baos.toByteArray();
    return bytesToHex(longBytes);
  }

  public static String bytesToHex(byte[] bytes) {
    char[] hexChars = new char[bytes.length * 2];
    for (int j = 0; j < bytes.length; j++) {
      int v = bytes[j] & 0xFF;
      hexChars[j * 2] = hexArray[v >>> 4];
      hexChars[j * 2 + 1] = hexArray[v & 0x0F];
    }
    return new String(hexChars);
  }

  private static class SafeObjectInputStream extends ObjectInputStream {
    public SafeObjectInputStream(ByteArrayInputStream in) throws IOException {
      super(in);
    }

    @Override
    protected Class<?> resolveClass(java.io.ObjectStreamClass desc) throws IOException, ClassNotFoundException {
      String className = desc.getName();
      if (!isAllowedClass(className)) {
        throw new ClassNotFoundException("Deserialization of class " + className + " is not allowed.");
      }
      return super.resolveClass(desc);
    }

    private boolean isAllowedClass(String className) {
      return className.startsWith("java.") || className.startsWith("javax.") || className.equals("org.owasp.webgoat.lessons.deserialization.AllowedClass");
    }
  }
}