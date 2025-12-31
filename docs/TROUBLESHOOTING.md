# Troubleshooting Reference Guide

This document contains a collection of helpful commands used for debugging Helm deployments, Kubernetes resources, and observability setup.

## Helm Commands

### Chart Management
- **List installed releases**: `helm list`
- **Show default values of a chart**: `helm show values <chart-path-or-repo-name>`
- **Check chart for issues**: `helm lint <chart-path>`
- **Preview templates before installing**:
  ```bash
  helm template <release-name> <chart-path> --set key=value
  ```
- **Install/Upgrade a release**:
  ```bash
  helm upgrade --install <release-name> <chart-path> -f values.yaml -n <namespace>
  ```

### Repository Management
- **List added repositories**: `helm repo list`
- **Search for a chart**: `helm search repo <keyword>`
- **Update repository metadata**: `helm repo update`

---

## Kubernetes Debugging

### Resource Inspection
- **Get pods in a namespace**: `kubectl get pods -n <namespace>`
- **Monitor pod status**: `kubectl get pods -n <namespace> -w`
- **Check pod details (events/configs)**: `kubectl describe pod <pod-name> -n <namespace>`
- **View service status**: `kubectl get svc -n <namespace>`
- **View ConfigMap content**: `kubectl get configmap <name> -n <namespace> -o yaml`

### Troubleshooting Logs
- **View container logs**: `kubectl logs <pod-name> -n <namespace>`
- **Follow logs in real-time**: `kubectl logs -f <pod-name> -n <namespace>`
- **Logs for a multi-container pod**: `kubectl logs <pod-name> -n <namespace> -c <container-name>`
- **View last N lines**: `kubectl logs <pod-name> -n <namespace> --tail=50`

### Connectivity Testing
- **Spin up a temporary test pod**:
  ```bash
  kubectl run busybox --image=busybox --restart=Never -it -- /bin/sh
  ```
- **Inside the test pod (testing URLs)**:
  ```bash
  wget -qO- http://<service-name>.<namespace>.svc.cluster.local:<port>/health
  ```
- **Port-forward to local machine**:
  ```bash
  kubectl port-forward <pod-name> <local-port>:<pod-port>
  ```

---

## Secret Management

### CLI Decoding
- **Decode a specific key**:
  ```bash
  kubectl get secret <name> -n <namespace> -o jsonpath="{.data.<key>}" | base64 --decode
  ```
- **Decode all keys in a secret (requires `jq`)**:
  ```bash
  kubectl get secret <name> -n <namespace> -o json | jq '.data | map_values(@base64d)'
  ```

### Manual Creation
- **Create secret from literal values**:
  ```bash
  kubectl create secret generic <name> --from-literal=key1=val1 --from-literal=key2=val2 -n <namespace>
  ```

---

## Maintenance
- **Force pod restart (DaemonSet)**: `kubectl rollout restart daemonset <name> -n <namespace>`
- **Force pod restart (Deployment)**: `kubectl rollout restart deployment <name> -n <namespace>`
- **Delete a stuck pod**: `kubectl delete pod <name> -n <namespace> --force`
