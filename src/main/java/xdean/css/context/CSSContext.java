package xdean.css.context;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.sun.javafx.css.ParsedValueImpl;
import com.sun.javafx.css.Selector;
import com.sun.javafx.css.Stylesheet;
import com.sun.javafx.css.parser.CSSParser;

import io.reactivex.Observable;
import javafx.css.ParsedValue;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import lombok.extern.slf4j.Slf4j;
import xdean.jex.util.collection.ListUtil;
import xdean.jex.util.task.TaskUtil;

@Slf4j
public class CSSContext {
  private static final CSSContext MODENA = new CSSContext();
  private static final String url = CSSContext.class.getResource(
      "/com/sun/javafx/scene/control/skin/modena/modena.css").toExternalForm();
  private static final String ROOT = "*.root";;
  private static final Font FONT = Font.font(1);
  static {
    try {
      MODENA.load(new URL(url));
    } catch (IOException e) {
      log.error("Load modena.css fail!", e);
      throw new RuntimeException(e);
    }
  }

  public static CSSContext createByDefault() {
    return new CSSContext(MODENA);
  }

  final Multiset<String> selectors;
  final Multiset<String> classes;
  final Multiset<String> javaClasses;
  final Multiset<String> ids;
  final Multiset<String> states;
  final LinkedListMultimap<String, ParsedValue<?, ?>> entries;

  // Values
  final LinkedListMultimap<String, ParsedValue<?, Paint>> paints;

  Pattern classPattern = Pattern.compile("\\.[a-zA-Z][A-Za-z0-9-_]*");
  Pattern idPattern = Pattern.compile("#[a-zA-Z][A-Za-z0-9-_]*");
  Pattern statePattern = Pattern.compile(":[a-zA-Z][A-Za-z0-9-_]*");
  Pattern javaPattern = Pattern.compile("([^.#:A-Za-z0-9-_]|(?<START>^))[a-zA-Z][A-Za-z0-9-_]*");

  public CSSContext() {
    selectors = LinkedHashMultiset.create();
    classes = LinkedHashMultiset.create();
    javaClasses = LinkedHashMultiset.create();
    ids = LinkedHashMultiset.create();
    states = LinkedHashMultiset.create();
    entries = LinkedListMultimap.create();
    paints = LinkedListMultimap.create();
  }

  public CSSContext(String text) {
    this();
    load(text);
  }

  public CSSContext(CSSContext context) {
    this();
    add(context);
  }

  public void add(CSSContext cr) {
    this.selectors.addAll(cr.selectors);
    this.classes.addAll(cr.classes);
    this.javaClasses.addAll(cr.javaClasses);
    this.ids.addAll(cr.ids);
    this.states.addAll(cr.states);
    this.entries.putAll(cr.entries);
    this.paints.putAll(cr.paints);
  }

  public void remove(CSSContext cr) {
    this.selectors.removeAll(cr.selectors);
    this.classes.removeAll(cr.classes);
    this.javaClasses.removeAll(cr.javaClasses);
    this.ids.removeAll(cr.ids);
    this.states.removeAll(cr.states);
    cr.entries.entries().forEach(e -> this.entries.remove(e.getKey(), e.getValue()));
    cr.paints.entries().forEach(e -> this.paints.remove(e.getKey(), e.getValue()));
  }

  public void load(URL url) throws IOException {
    load(new CSSParser().parse(url));
  }

  public void load(String styleText) {
    load(new CSSParser().parse(styleText));
  }

  private void load(Stylesheet css) {
    loadSelector(css);
    loadDeclaration(css);
    resolveRoot(css);
  }

  private void loadSelector(Stylesheet css) {
    Observable.just(css)
        .flatMap(s -> Observable.fromIterable(s.getRules()))
        .flatMap(r -> Observable.fromIterable(r.getSelectors()))
        .map(Selector::toString)
        .map(s -> s.replace("*", ""))
        .subscribe(s -> {
          selectors.add(s);
          classes.addAll(loadClass(s));
          ids.addAll(loadId(s));
          states.addAll(loadState(s));
          javaClasses.addAll(loadJavaClass(s));
        });
  }

  private void loadDeclaration(Stylesheet css) {
    css.getRules().stream()
        .flatMap(r -> r.getDeclarations().stream())
        .forEach(d -> entries.put(d.getProperty(), d.getParsedValue()));
  }

