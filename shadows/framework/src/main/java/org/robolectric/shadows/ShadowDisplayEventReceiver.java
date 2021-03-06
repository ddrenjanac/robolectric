package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN;
import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.KITKAT_WATCH;
import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N_MR1;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;

import android.os.MessageQueue;
import android.view.DisplayEventReceiver;
import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.res.android.NativeObjRegistry;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

@Implements(
    className = "android.view.DisplayEventReceiver",
    isInAndroidSdk = false,
    looseSignatures = true)
public class ShadowDisplayEventReceiver {

  private static NativeObjRegistry<NativeDisplayEventReceiver> nativeObjRegistry =
      new NativeObjRegistry<>(NativeDisplayEventReceiver.class);

  protected @RealObject DisplayEventReceiver receiver;

  private static final long VSYNC_DELAY_MS = 1;

  @Implementation(minSdk = O)
  protected static long nativeInit(
      WeakReference<DisplayEventReceiver> receiver, MessageQueue msgQueue, int vsyncSource) {
    return nativeObjRegistry.register(new NativeDisplayEventReceiver(receiver));
  }

  @Implementation(minSdk = M, maxSdk = N_MR1)
  protected static long nativeInit(
      WeakReference<DisplayEventReceiver> receiver, MessageQueue msgQueue) {
    return nativeObjRegistry.register(new NativeDisplayEventReceiver(receiver));
  }

  @Implementation(minSdk = KITKAT_WATCH, maxSdk = LOLLIPOP_MR1)
  protected static long nativeInit(DisplayEventReceiver receiver, MessageQueue msgQueue) {
    return nativeObjRegistry.register(
        new NativeDisplayEventReceiver(new WeakReference<>(receiver)));
  }

  @Implementation(maxSdk = KITKAT)
  protected static int nativeInit(Object receiver, Object msgQueue) {
    return (int)
        nativeObjRegistry.register(
            new NativeDisplayEventReceiver(new WeakReference<>((DisplayEventReceiver) receiver)));
  }

  @Implementation(minSdk = KITKAT_WATCH)
  protected static void nativeDispose(long receiverPtr) {
    nativeObjRegistry.unregister(receiverPtr);
  }

  @Implementation(maxSdk = KITKAT)
  protected static void nativeDispose(int receiverPtr) {
    nativeObjRegistry.unregister(receiverPtr);
  }

  @Implementation(minSdk = KITKAT_WATCH)
  protected static void nativeScheduleVsync(long receiverPtr) {
    nativeObjRegistry.getNativeObject(receiverPtr).scheduleVsync();
  }

  @Implementation(maxSdk = KITKAT)
  protected static void nativeScheduleVsync(int receiverPtr) {
    nativeObjRegistry.getNativeObject(receiverPtr).scheduleVsync();
  }

  protected void onVsync() {
    if (RuntimeEnvironment.getApiLevel() <= JELLY_BEAN) {
      ReflectionHelpers.callInstanceMethod(
          DisplayEventReceiver.class,
          receiver,
          "onVsync",
          ClassParameter.from(long.class, ShadowSystem.nanoTime()),
          ClassParameter.from(int.class, 1));
    } else {
      ReflectionHelpers.callInstanceMethod(
          DisplayEventReceiver.class,
          receiver,
          "onVsync",
          ClassParameter.from(long.class, ShadowSystem.nanoTime()),
          ClassParameter.from(int.class, 0), /* SurfaceControl.BUILT_IN_DISPLAY_ID_MAIN */
          ClassParameter.from(int.class, 1)
      );
    }
  }

  private static class NativeDisplayEventReceiver {

    private final WeakReference<DisplayEventReceiver> receiverRef;

    public NativeDisplayEventReceiver(WeakReference<DisplayEventReceiver> receiverRef) {
      this.receiverRef = receiverRef;
    }

    public void scheduleVsync() {
      // simulate an immediate callback
      DisplayEventReceiver receiver = receiverRef.get();
      ShadowRealisticSystemClock.advanceBy(VSYNC_DELAY_MS, TimeUnit.MILLISECONDS);
      if (receiver != null) {
        ShadowDisplayEventReceiver shadowReceiver = Shadow.extract(receiver);
        shadowReceiver.onVsync();
      }
    }
  }
}
