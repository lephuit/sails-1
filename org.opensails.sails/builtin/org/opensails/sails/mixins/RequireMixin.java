package org.opensails.sails.mixins;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashSet;
import java.util.Set;

import org.opensails.sails.IResourceResolver;
import org.opensails.sails.ISailsEvent;
import org.opensails.sails.SailsException;
import org.opensails.sails.url.ExternalUrl;
import org.opensails.sails.url.IUrl;
import org.opensails.sails.url.UrlType;

// TODO: Get rid of 'script' and 'style' html duplication
public class RequireMixin {
	protected ISailsEvent event;
	protected IResourceResolver loader;
	protected Set<Requirement> requirements;

	public RequireMixin(ISailsEvent event, IResourceResolver loader) {
		this.event = event;
		this.loader = loader;
		this.requirements = new LinkedHashSet<Requirement>();
	}

	/**
	 * @param identifier
	 */
	public void component(String identifier) {
		String descriptor = "component/" + identifier + "/.component";
		InputStream stream = loader.resolve(descriptor);
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

		try {
			String line = null;
			while ((line = reader.readLine()) != null)
				requirements.add(new ComponentRequirement(identifier, line.trim()));
		} catch (Exception e) {
			throw new SailsException("Could not locate component descriptor " + descriptor, e);
		}
	}

	public String output() {
		StringBuilder builder = new StringBuilder();
		for (Requirement requirement : requirements)
			builder.append(requirement);
		return builder.toString();
	}

	public void script(String identifier) {
		requirements.add(new Script(identifier));
	}

	public void style(String identifier) {
		requirements.add(new Style(identifier));
	}

	@Override
	public String toString() {
		return output();
	}

	class ComponentRequirement extends Requirement {
		private final String componentName;
		private final String line;

		ComponentRequirement(String componentName, String line) {
			this.componentName = componentName;
			this.line = line;
		};

		@Override
		public IUrl initializeUrl() {
			if (line.startsWith("http://")) return new ExternalUrl(event, line);
			if (line.startsWith("/")) return event.resolve(UrlType.CONTEXT, line);
			return event.resolve(UrlType.CONTEXT, "component" + "/" + componentName + "/" + line);
		}

		@Override
		public String toString() {
			if (url.toString().endsWith(".js")) return scriptToString();
			return styleToString();
		}
	}

	abstract class Requirement {
		IUrl url;

		@Override
		public boolean equals(Object obj) {
			return getUrl().equals(((Requirement) obj).getUrl());
		}

		public IUrl getUrl() {
			if (url == null) url = initializeUrl();
			return url;
		}

		@Override
		public int hashCode() {
			return getUrl().hashCode();
		}

		abstract IUrl initializeUrl();

		String scriptToString() {
			return "<script type=\"text/javascript\" src=\"" + url + "\"></script>";
		}

		String styleToString() {
			return "<link href=\"" + url + "\" type=\"text/css\" rel=\"stylesheet\" />";
		}
	}

	class Script extends Requirement {
		private String line;

		Script(String line) {
			this.line = line;
		}

		@Override
		public String toString() {
			return scriptToString();
		}

		@Override
		IUrl initializeUrl() {
			return event.resolve(UrlType.SCRIPT, line);
		}
	}

	class Style extends Requirement {
		private String line;

		Style(String line) {
			this.line = line;
		}

		@Override
		public String toString() {
			return styleToString();
		}

		@Override
		IUrl initializeUrl() {
			return event.resolve(UrlType.SCRIPT, line);
		}
	}
}