package org.opennms.poc.testdriver.controller;

import static j2html.TagCreator.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.opennms.core.ipc.grpc.server.manager.MinionInfo;
import org.opennms.core.ipc.grpc.server.manager.MinionManager;
import org.opennms.poc.ignite.model.workflows.Workflow;
import org.opennms.poc.ignite.model.workflows.Workflows;
import org.opennms.poc.testdriver.view.CodedView;
import org.opennms.poc.testdriver.view.ViewRegistry;
import org.opennms.poc.testdriver.workflow.WorkflowManager;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/")
public class TaskSetsController {

  private final MinionManager minionManager;
  private final WorkflowManager workflowManager;
  private final ViewRegistry viewRegistry;

  public TaskSetsController(ViewRegistry viewRegistry, MinionManager minionManager, WorkflowManager workflowManager) {
    viewRegistry.register("/", "Task sets");
    this.viewRegistry = viewRegistry;
    this.minionManager = minionManager;
    this.workflowManager = workflowManager;
  }

  @RequestMapping(method = RequestMethod.GET)
  ModelAndView getTasks() {
    List<MinionInfo> minions = minionManager.getMinions();
    Set<Workflow> workflows = workflowManager.getAll().stream().flatMap(w -> w.getWorkflows().stream())
        .collect(Collectors.toSet());
    return new ModelAndView(CodedView.supplied(() -> {
      return html(
        head(
          title("Task sets")
        ),
        body(
          viewRegistry.menu(),
          h1("Task sets"),
          table(
            tr(
              th("Task id"),
              th("Type"),
              th("Description"),
              th("Plugin")
            ),
          each(workflows, (workflow) -> tr(
            td(workflow.getUuid()),
            td(workflow.getType().name()),
            td(workflow.getDescription()),
            td(workflow.getPluginName())
          ))
          ).attr("border", 1).attr("cellspacing", 2).attr("cellpadding", 2).withStyle("width: 100%;")
        )
      );
    }));
  }

}
