# spatial-visualiser
<div style="float:right">
    <img align=right src="https://c1.staticflickr.com/3/2544/3800240305_73acc784ed.jpg" alt="Your alt text" title="Title"/>
</div>

A tool to produce spatial distribution visualisation videos.  As a sample, the tool in its current form produced [this video](https://www.youtube.com/watch?v=YYrsXf92jBo)

This repository is intended as a way to make available the visualisation tool I developed to help visualise the evolution of
spatial distribution of data over time.  The tool takes a ESRI shapefile (.shp) with a labelled polygon for each
geographical unit and needs a data source with labelled data points where the label for each polygon matches the label
in the dataset.

The tool was originally developed using the visualisation and timestepping capabilities of the  RePast agent based
modelling toolkit and this first version will have a dependency on that toolkit.  It was also developed to visualise
the UK feed-in tariff data, so the first version will similarly have a dependency on that database being made available
in a database that can process SQL queries.  The raw feed-in tariff data is publicly available [here](https://www.ofgem.gov.uk/environmental-programmes/feed-tariff-fit-scheme/feed-tariff-reports-and-statistics/installation-reports)
and the SQL scripts needed to prepare a database in the format required by the tools are available in
[my other repository](https://github.com/rsnape/data-transformations)

I hope to port the tool soon to remove dependencies on RePast and specific database formats.  I'll quite likely 
do that in Python, but may well keep the Java version here for posterity or in case someone wants to use it.
