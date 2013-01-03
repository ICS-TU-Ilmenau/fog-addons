#!/usr/bin/env python

import sys
import csv
from optparse import OptionParser

BT = {"nothing": "0", "node": "1", "bus": "2"}
RRM = {"0": "local", "1": "global", "2": "fromBroken"}

inFields = ["id", "predId", "type", "source", "target", "hops", "gates", \
            "initGates", "lastHop", "rrMethod", "brokenType", "brokenName"]
outFields = ["source", "target", "refHops", "refGates", "success", \
             "detectorHops", "detectorGates", "globalHops", "globalGates", \
             "signalHops", "signalGates", "localHops", "localGates", \
             "fromBrokenHops", "fromBrokenGates"]

parser = OptionParser(usage="parsestats FILE", version="0.1");
opts,args = parser.parse_args()

if len(args) == 0:
    parser.error("No filename given.")

inFilename = args[0];
inFile = open(inFilename, "rb")

outList = []

reader = csv.DictReader(inFile, fieldnames=inFields, dialect="excel-tab")

outLine = {}
pkgIdGlDetect = None
for line in reader:
    if line["type"] == "ReroutingPacket":
        if line["predId"] == "" and line["brokenType"] == BT["nothing"]:
            # reference packet without broken elements
            # first packet of new experiment
            if len(outLine) > 0:
                outList.append(outLine)
		outLine = {}
		outLine["success"] = 0
                pkgIdGlDetect = None
            outLine["refHops"] = int(line["hops"])
            outLine["refGates"] = int(line["gates"])
            outLine["source"] = line["source"]
            outLine["target"] = line["target"]
        if line["brokenType"] in [BT["node"], BT["bus"]]:
            # something along the path is broken
            # TODO: check if source and target match
            if line["target"] != line["lastHop"]:
                # detector package
                outLine["detectorHops"] = int(line["hops"])
                outLine["detectorGates"] = int(line["gates"])
                # remember detector package id for later Reroute package check
                if line["rrMethod"] in RRM.keys() and \
                   RRM[line["rrMethod"]] == "global":
                    pkgIdGlDetect = line["id"]
            else:
                # successfull reroute
                outLine["success"] = 1;
                if line["rrMethod"] in RRM.keys():
                    rrMethod = RRM[line["rrMethod"]]
                    outLine[rrMethod + "Hops"] = int(line["hops"]) # - outLine["refHops"]
                    outLine[rrMethod + "Gates"] = int(line["gates"]) # - outLine["refGates"]
    elif line["type"] == "Reroute":
	try:
		if pkgIdGlDetect != None and line["predId"] == pkgIdGlDetect and line["target"] == outLine["source"]:
		    # is successor of current detector package
		    outLine["signalHops"] = line["hops"]
		    outLine["signalGates"] = line["gates"]
	except KeyError:
		print >> sys.stderr, "You won't get information on packet in line " + str(line)

# append last outLine
if len(outLine) > 0: outList.append(outLine)

writer = csv.DictWriter(sys.stdout, fieldnames=outFields, dialect="excel-tab")

print '\t'.join(outFields)
for line in outList:
    writer.writerow(line)

