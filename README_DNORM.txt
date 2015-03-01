===========================
VERSIONS
===========================

0.0.6 Upgraded Woodstox to 4.2.0 and improved documentation
0.0.5 Fixed bug in abbreviation expansion code
0.0.4 Enabled abbreviation identification with Ab3P in ApplyDNorm

===========================
INSTRUCTIONS
===========================

The RunDNorm.sh script is the simplest way to RunDNorm. The script takes 5 parameters:
•	CONFIG is the BANNER configuration file; for running DNorm it is banner_NCBIDiseasePubtator_TEST.xml.
•	LEXICON is the copy of the MEDIC disease vocabulary from CTD, data/CTD_diseases.tsv.
•	MATRIX is the DNorm model, output/simmatrix_e4.bin, which is provided in the download.
•	INPUT is the input file, a sample is provided at sample.txt
•	OPUTPUT is the name of the output file, and sample-out2.txt is the output file, this is a simplified tab-delimited format.

Using the following command on a Linux command line will run DNorm on the sample.txt file and place the output in sample-out2.txt:
./RunDNorm.sh banner_NCBIDiseasePubtator_TEST.xml data/CTD_diseases.tsv output/simmatrix_e4.bin sample.txt sample-out2.txt

The tmBioC format is also supported, with similar parameters through the RunDNorm_BioC script. In this case INPUT and OUTPUT can either be filenames or directories, if the latter, DNorm will process each file in the INPUT directory and place the output in a file with the same name but in the output folder.

While the RunDNorm scripts are simple to use, for highest performance, you will also need to incorporate the UMLS features into the BANNER and also use the Ab3P abbreviation resolution tool.

First, get a copy of the 2013 AA version of the UMLS Metathesaurus. This version is necessary because this is what BANNER was trained against.
http://download.nlm.nih.gov/umls/kss/2013AA/active/2013aa-1-meta.nlm
http://download.nlm.nih.gov/umls/kss/2013AA/active/2013aa-2-meta.nlm
Change the “nlm” file extension to “zip”, unzip the files and place the MRCONSO.* and MRSTY.* files in folder accessible from your linux command line (call this UMLS). You may delete all other files from this download.

In the DNorm folder, open the config/banner_NCBIDisease_UMLS2013AA_TRAINDEV.xml file and change the “***PATH***” to the full path of the UMLS folder:
          <dirWithMRSTY>***PATH***</dirWithMRSTY>
          <dirWithMRCONSO>***PATH***</dirWithMRCONSO>

Next, get a copy of the Ab3P abbreviation resolution tool (ftp://ftp.ncbi.nlm.nih.gov/pub/wilbur) and extract it into a folder accessible from the linux command line (call this AB3P_DIR).

Next, decide on a file folder that can be used to store temporary files (call this TEMP). Any folder will do; this is used to communicate with Ab3P.

Now you can finally run DNorm:

./ApplyDNorm.sh config/banner_NCBIDisease_UMLS2013AA_TRAINDEV.xml data/CTD_diseases.tsv output/simmatrix_e4.bin AB3P_DIR TEMP INPUT OUTPUT

Again, INPUT and OUTPUT can either be single files or directories and there is also a version of this script that supports the BioC format (ApplyDNorm_BioC.sh)


