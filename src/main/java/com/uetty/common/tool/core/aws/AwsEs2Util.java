package com.uetty.common.tool.core.aws;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.uetty.common.tool.core.json.fastxml.JacksonUtil;
import com.uetty.common.tool.interfaces.IntIdentifiable;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.ArchitectureValues;
import software.amazon.awssdk.services.ec2.model.AttachmentStatus;
import software.amazon.awssdk.services.ec2.model.BootModeValues;
import software.amazon.awssdk.services.ec2.model.CreateTagsRequest;
import software.amazon.awssdk.services.ec2.model.DeleteVolumeRequest;
import software.amazon.awssdk.services.ec2.model.DescribeImagesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstanceStatusRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstanceStatusResponse;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;
import software.amazon.awssdk.services.ec2.model.DescribeVolumeStatusRequest;
import software.amazon.awssdk.services.ec2.model.DescribeVolumeStatusResponse;
import software.amazon.awssdk.services.ec2.model.DeviceType;
import software.amazon.awssdk.services.ec2.model.EbsInstanceBlockDevice;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.ec2.model.GroupIdentifier;
import software.amazon.awssdk.services.ec2.model.Image;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.InstanceLifecycleType;
import software.amazon.awssdk.services.ec2.model.InstanceState;
import software.amazon.awssdk.services.ec2.model.InstanceStatus;
import software.amazon.awssdk.services.ec2.model.InstanceStatusDetails;
import software.amazon.awssdk.services.ec2.model.InstanceStatusSummary;
import software.amazon.awssdk.services.ec2.model.InstanceType;
import software.amazon.awssdk.services.ec2.model.MonitoringState;
import software.amazon.awssdk.services.ec2.model.PlatformValues;
import software.amazon.awssdk.services.ec2.model.RebootInstancesRequest;
import software.amazon.awssdk.services.ec2.model.Reservation;
import software.amazon.awssdk.services.ec2.model.RunInstancesRequest;
import software.amazon.awssdk.services.ec2.model.RunInstancesResponse;
import software.amazon.awssdk.services.ec2.model.ShutdownBehavior;
import software.amazon.awssdk.services.ec2.model.StartInstancesRequest;
import software.amazon.awssdk.services.ec2.model.StopInstancesRequest;
import software.amazon.awssdk.services.ec2.model.SummaryStatus;
import software.amazon.awssdk.services.ec2.model.Tag;
import software.amazon.awssdk.services.ec2.model.TerminateInstancesRequest;
import software.amazon.awssdk.services.ec2.model.VirtualizationType;
import software.amazon.awssdk.services.ec2.model.VolumeStatusItem;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Slf4j
public class AwsEs2Util {

    @Data
    private static class AwsRegion {
        String accessKey;
        String secret;
        Region region;
    }

    private static class Ec2ClientWrapper {
        Ec2Client ec2Client;

