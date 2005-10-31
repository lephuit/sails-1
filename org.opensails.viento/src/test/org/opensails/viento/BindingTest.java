package org.opensails.viento;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

public class BindingTest extends TestCase {
	Binding binding = new Binding();
	ShamObject target = new ShamObject();

	public void testCall_Simple() throws Exception {
		try {
			binding.call("key");
			fail("Should throw exception");
		} catch (ResolutionFailedException e) {
		}
		
		binding.put("key", target);
		assertSame(target, binding.call("key"));
		
		assertEquals("one", binding.call(target, "one"));
	}
	
	public void testCall_SuperClassMethod() throws Exception {
		assertEquals("one", binding.call(new ShamSubclass(), "one"));
	}
	
	public void testCall_Arguments() throws Exception {
		assertEquals("two", binding.call(target, "two", new Object[] {new HashSet()}));
		assertEquals("three", binding.call(target, "three", new Object[] {new Integer(3)}));
		
		assertEquals("two", binding.call(target, "two", new Object[] {null}));
		try {
			assertEquals("three", binding.call(target, "three", new Object[] {null}));
			fail("Cannot pass null to a primitive type.");
		} catch (ResolutionFailedException expected) {
		}
	}
	
	public void testTopLevelMixin() throws Exception {
		binding.mixin(target);
		assertEquals("one", binding.call("one"));
		assertEquals("three", binding.call("three", new Object[] {new Integer(3)}));
	}
	
	public void testTypeMixin() throws Exception {
		binding.mixin(String.class, target);
		assertEquals("mixin", binding.call("string", "mixin"));
		assertEquals("str", binding.call("string", "sansSuffix", new Object[] {"ing"}));
		
		binding.mixin(Set.class, target);
		assertEquals("two", binding.call(new HashSet(), "two"));
	}
	
	public void testTypeMixin_Object() throws Exception {
		binding.mixin(Object.class, target);
		assertEquals("mixin", binding.call("string", "mixin"));
	}
	
	public void testTypeMixin_Parent() throws Exception {
		binding.mixin(String.class, target);
		Binding child = new Binding(binding);
		assertEquals("mixin", child.call("string", "mixin"));
	}
	
	public void testCustomName() throws Exception {
		binding.mixin(target);
		assertEquals("$", binding.call("$"));
	}
	
	public void testBeans() throws Exception {
		assertEquals("property", binding.call(target, "property"));
	}
	
	public void testParent() throws Exception {
		binding.setExceptionHandler(new ExceptionHandler() {
			public Object resolutionFailed(String methodName, Object[] args, List<Throwable> failedAttempts) {
				return "here";
			}

			public Object resolutionFailed(Object target, String methodName, Object[] args, List<Throwable> failedAttempts) {
				return "here";
			}
		});
		Binding child = new Binding(binding);
		binding.put("one", new ShamObject());
		assertNotNull(child.call("one"));
		
		child.put("one", "overrides");
		assertEquals("overrides", child.call("one"));
		
		assertEquals("here", child.call("notHere"));
	}
	
	public void testFailedAttempts() throws Exception {
		final List<Throwable> failedAttempts = new ArrayList<Throwable>();
		binding.setExceptionHandler(new ExceptionHandler() {
			public Object resolutionFailed(String methodName, Object[] args, List<Throwable> failedAttempts2) {
				failedAttempts.addAll(failedAttempts2);
				return null;
			}

			public Object resolutionFailed(Object target, String methodName, Object[] args, List<Throwable> failedAttempts2) {
				failedAttempts.addAll(failedAttempts2);
				return null;
			}
		});
		
		binding.call(target, "exception");
		assertEquals(1, failedAttempts.size());
		assertEquals("here", failedAttempts.get(0).getMessage());

		failedAttempts.clear();
		binding.mixin(ShamObject.class, target);
		binding.call(target, "exception");
		assertEquals(2, failedAttempts.size());
		
		failedAttempts.clear();
		binding.mixin(target);
		binding.call("exception");
		assertEquals("Becuase you have a type mixin on the top level mixin", 2, failedAttempts.size());
		
		failedAttempts.clear();
		Binding child = new Binding(binding);
		child.call(target, "exception");
		assertEquals(2, failedAttempts.size());

		failedAttempts.clear();
		child.call("exception");
		assertEquals(2, failedAttempts.size());
	}
	
	public void testMethodMissing() throws Exception {
		assertEquals("methodMissing", binding.call(new ShamMethodMissing(), "method"));
		assertEquals("methodMissing", binding.call(new ShamMethodMissingNoInterface(), "method"));
	}
	
	public void testPolymorphism() throws Exception {
		assertEquals("object", binding.call(target, "polymorphism", new Object[] {new Object()}));
		assertEquals("string", binding.call(target, "polymorphism", new Object[] {new String()}));
	}
	
	class ShamMethodMissing implements MethodMissing {
		public Object methodMissing(String methodName, Object[] args) {
			return "methodMissing";
		}
	}
	
	class ShamMethodMissingNoInterface {
	    public Object methodMissing(String methodName, Object[] args) {
	        return "methodMissing";
	    }
	}
	
	class ShamObject {
		public String one() {
			return "one";
		}
		
		public String two(Set set) {
			return "two";
		}
		
		public String three(int i) {
			return "three";
		}
		
		@Name("$")
		public String dollar() {
			return "$";
		}
		
		public String mixin(String target) {
			return "mixin";
		}
		
		public String sansSuffix(String target, String suffix) {
			if (target.endsWith(suffix))
				return target.substring(0, target.length() - suffix.length());
			return target;
		}
		
		public String getProperty() {
			return "property";
		}
		
		public String exception() {
			throw new RuntimeException("here");
		}
		
		public String exception(Object target) {
			throw new RuntimeException("here");
		}
		
		public String polymorphism(String string) {
			return "string";
		}
		
		public String polymorphism(Object object) {
			return "object";
		}
	}
	
	class ShamSubclass extends ShamObject {
	}
}