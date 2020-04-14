package compiler.codegen.runtime;

import static java.lang.invoke.MethodHandles.catchException;
import static java.lang.invoke.MethodHandles.dropArguments;
import static java.lang.invoke.MethodType.methodType;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.invoke.MutableCallSite;

public class Bootstrapper {
//	public static CallSite bootstrap(MethodHandles.Lookup caller, String name, MethodType type) {
//		if(type.parameterCount() == 2 ) {
//			MethodHandle handle;
//			MethodType newtype = MethodType.methodType(Object.class, Object.class, Object.class);
//		}
//		
//		return null;
//	}
	  private static MethodHandle FALLBACK_2;
	static {
		try {
		Lookup lookup = MethodHandles.lookup();
		FALLBACK_2 = lookup.findStatic(
		          Bootstrapper.class,
		          "fallback_2",
		          methodType(Object.class, MonomorphicInlineCache.class, Object[].class));
		}
		catch (NoSuchMethodException | IllegalAccessException e) {
		     e.printStackTrace();
		    }
	}
	  static class MonomorphicInlineCache extends MutableCallSite {

		    final Lookup callerLookup;
		    final String name;
		    MethodHandle fallback;

		    MonomorphicInlineCache(Lookup callerLookup, String name, MethodType type) {
		      super(type);
		      this.callerLookup = callerLookup;
		      this.name = name;
		    }
		  }
	public static Object fallback_2(MonomorphicInlineCache inlineCache, Object[] args) throws Throwable {

	    Class<?> arg1Class = (args[0] == null) ? Object.class : args[0].getClass();
	    Class<?> arg2Class = (args[1] == null) ? Object.class : args[1].getClass();
	    MethodHandle target;

	    try {
	      target = inlineCache.callerLookup.findStatic(
	          Bootstrapper.class, inlineCache.name, methodType(Object.class, arg1Class, arg2Class));
	      System.out.println(arg1Class+" "+arg2Class);
	    } catch (Throwable t1) {
	      try {
	        target = inlineCache.callerLookup.findStatic(
	            Bootstrapper.class, inlineCache.name + "_fallback", methodType(Object.class, Object.class, Object.class));
	      } catch (Throwable t2) {
	        return null;
	      }
	    }

	    target = target.asType(methodType(Object.class, Object.class, Object.class));
	    if (arg1Class == String.class || arg2Class == String.class) {
	    //  MethodHandle guard = insertArguments(GUARD_2, 0, arg1Class, arg2Class);
	    //  target = guardWithTest(guard, target, inlineCache.fallback);
	    } else {
	      target = catchException(target, ClassCastException.class, dropArguments(inlineCache.fallback, 0, ClassCastException.class));
	    }
	    inlineCache.setTarget(target);

	    return target.invokeWithArguments(args);
	  }
	
	public static CallSite bootstrap(Lookup caller, String name, MethodType type, int arity) throws NoSuchMethodException, IllegalAccessException {
	

	    MonomorphicInlineCache callSite = new MonomorphicInlineCache(caller, name, type);
	    MethodHandle fallback;
	    if (arity == 2) {
	      fallback = FALLBACK_2;
	    } else {
	      fallback = null;
	    }

	    MethodHandle fallbackHandle = fallback
	        .bindTo(callSite)
	        .asCollector(Object[].class, type.parameterCount())
	        .asType(type);
	    callSite.fallback = fallbackHandle;
	    callSite.setTarget(fallbackHandle);
	    System.out.println(fallbackHandle);
	    return callSite;
	  }
	
	public static Object add(Integer a,Integer b) {
		return a+b;
	}
	
	public static Object add(Float a,Float b) {
		return a+b;
	}
}