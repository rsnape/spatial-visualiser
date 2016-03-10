# spatial-visualiser

A tool to produce spatial distribution visualisation videos.  As a sample, the tool in its current form produced [this video](https://www.youtube.com/watch?v=YYrsXf92jBo)

This repository is intended as a way to make available the visualisation tool I developed to help visualise the evolution of
spatial distribution of data over time.  The tool takes a ESRI shapefile (.shp) with a labelled polygon for each
geographical unit and needs a data source with labelled data points where the label for each polygon matches the label
in the dataset.

###v0.1 alpha

This is the very initial version, used to produce plots for my PhD and a paper I had published in the MDPI Energies journal.  The tool was originally developed using the visualisation and timestepping capabilities of the  RePast agent based
modelling toolkit and this first version will has the following dependencies

#####Current dependencies

 * Repast Simphony
 * Postgres database server
 * Postgres JODBC drivers

#####Purpose

The tool was was also developed to visualise
the UK feed-in tariff data, so the first version depends on the postgres database being made available
setup as per https://github.com/rsnape/data-transformations.  Username, password, database server name and database schema name can all be input as .  The raw feed-in tariff data is publicly available [here](https://www.ofgem.gov.uk/environmental-programmes/feed-tariff-fit-scheme/feed-tariff-reports-and-statistics/installation-reports)
and the SQL scripts needed to prepare a database in the format required by the tools are available in
[my other repository](https://github.com/rsnape/data-transformations)

#####Usage

 * set up the postgres database
 * install RePast
 * import this repository as a repast project
 * The variable to be plotted is controlled by the various styled layer descriptors.  These are stored as `xml` files in the `misc` directory.  You can pick choose the SLD to plot different variables - you do this in the Scenario Tree of the RePast display, by double clicking on `A Display` and then clicking `Agent Style` tab and `Load SLD` for the `GeoAreaAgent` agent type.  You can use one of the ones I make available here, alter them to suit your needs, or make new ones of your own.

#####Future plans

I hope to port the tool soon to remove dependencies on RePast and specific database formats.  I'll quite likely 
do that in Python, but may well keep the Java version here for posterity or in case someone wants to use it.
