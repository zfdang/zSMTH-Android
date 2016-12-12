#!/usr/bin/python
# filename: extractDeviceInfos.py
"""
python extractDeviceInfos.py -f supported_devices-text.csv

The two files will be created:
1. branding.properties
2. marketingName.properties

copy them to device_model_to_marketing_name/src/main/java/com/zfdang/devicemodeltomarketingname/
"""
import sys
import getopt
import csv
import os
import codecs

##################################
# settings have been moved to config.py
##################################


# print trace stack info
def print_trace_stack_info():
    tp, val, tb = sys.exc_info()
    error_msgs = traceback.format_exception(tp, val, tb)
    print "Python Traceback Information:"
    for msg in error_msgs:
        print msg


def processFile(infile):

    if not os.path.exists(infile):
        print "file %s does not exist" % (infile)
        sys.exit(0)

    unkown_accounts = {}
    # csvReader = csv.reader(open(infile))
    csvReader = csv.reader(codecs.open(infile, 'rU', 'utf-16'))
    brandWriter = open("branding.properties", "w")
    marketingWriter = open("marketingName.properties", "w")
    counter = 0
    print csvReader
    for row in csvReader:

        if len(row) != 4:  # make sure row is parsed correctly
            continue

        counter += 1
        if counter == 1:  # ignore the first row
            continue

        branding = row[0]
        marketingName = row[1]
        model = row[3]
        if len(branding) > 0 and len(marketingName) > 0 and len(model) > 0:
            model = model.replace("=", "\=").replace(":", "\:").replace(" ", "\ ")
            branding = branding.replace("=", "\=").replace(":", "\:").replace(" ", "\ ")
            marketingName = marketingName.replace("=", "\=").replace(":", "\:").replace(" ", "\ ")
            brandWriter.write("%s=%s\n" % (model, branding))
            marketingWriter.write("%s=%s\n" % (model, marketingName))

########################################################
# main entrance here
########################################################
if __name__ == "__main__":

    infile = "latest.csv"

    try:
        opts, args = getopt.getopt(sys.argv[1:], "hf:", ["help", "file="])
    except getopt.GetoptError:
        print __doc__
        sys.exit(2)

    for opt, arg in opts:
        if opt in ("-h", "--help"):
            print __doc__
            sys.exit()
        elif opt in ('-f', '--file'):
            infile = arg

    if len(infile) == 0:
        print __doc__
        sys.exit(2)

    # process infile
    processFile(infile)
