import xml.dom.minidom
import datetime
from optparse import OptionParser
import random
import os
import sys

# Constants
INDENT = "    "
INDENT2 = INDENT + INDENT

class xml_to_python(object):
    def __init__(self, debug):
        '''
        constructor
        '''
        self.debug = debug
        self._output = False
        self.out_buffer = []

        self.cmd_name = None
        self.cmd_name_var = None
        self.cmd_name_resp = None

        self.glos = []

    def _write(self, output_string):
        '''
        internal print function
        '''
        self.out_buffer.append("%s\n" % output_string)

    def parse_parameters(self, dom):
        '''
        process parameters of command
        '''
        for param in dom.getElementsByTagName("parameters"):
            for item in param.getElementsByTagName("item"):
                itemName = item.getElementsByTagName("name")[0].childNodes[0].nodeValue.strip()
                itemParam = None
                itemValue = None

                # this could be handled much cleaner
                try:
                    itemValue = item.getElementsByTagName("value")[0].childNodes[0].nodeValue.strip()
                except:
                    itemValue = None
                try:
                    itemParam = item.getElementsByTagName("param")[0].childNodes[0].nodeValue.strip()
                except:
                    itemParam = None

                # handle getparam and setparam and random attributes here...
                if item.getAttribute("getparam") == "true" and itemParam is not None:
                    self._write("%s%s.%s = %s" % (INDENT, self.cmd_name_var, itemName, itemParam))
                    self.glos.append(itemParam)
                elif item.getAttribute("random") == "true" or item.getAttribute("randomnumber") == "true":
                    # we should do this in the resulting python file.
                    randValue = int(random.random() * 10000000)
                    self._write("%s%s.%s = '%s-randomName'" % (INDENT, self.cmd_name_var, itemName, str(randValue)))
                    if item.getAttribute("setparam") == "true" and itemParam is not None:
                        self._write("%s%s = '%s-randomName'" % (INDENT, itemParam, str(randValue)))
                        self.glos.append(itemParam)
                else:
                    try:
                        val = int(itemValue)
                    except:
                        val = "'%s'" % itemValue
                    self._write("%s%s.%s = %s" % (INDENT, self.cmd_name_var, itemName, val))


    def parse_returnvalue(self, dom):
        '''
        process returnvalue section of command
        '''
        for return_val in dom.getElementsByTagName("returnvalue"):
            for item in return_val.getElementsByTagName("item"):
                #if item.getAttribute("list") == "true":

                itemName = item.getElementsByTagName("name")[0].childNodes[0].nodeValue.strip()
                try:
                    itemParam = item.getElementsByTagName("param")[0].childNodes[0].nodeValue.strip()
                except:
                    print "parse_returnvalue: No 'param' found in : '" + item.toprettyxml() + "'"
                    itemParam = None

                if item.getAttribute("setparam") == "true":
                    self._write("%s%s = %s.%s" % (INDENT, itemParam, self.cmd_name_resp, itemName))
                else:
                    self._write("%sif %s != %s.%s:" % (INDENT, itemParam, self.cmd_name_resp, itemName))
                    self._write("%sprint %s.%s + \" does not match \" + %s" % (
                        INDENT2, self.cmd_name_resp, itemName, itemParam))


    def parse_command(self, dom):
        '''
        process command elements and their children
        '''
        for cmd in dom.getElementsByTagName("command"):
            self.cmd_name = cmd.getElementsByTagName("name")[0].childNodes[0].nodeValue.strip()
            self.cmd_name_var = "_%s" % self.cmd_name
            self.cmd_name_resp = "resp_%s" % self.cmd_name

            try:
                testCaseName = cmd.getElementsByTagName("testcase")[0].childNodes[0].nodeValue.strip()
            except:
                print "parse_command: No 'testcase' found in: " + cmd.toprettyxml()
                testCaseName = None
            self._write("\n%s# %s" % (INDENT, testCaseName))

            self._write("%s%s = %s.%sCmd()" % (INDENT, self.cmd_name_var, self.cmd_name, self.cmd_name))

            self.parse_parameters(cmd)
            # now we execute command
            self._write("%s%s = apiClient.%s(%s)" % (INDENT, self.cmd_name_resp, self.cmd_name, self.cmd_name_var))
            self._write("%sif %s is None:"  % (INDENT, self.cmd_name_resp))
            self._write("%sprint 'test [%s] failed'" % (INDENT2, testCaseName))
            self._write("%selse:" % INDENT)
            self._write("%sprint 'test [%s] succeeded'" % (INDENT2, testCaseName))
            self.parse_returnvalue(cmd)

    def generate_python_header(self, outfile):
        '''
        generates python file header

        the basic stuff to bootstrap the script
        '''
        now = datetime.datetime.now()

        outfile.write("# Generated by translator.py\n")
        outfile.write("# from %s\n" % options.xmlfile)
        outfile.write("# on %s\n\n" % str(now))
        outfile.write("from cloudstackTestCase import *\n")
        outfile.write("import cloudstackTestClient\n")
        outfile.write("import time\n\n")
        outfile.write("# These are global variables used in the script below\n")
        for key in set(self.glos):
            outfile.write("%s = None\n" % key)
        outfile.write("# End of globals\n\n")
        outfile.write("if __name__ == \"__main__\":\n")
        outfile.write("%s# Possible initialization parameters:\n" % INDENT)
        outfile.write("%s# cloudstackTestClient(mgtSvr=None, port=8096, apiKey = None, securityKey = None,\n" % INDENT) 
        outfile.write("%s#                      asyncTimeout=3600, defaultWorkerThreads=10, logging=None)\n" % INDENT)
        outfile.write("%stestClient = cloudstackTestClient.cloudstackTestClient(\"localhost\")\n" % INDENT)
        outfile.write("%sapiClient = testClient.getApiClient()\n" % INDENT)

    def output_python(self, outfile):
        self.generate_python_header(outfile)
        for line in self.out_buffer:
            outfile.write(line)
        outfile.close()


    def parse_xmlFile(self, xmlFile, outfile):
        '''
        parse_xmlFile, this is the main function of the translator
        '''
        dom = xml.dom.minidom.parse(xmlFile)

        self.parse_command(dom)
        self.output_python(outfile)


if __name__ == "__main__":
    opts = OptionParser()

    opts.add_option("-i", "--inputfile", dest="xmlfile", help="The XML file and it's path containing tests.",
                    default="../../test/metadata/func/portforwarding.xml")
    opts.add_option("-o", "--output_file_path", dest="outpath", help="The path where we create the python file.")
    opts.add_option("-d", action="store_true", dest="debug",
                    help="Don't create output file, but send output to stderr", default=False)

    (options, args) = opts.parse_args()

    if options.xmlfile is None or not os.path.exists(options.xmlfile):
        print "The input file MUST be specified and exist: %s" % options.xmlfile
        exit(1)

    if options.debug == False:
        if options.outpath is None:
            options.outpath = "%s.py" % (os.path.basename(options.xmlfile))
        else:
            if options.outpath.endswith('/'):
                options.outpath = "%s%s.py" % (options.outpath, os.path.basename(options.xmlfile))
            else:
                options.outpath = "%s/%s.py" % (options.outpath, os.path.basename(options.xmlfile))

        if os.path.exists(options.outpath):
            print "The output file already exists: %s" % options.outpath
            exit(1)

        outFile = open(options.outpath, "w")
    else:
        outFile = sys.stderr

    print("[Processing: %s Output: %s]" % (options.xmlfile, outFile.name))

    processor = xml_to_python(options.debug)

    processor.parse_xmlFile(options.xmlfile, outFile)