        @Override
        protected void finalize() {
            try {
                // shutdown before gc
                ec2Client.close();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    private static final WeakHashMap<AwsRegion, Ec2ClientWrapper> CLIENT_CACHE = new WeakHashMap<>();

    /**
     * AMI owner - 'self'
     */
    public static final String AMI_OWNER_SELF = "self";
    /**
     * AMI owner - 'amazon'
     */
    public static final String AMI_OWNER_AMAZON = "amazon";
    /**
     * AMI owner - 'aws-marketplace'
     */
    public static final String AMI_OWNER_AWS_MARKETPLACE = "aws-marketplace";

    private static final String INSTANCE_TAG_INSTANCE_NAME = "Name";

    private static Ec2Client getFromCache(AwsRegion awsRegion) {
        synchronized(CLIENT_CACHE) {
            final Ec2ClientWrapper ec2ClientWrapper = CLIENT_CACHE.get(awsRegion);
            if (ec2ClientWrapper != null) {
                return ec2ClientWrapper.ec2Client;
            } else {
                return null;
            }
        }
    }

    private static void putClientCache(AwsRegion awsRegion, Ec2Client ec2Client) {
        synchronized (CLIENT_CACHE) {
            Ec2ClientWrapper wrapper = new Ec2ClientWrapper();
            wrapper.ec2Client = ec2Client;
            CLIENT_CACHE.put(awsRegion, wrapper);
        }
    }

    private static AwsRegion newAwsRegion(String accessKey, String secret, String regionName) {
        Region regions = Region.of(regionName);
        AwsRegion awsRegion = new AwsRegion();
        awsRegion.setAccessKey(accessKey);
        awsRegion.setSecret(secret);
        awsRegion.setRegion(regions);
        return awsRegion;
    }

    public static Ec2Client obtainClient(String accessKey, String secret, String regionName) {
        AwsRegion awsRegion = newAwsRegion(accessKey, secret, regionName);
        // Ec2Client 是线程安全的，所以可以缓存多线程共用
        Ec2Client ec2Client = getFromCache(awsRegion);
        if (ec2Client == null) {
            AwsCredentials credentials = AwsBasicCredentials.create(accessKey, secret);
            ec2Client = Ec2Client.builder()
                    .region(awsRegion.region)
                    .credentialsProvider(StaticCredentialsProvider.create(credentials))
                    .build();

            putClientCache(awsRegion, ec2Client);
        }
        return ec2Client;
    }

    /**
     * 获取AMI镜像列表
     * @param accessKey key
     * @param secret 密钥
     * @param regionName 区域名
     * @param owners 拥有者，可选本类中常量：#AMI_OWNER_SELF，#AMI_OWNER_AMAZON, #AMI_OWNER_AWS_MARKETPLACE，或指定AWS用户id
     * @return AWS镜像列表
     */
    public static List<Image> getAmiList(String accessKey, String secret, String regionName, String... owners) {
        DescribeImagesRequest.Builder builder = DescribeImagesRequest.builder();
        if (owners != null && owners.length > 0) {
            builder.owners(owners);
        }
        final DescribeImagesRequest describeImagesRequest = builder.build();

        return obtainClient(accessKey, secret, regionName).describeImages(describeImagesRequest).images();
    }

    /**
     * 获取拥有者为SELF的AMI镜像列表
     * @param accessKey key
     * @param secret 密钥
     * @param regionName 区域名
     * @return AWS镜像列表
     */
    public static List<Image> getSelfAmiList(String accessKey, String secret, String regionName) {
        return getAmiList(accessKey, secret, regionName, AMI_OWNER_SELF);
    }

    /**
     * 新启动实例
     * @param accessKey key
     * @param secret 密钥
     * @param regionName 区域名
     * @param amiId 镜像id
     * @param secretKeyName 密钥对名称（没有密钥对就不能登录机器了）
     * @param instanceName 实例启动后被给予的名称
     * @param instanceType 实例规格（实例类型，见software.amazon.awssdk.services.ec2.model.InstanceType）
     * @param shutdownBehavior 停止实例时的行为
     * @param subnetId 子网id
     * @param securityGroupId 安全组id
     */
    public static RunInstanceResult runInstance(String accessKey, String secret, String regionName, String amiId,
                                                String secretKeyName, String instanceName, InstanceType instanceType,
                                                ShutdownBehavior shutdownBehavior, String subnetId, String securityGroupId) {

        final Ec2Client ec2Client = obtainClient(accessKey, secret, regionName);

        RunInstancesRequest runRequest = RunInstancesRequest.builder()
                .imageId(amiId)
                .instanceInitiatedShutdownBehavior(shutdownBehavior)
                .instanceType(instanceType)
                .maxCount(1)
                .minCount(1)
                .keyName(secretKeyName)
                .securityGroupIds(securityGroupId)
                .subnetId(subnetId)
                .build();

        RunInstancesResponse response = ec2Client.runInstances(runRequest);

        String instanceId = response.instances().get(0).instanceId();

        RunInstanceResult runInstanceResult = new RunInstanceResult();
        runInstanceResult.setInstanceId(instanceId);

        if (instanceName == null) {
            return runInstanceResult;
        }

        try {
            Tag tag = Tag.builder()
                    .key(INSTANCE_TAG_INSTANCE_NAME)
                    .value(instanceName)
                    .build();

            CreateTagsRequest tagRequest = CreateTagsRequest.builder()
                    .resources(instanceId)
                    .tags(tag)
                    .build();

            ec2Client.createTags(tagRequest);

            runInstanceResult.setInstanceName(instanceName);

        } catch (Ec2Exception e) {
            log.warn("failed to setup instance name", e);
        }

        return runInstanceResult;
    }

    /**
     * 启动停止的实例
     * @param accessKey key
     * @param secret 密钥
     * @param regionName 区域名
     * @param instanceId 实例ID
     */
    public static void startInstance(String accessKey, String secret, String regionName, String instanceId) {

        final Ec2Client ec2Client = obtainClient(accessKey, secret, regionName);

        StartInstancesRequest request = StartInstancesRequest.builder()
                .instanceIds(instanceId)
                .build();

//        StartInstancesResponse startResult = ec2Client.startInstances(request);
        ec2Client.startInstances(request);
    }

    /**
     * 停止运行中的实例（注：如果创建实例时指定的ShutdownBehavior是terminate，那么停止实例与终止实例效果一样）
     * @param accessKey key
     * @param secret 密钥
     * @param regionName 区域名
     * @param instanceId 实例ID
     */
    public static void stopInstance(String accessKey, String secret, String regionName, String instanceId) {

        final Ec2Client ec2Client = obtainClient(accessKey, secret, regionName);

        StopInstancesRequest request = StopInstancesRequest.builder()
                .instanceIds(instanceId)
                .build();

        ec2Client.stopInstances(request);
    }

    /**
     * 终止实例
     * @param accessKey key
     * @param secret 密钥
     * @param regionName 区域名
     * @param instanceId 实例ID
     */
    public static void terminateInstance(String accessKey, String secret, String regionName, String instanceId) {

        final Ec2Client ec2Client = obtainClient(accessKey, secret, regionName);
        TerminateInstancesRequest ti = TerminateInstancesRequest.builder()
                .instanceIds(instanceId)
                .build();

        ec2Client.terminateInstances(ti);
    }

    /**
     * 重新启动运行中的实例
     * @param accessKey key
     * @param secret 密钥
     * @param regionName 区域名
     * @param instanceId 实例ID
     */
    public static void rebootInstance(String accessKey, String secret, String regionName, String instanceId) {

        final Ec2Client ec2Client = obtainClient(accessKey, secret, regionName);
        RebootInstancesRequest request = RebootInstancesRequest.builder()
                .instanceIds(instanceId)
                .build();

        ec2Client.rebootInstances(request);
    }

    /**
     * 获取实例信息
     * @param accessKey key
     * @param secret 密钥
     * @param regionName 区域名
     * @param instanceIds 实例ID列表
     * @param withDescribeInfo 是否查询描述详情
     * @param withCheckStatus 是否查询检查状态
     */
    public static Map<String, AwsInstanceVo> getInstanceInfo(String accessKey, String secret, String regionName,
                                                             Collection<String> instanceIds, boolean withDescribeInfo, boolean withCheckStatus) {

        if (!(withDescribeInfo || withCheckStatus)) {
            throw new RuntimeException("invalid parameter, at least one of describeInfo and checkStatus is true");
        }

        final Ec2Client ec2Client = obtainClient(accessKey, secret, regionName);

        Map<String, AwsInstanceVo> map = new HashMap<>();

        // 查询实例描述信息
        if (withDescribeInfo) {
            DescribeInstancesRequest request = DescribeInstancesRequest.builder()
                    .instanceIds(instanceIds)
                    .build();
            DescribeInstancesResponse response = ec2Client.describeInstances(request);

            for (Reservation reservation : response.reservations()) {
                for (Instance instance : reservation.instances()) {
                    AwsInstanceVo awsInstanceVo = map.computeIfAbsent(instance.instanceId(), (key) -> new AwsInstanceVo());

                    awsInstanceVo.setInstanceId(instance.instanceId());
                    awsInstanceVo.setInstanceType(instance.instanceType());
                    awsInstanceVo.setPrivateIp(instance.privateIpAddress());
                    awsInstanceVo.setPrivateDnsName(instance.privateDnsName());
                    awsInstanceVo.setPublicIp(instance.publicIpAddress());
                    awsInstanceVo.setPublicDnsName(instance.publicDnsName());
                    awsInstanceVo.setState(InstanceStateEnum.of(instance.state().code()));
                    awsInstanceVo.setAmiLaunchIndex(instance.amiLaunchIndex());
                    awsInstanceVo.setBootMode(instance.bootMode());
                    awsInstanceVo.setArchitecture(instance.architecture());
                    awsInstanceVo.setCpuCoreCount(instance.cpuOptions().coreCount());
                    awsInstanceVo.setCpuThreadsPerCore(instance.cpuOptions().threadsPerCore());
                    awsInstanceVo.setImageId(instance.imageId());

                    awsInstanceVo.setRootDeviceName(instance.rootDeviceName());
                    awsInstanceVo.setRootDeviceType(instance.rootDeviceType());
                    List<BlockDevice> blockDevices = new ArrayList<>();
                    awsInstanceVo.setBlockDevices(blockDevices);
                    if (instance.blockDeviceMappings() != null) {
                        instance.blockDeviceMappings()
                                .forEach(item -> {
                                    BlockDevice blockDevice = new BlockDevice();
                                    blockDevice.setDeviceName(item.deviceName());
                                    final EbsInstanceBlockDevice ebs = item.ebs();
                                    blockDevice.setEbsVolumeId(ebs.volumeId());
                                    blockDevice.setStatus(ebs.status());
                                    blockDevice.setDeleteOnTermination(ebs.deleteOnTermination());
                                    final Instant instant = ebs.attachTime();
                                    blockDevice.setAttachTime(new Date(instant.toEpochMilli()));
                                    blockDevices.add(blockDevice);
                                });
                    }

                    awsInstanceVo.setRamdiskId(instance.ramdiskId());
                    awsInstanceVo.setVpcId(instance.vpcId());
                    awsInstanceVo.setSubnetId(instance.subnetId());
                    awsInstanceVo.setSecurityGroups(instance.securityGroups());
                    awsInstanceVo.setEbsOptimized(instance.ebsOptimized());
                    awsInstanceVo.setEnaSupport(instance.enaSupport());
                    awsInstanceVo.setInstanceLifecycleType(instance.instanceLifecycle());
                    awsInstanceVo.setSecretKeyName(instance.keyName());
                    final Instant instant = instance.launchTime();
                    awsInstanceVo.setLaunchTime(new Date(instant.toEpochMilli()));
                    if (instance.monitoring() != null) {
                        awsInstanceVo.setMonitoringState(instance.monitoring().state());
                    }
                    awsInstanceVo.setPlatform(instance.platform());
                    awsInstanceVo.setVirtualizationType(instance.virtualizationType());
                    awsInstanceVo.setSpotInstanceRequestId(instance.spotInstanceRequestId());

                }
            }
        }

        if (withCheckStatus) {
            DescribeInstanceStatusRequest request = DescribeInstanceStatusRequest.builder()
                    .instanceIds(instanceIds)
                    .build();

            DescribeInstanceStatusResponse response = ec2Client.describeInstanceStatus(request);
            for (InstanceStatus instance : response.instanceStatuses()) {
                InstanceState instanceState = instance.instanceState();

                AwsInstanceVo awsInstanceVo = map.computeIfAbsent(instance.instanceId(), (key) -> new AwsInstanceVo());

                awsInstanceVo.setInstanceId(instance.instanceId());
                awsInstanceVo.setState(InstanceStateEnum.of(instanceState.code()));
                InstanceStatusSummary systemStatus = instance.systemStatus();
                awsInstanceVo.setSystemStatus(systemStatus.status());
                awsInstanceVo.setInstanceStatus(instance.instanceStatus().status());
                awsInstanceVo.setRawStatus(instance);
            }

        }

        return map;
    }

    /**
     * 获取实例信息
     * @param accessKey key
     * @param secret 密钥
     * @param regionName 区域名
     * @param instanceId 实例ID
     * @param withDescribeInfo 是否查询描述详情
     * @param withCheckStatus 是否查询检查状态
     */
    public static AwsInstanceVo getInstanceInfo(String accessKey, String secret, String regionName,
                                                String instanceId, boolean withDescribeInfo, boolean withCheckStatus) {

        List<String> instanceIdList = new ArrayList<>();
        instanceIdList.add(instanceId);
        final Map<String, AwsInstanceVo> instanceInfoMap = getInstanceInfo(accessKey, secret, regionName, instanceIdList, withDescribeInfo, withCheckStatus);

        return instanceInfoMap.get(instanceId);
    }

    /**
     * 删除卷
     * @param accessKey key
     * @param secret 密钥
     * @param regionName 区域名
     * @param volumeId 卷ID
     */
    public static void deleteVolume(String accessKey, String secret, String regionName, String volumeId) {
        DeleteVolumeRequest deleteVolumeRequest = DeleteVolumeRequest.builder()
                .volumeId(volumeId)
                .build();

        final Ec2Client ec2Client = obtainClient(accessKey, secret, regionName);

        ec2Client.deleteVolume(deleteVolumeRequest);
    }

    public static List<String> checkExistsVolumes(String accessKey, String secret, String regionName, Collection<String> volumeIds) {
        List<String> list = new ArrayList<>();
        if (volumeIds == null || volumeIds.size() == 0) {
            return list;
        }

        final Ec2Client ec2Client = obtainClient(accessKey, secret, regionName);

        try {
            DescribeVolumeStatusRequest describeVolumesRequest = DescribeVolumeStatusRequest.builder()
                    .volumeIds(volumeIds)
                    .build();

            final DescribeVolumeStatusResponse volumeStatusResponse = ec2Client.describeVolumeStatus(describeVolumesRequest);
            final boolean b = volumeStatusResponse.hasVolumeStatuses();
            if (!b) {
                return list;
            }

            final List<VolumeStatusItem> volumeStatusItems = volumeStatusResponse.volumeStatuses();
            if (volumeStatusItems != null) {
                for (VolumeStatusItem volumeStatusItem : volumeStatusItems) {
                    list.add(volumeStatusItem.volumeId());
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return list;
    }

    // ================================================


    @Data
    public static class AwsInstanceVo {
        /**
         * 实例ID
         */
        private String instanceId;
        /**
         * 实例规格
         */
        private InstanceType instanceType;
        /**
         * 镜像ID
         */
        private String imageId;
        /**
         * 实例生命周期类型
         */
        private InstanceLifecycleType instanceLifecycleType;
        /**
         * 内网IP
         */
        private String privateIp;
        /**
         * 内网DNS名
         */
        private String privateDnsName;
        /**
         * 公网IP
         */
        private String publicIp;
        /**
         * 公网DNS名
         */
        private String publicDnsName;
        /**
         * 启动时间
         */
        private Date launchTime;
        /**
         * 登录密钥对键名
         */
        private String secretKeyName;
        /**
         * 子网ID
         */
        private String subnetId;
        /**
         * VPC ID
         */
        private String vpcId;
        /**
         * CPU核心数量
         */
        private Integer cpuCoreCount;
        /**
         * CPU单核线程数
         */
        private Integer cpuThreadsPerCore;
        /**
         * 启动队列位置
         */
        private Integer amiLaunchIndex;
        /**
         * 启动模式：legacy-bios、uefi
         */
        private BootModeValues bootMode;
        /**
         * CPU 架构：i386、x86_64、arm64
         */
        private ArchitectureValues architecture;
        /**
         * 是否ebs优化（得加钱）
         */
        private Boolean ebsOptimized;
        /**
         * 是否ena增强网络（也得价钱吧）
         */
        private Boolean enaSupport;
        /**
         * 平台
         */
        private PlatformValues platform;
        /**
         * 根设备卷名
         */
        private String rootDeviceName;
        /**
         * 根设备卷类型(ebs/instance-store)
         */
        private DeviceType rootDeviceType;
        /**
         * 块存储设备列表（卷列表）
         */
        private List<BlockDevice> blockDevices;
        /**
         * RAM磁盘ID（如果有的话）
         */
        private String ramdiskId;
        /**
         * 竞价实例（spot）请求ID
         */
        private String spotInstanceRequestId;
        /**
         * 虚拟化类型（hvm/paravirtual）
         */
        private VirtualizationType virtualizationType;
        /**
         * 安全组
         */
        private List<GroupIdentifier> securityGroups;
        /**
         * 状态
         */
        private InstanceStateEnum state;
        /**
         * 监视状态
         */
        private MonitoringState monitoringState;
        /**
         * 系统可到达性检查状态
         */
        private SummaryStatus systemStatus = SummaryStatus.INITIALIZING;
        /**
         * 实例可到达性检查状态
         */
        private SummaryStatus instanceStatus = SummaryStatus.INITIALIZING;
        /**
         * 检查状态原始对象
         */
        private InstanceStatus rawStatus;

        /**
         * 是否竞价实例（spot）
         */
        public boolean isSpotInstance() {
            return instanceLifecycleType == InstanceLifecycleType.SPOT;
        }
        /**
         * 是否windows平台
         */
        public boolean isWindowsPlatform() {
            return platform == PlatformValues.WINDOWS;
        }

        private String getSummaryError(Supplier<InstanceStatusSummary> summarySupplier) {
            InstanceStatusSummary statusSummary = summarySupplier.get();
            if (statusSummary == null) {
                return "initializing";
            }
            return summarySupplier.get().details()
                    .stream()
                    .map(InstanceStatusDetails::statusAsString)
                    .collect(Collectors.joining());
        }

        public int checkState() {
            switch (state) {
                case PENDING:
                    return 0;
                case RUNNING:
                    break;
                case SHUTTING_DOWN:
                case TERMINATED:
                case STOPPING:
                case STOPPED:
                    // 状态不ok
                    return -1;
            }
            return 1;
        }

        public int checkMonitoringState() {
            switch (monitoringState) {
                case DISABLED:
                case DISABLING:
                case UNKNOWN_TO_SDK_VERSION:
                    return -1;
                case ENABLED:
                    break;
                case PENDING:
                    return 0;
            }
            return 1;
        }

        private int checkSummaryStatus(SummaryStatus summaryStatus) {
            switch (summaryStatus) {
                case OK:
                    break;
                case INITIALIZING:
                    return 0;
                case UNKNOWN_TO_SDK_VERSION:
                case IMPAIRED:
                case INSUFFICIENT_DATA:
                case NOT_APPLICABLE:
                    // 状态不ok
                    return -1;
            }
            return 1;
        }

        public int checkSystemStatus() {
            return checkSummaryStatus(systemStatus);
        }

        public int checkInstanceStatus() {
            return checkSummaryStatus(instanceStatus);
        }

        public String getSystemErrorMessage() {
            return getSummaryError(() -> rawStatus == null ? null : rawStatus.systemStatus());
        }

        public String getInstanceErrorMessage() {
            return getSummaryError(() -> rawStatus == null ? null : rawStatus.instanceStatus());
        }

    }

    /**
     * 块设备
     */
    @Data
    public static class BlockDevice {

        /**
         * 设备名
         */
        private String deviceName;
        /**
         * 卷ID
         */
        private String ebsVolumeId;
        /**
         * 附加状态
         */
        private AttachmentStatus status;
        /**
         * 附加时间
         */
        private Date attachTime;
        /**
         * 是否终止时自动删除
         */
        private Boolean deleteOnTermination;
    }

    @Data
    public static class RunInstanceResult {
        private String instanceId;
        private String instanceName;
    }

    @Getter
    @RequiredArgsConstructor
    public enum InstanceStateEnum implements IntIdentifiable {
        /**
         * 0 : pending
         */
        PENDING(0),
        /**
         * 16 : running
         */
        RUNNING(16),
        /**
         * 32 : shutting-down
         */
        SHUTTING_DOWN(32),
        /**
         * 48 : terminated
         */
        TERMINATED(48),
        /**
         * 64 : stopping
         */
        STOPPING(64),
        /**
         * 80 : stopped
         */
        STOPPED(80),
        ;

        private final Integer id;

        public static InstanceStateEnum of(Integer id) {
            return IntIdentifiable.getEnumConstant(id, InstanceStateEnum.class);
        }
    }

    public static void main(String[] args) {
        final AwsInstanceVo instanceVo = getInstanceInfo("xxxxxxxxxxxxx",
                "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx",
                "cn-northwest-1",
                "i-03642bf15f5f4e6bf",
                true, true);

        try {
            System.out.println(new JacksonUtil().obj2Json(instanceVo));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
