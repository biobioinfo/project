#!/bin/bash

# Usage:
#  bash presentation.sh
# 
# Run the pdfTeX typesetter to create 'presentation.pdf'.

pdflatex presentation
bibtex presentation
pdflatex presentation
pdflatex presentation
