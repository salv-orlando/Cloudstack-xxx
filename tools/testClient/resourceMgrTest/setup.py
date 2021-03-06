'''
Created on Oct 18, 2011

@author: frank
'''
from optparse import OptionParser
from configGenerator import *

if __name__ == '__main__':
    parser = OptionParser()
    parser.add_option('-o', '--output', action='store', default='./setup.conf', dest='output', help='the path where the json config file generated')
    parser.add_option('-m', '--mshost', dest='mshost', help='hostname/ip of management server', action='store')
    
    (opts, args) = parser.parse_args()
    mandatories = ['mshost']
    for m in mandatories:
        if not opts.__dict__[m]:
            parser.error("mandatory option - " + m +" missing")
   
    zs = cloudstackConfiguration()
   
    #Define Zone
    z = zone()
    z.dns1 = "8.8.8.8"
    z.dns2 = "4.4.4.4"
    z.internaldns1 = "192.168.110.254"
    z.internaldns2 = "192.168.110.253"
    z.name = "testZone"
    z.networktype = 'Basic'

    #Define SecondaryStorage
    ss = secondaryStorage()
    ss.url ="nfs://172.16.15.32/export/share/secondary"
    z.secondaryStorages.append(ss)

    p = pod()
    p.name = "POD-1"
    p.gateway = "10.223.64.1"
    p.netmask = "255.255.254.0"
    p.startip = "10.223.64.50"
    p.endip = "10.223.64.60"

    ip = iprange()
    ip.vlan="untagged"
    ip.gateway = p.gateway
    ip.netmask = p.netmask
    ip.startip = "10.223.64.70"
    ip.endip = "10.223.64.220"
    p.guestIpRanges.append(ip)

    c = cluster()
    c.clustername = "CLUSTER-1"
    c.clustertype = "CloudManaged"
    c.hypervisor = "Simulator"
    p.clusters.append(c)
    
    z.pods.append(p)
    zs.zones.append(z)
    
    '''Add one mgt server'''
    mgt = managementServer()
    mgt.mgtSvrIp = opts.mshost
    zs.mgtSvr.append(mgt)
    
    '''Add a database'''
    db = dbServer()
    db.dbSvr = opts.mshost
    db.user = "root"
    db.passwd = ""
    zs.dbSvr = db

    generate_setup_config(zs,opts.output)
    