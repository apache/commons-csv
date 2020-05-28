This directory contains test files for the CSVFileParserTest class.

Files are of two types:
- test*.txt: these are test control and expected results
- test*.csv: these are the test input files, in various CSV formats

Test Control files (test*.txt)
==============================
The first line of this file consists of several space-separated fields:
- name of CSV data file (in same directory)
- (optional) settings to be applied to the default parsing format, which is delim=',' encap='"'
The settings have the form (see test source file for full details):
IgnoreEmpty=true|false
IgnoreSpaces=true|false
CommentStart=char
CheckComments - whether the test should check comment fields as well

The second line is the expected output from invoking CSVFormat#toString() on the parsing format

Subsequent lines are of the form:
n:[output]
where n is the expected result of CSVRecord#size, and 
[output] is the expected output from invoking CSVRecord#toString() on the parsed records.

Lines beginning with # are ignored, and can be used for comments.