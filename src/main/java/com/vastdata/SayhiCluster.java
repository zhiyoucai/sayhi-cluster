package com.vastdata;

import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Version;

@Version("v1alpha1")
@Group("sayhi.vastdata.com")
public class SayhiCluster extends CustomResource<SayhiClusterSpec, SayhiClusterStatus> implements Namespaced {}

