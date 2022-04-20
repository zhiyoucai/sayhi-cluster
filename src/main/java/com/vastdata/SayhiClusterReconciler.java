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

import java.util.*;

public class SayhiClusterReconciler implements Reconciler<SayhiCluster> {
  private static final Logger LOGGER = LoggerFactory.getLogger(SayhiClusterReconciler.class);
  private final KubernetesClient client;

  private String image;

  public SayhiClusterReconciler(KubernetesClient client) {
    this.client = client;
  }

  // TODO Fill in the rest of the reconciler

  @Override
  public UpdateControl<SayhiCluster> reconcile(SayhiCluster resource, Context context) {
    // TODO: fill in logic
    // 先读取出CR定义的元数据，现在只有两个分别是副本数和Docker镜像
    var size = resource.getSpec().getSize();
    image = resource.getSpec().getImage();

    // 新建一个Deployment和一个Service
    Deployment deployment = buildDeployment(size);
    Service service = buildService();

    // 拿到刚才新建的Deployment和Service的资源信息
    final var deploymentResource = client.resources(Deployment.class).withName("sayhi");
    final var serviceResource = client.resources(Service.class).withName("hello-sayhi");

    // 判断Deployment和Service是否存在
    final var deploymentExisting = deploymentResource.get();
    final var serviceExisting = serviceResource.get();

    // 得到Deployment的副本数量
    var replicaSize = deploymentResource.get().getStatus().getReplicas();
    LOGGER.info("Deployment {} has {} replicas", deploymentResource.get().getMetadata().getName(), replicaSize);
    try {
      // 如果Deployment不存在就创建
      // 如果副本数和CR定义的不一致，也修改
      if (deploymentExisting == null || !checkReplicas(replicaSize, size)) {
        deploymentResource.createOrReplace(deployment);
      }
      if (serviceExisting == null) {
        serviceResource.create(service);
      }
    } catch (Exception e) {
      LOGGER.error("", e);
    }
    return UpdateControl.noUpdate();
  }

  /**
   * 检查Deployment的实际副本数和期望副本数是否相等
   * @param size Deployment的实际副本数
   * @param expectSize 期望的副本数
   * @return 如果不相等则返回false
   */
  private boolean checkReplicas(Integer size, Integer expectSize) {
    return Objects.equals(size, expectSize);
  }

  private Service buildService() {
    Service service = new Service();
    service.setApiVersion("v1");
    service.setKind(ResourceTypeEnum.SERVICE.getType());
    ObjectMeta serviceMeta = new ObjectMeta();
    serviceMeta.setName("hello-sayhi");
    service.setMetadata(serviceMeta);
    service.setSpec(buildServiceSpec());
    return service;
  }

  private Deployment buildDeployment(Integer size) {
    Deployment deployment = new Deployment();
    deployment.setApiVersion("apps/v1");
    deployment.setKind(ResourceTypeEnum.DEPLOYMENT.getType());
    deployment.setSpec(buildDeploymentSpec(size));
    ObjectMeta meta = new ObjectMeta();
    meta.setName("sayhi");
    deployment.setMetadata(meta);
    return deployment;
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

  private DeploymentSpec buildDeploymentSpec(Integer size) {
    DeploymentSpec deploymentSpec = new DeploymentSpec();
    deploymentSpec.setReplicas(size);
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
    container.setImage(image);
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

