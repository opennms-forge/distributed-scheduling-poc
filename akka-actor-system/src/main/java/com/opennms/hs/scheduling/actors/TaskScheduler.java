/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package com.opennms.hs.scheduling.actors;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import com.opennms.hs.scheduling.messages.Workflow;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Cancellable;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TaskScheduler extends AbstractActor {
  ActorRef taskActor;
  Map<String, Cancellable> scheduledMap = new HashMap<>();

  private TaskScheduler(ActorRef taskActor) {
    this.taskActor = taskActor;
  }

  @Override
  public Receive createReceive() {
    return receiveBuilder()
        .match(Workflow.class, w-> {
          log.info("{} workflow stared", w.getUuid());
          startSchedule(w);
        })
        .matchAny(m->log.info("Received unknown message"))
        .build();
  }

  private void startSchedule(Workflow workFlow) {
    ActorSystem system = context().system();
    Cancellable schedule = system
        .scheduler()
        .scheduleAtFixedRate(Duration.ZERO, Duration.ofMillis(workFlow.getCron()), taskActor, workFlow, system.dispatcher(), ActorRef.noSender());
    scheduledMap.put(workFlow.getUuid(), schedule);
  }

  private void cancelSchedule(Workflow workFlow) {
    //stop any existing schedule
    Cancellable cancellable = scheduledMap.get(workFlow.getUuid());
    if(cancellable != null) {
      cancellable.cancel();
      scheduledMap.remove(workFlow.getUuid());
    }
  }
}
