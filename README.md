# Jenkins Pipeline: PipelineTerraformKitchen

## Description

This project is intended for use with [Jenkins](https://jenkins.io/) and Global Pipeline Libraries through the
[Pipeline Shared Groovy Libraries Plugin](https://wiki.jenkins.io/display/JENKINS/Pipeline+Shared+Groovy+Libraries+Plugin).

A common scenario when developing Jenkins [declarative pipelines](https://jenkins.io/doc/book/pipeline/syntax/), is
to bundle common custom pipeline tasks in a shared library so that all Jenkins pipeline configurations in an organisation
can leverage from them without the need to reimplement the same logic.

## Pipeline Usage & Workflow

### Jenkinsfile

To use this pipeline all that is required is adding _PipelineTerraformKitchen()_ into a file named `Jenkinsfile` in the root of your
git repository.

### Parameters

#### Required

Currently, there are _NO_ required parameters for this pipeline.

#### Optional

* _terraformDebugOutput_: [`TRACE`, `WARN`, `ERROR`, `DEBUG`] - Enable Terraform debugging output (defaults to `NULL`).
* _terraformDestroyTimeout_: `timeout` (in minutes) - Set a Terraform destroy timeout (defaults to `30` minutes).

### Examples

`Jenkinsfile`

```groovy
PipelineTerraformKitchen()
```

---

`Jenkinsfile`

```groovy
PipelineTerraformKitchen(terraformDebugOutput: 'DEBUG', terraformDestroyTimeout: '45')
```

### Additional Required Kitchen & Terraform Variable Files

This pipeline integrates with Kitchen Terraform and thus expects that the following files be present alongside
the `Jenkinsfile`:

* `.kitchen.yml`
* `testing.tfvars`

### Workflow

This pipeline follows a defined behavior and will perform the following workflow.

#### Non Master Branch

This pipeline will execute Kitchen workflow, performing kitchen converge, kitchen verify and kitchen destroy.
If the optional pipeline parameters `terraformDebugOutput` and `terraformDestroyTimeout` are present, this pipeline will retrieve the parameters and enhance debug level and set a timeout to execute Kitchen destroy.  If `terraformDebugOutput` is not present it will not provide enhanced debugging log.  If `terraformDestroyTimeout` is not present it will default to 30 minutes timeout to execute Kitchen destroy.

#### Master Branch

This pipeline will execute Kitchen workflow, performing kitchen converge, kitchen verify and kitchen destroy.
If the optional pipeline parameters `terraformDebugOutput` and `terraformDestroyTimeout` are present, this pipeline will retrieve the parameters and enhance debug level and set a timeout to execute Kitchen destroy.  If `terraformDebugOutput` is not present it will not provide enhanced debugging log.  If `terraformDestroyTimeout` is not present it will default to 30 minutes timeout to execute Kitchen destroy.

**This pipeline awaits user approval** to proceed with running Kitchen destroy but if not provided a default timeout of 30 minutes is defined to execute Kitchen destroy.
