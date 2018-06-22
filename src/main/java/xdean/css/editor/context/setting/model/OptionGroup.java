package xdean.css.editor.context.setting.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import xdean.jex.extra.collection.Either;
import xdean.jex.util.cache.CacheUtil;

public class OptionGroup {

  List<Either<Option<?>, OptionGroup>> list;
  String key;

  public OptionGroup(String key) {
    this.key = key;
    list = new ArrayList<>();
  }

  public <T extends Option<?>> T add(T o) {
    list.add(Either.left(o));
    return o;
  }

  public OptionGroup add(OptionGroup og) {
    list.add(Either.right(og));
    return og;
  }

  public List<Either<Option<?>, OptionGroup>> getChildren() {
    return CacheUtil.cache(this, () -> Collections.unmodifiableList(list));
  }

  @SuppressWarnings("unchecked")
  public <T> List<Option<T>> getChildrenWithValueType(Class<T> clz) {
    return list.stream()
        .filter(e -> e.isLeft())
        .map(e -> e.getLeft())
        .filter(o -> clz.isInstance(o.getValue()))
        .map(o -> (Option<T>) o)
        .collect(Collectors.toList());
  }

  @SuppressWarnings("unchecked")
  public <T extends Option<?>> List<T> getChildrenWithType(Class<T> clz) {
    return list.stream()
        .filter(e -> e.isLeft())
        .map(e -> e.getLeft())
        .filter(o -> clz.isInstance(o))
        .map(o -> (T) o)
        .collect(Collectors.toList());
  }

  public String getKey() {
    return key;
  }
}
