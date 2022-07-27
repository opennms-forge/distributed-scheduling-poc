package org.opennms.poc.testdriver.controller;

import static j2html.TagCreator.*;

import java.util.List;
import org.opennms.core.ipc.grpc.server.manager.MinionInfo;
import org.opennms.core.ipc.grpc.server.manager.MinionManager;
import org.opennms.poc.testdriver.view.CodedView;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/minions")
public class Minions {

  private final MinionManager minionManager;

  public Minions(MinionManager minionManager) {
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
          ).withStyle("border: 1px solid gray; width: 250px;")
        )
      );
    }));
  }

}
