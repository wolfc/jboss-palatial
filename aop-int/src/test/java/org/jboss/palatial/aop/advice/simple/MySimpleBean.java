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
package org.jboss.palatial.aop.advice.simple;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author <a href="cdewolf@redhat.com">Carlo de Wolf</a>
 */
public class MySimpleBean
{
   public String digest(String msg)
   {
      try
      {
         MessageDigest m = MessageDigest.getInstance("MD5");
         m.update(msg.getBytes());
         byte digest[] = m.digest();
         return toHexString(digest);
      }
      catch(NoSuchAlgorithmException e)
      {
         throw new RuntimeException(e);
      }
   }

   // the compiler should optimize this all away

   private static String toHexString(byte b)
   {
      String s = Integer.toHexString(b);
      if(s.length() < 2)
         return "0" + s;
      return s;
   }

   private static String toHexString(byte b[])
   {
      String s = "";
      for(int i = 0; i < b.length; i++)
         s += toHexString(b[i]);
      return s;
   }
}
