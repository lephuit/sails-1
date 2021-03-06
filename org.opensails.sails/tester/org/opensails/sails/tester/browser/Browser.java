package org.opensails.sails.tester.browser;

import org.apache.commons.lang.ArrayUtils;
import org.opensails.sails.ApplicationScope;
import org.opensails.sails.Sails;
import org.opensails.sails.adapter.AdaptationTarget;
import org.opensails.sails.adapter.ContainerAdapterResolver;
import org.opensails.sails.adapter.IAdapter;
import org.opensails.sails.adapter.IAdapterResolver;
import org.opensails.sails.configurator.IEventConfigurator;
import org.opensails.sails.event.IEventProcessingContext;
import org.opensails.sails.event.ISailsEvent;
import org.opensails.sails.event.oem.GetEvent;
import org.opensails.sails.event.oem.PostEvent;
import org.opensails.sails.form.FormFields;
import org.opensails.sails.oem.Dispatcher;
import org.opensails.sails.template.viento.VientoTemplateRenderer;
import org.opensails.sails.tester.Page;
import org.opensails.sails.tester.TesterApplicationContainer;
import org.opensails.sails.tester.oem.TestingHttpServletResponse;
import org.opensails.sails.tester.oem.VirtualResourceResolver;
import org.opensails.sails.tester.servletapi.ShamHttpServletRequest;
import org.opensails.sails.tester.servletapi.ShamHttpSession;
import org.opensails.sails.url.ActionUrl;

/**
 * Simulates a browser connected to a Sails application. These are obtained from
 * a SailsTestApplication.
 * <p>
 * It is intended that a Browser be obtained in one of two ways:
 * <ul>
 * <li> If you are writing lots of tests against one application, and only care
 * to have one Browser (most likely), then it is probably best to create a
 * subclass of this that initializes itself by creating it's application. For
 * example:
 * <p>
 * <code><pre>
 * public class MyApplicationBrowser extends Browser {
 * 	public MyApplicationBrowser() {
 * 		super(new SailsTestApplication(MyApplicationConfigurator.class));
 * 	}
 * }
 * </pre></code>
 * </p>
 * </li>
 * <li> Create an instance of SailsTestApplication, then tell it to
 * {@link SailsTestApplication#openBrowser()} </li>
 * </ul>
 * 
 * @author aiwilliams
 */
public class Browser {
	/**
	 * When set to true, the next request created will be marked as multipart.
	 * 
	 * @see ShamFormFields#multipart()
	 */
	public boolean nextRequestIsMultipart;
	protected Dispatcher eventDispatcher;
	protected TesterRequestContainer requestContainer;
	protected ShamHttpSession session;
	protected Class<? extends IEventProcessingContext> workingContext;
	protected SailsTestApplication application;
	protected boolean cookiesEnabled = true;

	protected Browser() {}

	protected Browser(SailsTestApplication application) {
		initialize(application);
	}

	/**
	 * @param action
	 * @return an ActionUrl pointing the specified action of the
	 *         {@link #workingContext()}
	 */
	public ActionUrl actionUrl(String action) {
		TesterGetEvent event = createGetEvent(workingContext(), action);
		return new ActionUrl(event, action);
	}

	public TesterPostEvent createPost(String action, FormFields formFields, Object... parameters) {
		return createPost(workingContext(), action, formFields, parameters);
	}

	public TesterPostEvent createPost(String context, String action, FormFields formFields, Object... parameters) {
		return createPostEvent(context, action, formFields, adaptParameters(parameters));
	}

	/**
	 * Creates an event and establishes the action view for it.
	 * 
	 * @see #registerTemplate(String, CharSequence)
	 * @param eventPath this must be in the form of 'controller/action'
	 * @param templateContent the content of the action view
	 * @return a TesterGetEvent that is configured by the ISailsEventConfigurator
	 *         and has the given eventPath
	 */
	public TesterGetEvent createVirtualEvent(String eventPath, CharSequence templateContent) {
		TesterGetEvent event = createGetEvent(eventPath);
		getContainer().instance(IEventConfigurator.class).configure(event, event.getContainer());
		registerTemplate(eventPath, templateContent);
		return event;
	}

	public void disableCookies() {
		cookiesEnabled = false;
	}

	/**
	 * Performs an HTTP GET request
	 * 
	 * @return the default page of the workingController, if set, otherwise the
	 *         default controller (Home, though that is left up to the
	 *         application).
	 */
	public Page get() {
		return get("index");
	}

