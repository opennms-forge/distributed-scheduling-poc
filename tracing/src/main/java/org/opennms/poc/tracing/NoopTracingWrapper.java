package org.opennms.poc.tracing;

import io.opentracing.Tracer;
import io.opentracing.noop.NoopTracer;
import io.opentracing.noop.NoopTracerFactory;
import org.opennms.core.tracing.api.TracerWrapper;

public class NoopTracingWrapper implements TracerWrapper {

  private final NoopTracer tracer = NoopTracerFactory.create();

  @Override
  public Tracer init(String s) {
    return tracer;
  }

}
