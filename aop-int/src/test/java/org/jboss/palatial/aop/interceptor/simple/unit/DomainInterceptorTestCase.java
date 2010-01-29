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
package org.jboss.palatial.aop.interceptor.simple.unit;

import org.jboss.aop.AspectXmlLoader;
import org.jboss.interceptor.model.InterceptionModel;
import org.jboss.interceptor.model.InterceptionModelBuilder;
import org.jboss.interceptor.registry.InterceptorRegistry;
import org.jboss.interceptor.util.InterceptionUtils;
import org.jboss.palatial.aop.interceptor.AOPDomainInterceptor;
import org.jboss.palatial.aop.interceptor.simple.FootballTeam;
import org.junit.Test;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="cdewolf@redhat.com">Carlo de Wolf</a>
 */
public class DomainInterceptorTestCase
{
   @Test
   public void test1() throws Exception
   {
      URL url = DomainInterceptorTestCase.class.getResource("../interceptors-aop.xml");
      AspectXmlLoader.deployXML(url);

      Class<?> cls = FootballTeam.class;
      FootballTeam instance = new FootballTeam();
      InterceptorRegistry<Class<?>,Class<?>> interceptorRegistry = new InterceptorRegistry<Class<?>, Class<?>>();
      InterceptionModelBuilder<Class<?>, Class<?>> builder = InterceptionModelBuilder.newBuilderFor(FootballTeam.class, (Class) Class.class);
      
      // TODO: I really want to just consume the annotations
      builder.interceptAll().with(AOPDomainInterceptor.class);
      
      InterceptionModel<Class<?>, Class<?>> interceptionModel = builder.build();
      interceptorRegistry.registerInterceptionModel(cls, interceptionModel);

      Map<String, Object> injectionEnv = new HashMap<String, Object>();
      injectionEnv.put(AOPDomainInterceptor.class.getName() + ".domainName", "TestDomain");

      FootballTeam proxy = InterceptionUtils.proxifyInstance(instance, cls, interceptorRegistry, new InjectingInterceptionHandlerFactory(injectionEnv));
      
      InterceptionUtils.executePostConstruct(proxy);
      
      proxy.setName("Test");
   }
}
