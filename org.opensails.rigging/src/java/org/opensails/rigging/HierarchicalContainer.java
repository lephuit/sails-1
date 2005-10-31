package org.opensails.rigging;

import java.util.ArrayList;
import java.util.List;

public class HierarchicalContainer extends SimpleContainer {
	protected HierarchicalContainer parent;

	protected List<HierarchicalContainer> children;

	public HierarchicalContainer() {
		children = new ArrayList<HierarchicalContainer>();
	}

	protected HierarchicalContainer(HierarchicalContainer parent) {
		this();
		this.parent = parent;
	}

	/**
	 * @return child
	 */
	public HierarchicalContainer makeChild() {
		HierarchicalContainer child = new HierarchicalContainer(this);
		children.add(child);
		return child;
	}

	public HierarchicalContainer getParent() {
		return parent;
	}

	/**
	 * @param key
	 * @return whether the key exists anywhere in this container or its
	 *         ancestry.
	 * @see HierarchicalContainer#containsLocally(Class)
	 */
	@Override
	public boolean contains(Class key) {
		return containsLocally(key) || (parent != null && parent.contains(key));
	}

	/**
	 * @param key
	 * @return whether the key exists locally in this container.
	 * @see HierarchicalContainer#contains(Class)
	 */
	public boolean containsLocally(Class key) {
		return super.contains(key);
	}

	@Override
	public <T> T instance(Class<T> key) {
		if (containsLocally(key))
			return super.instance(key);
		if (parent == null)
			return null;
		return parent.instance(key);
	}

	@Override
	public void start() {
		super.start();
		for (HierarchicalContainer child : children)
			child.start();
	}

	@Override
	public void stop() {
		super.stop();
		for (HierarchicalContainer child : children)
			child.stop();
	}

	@Override
	public void dispose() {
		super.dispose();
		for (HierarchicalContainer child : children)
			child.dispose();
	}
	
	public void removeChild(HierarchicalContainer child) {
		children.remove(child);
		child.orphan();
	}
	
	protected void orphan() {
		parent = null;
	}
}