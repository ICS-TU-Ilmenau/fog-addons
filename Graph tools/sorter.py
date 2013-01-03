#!/usr/bin/python

import sys
import csv
import operator
from optparse import OptionParser

BT = {"nothing": "0", "node": "1", "bus": "2"}
RRM = {"0": "local", "1": "global", "2": "fromBroken"}

inFields = ["id", "predId", "type", "source", "target", "hops", "gates", \
            "initGates", "lastHop", "rrMethod", "brokenType", "brokenName"]

parser = OptionParser(usage="parsestats FILE*", version="0.1");
opts,args = parser.parse_args()
            
data = csv.DictReader(open(args[0],"rb"), fieldnames=inFields, dialect="excel-tab")
sortedlist = sorted(data, key=operator.itemgetter('id'), reverse=False)
writer = csv.DictWriter(sys.stdout, fieldnames=inFields, dialect="excel-tab")

for i in range(0,len(sortedlist),1):
    writer.writerow(sortedlist[i])