	/**
	 * Performs an HTTP GET request
	 * 
	 * @param context becomes the working context
	 * @return the default page for the given controller
	 */
	public Page get(Class<? extends IEventProcessingContext> context) {
		this.workingContext = context;
		return get();
	}

	/**
	 * Performs an HTTP GET request
	 * 
	 * @param context becomes the working context
	 * @param action
	 * @param parameters
	 * @return the page for the given context/action
	 */
	public Page get(Class<? extends IEventProcessingContext> context, String action, Object... parameters) {
		this.workingContext = context;
		return get(action, parameters);
	}

	public Page get(GetEvent event) {
		eventDispatcher.dispatch(event);
		prepareForNextRequest();
		return createPage(event);
	}

	public Page get(String action) {
		return get(workingContext(), action, ArrayUtils.EMPTY_OBJECT_ARRAY);
	}

	/**
	 * Performs an HTTP GET request
	 * <p>
	 * Sails supports a form of namespacing for IEventProcessingContext. If no
	 * namespace is declared, the controller namespace is assumed.
	 * 
	 * @see Sails#eventContextName(Class)
	 * @param action on current working context
	 * @param parameters
	 * @return the page for &lt;workingContext&gt;/action
	 */
	public Page get(String action, Object... parameters) {
		return get(workingContext(), action, parameters);
	}

	/**
	 * Performs an HTTP GET request
	 * <p>
	 * This is the 'fundamental' get method. It will not alter the working
	 * context. The other get methods, which take an
	 * {@link IEventProcessingContext} class, are what should be used unless
	 * there is no context class for the action you would like to get.
	 * 
	 * @param context the context identifier
	 * @param action
	 * @param parameters
	 * @return the page for the given context/action
	 */
	public Page get(String context, String action, Object... parameters) {
		TesterGetEvent event = createGetEvent(context, action, adaptParameters(parameters));
		return get(event);
	}

	/**
	 * @return the application under test
	 */
	public SailsTestApplication getApplication() {
		return application;
	}

	/**
	 * @return the container of the application
	 */
	public TesterApplicationContainer getApplicationContainer() {
		return application.getContainer();
	}

	/**
	 * @return the RequestContainer that will be used for the next event
	 */
	public TesterRequestContainer getContainer() {
		return requestContainer;
	}

	/**
	 * Provides FormFields with methods that help you set up for a request.
	 * <p>
	 * You should get a new one of these for each request you expect to post.
	 * 
	 * @return a new instance of ShamFormFields
	 */
	public ShamFormFields getFormFields() {
		return new ShamFormFields(this, adapterResolver());
	}

	public TesterSession getSession() {
		return new TesterSession(this);
	}

	/**
	 * @return the current session. If create, creates one. If not, returns
	 *         null.
	 */
	public TesterSession getSession(boolean create) {
		return new TesterSession(this, true);
	}

	/**
	 * Performs a get and renders the provided templateContent.
	 * 
	 * @param templateContent
	 * @return the rendered page
	 */
	public Page getTemplated(CharSequence templateContent) {
		return get(createVirtualEvent("dynamicallyGeneratedInSailsTester/getTemplated", templateContent));
	}

	/**
	 * Performs a get on the specified controllerAction and renders the provided
	 * templateContent.
	 * 
	 * @param templateContent
	 * @return the rendered page
	 */
	public Page getTemplated(String controllerAction, CharSequence templateContent) {
		return get(createVirtualEvent(controllerAction, templateContent));
	}

	@SuppressWarnings("unchecked")
	public <T> void inject(Class<? super T> keyAndImplementation) {
		inject(keyAndImplementation, (Class<T>) keyAndImplementation, ApplicationScope.REQUEST);
	}

	public <T> void inject(Class<? super T> key, Class<T> implementation) {
		inject(key, implementation, ApplicationScope.REQUEST);
	}

	public <T> void inject(Class<? super T> key, Class<T> implementation, ApplicationScope scope) {
		getContainer().getContainerInHierarchy(scope).inject(key, implementation);
	}

	public <T> void inject(Class<? super T> key, T instance) {
		inject(key, instance, ApplicationScope.REQUEST);
	}

	public <T> void inject(Class<? super T> key, T instance, ApplicationScope scope) {
		getContainer().getContainerInHierarchy(scope).inject(key, instance);
	}

	/**
	 * @param <T>
	 * @param key
	 * @return the instance registered in the container for key
	 */
	public <T> T instance(Class<T> key) {
		return getContainer().instance(key);
	}

