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
package org.jboss.palatial.aop.interceptor;

import org.jboss.aop.AspectManager;
import org.jboss.aop.Domain;
import org.jboss.aop.DomainDefinition;
import org.jboss.aop.MethodInfo;
import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.MethodInvocation;
import org.jboss.aop.proxy.container.ClassProxyContainer;
import org.jboss.aop.proxy.container.InstanceProxyContainer;
import org.jboss.aop.proxy.container.ProxyAdvisorDomain;
import org.jboss.aop.util.MethodHashing;
import org.jboss.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import java.lang.reflect.Method;

/**
 * @author <a href="cdewolf@redhat.com">Carlo de Wolf</a>
 */
public class AOPDomainInterceptor
{
   private static final Logger log = Logger.getLogger(AOPDomainInterceptor.class);

   private String domainName;

   private InstanceProxyContainer instanceAdvisor;

   @AroundInvoke
   public Object aroundInvoke(final InvocationContext ctx) throws Exception
   {
      Method method = ctx.getMethod();
      long hash = MethodHashing.calculateHash(method);
      MethodInfo info = instanceAdvisor.getMethodInfo(hash);
      Interceptor[] aspects = info.getInterceptors();
//      if (instanceAdvisor != null && (instanceAdvisor.hasInterceptors()))
//      {
//         aspects = instanceAdvisor.getInterceptors(aspects);
//      }
      MethodInvocation invocation = new MethodInvocation(info, aspects)
      {
         @Override
         public Object invokeTarget() throws Throwable
         {
            return ctx.proceed();
         }
      };

      invocation.setArguments(ctx.getParameters());
      invocation.setTargetObject(ctx.getTarget());
      try
      {
         return invocation.invokeNext();
      }
      catch(Throwable t)
      {
         if(t instanceof Exception)
            throw (Exception) t;
         if(t instanceof Error)
            throw (Error) t;
         throw new RuntimeException(t);
      }
   }

   @PostConstruct
   public void postConstruct(InvocationContext ctx)
   {
      log.info("postConstruct " + ctx);

      DomainDefinition domainDef = AspectManager.instance().getContainer(domainName);
      if(domainDef == null)
         throw new IllegalStateException("Can't find domain " + domainName);
      AspectManager manager = domainDef.getManager();
      Class<?> cls = ctx.getTarget().getClass();
      String name = Domain.getDomainName(cls, false);
      ProxyAdvisorDomain domain = new ProxyAdvisorDomain(manager, name, cls, false);
      ClassProxyContainer container = new ClassProxyContainer(cls.getName(), domain);
      domain.setAdvisor(container);
      container.initialise(cls);

      this.instanceAdvisor = container.createInstanceProxyContainer();

      // hmm, chicken egg
      // Object target = invokeNew(container, 0);
   }

   @Resource
   public void setDomainName(String domainName)
   {
      this.domainName = domainName;
   }
}
