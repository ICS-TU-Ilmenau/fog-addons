#! /usr/bin/env python

"""
This script takes the output of parsestats.py and turns it
into something usable for GNU R
"""

import sys
import csv

if len(sys.argv) != 2:
	print "Usage: %s <prefix> <file>"%sys.argv[0]
	print "       <prefix>\tThe prefix to write as the first column of the output (e.g. NF or LF)"
	sys.exit(0)

f = open(sys.argv[2],'r')
infields = f.readline().rstrip('\r\n').split('\t')

print 'Fields found: '+str(infields)

reader = csv.DictReader(f, infields, dialect='excel-tab')

for line in reader:
	print '%s\tRH\t%s'%(sys.argv[1], line['refHops'])