	/**
	 * Invalidates the current HttpSession. Any references to the old
	 * HttpSession are not managed.
	 */
	public void invalidateSession() {
		session.invalidate();
		session = null;
	}

	public Page post(Class<? extends IEventProcessingContext> context, FormFields formFields, Object... parameters) {
		return post(Sails.eventContextName(context), formFields, parameters);
	}

	public Page post(Class<? extends IEventProcessingContext> context, String action, FormFields formFields, Object... actionParameters) {
		return post(Sails.eventContextName(context), action, formFields, actionParameters);
	}

	/**
	 * Performs an HTTP POST request
	 * 
	 * @return the index page of the current working controller
	 */
	public Page post(FormFields formFields, Object... parameters) {
		return post("index", formFields, parameters);
	}

	public Page post(String action, FormFields formFields, Object... parameters) {
		return post(workingContext(), action, formFields, parameters);
	}

	/**
	 * Performs an HTTP POST request
	 * <p>
	 * This is the 'fundamental' post method. It will not alter the working
	 * context. The other post methods, which take an
	 * {@link IEventProcessingContext} class, are what should be used unless
	 * there is no context class for the action you would like to get.
	 * 
	 * @param context the context identifier
	 * @param action
	 * @param parameters
	 * @return the page for the given context/action
	 */
	public Page post(String context, String action, FormFields formFields, Object... parameters) {
		TesterPostEvent postEvent = createPost(context, action, formFields, parameters);
		return post(postEvent);
	}

	/**
	 * Post the event.
	 * 
	 * @param event
	 * @return the Page representing the result of posting the event
	 * @throws NullPointerException if the event is null
	 * @throws IllegalArgumentException if the event did not originate from the
	 *         application of this
	 */
	public Page post(TesterPostEvent event) {
		if (event == null) throw new NullPointerException("You cannot post a null event");
		if (event.getApplication() != this.application) throw new IllegalArgumentException("Cannot post events that aren't bound for the application of this browser");
		eventDispatcher.dispatch(event);
		prepareForNextRequest();
		return createPage(event);
	}

	/**
	 * If you would like to have an action view, but not have a controller
	 * directory with templates, you can use this to establish the environment
	 * necessary to make this application think that the action view exists.
	 * 
	 * @param controllerAction this must be in the form of 'controller/action'
	 * @param templateContent the content of the action view
	 */
	public void registerTemplate(String controllerAction, CharSequence templateContent) {
		VirtualResourceResolver resourceResolver = getContainer().instance(VirtualResourceResolver.class);
		resourceResolver.register(controllerAction + VientoTemplateRenderer.TEMPLATE_IDENTIFIER_EXTENSION, templateContent);
	}

	/**
	 * A super-handy way to render arbitrary templates with no action code.
	 * 
	 * @param templateContent
	 * @return the resulting Page
	 */
	public Page render(String templateContent) {
		return get(createVirtualEvent("browser/render", templateContent));
	}

	/**
	 * Allows placement of <em>non-null</em> object into HttpSession.
	 * 
	 * @param value full name of getClass is used as attribute name
	 * @return the existing value replaced by new value
	 * @throws NullPointerException if value is null
	 */
	public Object setSessionAttribute(Object value) {
		return setSessionAttribute(value.getClass().getName(), value);
	}

	/**
	 * Causes an HttpSession to be created if it doesn't already exist and sets
	 * the provided attribute.
	 * 
	 * @param name
	 * @param value
	 * @return the existing value replaced by new value
	 */
	public Object setSessionAttribute(String name, Object value) {
		TesterSession s = getSession(true);
		Object existing = s.getAttribute(name);
		s.setAttribute(name, value);
		return existing;
	}

	public void setWorkingContext(Class<? extends IEventProcessingContext> context) {
		this.workingContext = context;
	}

	/**
	 * This is used by the Browser to specify the controller/component name that
	 * should be used when one is not supplied in methods like
	 * {@link #get(GetEvent)} and {@link #post(PostEvent)}.
	 * 
	 * @return the working context name (controller/component)
	 * @see #setWorkingContext(Class)
	 */
	/*
	 * TODO Rename to getWorkingContext and make support for Strings on
	 * setWorkingContext
	 */
	public String workingContext() {
		return workingContext != null ? Sails.eventContextName(workingContext) : "home";
	}

