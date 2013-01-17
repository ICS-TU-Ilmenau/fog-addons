Each class in de.tuilmenau.ics.filterToolsCVS contains a main method, which can be used as entry point for a program.
Start the program without parameters in order to get information about the usage.

Programs:
- Filter: Removes columns from CSV files; required to reduce the size of statistic files from large simulations
          All CSV in directory: "FoGSiEm\workspace" .AheadOfTime. .DeliveredPackets. .QueueLength.  .PacketsInFlight. .Retransmissions. .RoundTripTime. .DoStepDuration. .DoStepPacketCounter. .vertices .edges .size -i ".Global Routing Service."
          Single file         : "filename.csv" ...
- NodeStatistics: Extracts the node statistics from a large file by scanning for prefixes
                  "EndStatisticWriter_12345.csv"
- Distribution: Calculates a distribution of the values in one or more columns in a CSV file
                "EndStatisticWriter_12345.csv" de.tuilmenau.ics.fog.topology.NetworkInterface.totalSum de.tuilmenau.ics.fog.transfer.gates.DirectDownGate.totalSum
- All: Runs several other programs after another
       "EndStatisticWriter_12345.csv"  de.tuilmenau.ics.fog.topology.NetworkInterface.totalSum

License:
  GPL 2.0
  (see file "license.GPL2.txt" or http://www.gnu.org/licenses/gpl-2.0.txt)

