# purpose:
#  to create a PDF file from latex source

latex groovy-reference-card.tex
dvips -Ppdf -t landscape groovy-reference-card.dvi
ps2pdf groovy-reference-card.ps

# or

pdflatex groovy-reference-card.tex


# p.s. best for debugging use...
#   latex $1.tex; xdvi $1.dvi
# or
#  http://www.uoregon.edu/~koch/texshop/texshop.html
