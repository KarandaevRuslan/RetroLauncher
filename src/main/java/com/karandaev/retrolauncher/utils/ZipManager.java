package com.karandaev.retrolauncher.utils;

import java.io.*;
import java.util.*;
import java.util.zip.*;

public class ZipManager {
  /**
   * Unpacks a ZIP archive into the specified destination directory without creating an extra root
   * folder.
   *
   * @param zipFilePath the path to the ZIP file
   * @param destDir the destination directory where files will be extracted
   * @throws IOException if an I/O error occurs
   */
  public static void unzip(String zipFilePath, String destDir) throws IOException {
    File destDirectory = new File(destDir);
    if (!destDirectory.exists()) {
      destDirectory.mkdirs();
    }

    // Detect common root folder in the ZIP entries
    String commonRoot = findCommonRoot(zipFilePath);

    try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath))) {
      ZipEntry entry;

      while ((entry = zipIn.getNextEntry()) != null) {
        String entryName = entry.getName();

        // Remove the common root if it exists
        if (!commonRoot.isEmpty() && entryName.startsWith(commonRoot + "/")) {
          entryName = entryName.substring(commonRoot.length() + 1);
        }

        String filePath = destDir + File.separator + entryName;
        if (!entry.isDirectory()) {
          // Ensure parent directories exist
          new File(filePath).getParentFile().mkdirs();
          extractFile(zipIn, filePath);
        } else {
          new File(filePath).mkdirs();
        }
        zipIn.closeEntry();
      }
    }
  }

  /**
   * Finds the common root directory in the ZIP entries, if any.
   *
   * @param zipFilePath the path to the ZIP file
   * @return the common root directory name, or an empty string if none exists
   * @throws IOException if an I/O error occurs
   */
  private static String findCommonRoot(String zipFilePath) throws IOException {
    String commonRoot = null;

    try (ZipFile zipFile = new ZipFile(zipFilePath)) {
      Enumeration<? extends ZipEntry> entries = zipFile.entries();

      while (entries.hasMoreElements()) {
        String entryName = entries.nextElement().getName();
        String[] parts = entryName.split("/");

        if (parts.length > 0) {
          String root = parts[0];
          if (commonRoot == null) {
            commonRoot = root;
          } else if (!commonRoot.equals(root)) {
            return "";
          }
        } else {
          return "";
        }
      }
    }
    return commonRoot != null ? commonRoot : "";
  }

  /**
   * Extracts a file entry from the ZIP input stream.
   *
   * @param zipIn the ZIP input stream
   * @param filePath the destination file path
   * @throws IOException if an I/O error occurs
   */
  private static void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
    try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath))) {
      byte[] buffer = new byte[4096];
      int read;
      while ((read = zipIn.read(buffer)) != -1) {
        bos.write(buffer, 0, read);
      }
    }
  }
}
