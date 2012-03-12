# A simple script for enabling and disabling per-vif rules for explicitly
# allowing broadcast/multicast traffic on the port where the VIF is attached
# 
# Copyright (C) 2012 Citrix Systems

import os
import sys

import cloudstack_pluginlib as pluginlib

def apply_flows(bridge, vif_ofports):
    action = "".join("output:%s," %ofport
                for ofport in vif_ofports)[:-1]
    pluginlib.add_flow(bridge, priority=1100,
                       dl_dst='ff:ff:ff:ff:ff:ff', actions=action)
    pluginlib.add_flow(bridge, priority=1100,
                       nw_dst='224.0.0.0/24', actions=action)


def main(command, vif_raw):
    if command not in ('online', 'offline'):
        return
    # TODO (very important)
    # Quit immediately if networking is NOT being managed by the OVS tunnel manager
    vif_name, dom_id, vif_index = vif_raw.split('-')
    # validate vif and dom-id
    vif = "%s%s.%s" % (vif_name, dom_id, vif_index)

    bridge = pluginlib.do_cmd([pluginlib.VSCTL_PATH, 'iface-to-br', vif])
    vlan = pluginlib.do_cmd([pluginlib.VSCTL_PATH, 'br-to-vlan', bridge])
    if vlan != '0':
            # We need the REAL bridge name
            bridge = pluginlib.do_cmd([pluginlib.VSCTL_PATH,
                                       'br-to-parent', bridge])
    # For the OVS version shipped with XS56FP1 we need to retrieve
    # the ofport number for all interfaces
    vsctl_output = pluginlib.do_cmd([pluginlib.VSCTL_PATH,
                                     'list-ports', bridge])
    vifs = vsctl_output.split('\n')
    vif_ofports = []
    for vif in vifs:
        vif_ofports.append(pluginlib.do_cmd([pluginlib.VSCTL_PATH, 'get',
                                             'Interface', vif, 'ofport']))
    # So regardless of whether the VIF is brought online or offline we 
    # will always execute the same action
    apply_flows(bridge,  vif_ofports)    


if __name__ == "__main__":
    if len(sys.argv) != 3:
        print "usage: %s [online|offline] vif-domid-idx" % \
               os.path.basename(sys.argv[0])
        sys.exit(1)
    else:
        command, vif_raw = sys.argv[1:3]
        main(command, vif_raw)