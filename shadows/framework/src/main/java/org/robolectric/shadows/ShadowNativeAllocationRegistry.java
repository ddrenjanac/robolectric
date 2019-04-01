package org.robolectric.shadows;


import static android.os.Build.VERSION_CODES.N;
import static org.robolectric.shadow.api.Shadow.directlyOn;

import libcore.util.NativeAllocationRegistry;
import libcore.util.NativeAllocationRegistry.Allocator;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;


@Implements(value = NativeAllocationRegistry.class, minSdk = N, isInAndroidSdk = false, looseSignatures = true)
public class ShadowNativeAllocationRegistry {

  @RealObject
  protected NativeAllocationRegistry realNativeAllocationRegistry;

  private static boolean hasNativeImplementation(Class<?> clazz) {
    try {
      boolean nativesRegistered = clazz
          .getDeclaredField("$$robo_native_registered_flag$$").getBoolean(null);
      return nativesRegistered;
    } catch (NoSuchFieldException ignored) {
      //Probably not instrumented
    } catch (IllegalAccessException e) {
      throw new RuntimeException("ROBO_NATIVE_FLAG should be public", e);
    }
    return false;
  }

  @Implementation
  protected Runnable registerNativeAllocation(Object referent, Object allocator) {
    if (hasNativeImplementation(referent.getClass())) {
      return directlyOn(realNativeAllocationRegistry, NativeAllocationRegistry.class).
          registerNativeAllocation(referent, (Allocator) allocator);
    }
    return () -> {};
  }


  @Implementation
  public Runnable registerNativeAllocation(Object referent, long nativePtr) {
    if (hasNativeImplementation(referent.getClass())) {
      return directlyOn(realNativeAllocationRegistry, NativeAllocationRegistry.class).
          registerNativeAllocation(referent, nativePtr);
    }
    return () -> {};
  }
}
