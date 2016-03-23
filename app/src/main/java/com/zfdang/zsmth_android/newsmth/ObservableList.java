package com.zfdang.zsmth_android.newsmth;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by zfdang on 2016-3-23.
 */
public class ObservableList<T> {
    protected final List<T> list;
    protected final PublishSubject<T> onAdd;

    public ObservableList() {
        this.list = new ArrayList<T>();
        this.onAdd = PublishSubject.create();
    }
    public void add(T value) {
        list.add(value);
        onAdd.onNext(value);
    }

    public Observable<T> getObservable() {
        return onAdd;
    }
}
