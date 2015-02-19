#!/bin/bash

# Usage:
#  bash rapport.sh
# 
# Run the pdfTeX typesetter and make the bibliography for TeX to create
# 'rapport.pdf'.

pdflatex rapport
bibtex rapport
pdflatex rapport
pdflatex rapport
