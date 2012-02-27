# A simple script for enabling and disabling per-vif rules for explicitly
# allowing broadcast/multicast traffic on the port where the VIF is attached
# 
# Copyright (C) 2012 Citrix Systems

import os
import subprocess
import sys


class PluginError(Exception):
    """Base Exception class for all plugin errors."""
    def __init__(self, *args):
        Exception.__init__(self, *args)


def do_cmd(cmd):
    """Abstracts out the basics of issuing system commands. If the command
	returns anything in stderr, a PluginError is raised with that information.
	Otherwise, the output from stdout is returned.
	"""

    pipe = subprocess.PIPE
    proc = subprocess.Popen(cmd, shell=False, stdin=pipe, stdout=pipe,
						    stderr=pipe, close_fds=True)
    ret_code = proc.wait()
    err = proc.stderr.read()
    if ret_code:
        raise PluginError(err)
    output = proc.stdout.read()
    if output.endswith('\n'):
        output = output[:-1]
    return output


def clear_flows(bridge, vif_ofport):
	pass

def apply_flows(bridge, vif_ofport):
	pass

def main(command, vif_raw):
    if command not in ('online', 'offline'):
        return

    vif_name, dom_id, vif_index = vif_raw.split('-')
    # validate vif and dom-id
    vif = "%s%s.%s" % (vif_name, dom_id, vif_index)

    bridge = do_cmd(['/usr/bin/ovs-vsctl', 'iface-to-br', vif])

    vif_ofport = do_cmd(['/usr/bin/ovs-vsctl', 'get',
                         'Interface', vif, 'ofport'])

    if command == 'offline':
        # I haven't found a way to clear only IPv4 or IPv6 rules.
        clear_flows(bridge, vif_ofport)

    if command == 'online':
        apply_flows(bridge,  vif_ofport)


if __name__ == "__main__":
    if len(sys.argv) != 4:
        print "usage: %s [online|offline] vif-domid-idx" % \
               os.path.basename(sys.argv[0])
        sys.exit(1)
    else:
        command, vif_raw = sys.argv[1:3]
        main(command, vif_raw)