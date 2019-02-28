/*
 * Copyright 2002-2010 the original author or authors.
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

package org.springframework.beans.factory.support;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;

/**
 * Extension to the standard {@link BeanFactoryPostProcessor} SPI, allowing for
 * the registration of further bean definitions <i>before</i> regular
 * BeanFactoryPostProcessor detection kicks in. In particular,
 * BeanDefinitionRegistryPostProcessor may register further bean definitions
 * which in turn define BeanFactoryPostProcessor instances.
 *
 * @author Juergen Hoeller
 * @since 3.0.1
 * @see org.springframework.context.annotation.ConfigurationClassPostProcessor
 */
public interface BeanDefinitionRegistryPostProcessor extends BeanFactoryPostProcessor {

	/**
	 * Modify the application context's internal bean definition registry after its
	 * standard initialization. All regular bean definitions will have been loaded,
	 * but no beans will have been instantiated yet. This allows for adding further
	 * bean definitions before the next post-processing phase kicks in.
	 * @param registry the bean definition registry used by the application context
	 * @throws org.springframework.beans.BeansException in case of errors
	 */
	/**
	 * BeanDefinitionRegistryPostProcessor拓展了一个方法为void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry)
	 * 在对象工厂BeanFactory创建完毕而且正常的BeanDefinition都已经加载完毕而且尚未初始化时调用。用来修改BeanDefinitionRegistry

	 1.直接注册到AbstractApplicationContext中的beanFactoryPostProcessors且类型为BeanDefinitionRegistryPostProcessor
	 执行BeanDefinitionRegistryPostProcessor.postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry)
	 2.被BeanFactory通过AbstractRefreshableApplicationContext创建BeanFactory时的loadBeanDefinition加载的（或者第一步修改registry注册进去的）类型为
	 BeanDefinitionRegistryPostProcessor的而且实现了PriorityOrdered接口的BeanDefinition。然后根据getOrder()的值通过排序器
	 ((DefaultListableBeanFactory) beanFactory).getDependencyComparator()如果不存在则用OrderComparator.INSTANCE。
	 调用postProcessor.postProcessBeanDefinitionRegistry(registry);
	 3.被BeanFactory通过AbstractRefreshableApplicationContext创建BeanFactory时的loadBeanDefinition加载的（或者第一步、第二步修改registry注册进去的）类型为
	 BeanDefinitionRegistryPostProcessor的而且实现了Ordered接口的BeanDefinition,然后根据getOrder()的值通过排序器
	 ((DefaultListableBeanFactory) beanFactory).getDependencyComparator()如果不存在则用OrderComparator.INSTANCE。
	 调用postProcessor.postProcessBeanDefinitionRegistry(registry);
	 注意：PriorityOrdered是Ordered的子接口，因此这里就算第二部注册了一些实现了PriorityOrdered的BeanDefinitionRegistryPostProcessor，
	 仅仅会根据getOrder()值进行排序执行的。
	 4.到这可能会由3步骤产生新的BeanDefinitionRegistryPostProcessor类型的BeanDefinition，而且每一次调用都可能会产生新的该类型BeanDefinition
	 这里直接循环（+ 排序）调用postProcessor.postProcessBeanDefinitionRegistry(registry);，
	 直至没有需要处理的BeanDefinitionRegistryPostProcessor类型的为止。
	 5.执行上面获取的所有BeanPostProcessor中的postProcessor.postProcessBeanFactory(beanFactory);
	 虽然BeanDefinitionRegistryPostProcessor为BeanPostProcessor的子类，但是上面处理并没有开始执行postProcessor.postProcessBeanFactory(beanFactory);
	 方法，这一步直接全部挨个先调用BeanDefinitionRegistryPostProcessor的postProcessBeanFactory(beanFactory)后调用
	 上面从AbstractApplicationContext中获取的类型不为BeanDefinitionRegistryPostProcessor以及1、2、3、4获取的类型为BeanPostProcessor的
	 postProcessBeanFactory(beanFactory)回调。
	 6.在第四步循环处理的是为子类型BeanDefinitionRegistryPostProcessor，因此会产生很多新的BeanPostProcessor这里统一处理有类型为BeanFactoryPostProcessor的
	 将所有类型为BeanPostP
	 ---------------------

	 * @param registry
	 * @throws BeansException
	 */
	void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException;

}
