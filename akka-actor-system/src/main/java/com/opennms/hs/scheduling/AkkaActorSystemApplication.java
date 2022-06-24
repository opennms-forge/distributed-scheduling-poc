package com.opennms.hs.scheduling;

import java.util.List;
import java.util.stream.IntStream;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import com.opennms.hs.scheduling.actors.TaskScheduler;
import com.opennms.hs.scheduling.actors.WorkflowTaskActor;
import com.opennms.hs.scheduling.messages.Network;
import com.opennms.hs.scheduling.messages.Workflow;
import com.opennms.hs.scheduling.messages.WorkflowGenerator;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.routing.RoundRobinPool;
import akka.routing.RoundRobinRoutingLogic;

@SpringBootApplication
public class AkkaActorSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(AkkaActorSystemApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void startSystem() {
		ActorSystem system = ActorSystem.create("HS-Scheduling");
		ActorRef taskRouter = system.actorOf(new RoundRobinPool(10).props(Props.create(WorkflowTaskActor.class)), "taskRouter"); //TODO this one can be a cluster
		ActorRef scheduler = system.actorOf(Props.create(TaskScheduler.class, taskRouter), "scheduler");
		WorkflowGenerator generator = new WorkflowGenerator(Network.ofSize(Network.NetworkSize.MEDIUM)); //Todo this can be triggered from remote system.
		List<Workflow> workflows = generator.getWorkflows();
		workflows.forEach(w->scheduler.tell(w, ActorRef.noSender()));
	}
}
