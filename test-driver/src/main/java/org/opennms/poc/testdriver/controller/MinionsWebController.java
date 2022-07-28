package org.opennms.poc.testdriver.controller;

import static j2html.TagCreator.*;

import java.util.List;
import org.opennms.core.ipc.grpc.server.manager.MinionInfo;
import org.opennms.core.ipc.grpc.server.manager.MinionManager;
import org.opennms.poc.testdriver.view.CodedView;
import org.opennms.poc.testdriver.view.ViewRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/minions")
public class MinionsWebController {

  private final MinionManager minionManager;
  private final ViewRegistry viewRegistry;

  public MinionsWebController(ViewRegistry viewRegistry, MinionManager minionManager) {
    viewRegistry.register("/minions", "Minions");
    this.viewRegistry = viewRegistry;
    this.minionManager = minionManager;
  }

  @RequestMapping(method = RequestMethod.GET)
  ModelAndView getMinions() {
    List<MinionInfo> minions = minionManager.getMinions();
    return new ModelAndView(CodedView.supplied(() -> {
      return html(
        head(
          title("Minions")
        ),
        body(
          viewRegistry.menu(),
          h1("Minions"),
          table(
            tr(
              th("id"),
              th("location")
            ),
            each(minions, minion -> tr(
              td(minion.getId()),
              td(minion.getLocation())
            ))
          ).attr("border", 1).attr("cellspacing", 2).attr("cellpadding", 2).withStyle("width: 100%;")
        )
      );
    }));
  }

}
