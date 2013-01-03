#!/usr/bin/python

import sys
import csv
from optparse import OptionParser

BT = {"nothing": "0", "node": "1", "bus": "2"}
RRM = {"0": "local", "1": "global", "2": "fromBroken"}

inFields = ["id", "predId", "type", "source", "target", "hops", "gates", \
            "initGates", "lastHop", "rrMethod", "brokenType", "brokenName"]

parser = OptionParser(usage="parsestats FILE*", version="0.1");
opts,args = parser.parse_args()
            
class CSVManager:
    """This is a Manager for several CSV files"""
    def __init__(self, filenames):
        # We find out which files we have
        self.files = [csv.DictReader(open(filenames[0],"rb"), fieldnames=inFields, dialect="excel-tab"), csv.DictReader(open(filenames[1],"rb"), fieldnames=inFields, dialect="excel-tab")]
        for i in range(2,len(filenames),1):
            print >> sys.stderr, "Creating File Reader for file " + filenames[i]
            self.files.append(csv.DictReader(open(args[i],"rb"), fieldnames=inFields, dialect="excel-tab"))
        # We create an empty list for the lines we read in
        self.lines = []
        # We append empty dictionaries to the list
        self.lines.append({})
        for i in range(1,len(filenames),1):
            self.lines.append({})
        self.minimum = 9999999
        self.filerange = range(0,len(self.files),1)
        self.linerange = range(0,len(self.lines),1)
        j=0
        print >> sys.stderr, "Self lines are" +str(self.lines)
        print >> sys.stderr, "There are " + str(len(self.lines)) + " lines"
        print >> sys.stderr, "Successfully created CSV Manager"
        for i in self.filerange:
            self.readNextEntry(i)
        print >> sys.stderr, "----------------------------------------"
        print >> sys.stderr, "Giving you the lines I have read"
        for i in self.linerange:
            print >> sys.stderr, str(self.lines[i])
        print >> sys.stderr, "Initialization complete"
        print >> sys.stderr, "----------------------------------------"

    def getLowestEntry(self):
        print >> sys.stderr, "------------------------------------------------------"
        print >> sys.stderr, "Giving you the line with lowest entry"
        if((self.getLowestIndex() in self.linerange) and (True != ((self.getLowestIndex() in self.filerange)))):
            print >> sys.stderr, "You finally read the line at the file that went of of data"
            self.linerange.remove(self.getLowestIndex())
        line = self.lines[self.getLowestIndex()]
        print >> sys.stderr, "------------------------------------------------------"
        print >> sys.stderr, "Writing out"
        print >> sys.stderr, line
        print >> sys.stderr, "------------------------------------------------------"
        return line

    def regather(self):
        print >> sys.stderr, "readers range: " + str(self.filerange) + "...regathering"
        self.readNextEntry(self.getLowestIndex())

    def completed(self):
        if(len(self.linerange) != 0):
            return bool(False)
        else:
            return bool(True)

    def readNextEntry(self,i):
        print >> sys.stderr, "Will read " + str(i)
        try:
            self.lines[i] = next(self.files[int(i)])
            print >> sys.stderr, "I have read " + str(self.lines[i])
        except StopIteration:
            print >> sys.stderr, "Exception caught, deleting index" + str(self.files[i])
            try:
                self.filerange.remove(i)
            except ValueError:
                print >> sys.stderr, "Removed index " + str(i) +" from range"
        print >>sys.stderr, "readNextEntry " +str(i) + " has read " + str(self.lines[i])

    def getPacketIndex(self, i):
        #print >> sys.stderr, "getPacketIndex " +str(i) + " returning " + str((self.lines[i]).get('id'))
        return int((self.lines[i]).get('id'))

    def getLowestIndex(self):
        print >>sys.stderr, "The linerange is " + str(self.linerange)
        print >> sys.stderr, "-------------------------------------------"
        print >> sys.stderr, "Choosing between"
        self.minimum = self.getPacketIndex(self.linerange[0])
        for i in self.linerange:
            print >> sys.stderr, str(self.getPacketIndex(int(i)))
            print >> sys.stderr, "Comparing " + str(int(self.minimum)) + " against " + str(self.getPacketIndex(int(i))) +" what is " + str(int(self.minimum) >= self.getPacketIndex(int(i)))
            if(int(self.minimum) >= self.getPacketIndex(int(i))):
                self.minimum = self.getPacketIndex(int(i))
                print >> sys.stderr, "Minimum is now " + str(self.minimum)
                self.j = i
        print >> sys.stderr, "-------------------------------------------"
        print >> sys.stderr, "get LowestIndex returning " + str(self.j)
        return int(self.j)
        
manager = CSVManager(args)
writer = csv.DictWriter(sys.stdout, fieldnames=inFields, dialect="excel-tab")

while(bool(manager.completed()) != True ):
    writer.writerow(manager.getLowestEntry())
    manager.regather()

print >> sys.stderr, "Completed Merge"
