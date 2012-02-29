# Common function for Cloudstack's XenAPI plugins
# 
# Copyright (C) 2012 Citrix Systems

import ConfigParser
import logging
import os
import subprocess

from time import localtime, asctime

DEFAULT_LOG_FORMAT = "%(asctime)s %(levelname)8s [%(name)s] %(message)s"
DEFAULT_LOG_DATE_FORMAT = "%Y-%m-%d %H:%M:%S"
DEFAULT_LOG_FILE = "/var/log/cloudstack_plugins.log"

PLUGIN_CONFIG_PATH='/etc/xensource/cloudstack_plugins.conf'
VSCTL_PATH='/usr/bin/ovs-vsctl'
OFCTL_PATH='/usr/bin/ovs-ofctl'


class PluginError(Exception):
    """Base Exception class for all plugin errors."""
    def __init__(self, *args):
        Exception.__init__(self, *args)

def setup_logging(log_file=None):
    debug = False
    verbose = False
    log_format = DEFAULT_LOG_FORMAT
    log_date_format = DEFAULT_LOG_DATE_FORMAT

    # try to read plugin configuration file
    if os.path.exists(PLUGIN_CONFIG_PATH):
        config = ConfigParser.ConfigParser()
        config.read(file)
        try:
            debug = config.getboolean('LOGGING','debug')
            verbose = config.getboolean('LOGGING','verbose')
            log_format = config.get('LOGGING','log_format')
            log_date_format = config.get('LOGGING','log_date_format')
            log_file_2 = config.get('LOGGING','log_file')
        except ValueError:
            # configuration file contained invalid attributes
            # ignore them
            pass 
    
    root_logger = logging.root
    if debug:
        root_logger.setLevel(logging.DEBUG)
    elif verbose:
        root_logger.setLevel(logging.INFO)
    else:
        root_logger.setLevel(logging.WARNING)
    formatter = logging.Formatter(log_format, log_date_format)

    log_filename = log_file or log_file_2 or DEFAULT_LOG_FILE
    
    logfile_handler = logging.FileHandler(log_filename)
    logfile_handler.setFormatter(formatter)
    logfile_handler.setFormatter(formatter)
    root_logger.addHandler(logfile_handler)


def pr (str):
    global fLog

    if fLog != None:
        str = "[%s]:" % _asctime (_localtime()) + str + "\n"
        fLog.write (str)


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


def _build_flow_expr(**kwargs):
    flow = "hard_timeout=%s,idle_timeout=%s,priority=%s"\
            % (kwargs.get('hard_timeout','0'),
               kwargs.get('idle_timeout','0'),
               kwargs.get('priority','1'))
    in_port = 'in_port' in kwargs and ",in_port=%s" % kwargs['in_port'] or ''
    dl_src = 'dl_src' in kwargs and ",dl_src=%s" % kwargs['dl_src'] or ''
    dl_dst = 'dl_dst' in kwargs and ",dl_dst=%s" % kwargs['dl_dst'] or ''
    nw_src = 'nw_src' in kwargs and ",nw_src=%s" % kwargs['nw_src'] or ''
    nw_dst = 'nw_dst' in kwargs and ",nw_dst=%s" % kwargs['nw_dst'] or ''
    ip = ('nw_src' in kwargs or 'nw_dst' in kwargs) and ',ip' or ''
    flow = flow + in_port + dl_src + dl_dst + ip + nw_src + nw_dst
    return flow


def add_flow(bridge, **kwargs):
    """
    Builds a flow expression for **kwargs and adds the flow entry
    to an Open vSwitch instance
    """
    flow = _build_flow_expr(**kwargs)
    actions = 'actions' in kwargs and ",actions=%s" % kwargs['actions'] or ''
    flow = flow + actions
    addflow = [OFCTL_PATH, "add-flow", bridge, flow]
    do_cmd(addflow)


def del_flows(bridge, **kwargs):
    """ 
    Removes flows according to criteria passed as keyword.
    """
    flow = _build_flow_expr(**kwargs)
    # out_port condition does not exist for all flow commands
    out_port = 'out_port' in kwargs and ",out_port=%s" % kwargs['out_port'] or ''
    flow = flow + out_port
    delFlow = [OFCTL_PATH, 'del-flows', bridge, flow]
    do_cmd(delFlow)
