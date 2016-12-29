package xdean.css;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class CSSConstants {

  public static final Set<Character> LEGAL_CHARS;

  static {
    Set<Character> set = new HashSet<>();
    for (char c = '0'; c <= '9'; c++) {
      set.add(c);
    }
    for (char c = 'a'; c <= 'z'; c++) {
      set.add(c);
      set.add((char) (c - 'a' + 'A'));
    }
    set.add('-');
    set.add('_');
    LEGAL_CHARS = Collections.unmodifiableSet(set);
  }
}