	protected ContainerAdapterResolver adapterResolver() {
		return new ContainerAdapterResolver(getContainer().instance(IAdapterResolver.class), requestContainer);
	}

	protected String[] adaptParameters(Object... parameters) {
		return adaptParameters(parameters, adapterResolver());
	}

	@SuppressWarnings("unchecked")
	protected String[] adaptParameters(Object[] parameters, ContainerAdapterResolver resolver) {
		if (parameters != null && parameters.length > 0) {
			String[] params = new String[parameters.length];
			for (int i = 0; i < parameters.length; i++) {
				Object object = parameters[i];
				IAdapter adapter = resolver.resolve(object.getClass());
				params[i] = String.valueOf(adapter.forWeb(new AdaptationTarget<Object>((Class<Object>) object.getClass()), object));
			}
			return params;
		}
		return ArrayUtils.EMPTY_STRING_ARRAY;
	}

	/**
	 * @param pathInfo a 'raw' HttpServletRequest pathInfo
	 * @return a GET request event, all wired up to the application
	 */
	protected TesterGetEvent createGetEvent(String pathInfo) {
		ShamHttpServletRequest request = createRequest();
		request.setPathInfo(pathInfo);
		TestingHttpServletResponse response = createResponse();
		TesterGetEvent event = new TesterGetEvent(application, requestContainer, request, response);
		requestContainer.bind(event);
		response.set(event);
		return event;
	}

	protected TesterGetEvent createGetEvent(String context, String action, String... parameters) {
		return createGetEvent(toPathInfo(context, action, parameters));
	}

	protected Page createPage(ISailsEvent event) {
		return new Page(event);
	}

	protected TesterPostEvent createPostEvent(String pathInfo, FormFields formFields) {
		ShamHttpServletRequest request = createRequest();
		request.setPathInfo(pathInfo);
		request.setParameters(formFields.toMap());
		TestingHttpServletResponse response = createResponse();
		TesterPostEvent event = new TesterPostEvent(application, requestContainer, request, response);
		requestContainer.bind(event);
		response.set(event);
		return event;
	}

	protected TesterPostEvent createPostEvent(String context, String action, FormFields formFields, String... parameters) {
		return createPostEvent(toPathInfo(context, action, parameters), formFields);
	}

	/**
	 * @return a request that is bound to this browser such that when a session
	 *         is created, we have it
	 */
	protected ShamHttpServletRequest createRequest() {
		ShamHttpServletRequest request = new ShamHttpServletRequest(Browser.this.session) {
			@Override
			public javax.servlet.http.HttpSession getSession() {
				return Browser.this.session = (ShamHttpSession) super.getSession();
			};

			@Override
			public javax.servlet.http.HttpSession getSession(boolean create) {
				return Browser.this.session = (ShamHttpSession) super.getSession(create);
			};
		};
		request.multipart = nextRequestIsMultipart;
		nextRequestIsMultipart = false;
		return request;
	}

	protected TestingHttpServletResponse createResponse() {
		TestingHttpServletResponse response = new TestingHttpServletResponse() {
			@Override
			protected boolean isCookiesSupported() {
				return cookiesEnabled & cookiesSupported;
			}
		};
		return response;
	}

	protected Page get(TesterGetEvent event) {
		event.getContainer().registerAll(getContainer());
		return get((GetEvent) event);
	}

	protected ShamHttpSession getHttpSession(boolean create) {
		if (session == null && create) session = new ShamHttpSession();
		return session;
	}

	protected void initialize(SailsTestApplication application) {
		this.application = application;
		this.eventDispatcher = application.getDispatcher();
		prepareForNextRequest();
	}

	// TODO: Bind to lazy created session container
	protected void prepareForNextRequest() {
		if (requestContainer != null) requestContainer = new TesterRequestContainer(application.getContainer(), requestContainer.injections);
		else requestContainer = new TesterRequestContainer(application.getContainer());
	}

	protected String toParametersString(String... parameters) {
		StringBuilder string = new StringBuilder();
		for (String param : parameters) {
			string.append("/");
			string.append(param);
		}
		return string.toString();
	}

	/**
	 * @param context
	 * @param action
	 * @param parameters
	 * @return
	 */
	protected String toPathInfo(String context, String action, String... parameters) {
		StringBuilder pathInfo = new StringBuilder();
		pathInfo.append(context);
		pathInfo.append("/");
		pathInfo.append(action);
		pathInfo.append(toParametersString(parameters));
		return pathInfo.toString();
	}
}
