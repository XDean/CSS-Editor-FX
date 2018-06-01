package xdean.css.editor.util;

import java.util.Iterator;

import xdean.jex.extra.collection.IntList;
import xdean.jex.util.task.TaskUtil;

public class IntSequence implements Iterator<Integer> {
  final IntList useSet, releaseSet;
  final int min;
  transient int current;

  public IntSequence(int start) {
    this.useSet = IntList.create();
    this.releaseSet = IntList.create();
    this.min = start;
    this.current = start - 1;
  }

  @Override
  public boolean hasNext() {
    return true;
  }

  @Override
  public synchronized Integer next() {
    if (releaseSet.isEmpty()) {
      while (useSet.remove(++current)) {
        ;
      }
      return current;
    } else {
      return TaskUtil.andFinal(() -> releaseSet.stream().min().getAsInt(), i -> releaseSet.remove(i));
    }
  }

  public synchronized boolean release(int i) {
    if (i >= min && i <= current || useSet.remove(i)) {
      return releaseSet.add(i);
    }
    return false;
  }

  public synchronized boolean use(int i) {
    if (i > current || releaseSet.remove(i)) {
      return useSet.add(i);
    }
    return false;
  }
}
