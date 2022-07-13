#!/bin/bash

USER=admin

kubectl exec -it deployment.apps/poc-distributed-scheduling -- /bin/bash "$@"
