package io.github.qi1002.ilearn;

/**
 * Created by QI on 2017/2/18.
 */
public interface IEnumerable {
    abstract public DatasetRecord getCurrent();
    abstract public DatasetRecord moveNext();
    abstract public void reset();
}

