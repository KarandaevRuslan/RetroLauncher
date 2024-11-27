package com.karandaev.retrolauncher.utils;

import java.util.*;

/** Utility class for managing application language. */
public class LanguageManager {
  private static Locale locale;
  private static ResourceBundle resourceBundle;

  public static void setLocale(Locale newLocale) {
    locale = newLocale;
    resourceBundle = ResourceBundle.getBundle("i18n.messages", locale);
  }

  public static ResourceBundle getResourceBundle() {
    return resourceBundle;
  }

  public static List<String> getAllTranslates(String resource) {
    var en = ResourceBundle.getBundle("i18n.messages", new Locale("en"));
    var ru = ResourceBundle.getBundle("i18n.messages", new Locale("ru"));
    return Arrays.stream(new String[] {en.getString(resource), ru.getString(resource)}).toList();
  }
}
