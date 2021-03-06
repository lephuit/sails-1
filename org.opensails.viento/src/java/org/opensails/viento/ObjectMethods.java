package org.opensails.viento;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ObjectMethods implements IMethodResolver {

	public CallableMethod find(TargetedMethodKey key) {
		Method method = findAppropriateMethod(key.targetClass, key.methodName,
				key.argClasses);
		if (method != null)
			return new ObjectMethod(method);

		Field field = findField(key.targetClass, key.methodName);
		if (field != null && key.argClasses.length == 0)
			return new ObjectField(field);
		if (field != null && key.argClasses.length == 1
				&& field.getType().isAssignableFrom(key.argClasses[0]))
			return new FieldSetter(field);

		method = findMethodMissing(key.targetClass);
		if (method != null)
			return new MethodMissingMethod(method, key.methodName);
		return null;
	}

	protected Method findAppropriateMethod(Class<?> type, String methodName,
			Class[] args) {
		Method theMethod = null;
		Method[] methods = type.getMethods();
		for (Method method : methods)
			if (!method.isAnnotationPresent(Invisible.class)
					&& nameMatch(methodName, method)
					&& typesMatch(method.getParameterTypes(), args, theMethod))
				theMethod = method;
		if (type.getSuperclass() != null && theMethod == null)
			return findAppropriateMethod(type.getSuperclass(), methodName, args);
		return theMethod;
	}

	protected Field findField(Class<?> type, String methodName) {
		Field theField = null;
		Field[] fields = type.getFields();
		for (Field field : fields)
			if (!field.isAnnotationPresent(Invisible.class) && nameMatch(methodName, field))
				theField = field;
		if (type.getSuperclass() != null && theField == null)
			return findField(type.getSuperclass(), methodName);
		return theField;
	}
	
	protected Method findMethodMissing(Class<?> type) {
		Method found = null;
		while (found == null && type != Object.class)
			try {
				found = type.getDeclaredMethod("methodMissing", new Class[] {
						String.class, Object[].class });
			} catch (Exception e) {
				type = type.getSuperclass();
			}
		return found;
	}

	protected String getter(String methodName) {
		return "get" + Character.toUpperCase(methodName.charAt(0))
				+ methodName.substring(1);
	}
	
	protected boolean annotation(AnnotatedElement element, String methodName) {
		if (element.isAnnotationPresent(Name.class))
			for (String name : element.getAnnotation(Name.class).value())
				if (name.equals(methodName))
					return true;
		return false;
	}


	protected boolean nameMatch(String methodName, Field field) {
		return annotation(field, methodName) || field.getName().equals(methodName);
	}
	
	protected boolean nameMatch(String methodName, Method method) {
		return annotation(method, methodName) || method.getName().equals(methodName)
				|| method.getName().equals(getter(methodName))
				|| izzer(method, methodName);
	}

	protected boolean primitiveMatch(Class<?> type, Class arg) {
		return ((type == boolean.class && arg == Boolean.class)
				|| (type == char.class && arg == Character.class)
				|| (type == byte.class && arg == Byte.class)
				|| (type == short.class && arg == Short.class)
				|| (type == int.class && arg == Integer.class)
				|| (type == long.class && arg == Long.class)
				|| (type == float.class && arg == Float.class) || (type == double.class && arg == Double.class));
	}

	protected boolean typesMatch(Class<?> parameterType, Class arg) {
		if (arg == null)
			return !parameterType.isPrimitive();
		return parameterType.isAssignableFrom(arg)
				|| primitiveMatch(parameterType, arg) || enumMatch(parameterType, arg);
	}

	protected boolean enumMatch(Class<?> parameterType, Class<?> arg) {
		return parameterType.isEnum() && arg.isAssignableFrom(String.class);
	}

	protected boolean typesMatch(Class<?>[] parameterTypes, Class[] args,
			Method theMethod) {
		// Allow varargs when there's nothing else
		// No, there's no way to tell for sure if it's varargs.
		if (ReflectionHelper.isOnlyOneArray(parameterTypes))
			return true;
		if (parameterTypes.length != args.length)
			return false;
		for (int i = 0; i < parameterTypes.length; i++)
			if (!typesMatch(parameterTypes[i], args[i])
					|| (theMethod != null && parameterTypes[i]
							.isAssignableFrom(theMethod.getParameterTypes()[i])))
				return false;
		return true;
	}

	// You know, isProperty()
	private boolean izzer(Method method, String methodName) {
		return method.getReturnType() == boolean.class
				&& method.getName().equals(
						"is" + Character.toUpperCase(methodName.charAt(0))
								+ methodName.substring(1));
	}

	public class FieldSetter implements CallableMethod {
		private final Field field;

		public FieldSetter(Field field) {
			this.field = field;
		}

		public Object call(Object target, Object[] args) {
			try {
				field.set(target, args[0]);
				return target;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	public class MethodMissingMethod extends ObjectMethod {
		protected final String methodName;

		public MethodMissingMethod(Method method, String methodName) {
			super(method);
			this.methodName = methodName;
		}

		@Override
		public Object call(Object target, Object[] args) {
			return super.call(target, new Object[] { methodName, args });
		}
	}

	public class ObjectField implements CallableMethod {
		private final Field field;

		public ObjectField(Field field) {
			this.field = field;
		}

		public Object call(Object target, Object[] args) {
			try {
				return field.get(target);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
}