  private void resolveRoot(Stylesheet css) {
    css.getRules().stream()
        .filter(r -> r.getSelectors().stream()
            .map(Selector::toString)
            .map(String::trim)
            .filter(s -> s.equals(ROOT))
            .findAny()
            .isPresent())
        .flatMap(r -> r.getDeclarations().stream())
        .forEach(d -> {
          ParsedValue<?, ?> pv = d.getParsedValue();
          ParsedValue<?, ?> resolve = resolve(pv);
          if (classifyValue(resolve.getValue(), d.getProperty(), d.getParsedValue()) == false) {
            classifyValue(resolve.convert(FONT), d.getProperty(), new FunctionParsedValue<>(() -> resolve(pv).convert(FONT)));
          }
        });
  }

  @SuppressWarnings("unchecked")
  private boolean classifyValue(Object classObject, String key, ParsedValue<?, ?> value) {
    if (classObject instanceof Paint) {
      paints.put(key, (ParsedValue<?, Paint>) value);
      return true;
    }
    return false;
  }

  private List<String> loadClass(String selector) {
    List<String> ids = new ArrayList<>();
    Matcher matcher = classPattern.matcher(selector);
    while (matcher.find()) {
      ids.add(selector.substring(matcher.start() + 1, matcher.end()));
    }
    return ids;
  }

  private List<String> loadJavaClass(String selector) {
    List<String> javaClasses = new ArrayList<>();
    Matcher matcher = javaPattern.matcher(selector);
    while (matcher.find()) {
      javaClasses.add(selector.substring(matcher.start() + (matcher.group("START") == null ? 1 : 0), matcher.end()));
    }
    return javaClasses;
  }

  private List<String> loadId(String selector) {
    List<String> ids = new ArrayList<>();
    Matcher matcher = idPattern.matcher(selector);
    while (matcher.find()) {
      ids.add(selector.substring(matcher.start() + 1, matcher.end()));
    }
    return ids;
  }

  private List<String> loadState(String selector) {
    List<String> state = new ArrayList<>();
    Matcher matcher = statePattern.matcher(selector);
    while (matcher.find()) {
      state.add(selector.substring(matcher.start() + 1, matcher.end()));
    }
    return state;
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  public <V, T> ParsedValue<V, T> resolve(ParsedValue<V, T> pv) {
    try {
      if (pv instanceof ParsedValueImpl) {
        ParsedValueImpl<V, T> pvi = (ParsedValueImpl<V, T>) pv;
        if (pvi.isContainsLookups() == false) {
          return pv;
        } else if (pvi.isLookup()) {
          T lookup = lookup(pv.getValue());
          // System.out.printf("lookup %s find %s\n", pv.getValue(), lookup);
          return new SimpleParsedValue<>(lookup);
        } else {
          V value = pv.getValue();
          if (value instanceof ParsedValue) {
            return new ParsedValueImpl<>((V) resolve((ParsedValue<?, ?>) value), pv.getConverter());
          } else if (value instanceof ParsedValue[]) {
            ParsedValue[] originPvs = (ParsedValue[]) value;
            ParsedValue[] pvs = new ParsedValue[originPvs.length];
            for (int i = 0; i < pvs.length; i++) {
              pvs[i] = resolve(originPvs[i]);
            }
            return new ParsedValueImpl<>((V) pvs, pv.getConverter());
          }
        }
      }
    } catch (ClassCastException e) {
      return pv;
    }
    return pv;
  }

  public <T> T lookup(Object o) {
    return TaskUtil.firstSuccess(
        () -> _lookup(o),
        () -> this == MODENA ? null : MODENA._lookup(o)
        );
  }

  @SuppressWarnings("unchecked")
  private <T> T _lookup(Object o) {
    return TaskUtil.firstSuccess(
        () -> (T) ListUtil.lastGet(paints.get(o.toString().trim().toLowerCase()), 0).convert(FONT)
        );
  }

  public Multiset<String> getSelectors() {
    return selectors;
  }

  public Multiset<String> getClasses() {
    return classes;
  }

  public Multiset<String> getIds() {
    return ids;
  }

  public Multiset<String> getStates() {
    return states;
  }

  public Multiset<String> getKeys() {
    return entries.keys();
  }

  public Multimap<String, ParsedValue<?, ?>> getEntries() {
    return entries;
  }

  public Multiset<String> getJavaClasses() {
    return javaClasses;
  }

  public LinkedListMultimap<String, ParsedValue<?, Paint>> getPaints() {
    return paints;
  }
}
