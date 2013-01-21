#!/usr/bin/env python

import sys
import csv
from optparse import OptionParser
from pygraph.classes.graph import graph
import pygraph.algorithms.accessibility as acc
#import matplotlib.pyplot as plt
import gc

def unifyEdges(edges):
    tempEdges = set()
    for edge in edges:
        lstEdge = list(edge)
        lstEdge.sort()
        tempEdges.add(tuple(lstEdge))
    return list(tempEdges)

parser = OptionParser(usage="checkgraph FILE", version="0.1");
opts,args = parser.parse_args()

if len(args) == 0:
    parser.error("No filename given.")

inFilename = args[0];
nodesFile = open(inFilename + "_nodes.csv", "rb")
edgesFile = open(inFilename + "_edges.csv", "rb")

nodeReader = csv.DictReader(nodesFile, fieldnames=["node"])
edgeReader = csv.DictReader(edgesFile, fieldnames=["from","to"])

gr = graph()

for line in nodeReader:
    gr.add_node(line["node"])

duplicate_edges = 0
for line in edgeReader:
    if gr.has_edge((line["from"], line["to"])):
        duplicate_edges = duplicate_edges + 1
    else:
        gr.add_edge((line["from"], line["to"]))
print("Removed {0} duplicate edges".format(duplicate_edges))

nodesFile.close()
edgesFile.close()

cc = acc.connected_components(gr)

ccv = cc.values()

i = 1
hist = [0]
while True:
    if ccv.count(i) == 0: break
    hist.append(ccv.count(i))
    i = i + 1
inetIdx = hist.index(max(hist))
print("The biggest component (the Internet, #{0}) has {1} nodes.".format(inetIdx, hist[inetIdx]))
del hist[inetIdx]
print("There are {0} nodes in {1} other components.".format(sum(hist), len(hist) - 1))

if sum(hist) > 0:
    print("Removing them...")

    i = 0
    for n in cc:
        if (cc[n] != inetIdx):
            gr.del_node(n)
            i = i + 1
    print("done.")
else: print

outFilename = inFilename + "-ok"
print("Exporting graph to {0}...".format(outFilename))
sys.stdout.flush()

nodesFile = open(outFilename + "_nodes.csv", "w")
edgesFile = open(outFilename + "_edges.csv", "w")

nodeWriter = csv.DictWriter(nodesFile, lineterminator='\n', fieldnames=["IP","Field1","Field2","Field3","ASNumber"])
edgeWriter = csv.DictWriter(edgesFile, lineterminator='\n', fieldnames=["from","to","InterAS"])

line = {}
line["IP"] = "IP"
line["Field1"]=""
line["Field2"]=""
line["Field3"]=""
line["ASNumber"]="ASNumber"
nodeWriter.writerow(line)
nodes = gr.nodes()
nodes.sort()
for node in nodes:
    line["IP"] = node
    line["Field1"]=""
    line["Field2"]=""
    line["Field3"]=""
    line["ASNumber"]=node
    nodeWriter.writerow(line)

line = {}
edges = unifyEdges(gr.edges())
edges.sort()

line["from"]="Node1"
line["to"]="Node2"
line["InterAS"]="InterAS"
edgeWriter.writerow(line)
for edge in edges:
    (line["from"], line["to"]) = edge
    line["InterAS"]="0"
    edgeWriter.writerow(line)

print("done.")

