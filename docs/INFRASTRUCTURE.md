# Infrastructure & Helm Chart Guide

This document provides a comprehensive overview of the Helm infrastructure used in this project, explaining how to manage, verify, and extend our deployments.

## Directory Structure

The project uses Helm to manage Kubernetes resources, located in the `helm/` directory:

```text
helm/
├── microservice/        # The core application chart
└── monitoring-agent/   # Umbrella chart for observability (Alloy + Secrets)
```

---

## Chart Deep Dives

### 1. `microservice` Chart
This chart manages the deployment of the Java microservice.

- **Deployment**: Standard Kubernetes Deployment with liveness and readiness probes.
- **Service**: Exposes the application internally on port `9000`.
- **Instrumentation**: Configured for OpenTelemetry (OTLP) to export metrics, traces, and logs to the `monitoring-agent`.
- **Database**: Defaults to an in-memory H2 database for development.
- **Port Management**: Uses a `SERVER_PORT` environment variable to ensure the application port matches the Kubernetes `containerPort` (9000).

### 2. `monitoring-agent` Chart (Umbrella Chart)
This is an "Umbrella Chart" that bundles multiple components into a single management unit.

- **Dependencies**: It depends on the official `grafana/alloy` chart.
- **Secret Management**: It contains a local `templates/secret.yaml` that manages `grafana-cloud-credentials`.
- **Purpose**: Consolidating Alloy and its required secrets into one chart ensures that the credentials are always present when Alloy starts, and allows managing the entire observability stack with a single `helm` command.

---

## Core Helm Concepts

### `Chart.yaml`
The metadata for the chart. It defines the name, version, and most importantly, any **dependencies** (sub-charts).
- When you see a `charts/` directory inside a chart, those are the downloaded dependencies.

### `values.yaml`
The user-facing configuration. 
- In an Umbrella chart like `monitoring-agent`, sub-chart values are nested under the sub-chart's name (e.g., `alloy: { ... }`).
- We use placeholders for sensitive data (like `secrets.grafanaToken: ""`) to be overridden during deployment.

### `templates/`
This folder contains the actual Kubernetes manifest files (YAML). These files use Go templating to inject values from `values.yaml`.

---

## Workflow & Best Practices

### 1. Managing Repositories
Before working with dependencies, ensure you have the required repositories added:
```bash
helm repo add grafana https://grafana.github.io/helm-charts
helm repo update
```

### 2. Dependency Management
If you modify `Chart.yaml` dependencies, you must update the local lock file:
```bash
helm dependency update ./helm/monitoring-agent
```

### 3. Verifying Templates
Always verify your changes before applying them to the cluster.

**Linting (checking for syntax errors):**
```bash
helm lint ./helm/monitoring-agent
```

**Template Rendering (seeing the final YAML):**
```bash
helm template monitoring-agent ./helm/monitoring-agent \
  --namespace monitoring \
  --create-namespace \
  --set secrets.mimirUser="$SECRET-MIMIR-USER" \
  --set secrets.lokiUser="$SECRET-LOKI-USER" \
  --set secrets.tempoUser="$SECRET-TEMPO-USER" \
  --set secrets.grafanaToken="$SECRET-GRAPFANA-TOKEN"
```

### 4. Secret Injection via CI/CD
Never store real credentials in `values.yaml`. Use the `--set` flag in your CI/CD pipeline:
```bash
helm upgrade --install monitoring-agent ./helm/monitoring-agent \
  --set secrets.mimirUser="$SECRET-MIMIR-USER" \
  --set secrets.lokiUser="$SECRET-LOKI-USER" \
  --set secrets.tempoUser="$SECRET-TEMPO-USER" \
  --set secrets.grafanaToken="$SECRET-GRAPFANA-TOKEN"
```

---

## Troubleshooting commands
For a full list of commands used during the setup and debugging of these charts, see [TROUBLESHOOTING.md](TROUBLESHOOTING.md).
