package it.gov.pagopa.payment.options.util;

public class StringUtil {

  // Remove all Unicode control characters and replace any non-basic allowed char with '_'
  public static String sanitize(String input) {
    if (input == null) {
      return null;
    }
    // Remove all Unicode control characters (includes all line break and other special chars)
    // Only allow alphanumerics, dash, and underscore; replace others with '_'
    String noControls = input.replaceAll("[\\p{Cntrl}]", "_");
    return noControls.replaceAll("[^A-Za-z0-9_-]", "_");
  }

  private StringUtil() {}
}
