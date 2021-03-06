/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.catalina.core;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.tomcat.jni.Library;
import org.jboss.web.CatalinaLogger;



/**
 * Implementation of <code>LifecycleListener</code> that will init and
 * and destroy APR.
 *
 * @author Remy Maucherat
 * @author Filip Hanik
 * @version $Revision: 1692 $ $Date: 2011-04-01 20:00:38 +0200 (Fri, 01 Apr 2011) $
 * @since 4.1
 */

public class AprLifecycleListener
    implements LifecycleListener {

    // ---------------------------------------------- Constants


    protected static final int TCN_REQUIRED_MAJOR = 1;
    protected static final int TCN_REQUIRED_MINOR = 1;
    protected static final int TCN_REQUIRED_PATCH = 8;
    protected static final int TCN_RECOMMENDED_PV = 21;


    // ---------------------------------------------- Properties
    protected static String SSLEngine = "on"; //default on
    protected static String SSLRandomSeed = "builtin";
    protected static boolean sslInitialized = false;
    protected static boolean aprInitialized = false;

    // ---------------------------------------------- LifecycleListener Methods

    /**
     * Primary entry point for startup and shutdown events.
     *
     * @param event The event that has occurred
     */
    public void lifecycleEvent(LifecycleEvent event) {

        if (Lifecycle.INIT_EVENT.equals(event.getType())) {
            aprInitialized = init();
            if (aprInitialized) {
                try {
                    initializeSSL();
                } catch (Throwable t) {
                    if (!CatalinaLogger.CORE_LOGGER.isDebugEnabled()) {
                        CatalinaLogger.CORE_LOGGER.aprSslEngineInitFailed();
                    } else {
                        CatalinaLogger.CORE_LOGGER.aprSslEngineInitFailedWithThrowable(t);
                    }
                }
            }
        }

    }

    private boolean init()
    {
        int major = 0;
        int minor = 0;
        int patch = 0;
        if (aprInitialized) {
            return true;    
        }
        try {
            String methodName = "initialize";
            Class<?> paramTypes[] = new Class[1];
            paramTypes[0] = String.class;
            Object paramValues[] = new Object[1];
            paramValues[0] = null;
            Class<?> clazz = Class.forName("org.apache.tomcat.jni.Library");
            Method method = clazz.getMethod(methodName, paramTypes);
            method.invoke(null, paramValues);
            major = clazz.getField("TCN_MAJOR_VERSION").getInt(null);
            minor = clazz.getField("TCN_MINOR_VERSION").getInt(null);
            patch = clazz.getField("TCN_PATCH_VERSION").getInt(null);
        } catch (Throwable t) {
            if (!CatalinaLogger.CORE_LOGGER.isDebugEnabled()) {
                CatalinaLogger.CORE_LOGGER.aprInitFailed(System.getProperty("java.library.path"));
            } else {
                CatalinaLogger.CORE_LOGGER.aprInitFailedWithThrowable(System.getProperty("java.library.path"), t);
            }
            return false;
        }
        if ((major != TCN_REQUIRED_MAJOR)  ||
            (minor != TCN_REQUIRED_MINOR) ||
            (patch <  TCN_REQUIRED_PATCH)) {
            CatalinaLogger.CORE_LOGGER.aprInvalidVersion(major, minor, patch, 
                    TCN_REQUIRED_MAJOR, TCN_REQUIRED_MINOR, TCN_REQUIRED_PATCH);
            return false;
        }
        if (patch <  TCN_RECOMMENDED_PV) {
            CatalinaLogger.CORE_LOGGER.aprRecommendedVersion(major, minor, patch, 
                    TCN_REQUIRED_MAJOR, TCN_REQUIRED_MINOR, TCN_RECOMMENDED_PV);
        }
        CatalinaLogger.CORE_LOGGER.aprInit(major, minor, patch, Library.APR_HAVE_IPV6, Library.APR_HAS_SENDFILE, 
                    Library.APR_HAS_RANDOM);
        return true;
    }

    private static synchronized void initializeSSL()
        throws ClassNotFoundException, NoSuchMethodException,
               IllegalAccessException, InvocationTargetException
    {

        if ("off".equalsIgnoreCase(SSLEngine)) {
            return;
        }
        if (sslInitialized) {
             //only once per VM
            return;
        }
        String methodName = "randSet";
        Class<?> paramTypes[] = new Class[1];
        paramTypes[0] = String.class;
        Object paramValues[] = new Object[1];
        paramValues[0] = SSLRandomSeed;
        Class<?> clazz = Class.forName("org.apache.tomcat.jni.SSL");
        Method method = clazz.getMethod(methodName, paramTypes);
        method.invoke(null, paramValues);

        methodName = "initialize";
        paramValues[0] = "on".equalsIgnoreCase(SSLEngine)?null:SSLEngine;
        method = clazz.getMethod(methodName, paramTypes);
        method.invoke(null, paramValues);
 
        sslInitialized = true;
    }

    public static boolean isAprInitialized() {
        return aprInitialized;
    }

    public String getSSLEngine() {
        return SSLEngine;
    }

    public void setSSLEngine(String SSLEngine) {
        this.SSLEngine = SSLEngine;
    }

    public String getSSLRandomSeed() {
        return SSLRandomSeed;
    }

    public void setSSLRandomSeed(String SSLRandomSeed) {
        this.SSLRandomSeed = SSLRandomSeed;
    }
}
