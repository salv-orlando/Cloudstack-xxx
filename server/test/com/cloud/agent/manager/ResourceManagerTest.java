package com.cloud.agent.manager;

import java.io.Serializable;
import java.lang.reflect.Field;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;

import com.cloud.alert.AlertManagerImpl;
import com.cloud.alert.dao.AlertDaoImpl;
import com.cloud.api.BaseCmd;
import com.cloud.api.commands.CreatePodCmd;
import com.cloud.api.commands.CreateZoneCmd;
import com.cloud.async.AsyncJobExecutorContextImpl;
import com.cloud.async.AsyncJobManagerImpl;
import com.cloud.async.SyncQueueManagerImpl;
import com.cloud.async.dao.AsyncJobDaoImpl;
import com.cloud.async.dao.SyncQueueDaoImpl;
import com.cloud.async.dao.SyncQueueItemDaoImpl;
import com.cloud.capacity.CapacityManagerImpl;
import com.cloud.capacity.dao.CapacityDaoImpl;
import com.cloud.certificate.dao.CertificateDaoImpl;
import com.cloud.cluster.CheckPointManagerImpl;
import com.cloud.cluster.ClusterFenceManagerImpl;
import com.cloud.cluster.ClusterManagerImpl;
import com.cloud.cluster.agentlb.dao.HostTransferMapDaoImpl;
import com.cloud.cluster.dao.ManagementServerHostDaoImpl;
import com.cloud.cluster.dao.StackMaidDaoImpl;
import com.cloud.configuration.ConfigurationManagerImpl;
import com.cloud.configuration.ConfigurationService;
import com.cloud.configuration.dao.ConfigurationDaoImpl;
import com.cloud.configuration.dao.ResourceCountDaoImpl;
import com.cloud.configuration.dao.ResourceLimitDaoImpl;
import com.cloud.consoleproxy.ConsoleProxyManagerImpl;
import com.cloud.dao.EntityManagerImpl;
import com.cloud.dc.ClusterDetailsDaoImpl;
import com.cloud.dc.dao.AccountVlanMapDaoImpl;
import com.cloud.dc.dao.ClusterDaoImpl;
import com.cloud.dc.dao.DataCenterDaoImpl;
import com.cloud.dc.dao.DataCenterIpAddressDaoImpl;
import com.cloud.dc.dao.DcDetailsDaoImpl;
import com.cloud.dc.dao.HostPodDaoImpl;
import com.cloud.dc.dao.PodVlanMapDaoImpl;
import com.cloud.dc.dao.VlanDaoImpl;
import com.cloud.domain.dao.DomainDaoImpl;
import com.cloud.event.dao.EventDaoImpl;
import com.cloud.event.dao.UsageEventDaoImpl;
import com.cloud.ha.HighAvailabilityManagerImpl;
import com.cloud.ha.dao.HighAvailabilityDaoImpl;
import com.cloud.host.dao.HostDaoImpl;
import com.cloud.host.dao.HostDetailsDaoImpl;
import com.cloud.host.dao.HostTagsDaoImpl;
import com.cloud.hypervisor.HypervisorGuruManagerImpl;
import com.cloud.hypervisor.dao.HypervisorCapabilitiesDaoImpl;
import com.cloud.keystore.KeystoreDaoImpl;
import com.cloud.keystore.KeystoreManagerImpl;
import com.cloud.maint.UpgradeManagerImpl;
import com.cloud.maint.dao.AgentUpgradeDaoImpl;
import com.cloud.network.NetworkManagerImpl;
import com.cloud.network.dao.FirewallRulesCidrsDaoImpl;
import com.cloud.network.dao.FirewallRulesDaoImpl;
import com.cloud.network.dao.IPAddressDaoImpl;
import com.cloud.network.dao.InlineLoadBalancerNicMapDaoImpl;
import com.cloud.network.dao.LoadBalancerDaoImpl;
import com.cloud.network.dao.LoadBalancerVMMapDaoImpl;
import com.cloud.network.dao.NetworkDaoImpl;
import com.cloud.network.dao.NetworkDomainDaoImpl;
import com.cloud.network.dao.NetworkRuleConfigDaoImpl;
import com.cloud.network.dao.RemoteAccessVpnDaoImpl;
import com.cloud.network.dao.VpnUserDaoImpl;
import com.cloud.network.firewall.FirewallManagerImpl;
import com.cloud.network.lb.ElasticLoadBalancerManagerImpl;
import com.cloud.network.lb.LoadBalancingRulesManagerImpl;
import com.cloud.network.lb.dao.ElasticLbVmMapDaoImpl;
import com.cloud.network.ovs.OvsNetworkManagerImpl;
import com.cloud.network.ovs.OvsTunnelManagerImpl;
import com.cloud.network.ovs.dao.GreTunnelDaoImpl;
import com.cloud.network.ovs.dao.OvsTunnelNetworkDaoImpl;
import com.cloud.network.ovs.dao.OvsTunnelDaoImpl;
import com.cloud.network.ovs.dao.OvsWorkDaoImpl;
import com.cloud.network.ovs.dao.VlanMappingDaoImpl;
import com.cloud.network.ovs.dao.VlanMappingDirtyDaoImpl;
import com.cloud.network.ovs.dao.VmFlowLogDaoImpl;
import com.cloud.network.router.VirtualNetworkApplianceManagerImpl;
import com.cloud.network.rules.RulesManagerImpl;
import com.cloud.network.rules.dao.PortForwardingRulesDaoImpl;
import com.cloud.network.security.SecurityGroupManagerImpl2;
import com.cloud.network.security.dao.SecurityGroupDaoImpl;
import com.cloud.network.security.dao.SecurityGroupRulesDaoImpl;
import com.cloud.network.security.dao.SecurityGroupVMMapDaoImpl;
import com.cloud.network.security.dao.SecurityGroupWorkDaoImpl;
import com.cloud.network.security.dao.VmRulesetLogDaoImpl;
import com.cloud.network.vpn.RemoteAccessVpnManagerImpl;
import com.cloud.offerings.dao.NetworkOfferingDaoImpl;
import com.cloud.projects.ProjectManagerImpl;
import com.cloud.projects.dao.ProjectAccountDaoImpl;
import com.cloud.projects.dao.ProjectDaoImpl;
import com.cloud.resource.ResourceManagerImpl;
import com.cloud.resourcelimit.ResourceLimitManagerImpl;
import com.cloud.service.dao.ServiceOfferingDaoImpl;
import com.cloud.storage.OCFS2ManagerImpl;
import com.cloud.storage.StorageManagerImpl;
import com.cloud.storage.dao.DiskOfferingDaoImpl;
import com.cloud.storage.dao.GuestOSCategoryDaoImpl;
import com.cloud.storage.dao.GuestOSDaoImpl;
import com.cloud.storage.dao.LaunchPermissionDaoImpl;
import com.cloud.storage.dao.SnapshotDaoImpl;
import com.cloud.storage.dao.SnapshotPolicyDaoImpl;
import com.cloud.storage.dao.SnapshotScheduleDaoImpl;
import com.cloud.storage.dao.StoragePoolDaoImpl;
import com.cloud.storage.dao.StoragePoolHostDaoImpl;
import com.cloud.storage.dao.StoragePoolWorkDaoImpl;
import com.cloud.storage.dao.SwiftDaoImpl;
import com.cloud.storage.dao.UploadDaoImpl;
import com.cloud.storage.dao.VMTemplateDaoImpl;
import com.cloud.storage.dao.VMTemplateHostDaoImpl;
import com.cloud.storage.dao.VMTemplatePoolDaoImpl;
import com.cloud.storage.dao.VMTemplateZoneDaoImpl;
import com.cloud.storage.dao.VolumeDaoImpl;
import com.cloud.storage.download.DownloadMonitorImpl;
import com.cloud.storage.secondary.SecondaryStorageManagerImpl;
import com.cloud.storage.snapshot.SnapshotManagerImpl;
import com.cloud.storage.snapshot.SnapshotSchedulerImpl;
import com.cloud.storage.upload.UploadMonitorImpl;
import com.cloud.template.TemplateManagerImpl;
import com.cloud.user.AccountManagerImpl;
import com.cloud.user.DomainManagerImpl;
import com.cloud.user.dao.AccountDaoImpl;
import com.cloud.user.dao.SSHKeyPairDaoImpl;
import com.cloud.user.dao.UserAccountDaoImpl;
import com.cloud.user.dao.UserDaoImpl;
import com.cloud.user.dao.UserStatisticsDaoImpl;
import com.cloud.utils.component.ComponentLocator;
import com.cloud.utils.component.ComponentLocator.ComponentInfo;
import com.cloud.utils.component.Manager;
import com.cloud.utils.component.MockComponentLocator;
import com.cloud.utils.db.GenericDao;
import com.cloud.vm.ClusteredVirtualMachineManagerImpl;
import com.cloud.vm.ItWorkDaoImpl;
import com.cloud.vm.UserVmManagerImpl;
import com.cloud.vm.dao.ConsoleProxyDaoImpl;
import com.cloud.vm.dao.DomainRouterDaoImpl;
import com.cloud.vm.dao.InstanceGroupDaoImpl;
import com.cloud.vm.dao.InstanceGroupVMMapDaoImpl;
import com.cloud.vm.dao.NicDaoImpl;
import com.cloud.vm.dao.SecondaryStorageVmDaoImpl;
import com.cloud.vm.dao.UserVmDaoImpl;
import com.cloud.vm.dao.UserVmDetailsDaoImpl;
import com.cloud.vm.dao.VMInstanceDaoImpl;

