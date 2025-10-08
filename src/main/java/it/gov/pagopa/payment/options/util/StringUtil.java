package it.gov.pagopa.payment.options.util;

public class StringUtil {

  // Replace newline, carriage return, tab, single quote, double quote, and backslash characters
  public static String sanitize(String input) {
    if (input == null) {
      return null;
    }
    return input.replaceAll("[\\n\\r\\t'\"\\\\]", "_");
  }

  private StringUtil() {}
}
