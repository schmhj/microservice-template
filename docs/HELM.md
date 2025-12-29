# Helm Guide

This guide provides comprehensive instructions for working with Helm, the package manager for Kubernetes.

## Table of Contents

- [Installation](#installation)
- [Repository Management](#repository-management)
- [Chart Operations](#chart-operations)
- [Creating and Customizing Charts](#creating-and-customizing-charts)
- [Packaging and Distribution](#packaging-and-distribution)
- [Best Practices](#best-practices)

## Installation

### Install Helm

**macOS:**
```bash
brew install helm
```

**Linux:**
```bash
curl https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash
```

**Windows (using Chocolatey):**
```bash
choco install kubernetes-helm
```

### Verify Installation

```bash
helm version
```

## Repository Management

### Add a Repository

Add the official Helm stable charts repository:
```bash
helm repo add stable https://charts.helm.sh/stable
```

Add Bitnami charts (popular choice):
```bash
helm repo add bitnami https://charts.bitnami.com/bitnami
```

### Update Repositories

Always update your repositories to get the latest chart versions:
```bash
helm repo update
```

### List Repositories

```bash
helm repo list
```

### Search Repository

Search all repositories for a chart:
```bash
helm search repo nginx
```

Search with version constraints:
```bash
helm search repo nginx --version "^1.0.0"
```

Search all versions:
```bash
helm search repo nginx --versions
```

### Remove a Repository

```bash
helm repo remove bitnami
```

## Chart Operations

### Install a Chart

Basic installation:
```bash
helm install my-release bitnami/nginx
```

Install with custom values file:
```bash
helm install my-release bitnami/nginx -f values.yaml
```

Install with inline value overrides:
```bash
helm install my-release bitnami/nginx \
  --set service.type=LoadBalancer \
  --set image.tag=1.21.0
```

Install in a specific namespace:
```bash
helm install my-release bitnami/nginx -n production --create-namespace
```

Dry run (validate without installing):
```bash
helm install my-release bitnami/nginx --dry-run --debug
```

### Upgrade a Release

Upgrade with new values:
```bash
helm upgrade my-release bitnami/nginx -f values-updated.yaml
```

Upgrade and install if not exists:
```bash
helm upgrade --install my-release bitnami/nginx
```

Upgrade with inline overrides:
```bash
helm upgrade my-release bitnami/nginx --set replicaCount=3
```

Force upgrade (recreate resources):
```bash
helm upgrade my-release bitnami/nginx --force
```

### Rollback a Release

Rollback to previous version:
```bash
helm rollback my-release
```

Rollback to specific revision:
```bash
helm rollback my-release 2
```

### Uninstall a Release

```bash
helm uninstall my-release
```

Uninstall and keep history:
```bash
helm uninstall my-release --keep-history
```

### List Releases

List all releases in current namespace:
```bash
helm list
```

List releases in all namespaces:
```bash
helm list --all-namespaces
```

List all releases including uninstalled:
```bash
helm list --uninstalled
```

### Get Release Information

Get manifest:
```bash
helm get manifest my-release
```

Get values:
```bash
helm get values my-release
```

Get all information:
```bash
helm get all my-release
```

### View Release History

```bash
helm history my-release
```

## Creating and Customizing Charts

### Create a New Chart

Generate a new chart scaffold:
```bash
helm create microservice
```

This creates the following structure:
```
microservice/
├── Chart.yaml              # Chart metadata
├── values.yaml             # Default values
├── charts/                 # Dependencies
├── templates/              # Kubernetes manifests
│   ├── deployment.yaml
│   ├── service.yaml
│   ├── serviceaccount.yaml
│   ├── ingress.yaml
│   ├── hpa.yaml
│   ├── _helpers.tpl       # Template helpers
│   ├── NOTES.txt          # Post-install notes
│   └── tests/
│       └── test-connection.yaml
└── .helmignore            # Files to ignore
```

### Update Chart Metadata

Edit `Chart.yaml`:
```yaml
apiVersion: v2
name: microservice
description: Spring Boot microservice with DDD and Clean Architecture
type: application
version: 0.1.0              # Chart version
appVersion: "1.0.0"         # Application version

dependencies:
  - name: postgresql
    version: "12.x.x"
    repository: https://charts.bitnami.com/bitnami
    condition: postgresql.enabled
```

### Customize Default Templates

#### Update `values.yaml`

Define your application-specific defaults:
```yaml
replicaCount: 2

image:
  repository: myregistry/microservice
  pullPolicy: IfNotPresent
  tag: "latest"

service:
  type: ClusterIP
  port: 80

ingress:
  enabled: true
  className: nginx
  annotations:
    cert-manager.io/cluster-issuer: letsencrypt-prod
  hosts:
    - host: api.example.com
      paths:
        - path: /
          pathType: Prefix

resources:
  limits:
    cpu: 500m
    memory: 512Mi
  requests:
    cpu: 250m
    memory: 256Mi

autoscaling:
  enabled: true
  minReplicas: 2
  maxReplicas: 10
  targetCPUUtilizationPercentage: 80
```

#### Update `templates/deployment.yaml`

Add Spring Boot specific environment variables:
```yaml
env:
  - name: SPRING_PROFILES_ACTIVE
    value: {{ .Values.appProfile }}
  - name: SPRING_DATASOURCE_URL
    value: {{ .Values.datasource.url }}
  - name: SPRING_DATASOURCE_USERNAME
    valueFrom:
      secretKeyRef:
        name: {{ include "microservice.fullname" . }}-db
        key: username
  - name: SPRING_DATASOURCE_PASSWORD
    valueFrom:
      secretKeyRef:
        name: {{ include "microservice.fullname" . }}-db
        key: password
```

#### Add Named Templates in `_helpers.tpl`

```yaml
{{/*
Common labels
*/}}
{{- define "microservice.labels" -}}
helm.sh/chart: {{ include "microservice.chart" . }}
{{ include "microservice.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/*
Selector labels
*/}}
{{- define "microservice.selectorLabels" -}}
app.kubernetes.io/name: {{ include "microservice.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}
```

### Validate Your Chart

Lint the chart:
```bash
helm lint ./microservice
```

Template the chart (view rendered manifests):
```bash
helm template my-release ./microservice
```

Template with specific values:
```bash
helm template my-release ./microservice -f values-prod.yaml
```

Dry run installation:
```bash
helm install my-release ./microservice --dry-run --debug
```

## Packaging and Distribution

### Package a Chart

Create a versioned archive:
```bash
helm package ./microservice
```

This creates: `microservice-0.1.0.tgz`

Package with specific version:
```bash
helm package ./microservice --version 1.2.3
```

Package with app version:
```bash
helm package ./microservice --app-version 2.0.0
```

### Update Chart Dependencies

Download dependencies defined in `Chart.yaml`:
```bash
helm dependency update ./microservice
```

List dependencies:
```bash
helm dependency list ./microservice
```

### Distribution via Git Repository

**Option 1: Store packaged charts**

1. Create a `charts` directory in your repository
2. Package and move charts:
   ```bash
   helm package ./microservice -d ./charts
   ```
3. Generate index file:
   ```bash
   helm repo index ./charts --url https://mycompany.github.io/helm-charts
   ```
4. Commit and push:
   ```bash
   git add charts/
   git commit -m "Add microservice chart v0.1.0"
   git push
   ```

5. Users can add your repo:
   ```bash
   helm repo add mycompany https://mycompany.github.io/helm-charts
   helm install my-app mycompany/microservice
   ```

**Option 2: Use GitHub Pages**

1. Create a `gh-pages` branch
2. Package charts and generate index
3. GitHub automatically serves files from `gh-pages`

**Option 3: Direct chart installation from Git**

Users can install directly from Git:
```bash
git clone https://github.com/mycompany/charts.git
helm install my-release ./charts/microservice
```

### Distribution via Artifactory/Harbor

**JFrog Artifactory:**

1. Configure Helm to use Artifactory:
   ```bash
   helm repo add artifactory https://artifactory.company.com/artifactory/helm-local \
     --username myuser --password mypassword
   ```

2. Push chart to Artifactory:
   ```bash
   curl -u myuser:mypassword -T microservice-0.1.0.tgz \
     "https://artifactory.company.com/artifactory/helm-local/microservice-0.1.0.tgz"
   ```

   Or using Helm plugin:
   ```bash
   helm plugin install https://github.com/belitre/helm-push-artifactory-plugin
   helm push-artifactory microservice-0.1.0.tgz artifactory
   ```

**Harbor:**

1. Log in to Harbor:
   ```bash
   helm registry login harbor.company.com
   ```

2. Package and push:
   ```bash
   helm package ./microservice
   helm push microservice-0.1.0.tgz oci://harbor.company.com/library
   ```

3. Install from Harbor:
   ```bash
   helm install my-release oci://harbor.company.com/library/microservice --version 0.1.0
   ```

### Semantic Versioning

Follow semantic versioning for chart versions:

- **MAJOR** (1.0.0): Incompatible API changes
- **MINOR** (0.1.0): Backward-compatible functionality
- **PATCH** (0.0.1): Backward-compatible bug fixes

Update `Chart.yaml` accordingly:
```yaml
version: 1.2.3  # Chart version (semver)
appVersion: "2.5.1"  # Application version (can be any format)
```

## Best Practices

### Values File Organization

Create environment-specific values files:
```
helm/microservice/
├── values.yaml           # Defaults
├── values-dev.yaml       # Development overrides
├── values-staging.yaml   # Staging overrides
└── values-prod.yaml      # Production overrides
```

Install with specific environment:
```bash
helm install my-release ./microservice -f values-prod.yaml
```

### Template Best Practices

1. **Use named templates** for repeated logic in `_helpers.tpl`
2. **Add comments** to complex template logic
3. **Validate required values**:
   ```yaml
   {{- required "A valid .Values.image.repository is required!" .Values.image.repository }}
   ```
4. **Use default function** for optional values:
   ```yaml
   replicas: {{ .Values.replicaCount | default 1 }}
   ```

### Security

1. **Store secrets in Kubernetes secrets**, not values files
2. **Use `.helmignore`** to exclude sensitive files
3. **Scan charts** for vulnerabilities:
   ```bash
   helm plugin install https://github.com/chartmuseum/helm-chartmuseum
   ```

### Testing

Create test resources in `templates/tests/`:
```yaml
apiVersion: v1
kind: Pod
metadata:
  name: "{{ include "microservice.fullname" . }}-test"
  annotations:
    "helm.sh/hook": test
spec:
  containers:
    - name: wget
      image: busybox
      command: ['wget']
      args: ['{{ include "microservice.fullname" . }}:{{ .Values.service.port }}']
  restartPolicy: Never
```

Run tests:
```bash
helm test my-release
```

### Documentation

Update `templates/NOTES.txt` with post-installation instructions:
```
Thank you for installing {{ .Chart.Name }}!

Your release is named {{ .Release.Name }}.

To access your application:
{{- if .Values.ingress.enabled }}
  http://{{ index .Values.ingress.hosts 0 "host" }}
{{- else }}
  export POD_NAME=$(kubectl get pods -l "app.kubernetes.io/name={{ include "microservice.name" . }}" -o jsonpath="{.items[0].metadata.name}")
  kubectl port-forward $POD_NAME 8080:80
{{- end }}
```

### Debugging

Enable debug output:
```bash
helm install my-release ./microservice --debug
```

View rendered templates without installing:
```bash
helm template my-release ./microservice --debug
```

Get Kubernetes events:
```bash
kubectl get events --sort-by=.metadata.creationTimestamp
```

## Quick Reference

| Command | Description |
|---------|-------------|
| `helm create NAME` | Create new chart |
| `helm package CHART` | Package chart into archive |
| `helm lint CHART` | Validate chart |
| `helm template RELEASE CHART` | Render templates locally |
| `helm install RELEASE CHART` | Install a chart |
| `helm upgrade RELEASE CHART` | Upgrade a release |
| `helm rollback RELEASE [REVISION]` | Rollback to previous version |
| `helm uninstall RELEASE` | Uninstall a release |
| `helm list` | List releases |
| `helm repo add NAME URL` | Add chart repository |
| `helm repo update` | Update repository index |
| `helm search repo KEYWORD` | Search repositories |
| `helm get values RELEASE` | Get values for release |
| `helm history RELEASE` | View release history |
| `helm test RELEASE` | Run chart tests |

## Additional Resources

- [Official Helm Documentation](https://helm.sh/docs/)
- [Helm Chart Best Practices](https://helm.sh/docs/chart_best_practices/)
- [Artifact Hub](https://artifacthub.io/) - Discover Helm charts
- [Helm GitHub Repository](https://github.com/helm/helm)
