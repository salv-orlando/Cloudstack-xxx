# A simple script for enabling and disabling per-vif rules for explicitly
# allowing broadcast/multicast traffic on the port where the VIF is attached
# 
# Copyright (C) 2012 Citrix Systems

import os
import sys

import cloudstack_pluginlib as pluginlib

#TODO this does not work with OVS shipped in XS56FP1

def clear_flows(bridge, vif_ofport):
    # Leverage out_port option for removing flows based on actions
    # FIXME: this will remove the whole flow!!!
    pluginlib.del_flows(bridge, out_port=vif_ofport)


def apply_flows(bridge, vif_ofport):
    action = "output:%s," % vif_ofport
    # If we do this in this way this will just do bad things as it will replace a flow, not append an action
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
            bridge = pluginlib.do_cmd([pluginlib.VSCTL_PATH, 'br-to-parent', bridge])
    vif_ofport = pluginlib.do_cmd([pluginlib.VSCTL_PATH, 'get', 'Interface',
                                   vif, 'ofport'])

    if command == 'offline':
        # I haven't found a way to clear only IPv4 or IPv6 rules.
        clear_flows(bridge, vif_ofport)

    if command == 'online':
        apply_flows(bridge,  vif_ofport)


if __name__ == "__main__":
    if len(sys.argv) != 3:
        print "usage: %s [online|offline] vif-domid-idx" % \
               os.path.basename(sys.argv[0])
        sys.exit(1)
    else:
        command, vif_raw = sys.argv[1:3]
        main(command, vif_raw)