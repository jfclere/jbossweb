/*
 * Copyright 1999-2001,2005 The Apache Software Foundation.
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


package org.jboss.web.tomcat.filters;


import java.io.IOException;

import javax.servlet.ServletException;

import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.util.LifecycleSupport;
import org.apache.catalina.util.StringManager;
import org.apache.catalina.valves.ValveBase;

import EDU.oswego.cs.dl.util.concurrent.FIFOSemaphore;
import EDU.oswego.cs.dl.util.concurrent.Sync;

/**
 * <p>Implementation of a Valve that limits concurrency.</p>
 *
 * <p>This Valve may be attached to any Container, depending on the granularity
 * of the concurrency control you wish to perform.  This is a Java 1.4 port
 * of the SemaphoreValve class that Remy implemented.</p>
 * 
 * @author Remy Maucherat, Scott Marlow
 * @version $Revision: 1.1 $ $Date: 2005/07/28 16:31:22 $
 */

public class SemaphoreValve
    extends ValveBase
    implements Lifecycle {


    // ----------------------------------------------------- Instance Variables


    /**
     * The descriptive information related to this implementation.
     */
    private static final String info =
        "org.jboss.web.tomcat.filters.SemaphoreValve/1.0";


    /**
     * Semaphore.
     */
    protected Sync semaphore = null;
    

    /**
     * The lifecycle event support for this component.
     */
    protected LifecycleSupport lifecycle = new LifecycleSupport(this);


    /**
     * Has this component been started yet?
     */
    private boolean started = false;


    // ------------------------------------------------------------- Properties

    
    /**
     * Concurrency level of the semaphore.
     */
    protected int concurrency = 10;
    public int getConcurrency() { return concurrency; }
    public void setConcurrency(int concurrency) { this.concurrency = concurrency; }
    

    // ------------------------------------------------------ Lifecycle Methods


    /**
     * Add a lifecycle event listener to this component.
     *
     * @param listener The listener to add
     */
    public void addLifecycleListener(LifecycleListener listener) {

        lifecycle.addLifecycleListener(listener);

    }


    /**
     * Get the lifecycle listeners associated with this lifecycle. If this
     * Lifecycle has no listeners registered, a zero-length array is returned.
     */
    public LifecycleListener[] findLifecycleListeners() {

        return lifecycle.findLifecycleListeners();

    }


    /**
     * Remove a lifecycle event listener from this component.
     *
     * @param listener The listener to add
     */
    public void removeLifecycleListener(LifecycleListener listener) {

        lifecycle.removeLifecycleListener(listener);

    }


    /**
     * Prepare for the beginning of active use of the public methods of this
     * component.  This method should be called after <code>configure()</code>,
     * and before any of the public methods of the component are utilized.
     *
     * @exception LifecycleException if this component detects a fatal error
     *  that prevents this component from being used
     */
    public void start() throws LifecycleException {

        // Validate and update our current component state
        if (started)
            throw new LifecycleException
                ("Semaphore valve already started");
        lifecycle.fireLifecycleEvent(START_EVENT, null);
        started = true;

        semaphore = new FIFOSemaphore(concurrency);

    }


    /**
     * Gracefully terminate the active use of the public methods of this
     * component.  This method should be the last one called on a given
     * instance of this component.
     *
     * @exception LifecycleException if this component detects a fatal error
     *  that needs to be reported
     */
    public void stop() throws LifecycleException {

        // Validate and update our current component state
        if (!started)
            throw new LifecycleException
                ("Semaphore valve not started");
        lifecycle.fireLifecycleEvent(STOP_EVENT, null);
        started = false;

        semaphore = null;

    }

    
    // --------------------------------------------------------- Public Methods


    /**
     * Return descriptive information about this Valve implementation.
     */
    public String getInfo() {
        return (info);
    }


    /**
     * Do concurrency control on the request using the semaphore.
     *
     * @param request The servlet request to be processed
     * @param response The servlet response to be created
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet error occurs
     */
    public void invoke(Request request, Response response)
        throws IOException, ServletException {

	boolean shouldRelease = false;
        try {
            semaphore.acquire();
            shouldRelease = true;
            // Perform the request
            getNext().invoke(request, response);
        } catch( java.lang.InterruptedException e)  {
            container.getLogger().error(e.getMessage(), e);
        } finally {
            if (shouldRelease)
                semaphore.release();
        }

    }


}
