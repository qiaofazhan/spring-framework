/*
 * Copyright 2002-2018 the original author or authors.
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

package org.springframework.core.env;

import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

import org.springframework.lang.Nullable;

/**
 * The default implementation of the {@link PropertySources} interface.
 * Allows manipulation of contained property sources and provides a constructor
 * for copying an existing {@code PropertySources} instance.
 *
 * <p>Where <em>precedence</em> is mentioned in methods such as {@link #addFirst}
 * and {@link #addLast}, this is with regard to the order in which property sources
 * will be searched when resolving a given property with a {@link PropertyResolver}.
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @since 3.1
 * @see PropertySourcesPropertyResolver
 */
public class MutablePropertySources implements PropertySources {

	//qfz> 在list中越靠前，优先级越高。
	private final List<PropertySource<?>> propertySourceList = new CopyOnWriteArrayList<>();


	/**
	 * Create a new {@link MutablePropertySources} object.
	 */
	public MutablePropertySources() {
	}

	/**
	 * Create a new {@code MutablePropertySources} from the given propertySources
	 * object, preserving the original order of contained {@code PropertySource} objects.
	 */
	public MutablePropertySources(PropertySources propertySources) {
		this();
		for (PropertySource<?> propertySource : propertySources) {
			addLast(propertySource);
		}
	}


	@Override
	public Iterator<PropertySource<?>> iterator() {
		return this.propertySourceList.iterator();
	}

	@Override
	public Spliterator<PropertySource<?>> spliterator() {
		return Spliterators.spliterator(this.propertySourceList, 0);
	}

	@Override
	public Stream<PropertySource<?>> stream() {
		return this.propertySourceList.stream();
	}

	@Override
	public boolean contains(String name) {
		return this.propertySourceList.contains(PropertySource.named(name));
	}

	@Override
	@Nullable
	public PropertySource<?> get(String name) {
		int index = this.propertySourceList.indexOf(PropertySource.named(name));
		return (index != -1 ? this.propertySourceList.get(index) : null);
	}


	/**
	 * Add the given property source object with highest precedence.
	 */
	//qfz>  将给定的（传入的）属性源指定最高优先级，即添加到list头部；在list中越靠前，优先级越高。
	public void addFirst(PropertySource<?> propertySource) {
		removeIfPresent(propertySource);
		this.propertySourceList.add(0, propertySource);
	}

	/**
	 * Add the given property source object with lowest precedence.
	 */
	//qfz>  将给定的（传入的）属性源指定最低优先级，即添加到list尾部；在list中越靠前，优先级越高。
	public void addLast(PropertySource<?> propertySource) {
		removeIfPresent(propertySource);
		this.propertySourceList.add(propertySource);
	}

	/**
	 * Add the given property source object with precedence immediately higher
	 * than the named relative property source.
	 */
	//在list中越靠前，优先级越高。
	//qfz>  将给定的（传入的）属性源（propertySource）指定相对优先级，相对传入的属性源名称（relativePropertySourceName）高一级，即添加到relativePropertySourceName前面
	public void addBefore(String relativePropertySourceName, PropertySource<?> propertySource) {
		assertLegalRelativeAddition(relativePropertySourceName, propertySource);
		removeIfPresent(propertySource);
		int index = assertPresentAndGetIndex(relativePropertySourceName);
		addAtIndex(index, propertySource);
	}

	/**
	 * Add the given property source object with precedence immediately lower
	 * than the named relative property source.
	 */
	//qfz>  将给定的（传入的）属性源（propertySource）指定相对优先级，相对传入的属性源名称（relativePropertySourceName）低一级，即添加到relativePropertySourceName后面
	public void addAfter(String relativePropertySourceName, PropertySource<?> propertySource) {
		assertLegalRelativeAddition(relativePropertySourceName, propertySource);
		removeIfPresent(propertySource);
		int index = assertPresentAndGetIndex(relativePropertySourceName);
		addAtIndex(index + 1, propertySource);
	}

	/**
	 * Return the precedence of the given property source, {@code -1} if not found.
	 */
	//qfz>  返回指定属性源的优先级，即索引。
	public int precedenceOf(PropertySource<?> propertySource) {
		return this.propertySourceList.indexOf(propertySource);
	}

	/**
	 * Remove and return the property source with the given name, {@code null} if not found.
	 * @param name the name of the property source to find and remove
	 */
	@Nullable
	public PropertySource<?> remove(String name) {
		int index = this.propertySourceList.indexOf(PropertySource.named(name));
		return (index != -1 ? this.propertySourceList.remove(index) : null);
	}

	/**
	 * Replace the property source with the given name with the given property source object.
	 * @param name the name of the property source to find and replace
	 * @param propertySource the replacement property source
	 * @throws IllegalArgumentException if no property source with the given name is present
	 * @see #contains
	 */
	//qfz>  替换属性源
	public void replace(String name, PropertySource<?> propertySource) {
		int index = assertPresentAndGetIndex(name);
		this.propertySourceList.set(index, propertySource);
	}

	/**
	 * Return the number of {@link PropertySource} objects contained.
	 */
	public int size() {
		return this.propertySourceList.size();
	}

	@Override
	public String toString() {
		return this.propertySourceList.toString();
	}

	/**
	 * Ensure that the given property source is not being added relative to itself.
	 */
	protected void assertLegalRelativeAddition(String relativePropertySourceName, PropertySource<?> propertySource) {
		String newPropertySourceName = propertySource.getName();
		if (relativePropertySourceName.equals(newPropertySourceName)) {
			throw new IllegalArgumentException(
					"PropertySource named '" + newPropertySourceName + "' cannot be added relative to itself");
		}
	}

	/**
	 * Remove the given property source if it is present.
	 */
	protected void removeIfPresent(PropertySource<?> propertySource) {
		this.propertySourceList.remove(propertySource);
	}

	/**
	 * Add the given property source at a particular index in the list.
	 */
	//qfz>  在指定索引（优先级）处，添加属性源；即添加属性源并指定优先级
	private void addAtIndex(int index, PropertySource<?> propertySource) {
		removeIfPresent(propertySource);
		this.propertySourceList.add(index, propertySource);
	}

	/**
	 * Assert that the named property source is present and return its index.
	 * @param name {@linkplain PropertySource#getName() name of the property source} to find
	 * @throws IllegalArgumentException if the named property source is not present
	 */
	//qfz>  返回指定名称的属性源的优先级（索引）
	private int assertPresentAndGetIndex(String name) {
		int index = this.propertySourceList.indexOf(PropertySource.named(name));
		if (index == -1) {
			throw new IllegalArgumentException("PropertySource named '" + name + "' does not exist");
		}
		return index;
	}

}
