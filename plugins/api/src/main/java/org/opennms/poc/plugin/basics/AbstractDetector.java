/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.poc.plugin.basics;

import lombok.Data;
import org.opennms.poc.plugin.api.ServiceDetector;

/**
 * <p>AbstractDetector class.</p>
 *
 * @author ranger
 */
@Data
public abstract class AbstractDetector implements ServiceDetector {
    
    private static final int DEFAULT_TIMEOUT = 2000;
    private static final int DEFAULT_RETRIES = 1;
    private int port;
    private int retries;
    private int timeout;
    private String ipMatch;
    private String serviceName;

    /**
     * <p>Constructor for AbstractDetector.</p>
     *
     * @param serviceName a {@link String} object.
     * @param port a int.
     * @param timeout a int.
     * @param retries a int.
     */
    protected AbstractDetector(final String serviceName, final int port, final int timeout, final int retries) {
        this.serviceName = serviceName;
        this.port = port;
        this.timeout = timeout;
        this.retries = retries;
    }

    /**
     * <p>Constructor for AbstractDetector.</p>
     *
     * @param serviceName a {@link String} object.
     * @param port a int.
     */
    protected AbstractDetector(final String serviceName, final int port) {
        this(serviceName, port, DEFAULT_TIMEOUT, DEFAULT_RETRIES);
    }

    /**
     * <p>init</p>
     */
//    @Override
    public final void init() {
        if (serviceName == null || timeout < -1) {
            throw new IllegalStateException(String.format("ServiceName is null or timeout of %d is invalid. Timeout must be > -1", timeout));
        }
        onInit();
    }
    
    /**
     * <p>onInit</p>
     */
    protected abstract void onInit();

}
