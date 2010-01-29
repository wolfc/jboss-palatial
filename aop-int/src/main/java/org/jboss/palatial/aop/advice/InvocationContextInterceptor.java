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
package org.jboss.palatial.aop.advice;

import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aop.joinpoint.MethodInvocation;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * The invocation context interceptor makes sure that invocation context
 * is available.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 */
public class InvocationContextInterceptor implements Interceptor
{
   public static final String INVOCATION_CONTEXT_ATTR = "org.jboss.palatial.aop";
   public static final String INVOCATION_CONTEXT_TAG = InvocationContext.class.getName();

   /**
    * Obtain the invocation context associated with the given invocation.
    *
    * @param invocation     the AOP invocation
    * @return               the EJB invocation context
    */
   public static InvocationContext getInvocationContext(Invocation invocation)
   {
      InvocationContext ctx = (InvocationContext) invocation.getMetaData(INVOCATION_CONTEXT_TAG, INVOCATION_CONTEXT_ATTR);
      if(ctx == null)
         throw new IllegalStateException("Could not find meta data " + INVOCATION_CONTEXT_TAG + ":" + INVOCATION_CONTEXT_ATTR + " in " + invocation);
      //assert ctx.invocation == invocation : "using InvocationContextInterceptor from a different bind";
      // FIXME: is this allowed?
      ctx.invocation = invocation;
      return ctx;
   }

   public String getName()
   {
      return "InvocationContextInterceptor";
   }

   /**
    * Setup an empty invocation context which can be used for lifecycle callbacks.
    *
    * @param invocation
    * @return
    * @throws Throwable
    */
   public Object invoke(final Invocation invocation) throws Throwable
   {
      InvocationContext ctx = new InvocationContext(invocation);
      if(invocation instanceof MethodInvocation)
      {
         MethodInvocation mi = (MethodInvocation) invocation;
         ctx.setBusinessMethodInvocation(mi.getMethod(), mi.getArguments());
      }
      invocation.getMetaData().addMetaData(INVOCATION_CONTEXT_TAG, INVOCATION_CONTEXT_ATTR, ctx);
      try
      {
         return invocation.invokeNext();
      }
      finally
      {
         invocation.getMetaData().removeMetaData(INVOCATION_CONTEXT_TAG, INVOCATION_CONTEXT_ATTR);
      }
   }

   private static class InvocationContext implements javax.interceptor.InvocationContext
   {
      private Invocation invocation;
      private Map<String, Object> contextData = new HashMap<String, Object>();
      private Method method = null;
      private Object params[] = null;

      private InvocationContext(Invocation invocation)
      {
         this.invocation = invocation;
         // Whether we're calling a business method or a lifecycle callback is beyond scope
         // here. This must be explicitly set via setBusinessMethodInvocation.
      }

      public Map<String, Object> getContextData()
      {
         return contextData;
      }

      public Method getMethod()
      {
         return method;
      }

      public Object[] getParameters()
      {
         if(method == null)
            throw new IllegalStateException("Get parameters is not allowed on lifecycle callbacks (EJB 3 12)");
         return params;
      }

      public Object getTarget()
      {
         return invocation.getTargetObject();
      }

      public Object getTimer()
      {
         throw new RuntimeException("NYI");
      }

      public Object proceed() throws Exception
      {
         try
         {
            return invocation.invokeNext();
         }
         catch(Exception e)
         {
            throw e;
         }
         catch(Throwable t)
         {
            throw new RuntimeException(t);
         }
      }

      private void setBusinessMethodInvocation(Method method, Object params[])
      {
         this.method = method;
         this.params = params;
      }

      public void setParameters(Object[] params)
      {
         if(method == null)
            throw new IllegalStateException("Setting parameters is not allowed on lifecycle callbacks (EJB 3 12)");
         // TODO: might need more checks
         this.params = params;
         ((MethodInvocation) invocation).setArguments(params);
      }

      public String toString()
      {
         return "[target=" + getTarget() + ", method=" + method + ", parameters=" + Arrays.toString(params) + ", contextData=" + contextData + "]";
      }
   }
}
