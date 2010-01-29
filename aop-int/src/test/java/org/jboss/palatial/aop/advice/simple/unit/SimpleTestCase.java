/*
 * JBoss, Home of Professional Open Source
 * Copyright (c) 2010, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.palatial.aop.advice.simple.unit;

import org.jboss.aop.*;
import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.ConstructorInvocation;
import org.jboss.aop.joinpoint.MethodInvocation;
import org.jboss.aop.proxy.container.ClassProxyContainer;
import org.jboss.aop.proxy.container.InstanceProxyContainer;
import org.jboss.aop.proxy.container.ProxyAdvisorDomain;
import org.jboss.aop.util.MethodHashing;
import org.jboss.palatial.aop.advice.simple.MySimpleBean;
import org.junit.Test;

import java.lang.reflect.Method;
import java.net.URL;

/**
 * @author <a href="cdewolf@redhat.com">Carlo de Wolf</a>
 */
public class SimpleTestCase
{
   private Object invokeNew(Advisor advisor, int idx, Object... args) throws Throwable
   {
      ConstructorInfo constructorInfo = advisor.getConstructorInfos()[idx];
      Interceptor[] cInterceptors = constructorInfo.getInterceptors();
      if (cInterceptors == null) cInterceptors = new Interceptor[0];
      ConstructorInvocation invocation = new ConstructorInvocation(cInterceptors);

      invocation.setAdvisor(advisor);
      invocation.setArguments(args);
      invocation.setConstructor(advisor.getConstructors()[idx]);
      return invocation.invokeNext();
   }

   private Object invokeMethod(InstanceProxyContainer instanceAdvisor, Object target, long hash, Object[] arguments) throws Throwable
   {
      MethodInfo info = instanceAdvisor.getMethodInfo(hash);      
      Interceptor[] aspects = info.getInterceptors();
//      if (instanceAdvisor != null && (instanceAdvisor.hasInterceptors()))
//      {
//         aspects = instanceAdvisor.getInterceptors(aspects);
//      }
      MethodInvocation invocation = new MethodInvocation(info, aspects);

      invocation.setArguments(arguments);
      invocation.setTargetObject(target);
      return invocation.invokeNext();
   }

   @Test
   public void test1() throws Throwable
   {
      URL url = SimpleTestCase.class.getResource("../interceptors-aop.xml");
      AspectXmlLoader.deployXML(url);

      AspectManager manager = AspectManager.instance().getContainer("Test").getManager();
      Class<?> cls = MySimpleBean.class;
      /*
      ClassAdvisor advisor = new ClassAdvisor(cls, manager);
      advisor.attachClass(MySimpleBean.class);

      InstanceAdvisor instanceAdvisor = new ClassInstanceAdvisor(advisor);
      Object target = advisor.invokeNew(null, 0);
      Method method = cls.getMethod("digest", String.class);
      long hash = MethodHashing.calculateHash(method);
      Object arguments[] = { "Hello world" };
      Object result = advisor.invokeMethod(instanceAdvisor, target, hash, arguments);
      */
      // borrowed from ContainerCache.createContainer
      String name = Domain.getDomainName(cls, false);
      ProxyAdvisorDomain domain = new ProxyAdvisorDomain(manager, name, cls, false);
      ClassProxyContainer container = new ClassProxyContainer(cls.getName(), domain);
      domain.setAdvisor(container);
      container.initialise(cls);
      
      InstanceProxyContainer instanceContainer = container.createInstanceProxyContainer();

      Object target = invokeNew(container, 0);

      Method method = MySimpleBean.class.getMethod("digest", String.class);
      long hash = MethodHashing.calculateHash(method);
      Object arguments[] = { "Hello world" };
      Object result = invokeMethod(instanceContainer, target, hash, arguments);
   }
}
