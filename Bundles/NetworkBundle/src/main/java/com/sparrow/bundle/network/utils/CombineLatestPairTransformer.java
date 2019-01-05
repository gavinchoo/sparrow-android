package com.sparrow.bundle.network.utils;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;

public final class CombineLatestPairTransformer<S, T> implements ObservableTransformer<S, Pair<S, T>> {
  @NonNull
  private final Observable<T> second;

  public CombineLatestPairTransformer(final @NonNull Observable<T> second) {
    this.second = second;
  }


  @Override
  public ObservableSource<Pair<S, T>> apply(Observable<S> upstream) {
    return Observable.combineLatest(upstream, this.second, Pair::new);
  }
}