public class ResourceManagerTest extends TestCase {
	MockComponentLocator _locator;
	private static final Logger s_logger = Logger.getLogger(ResourceManagerTest.class);
	ConfigurationService _configService;

	@Override
	@Before
	public void setUp() throws Exception {
		_locator = new MockComponentLocator("management-server");
		_locator.addDao("StackMaidDao", StackMaidDaoImpl.class);
		_locator.addDao("VMTemplateZoneDao", VMTemplateZoneDaoImpl.class);
		_locator.addDao("DomainRouterDao", DomainRouterDaoImpl.class);
		_locator.addDao("HostDao", HostDaoImpl.class);
		_locator.addDao("VMInstanceDao", VMInstanceDaoImpl.class);
		_locator.addDao("UserVmDao", UserVmDaoImpl.class);
		ComponentInfo<? extends GenericDao<?, ? extends Serializable>> info = _locator.addDao("ServiceOfferingDao", ServiceOfferingDaoImpl.class);
		info.addParameter("cache.size", "50");
		info.addParameter("cache.time.to.live", "600");
		info = _locator.addDao("DiskOfferingDao", DiskOfferingDaoImpl.class);
		info.addParameter("cache.size", "50");
		info.addParameter("cache.time.to.live", "600");
		info = _locator.addDao("DataCenterDao", DataCenterDaoImpl.class);
		info.addParameter("cache.size", "50");
		info.addParameter("cache.time.to.live", "600");
		info = _locator.addDao("HostPodDao", HostPodDaoImpl.class);
		info.addParameter("cache.size", "50");
		info.addParameter("cache.time.to.live", "600");
		_locator.addDao("IPAddressDao", IPAddressDaoImpl.class);
		info = _locator.addDao("VlanDao", VlanDaoImpl.class);
		info.addParameter("cache.size", "30");
		info.addParameter("cache.time.to.live", "3600");
		_locator.addDao("PodVlanMapDao", PodVlanMapDaoImpl.class);
		_locator.addDao("AccountVlanMapDao", AccountVlanMapDaoImpl.class);
		_locator.addDao("VolumeDao", VolumeDaoImpl.class);
		_locator.addDao("EventDao", EventDaoImpl.class);
		info = _locator.addDao("UserDao", UserDaoImpl.class);
		info.addParameter("cache.size", "5000");
		info.addParameter("cache.time.to.live", "300");
		_locator.addDao("UserStatisticsDao", UserStatisticsDaoImpl.class);
		_locator.addDao("FirewallRulesDao", FirewallRulesDaoImpl.class);
		_locator.addDao("LoadBalancerDao", LoadBalancerDaoImpl.class);
		_locator.addDao("NetworkRuleConfigDao", NetworkRuleConfigDaoImpl.class);
		_locator.addDao("LoadBalancerVMMapDao", LoadBalancerVMMapDaoImpl.class);
		_locator.addDao("DataCenterIpAddressDao", DataCenterIpAddressDaoImpl.class);
		_locator.addDao("SecurityGroupDao", SecurityGroupDaoImpl.class);
		//_locator.addDao("IngressRuleDao", IngressRuleDaoImpl.class);
		_locator.addDao("SecurityGroupVMMapDao", SecurityGroupVMMapDaoImpl.class);
		_locator.addDao("SecurityGroupRulesDao", SecurityGroupRulesDaoImpl.class);
		_locator.addDao("SecurityGroupWorkDao", SecurityGroupWorkDaoImpl.class);
		_locator.addDao("VmRulesetLogDao", VmRulesetLogDaoImpl.class);
		_locator.addDao("AlertDao", AlertDaoImpl.class);
		_locator.addDao("CapacityDao", CapacityDaoImpl.class);
		_locator.addDao("DomainDao", DomainDaoImpl.class);
		_locator.addDao("AccountDao", AccountDaoImpl.class);
		_locator.addDao("ResourceLimitDao", ResourceLimitDaoImpl.class);
		_locator.addDao("ResourceCountDao", ResourceCountDaoImpl.class);
		_locator.addDao("UserAccountDao", UserAccountDaoImpl.class);
		_locator.addDao("VMTemplateHostDao", VMTemplateHostDaoImpl.class);
		_locator.addDao("UploadDao", UploadDaoImpl.class);
		_locator.addDao("VMTemplatePoolDao", VMTemplatePoolDaoImpl.class);
		_locator.addDao("LaunchPermissionDao", LaunchPermissionDaoImpl.class);
		_locator.addDao("ConfigurationDao", ConfigurationDaoImpl.class);
		info = _locator.addDao("VMTemplateDao", VMTemplateDaoImpl.class);
		info.addParameter("cache.size", "100");
		info.addParameter("cache.time.to.live", "600");
		info.addParameter("routing.uniquename", "routing");
		_locator.addDao("HighAvailabilityDao", HighAvailabilityDaoImpl.class);
		_locator.addDao("ConsoleProxyDao", ConsoleProxyDaoImpl.class);
		_locator.addDao("SecondaryStorageVmDao", SecondaryStorageVmDaoImpl.class);
		_locator.addDao("ManagementServerHostDao", ManagementServerHostDaoImpl.class);
		_locator.addDao("AgentUpgradeDao", AgentUpgradeDaoImpl.class);
		_locator.addDao("SnapshotDao", SnapshotDaoImpl.class);
		_locator.addDao("AsyncJobDao", AsyncJobDaoImpl.class);
		_locator.addDao("SyncQueueDao", SyncQueueDaoImpl.class);
		_locator.addDao("SyncQueueItemDao", SyncQueueItemDaoImpl.class);
		_locator.addDao("GuestOSDao", GuestOSDaoImpl.class);
		_locator.addDao("GuestOSCategoryDao", GuestOSCategoryDaoImpl.class);
		_locator.addDao("StoragePoolDao", StoragePoolDaoImpl.class);
		_locator.addDao("StoragePoolHostDao", StoragePoolHostDaoImpl.class);
		_locator.addDao("DetailsDao", HostDetailsDaoImpl.class);
		_locator.addDao("SnapshotPolicyDao", SnapshotPolicyDaoImpl.class);
		_locator.addDao("SnapshotScheduleDao", SnapshotScheduleDaoImpl.class);
		_locator.addDao("ClusterDao", ClusterDaoImpl.class);
		_locator.addDao("CertificateDao", CertificateDaoImpl.class);
		_locator.addDao("NetworkConfigurationDao", NetworkDaoImpl.class);
		_locator.addDao("NetworkOfferingDao", NetworkOfferingDaoImpl.class);
		_locator.addDao("NicDao", NicDaoImpl.class);
		_locator.addDao("InstanceGroupDao", InstanceGroupDaoImpl.class);
		_locator.addDao("InstanceGroupVMMapDao", InstanceGroupVMMapDaoImpl.class);
		_locator.addDao("RemoteAccessVpnDao", RemoteAccessVpnDaoImpl.class);
		_locator.addDao("VpnUserDao", VpnUserDaoImpl.class);
		_locator.addDao("ItWorkDao", ItWorkDaoImpl.class);
		_locator.addDao("FirewallRulesDao", FirewallRulesDaoImpl.class);
		_locator.addDao("PortForwardingRulesDao", PortForwardingRulesDaoImpl.class);
		_locator.addDao("FirewallRulesCidrsDao", FirewallRulesCidrsDaoImpl.class);
		_locator.addDao("SSHKeyPairDao", SSHKeyPairDaoImpl.class);
		_locator.addDao("UsageEventDao", UsageEventDaoImpl.class);
		_locator.addDao("ClusterDetailsDao", ClusterDetailsDaoImpl.class);
		_locator.addDao("UserVmDetailsDao", UserVmDetailsDaoImpl.class);
		_locator.addDao("VlanMappingDao", VlanMappingDaoImpl.class);
		_locator.addDao("VlanMappingDirtyDao", VlanMappingDirtyDaoImpl.class);
		_locator.addDao("OvsWorkDao", OvsWorkDaoImpl.class);
		_locator.addDao("VmFlowLogDao", VmFlowLogDaoImpl.class);
		_locator.addDao("GreTunnelDao", GreTunnelDaoImpl.class);
		_locator.addDao("OvsTunnelDao", OvsTunnelDaoImpl.class);
		_locator.addDao("OvsTunnelAccountDao", OvsTunnelNetworkDaoImpl.class);
		_locator.addDao("StoragePoolWorkDao", StoragePoolWorkDaoImpl.class);
		_locator.addDao("HostTagsDao", HostTagsDaoImpl.class);
		_locator.addDao("NetworkDomainDao", NetworkDomainDaoImpl.class);
		_locator.addDao("KeystoreDao", KeystoreDaoImpl.class);
		_locator.addDao("DcDetailsDao", DcDetailsDaoImpl.class);
		_locator.addDao("SwiftDao", SwiftDaoImpl.class);
		_locator.addDao("AgentTransferMapDao", HostTransferMapDaoImpl.class);
		_locator.addDao("ProjectDao", ProjectDaoImpl.class);
		_locator.addDao("InlineLoadBalancerNicMapDao", InlineLoadBalancerNicMapDaoImpl.class);
		_locator.addDao("ElasticLbVmMap", ElasticLbVmMapDaoImpl.class);
		_locator.addDao("ProjectsAccountDao", ProjectAccountDaoImpl.class);
		info = _locator.addDao("HypervisorCapabilitiesDao", HypervisorCapabilitiesDaoImpl.class);
		info.addParameter("cache.size", "100");
		info.addParameter("cache.time.to.live", "600");

		_locator.addManager("StackMaidManager", CheckPointManagerImpl.class);
		_locator.addManager("account manager", AccountManagerImpl.class);
		_locator.addManager("domain manager", DomainManagerImpl.class);
		_locator.addManager("resource limit manager", ResourceLimitManagerImpl.class);
		_locator.addManager("configuration manager", ConfigurationManagerImpl.class);
		_locator.addManager("network manager", NetworkManagerImpl.class);
		_locator.addManager("download manager", DownloadMonitorImpl.class);
		_locator.addManager("upload manager", UploadMonitorImpl.class);
		_locator.addManager("keystore manager", KeystoreManagerImpl.class);
		_locator.addManager("secondary storage vm manager", SecondaryStorageManagerImpl.class);
		_locator.addManager("vm manager", UserVmManagerImpl.class);
		_locator.addManager("upgrade manager", UpgradeManagerImpl.class);
		_locator.addManager("StorageManager", StorageManagerImpl.class);
		_locator.addManager("SyncQueueManager", SyncQueueManagerImpl.class);
		_locator.addManager("AsyncJobManager", AsyncJobManagerImpl.class);
		_locator.addManager("AsyncJobExecutorContext", AsyncJobExecutorContextImpl.class);
		_locator.addManager("HA Manager", HighAvailabilityManagerImpl.class);
		_locator.addManager("Alert Manager", AlertManagerImpl.class);
		_locator.addManager("Template Manager", TemplateManagerImpl.class);
		_locator.addManager("Snapshot Manager", SnapshotManagerImpl.class);
		_locator.addManager("SnapshotScheduler", SnapshotSchedulerImpl.class);
		_locator.addManager("SecurityGroupManager", SecurityGroupManagerImpl2.class);
		_locator.addManager("DomainRouterManager", VirtualNetworkApplianceManagerImpl.class);
		_locator.addManager("EntityManager", EntityManagerImpl.class);
		_locator.addManager("LoadBalancingRulesManager", LoadBalancingRulesManagerImpl.class);
		_locator.addManager("RulesManager", RulesManagerImpl.class);
		_locator.addManager("RemoteAccessVpnManager", RemoteAccessVpnManagerImpl.class);
		_locator.addManager("OvsNetworkManager", OvsNetworkManagerImpl.class);
		_locator.addManager("OvsTunnelManager", OvsTunnelManagerImpl.class);
		_locator.addManager("Capacity Manager", CapacityManagerImpl.class);
		_locator.addManager("Cluster Manager", ClusterManagerImpl.class);
		_locator.addManager("VirtualMachineManager", ClusteredVirtualMachineManagerImpl.class);
		_locator.addManager("HypervisorGuruManager", HypervisorGuruManagerImpl.class);
		_locator.addManager("ClusterFenceManager", ClusterFenceManagerImpl.class);
		_locator.addManager("ResourceManager", ResourceManagerImpl.class);

		_locator.addManager("OCFS2Manager", OCFS2ManagerImpl.class);
		_locator.addManager("FirewallManager", FirewallManagerImpl.class);
		ComponentInfo<? extends Manager> info1 = _locator.addManager("ConsoleProxyManager", ConsoleProxyManagerImpl.class);
		info1.addParameter("consoleproxy.sslEnabled", "true");
		_locator.addManager("ClusteredAgentManager", ClusteredAgentManagerImpl.class);
		_locator.addManager("ProjectManager", ProjectManagerImpl.class);
		_locator.addManager("ElasticLoadBalancerManager", ElasticLoadBalancerManagerImpl.class);
		
		_locator.makeActive(null);
		_configService = ComponentLocator.inject(ConfigurationManagerImpl.class);
	}

