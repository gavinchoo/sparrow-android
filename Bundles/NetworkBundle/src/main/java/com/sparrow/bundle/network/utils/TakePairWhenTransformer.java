package com.sparrow.bundle.network.utils;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;

public final class TakePairWhenTransformer<S, T> implements ObservableTransformer<S, Pair<S, T>> {
  @NonNull
  private final Observable<T> when;

  public TakePairWhenTransformer(final @NonNull Observable<T> when) {
    this.when = when;
  }

  @Override
  @NonNull
  public Observable<Pair<S, T>> apply(final @NonNull Observable<S> source) {
    return this.when.withLatestFrom(source, (x, y) -> new Pair<>(y, x));
  }
}
