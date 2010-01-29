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

import org.jboss.aop.Advisor;
import org.jboss.aop.InstanceAdvisor;
import org.jboss.aop.advice.AspectFactory;
import org.jboss.aop.advice.AspectFactoryWithClassLoaderSupport;
import org.jboss.aop.joinpoint.Joinpoint;
import org.jboss.util.xml.XmlLoadable;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @author <a href="cdewolf@redhat.com">Carlo de Wolf</a>
 */
public class SpecInterceptorFactory extends AspectFactoryWithClassLoaderSupport
   implements AspectFactory, XmlLoadable
{
   private String interceptorClassName;
   private Class<? extends InterceptorWrapper> interceptorWrapperClass;

   public Object createPerVM()
   {
      throw new RuntimeException("Only per instance is supported");
   }

   public Object createPerClass(Advisor advisor)
   {
      throw new RuntimeException("Only per instance is supported");
   }

   public Object createPerInstance(Advisor advisor, InstanceAdvisor instanceAdvisor)
   {
      try
      {
         if(interceptorWrapperClass == null)
            initialize();

         InterceptorWrapper wrapper = interceptorWrapperClass.newInstance();
         // TODO: invoke postConstruct etc
         //wrapper.postConstruct(ctx);
         return new SpecInterceptor(wrapper);
      }
      catch(ClassNotFoundException e)
      {
         throw new RuntimeException(e);
      }
      catch(InstantiationException e)
      {
         throw new RuntimeException(e);
      }
      catch(IllegalAccessException e)
      {
         throw new RuntimeException(e);
      }
   }

   public Object createPerJoinpoint(Advisor advisor, Joinpoint joinpoint)
   {
      throw new RuntimeException("Only per instance is supported");
   }

   public Object createPerJoinpoint(Advisor advisor, InstanceAdvisor instanceAdvisor, Joinpoint joinpoint)
   {
      throw new RuntimeException("Only per instance is supported");
   }

   public String getName()
   {
      return getClass().getName();
   }

   public void importXml(Element element)
   {
      // TODO: lots of checking
      NodeList children = element.getElementsByTagName("interceptor-class");
      for(int i = 0; i < children.getLength(); i++)
      {
         Element child = (Element) children.item(i);
         this.interceptorClassName = child.getTextContent();
      }
   }

   protected synchronized void initialize() throws ClassNotFoundException
   {
      if(interceptorWrapperClass != null)
         return;

      // TODO: transform any spec interceptor into an InterceptorWrapper
      interceptorWrapperClass = (Class<? extends InterceptorWrapper>) loadClass(interceptorClassName);
   }
}
