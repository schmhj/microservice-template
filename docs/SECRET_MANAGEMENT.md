# Secret Management Automation Guide

This guide explains how to automate the management of sensitive credentials like the Grafana Cloud Bearer Token across multiple Kubernetes clusters.

## Strategy 1: CI/CD Variable Injection (Current)

This approach uses your CI/CD pipeline (GitHub Actions, GitLab CI, Jenkins) to inject secrets as environment variables during the Helm deployment.

### How it works
1.  **Store Secrets**: Save your credentials (`MIMIR_USER`, `GRAFANA_TOKEN`, etc.) as "Secret Variables" in your CI/CD platform.
2.  **Deploy**: Run `helm upgrade --install` and use the `--set` flag to pass these variables from the CI/CD environment into the `alloy-secrets` chart.

### Command Example
```bash
helm upgrade --install alloy-secrets ./helm/alloy-secrets \
  --namespace monitoring \
  --set mimirUser="$GRAFANA_MIMIR_ID" \
  --set lokiUser="$GRAFANA_LOKI_ID" \
  --set tempoUser="$GRAFANA_TEMPO_ID" \
  --set grafanaToken="$GRAFANA_CLOUD_TOKEN"
```

### Pros/Cons
- **Pros**: Easy to set up, standard for most teams, no extra tools needed on the cluster.
- **Cons**: Secrets are still "plain text" in the CI/CD memory during execution.

---

## Strategy 2: GitOps with Sealed Secrets (Next Step)

This approach allows you to store encrypted secrets safely inside your Git repository. Only the cluster's controller can decrypt them.

### Prerequisites
1.  Install the **Sealed Secrets Controller** on your cluster.
2.  Install the **kubeseal** CLI tool on your local machine.

### Workflow
1.  **Create a local Secret YAML**:
    ```bash
    kubectl create secret generic grafana-cloud-credentials \
      --from-literal=GRAFANA_TOKEN="your-token" \
      --dry-run=client -o yaml > secret.yaml
    ```
2.  **Encrypt (Seal) the Secret**:
    ```bash
    kubeseal --format yaml < secret.yaml > sealed-secret.yaml
    ```
3.  **Commit `sealed-secret.yaml`**: You can safely push this to your public or private Git repo.
4.  **Deploy**: Apply the file to your cluster: `kubectl apply -f sealed-secret.yaml`. The controller will automatically create a standard Kubernetes Secret from it.

### Pros/Cons
- **Pros**: Follows "GitOps" principles (everything is in Git), highly secure, works perfectly with tools like ArgoCD.
- **Cons**: Requires installing a controller on the cluster, depends on the cluster's private key.

---

## Pro-tips: Managing Secrets via CLI

If you ever need to inspect or verify your secrets manually in the terminal:

### Decoding a specific key:
```bash
kubectl get secret grafana-cloud-credentials -n monitoring -o jsonpath="{.data.GRAFANA_TOKEN}" | base64 --decode
```

### Decoding all keys at once (requires `jq`):
```bash
kubectl get secret grafana-cloud-credentials -n monitoring -o json | jq '.data | map_values(@base64d)'
```

## Summary Comparison
| Feature | Strategy 1 (CI/CD) | Strategy 2 (Sealed Secrets) |
| :--- | :--- | :--- |
| **Storage Location** | CI/CD Platform (Settings) | Git Repository (Encrypted) |
| **Deployment Trigger** | CI/CD Pipeline | `kubectl apply` or GitOps tool |
| **Tooling Needed** | Helm | kubeseal + Controller |
| **Ease of Use** | High | Medium |
| **Security** | Good | Excellent |
