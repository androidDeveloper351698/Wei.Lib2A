/*
 * Copyright (C) 2014 Wei Chou (weichou2010@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wei.c.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.wei.c.framework.AbsApp;

/**
 * @author 周伟 Wei Chou(weichou2010@gmail.com)
 */
public abstract class AbsBroadcastReceiver extends BroadcastReceiver {
	private CntObservable<?, ?> mObservable;
	private boolean mFirstCreate = true;

	protected static <O extends Observer, B extends AbsBroadcastReceiver> void registerObserver(O observer, Class<B> clazz) {
		Context context = AbsApp.get();
		B receiver = get(clazz, true);
		if(context != null && receiver != null) {
			CntObservable<O, ?> observable = receiver.getObservable();
			observable.registerObserver(observer);
			if(receiver.mFirstCreate) {
				context.registerReceiver(receiver, receiver.newFilter());
				receiver.mFirstCreate = false;
			}
		}
	}

	protected static <O extends Observer, B extends AbsBroadcastReceiver> void unregisterObserver(O observer, Class<B> clazz) {
		Context context = AbsApp.get();
		B receiver = get(clazz, false);
		if(context != null && receiver != null) {
			CntObservable<O, ?> observable = receiver.getObservable();
			observable.unregisterObserver(observer);
			if(observable.countObservers() == 0) {
				receiver.removeInstance();
				context.unregisterReceiver(receiver);
			}
		}
	}

	protected static <B extends AbsBroadcastReceiver> void unregisterAll(Class<B> clazz) {
		Context context = AbsApp.get();
		B receiver = get(clazz, false);
		if(context != null && receiver != null) {
			receiver.getObservable().unregisterAll();
			receiver.removeInstance();
			context.unregisterReceiver(receiver);
		}
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		getObservable().notifyChanged(context, intent);
	}

	private void removeInstance() {
		AbsApp app = AbsApp.get();
		if(app != null) app.removeSingleInstance(getClass());
	}

	@SuppressWarnings("unchecked")
	private <C extends CntObservable<?, ?>> C getObservable() {
		if(mObservable == null) mObservable = newObservable();
		return (C)mObservable;
	}

	private static <B extends AbsBroadcastReceiver> B get(Class<B> clazz, boolean create) {
		AbsApp app = AbsApp.get();
		B receiver = null;
		if(app != null) {
			receiver = app.getSingleInstance(clazz);
			if(receiver == null && create) {
				try {
					receiver = clazz.newInstance();
					app.cacheSingleInstance(receiver);
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
		return receiver;
	}

	protected abstract <C extends CntObservable<?, ?>> C newObservable();
	protected abstract IntentFilter newFilter();
}