	private <T extends BaseCmd> void evaluateCmd(T cmd, String name, Object value) {
		try {
			Field f = cmd.getClass().getDeclaredField(name);
			f.set(cmd, value);
		} catch (Exception e) {
			s_logger.debug("Unable to evaluate " + cmd.getClass().getName() + "." + name, e);
			TestCase.fail();
		}
	}

	private void createZone(String zoneName) {
		CreateZoneCmd cZone = new CreateZoneCmd();
		evaluateCmd(cZone, "dns1", "10.223.110.254");
		evaluateCmd(cZone, "internalDns1", "10.223.110.254");
		evaluateCmd(cZone, "zoneName", zoneName);
		evaluateCmd(cZone, "networkType", "Basic");
		evaluateCmd(cZone, "securitygroupenabled", false);
		s_logger.info("Create zone:" + cZone.getZoneName());

		_configService.createZone(cZone);
	}

	private void createPod(String name) {
		CreatePodCmd cPod = new CreatePodCmd();
		evaluateCmd(cPod, "netmask", "255.255.255.0");

	}

	private void deploy100Hosts() {
		createZone("test1");
	}

	public void testDeploy100Hosts() {
		deploy100Hosts();
	}

	@Override
	@After
	public void tearDown() throws Exception {
	}

}
