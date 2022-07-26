package org.opennms.poc.testdriver.view;

import j2html.rendering.IndentedHtml;
import j2html.tags.Renderable;
import java.io.PrintWriter;
import java.util.Map;
import java.util.concurrent.Callable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import org.springframework.web.servlet.View;

public abstract class CodedView implements View {

  @Override
  public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
    Renderable renderable = create(model);
    if (renderable != null) {
      response.setHeader("Content-Type", MediaType.TEXT_HTML);
      IndentedHtml<PrintWriter> renderer = IndentedHtml.into(response.getWriter());
      renderable.render(renderer).flush();
    }
  }

  protected abstract Renderable create(Map<String, ?> model) throws Exception;

  public static CodedView supplied(Callable<Renderable> renderer) {
    return new CodedView() {
      @Override
      protected Renderable create(Map<String, ?> model) throws Exception {
        return renderer.call();
      }
    };
  }

}
