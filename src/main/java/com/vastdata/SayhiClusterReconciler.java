package com.vastdata;

import com.vastdata.constants.ImagePullPolicyEnum;
import com.vastdata.constants.ResourceTypeEnum;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentSpec;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.Reconciler;
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SayhiClusterReconciler implements Reconciler<SayhiCluster> {
  private static final Logger LOGGER = LoggerFactory.getLogger(SayhiClusterReconciler.class);
  private final KubernetesClient client;

  public SayhiClusterReconciler(KubernetesClient client) {
    this.client = client;
  }

  // TODO Fill in the rest of the reconciler

  @Override
  public UpdateControl<SayhiCluster> reconcile(SayhiCluster resource, Context context) {
    // TODO: fill in logic
    Deployment deployment = new Deployment();
    deployment.setApiVersion("apps/v1");
    deployment.setKind(ResourceTypeEnum.DEPLOYMENT.getType());
    deployment.setSpec(buildDeploymentSpec());
    ObjectMeta meta = new ObjectMeta();
    meta.setName("sayhi");
    deployment.setMetadata(meta);

    Service service = new Service();
    service.setApiVersion("v1");
    service.setKind(ResourceTypeEnum.SERVICE.getType());
    ObjectMeta serviceMeta = new ObjectMeta();
    serviceMeta.setName("hello-sayhi");
    service.setMetadata(serviceMeta);
    service.setSpec(buildServiceSpec());

    final var deploymentResource = client.resources(Deployment.class).withName("sayhi");
    final var serviceResource = client.resources(Service.class).withName("hello-sayhi");
    final var deploymentExisting = deploymentResource.get();
    final var serviceExisting = serviceResource.get();
    try {
      if (deploymentExisting == null) {
        deploymentResource.create(deployment);
      }
      if (serviceExisting == null) {
        serviceResource.create(service);
      }
    } catch (Exception e) {
      LOGGER.error("", e);
    }
    return UpdateControl.noUpdate();
  }

  private ServiceSpec buildServiceSpec() {
    ServicePort servicePort = new ServicePort();
    servicePort.setName("http");
    servicePort.setPort(9090);
    IntOrString intOrString = new IntOrString();
    intOrString.setIntVal(9090);
    servicePort.setTargetPort(intOrString);
    servicePort.setNodePort(30780);
    servicePort.setProtocol("TCP");
    List<ServicePort> servicePorts = new ArrayList<>();
    servicePorts.add(servicePort);

    ServiceSpec serviceSpec = new ServiceSpec();
    serviceSpec.setType("NodePort");
    Map<String, String> selector = new HashMap<>();
    selector.put("app", "sayhi");
    serviceSpec.setSelector(selector);
    serviceSpec.setPorts(servicePorts);
    return serviceSpec;
  }

  private DeploymentSpec buildDeploymentSpec() {
    DeploymentSpec deploymentSpec = new DeploymentSpec();
    deploymentSpec.setSelector(buildLabelSelector());
    deploymentSpec.setTemplate(buildPodTemplateSpec());
    return deploymentSpec;
  }


  private PodTemplateSpec buildPodTemplateSpec() {
    PodTemplateSpec podTemplateSpec = new PodTemplateSpec();
    podTemplateSpec.setSpec(buildPodSpec());
    podTemplateSpec.setMetadata(buildObjectMeta());
    return podTemplateSpec;
  }

  private PodSpec buildPodSpec() {
    ContainerPort containerPort = new ContainerPort();
//    containerPort.setHostPort(9090);
    containerPort.setContainerPort(9090);
    List<ContainerPort> containerPorts = new ArrayList<>();
    containerPorts.add(containerPort);

    Container container = new Container();
    container.setImage("youcai/sayhi:1.0.0");
    container.setName("sayhi");
    container.setImagePullPolicy(ImagePullPolicyEnum.IF_NOT_PRESENT.getPolicy());
    container.setPorts(containerPorts);

    List<Container> containers = new ArrayList<>();
    containers.add(container);

    PodSpec podSpec = new PodSpec();
    podSpec.setContainers(containers);
    LOGGER.info("container port is {}", containerPort);
    return podSpec;
  }

  private ObjectMeta buildObjectMeta() {
    ObjectMeta objectMeta = new ObjectMeta();
    Map<String, String> labels = new HashMap<>();
    labels.put("app", "sayhi");
    objectMeta.setLabels(labels);
    return objectMeta;
  }

  private LabelSelector buildLabelSelector() {
    LabelSelector labelSelector = new LabelSelector();
    Map<String, String> labels = new HashMap<>();
    labels.put("app", "sayhi");
    labelSelector.setMatchLabels(labels);
    return labelSelector;
  }

}

