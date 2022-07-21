/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
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

package org.opennms.poc.web;

import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.util.EntityUtils;
import org.opennms.core.web.HttpClientWrapper;
import org.opennms.horizon.core.lib.InetAddressUtils;
import org.opennms.poc.plugin.api.AbstractServiceMonitor;
import org.opennms.poc.plugin.api.MonitoredService;
import org.opennms.poc.plugin.api.ParameterMap;
import org.opennms.poc.plugin.api.PollStatus;
import org.opennms.poc.plugin.api.ServiceMonitorResponse;
import org.opennms.poc.plugin.api.ServiceMonitorResponse.Status;
import org.opennms.poc.plugin.api.ServiceMonitorResponseImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebMonitor extends AbstractServiceMonitor {
    private static final Logger LOG = LoggerFactory.getLogger(WebMonitor.class);
    static Integer DEFAULT_TIMEOUT = 3000;
    static Integer DEFAULT_PORT = 80;
    static String DEFAULT_USER_AGENT = "OpenNMS WebMonitor";
    static String DEFAULT_PATH = "/";
    static String DEFAULT_USER = "admin";
    static String DEFAULT_PASSWORD = "admin";
    static String DEFAULT_HTTP_STATUS_RANGE = "100-399";
    static String DEFAULT_SCHEME = "http";

    //TODO: maybe put the translation in the new Status class? Or just refactor all to use the same Enum.
    Map<Integer, Status> pollStatusMapper =
            ImmutableMap.of(
                    PollStatus.SERVICE_UNKNOWN, Status.Unknown,
                    PollStatus.SERVICE_AVAILABLE, Status.Up,
                    PollStatus.SERVICE_UNAVAILABLE, Status.Down,
                    PollStatus.SERVICE_UNRESPONSIVE, Status.Unresponsive);

    /** {@inheritDoc} */
    @Override
    public CompletableFuture<ServiceMonitorResponse> poll(MonitoredService svc, Map<String, Object> parameters) {

        HttpClientWrapper clientWrapper = HttpClientWrapper.create();
        HttpClientWrapperConfigHelper.setUseSystemProxyIfDefined(clientWrapper, parameters);

        CompletableFuture<PollStatus> future = CompletableFuture.supplyAsync(() -> {
            PollStatus pollStatus = PollStatus.unresponsive();
            try {
                final String hostAddress = InetAddressUtils.str(svc.getAddress());

                URIBuilder ub = new URIBuilder();
                ub.setScheme(ParameterMap.getKeyedString(parameters, "scheme", DEFAULT_SCHEME));
                ub.setHost(hostAddress);
                ub.setPort(ParameterMap.getKeyedInteger(parameters, "port", DEFAULT_PORT));
                ub.setPath(ParameterMap.getKeyedString(parameters, "path", DEFAULT_PATH));

                String queryString = ParameterMap.getKeyedString(parameters, "queryString", null);
                if (queryString != null && !queryString.trim().isEmpty()) {
                    final List<NameValuePair> params = URLEncodedUtils.parse(queryString, StandardCharsets.UTF_8);
                    if (!params.isEmpty()) {
                        ub.setParameters(params);
                    }
                }

                final HttpGet getMethod = new HttpGet(ub.build());
                clientWrapper.setConnectionTimeout(ParameterMap.getKeyedInteger(parameters, "timeout", DEFAULT_TIMEOUT))
                        .setSocketTimeout(ParameterMap.getKeyedInteger(parameters, "timeout", DEFAULT_TIMEOUT));

                final String userAgent = ParameterMap.getKeyedString(parameters, "user-agent", DEFAULT_USER_AGENT);
                if (userAgent != null && !userAgent.trim().isEmpty()) {
                    clientWrapper.setUserAgent(userAgent);
                }

                final String virtualHost = ParameterMap.getKeyedString(parameters, "virtual-host", hostAddress);
                if (virtualHost != null && !virtualHost.trim().isEmpty()) {
                    clientWrapper.setVirtualHost(virtualHost);
                }

                if (ParameterMap.getKeyedBoolean(parameters, "http-1.0", false)) {
                    clientWrapper.setVersion(HttpVersion.HTTP_1_0);
                }

                for (final Object okey : parameters.keySet()) {
                    final String key = okey.toString();
                    if (key.matches("header_[0-9]+$")) {
                        final String headerName = ParameterMap.getKeyedString(parameters, key, null);
                        final String headerValue = ParameterMap.getKeyedString(parameters, key + "_value", null);
                        getMethod.setHeader(headerName, headerValue);
                    }
                }

                if (ParameterMap.getKeyedBoolean(parameters, "use-ssl-filter", false)) {
                    clientWrapper.trustSelfSigned(ParameterMap.getKeyedString(parameters, "scheme", DEFAULT_SCHEME));
                }

                if (ParameterMap.getKeyedBoolean(parameters, "auth-enabled", false)) {
                    clientWrapper.addBasicCredentials(ParameterMap.getKeyedString(parameters, "auth-user", DEFAULT_USER), ParameterMap.getKeyedString(parameters, "auth-password", DEFAULT_PASSWORD));
                    if (ParameterMap.getKeyedBoolean(parameters, "auth-preemptive", true)) {
                        clientWrapper.usePreemptiveAuth();
                    }
                }

                LOG.debug("getMethod parameters: {}", getMethod);
                CloseableHttpResponse response = clientWrapper.execute(getMethod);
                int statusCode = response.getStatusLine().getStatusCode();
                String statusText = response.getStatusLine().getReasonPhrase();
                String expectedText = ParameterMap.getKeyedString(parameters, "response-text", null);

                LOG.debug("returned results are:");

                if (!inRange(ParameterMap.getKeyedString(parameters, "response-range", DEFAULT_HTTP_STATUS_RANGE), statusCode)) {
                    pollStatus = PollStatus.unavailable(statusText);
                } else {
                    pollStatus = PollStatus.available();
                }

                if (expectedText != null) {
                    String responseText = EntityUtils.toString(response.getEntity());
                    if (expectedText.charAt(0) == '~') {
                        if (!responseText.matches(expectedText.substring(1))) {
                            pollStatus = PollStatus.unavailable("Regex Failed");
                        } else
                            pollStatus = PollStatus.available();
                    } else {
                        if (expectedText.equals(responseText))
                            pollStatus = PollStatus.available();
                        else
                            pollStatus = PollStatus.unavailable("Did not find expected Text");
                    }
                }

            } catch (IOException e) {
                LOG.info(e.getMessage());
                pollStatus = PollStatus.unavailable(e.getMessage());
            } catch (URISyntaxException e) {
                LOG.info(e.getMessage());
                pollStatus = PollStatus.unavailable(e.getMessage());
            } catch (GeneralSecurityException e) {
                LOG.error("Unable to set SSL trust to allow self-signed certificates", e);
                pollStatus = PollStatus.unavailable("Unable to set SSL trust to allow self-signed certificates");
            } catch (Throwable e) {
                LOG.error("Unexpected exception while running " + getClass().getName(), e);
                pollStatus = PollStatus.unavailable("Unexpected exception: " + e.getMessage());
            } finally {
                IOUtils.closeQuietly(clientWrapper);
            }
            return pollStatus;
        });

        return future.thenApply(status -> ServiceMonitorResponseImpl.builder().status(pollStatusMapper.get(status)).build());
    }

    private boolean inRange(String range,Integer val){
        String[] boundries = range.split("-");
        if(val < Integer.valueOf(boundries[0]) || val > Integer.valueOf(boundries[1]))
            return false;
        else
            return true;
    }
}
